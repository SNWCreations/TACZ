package com.tacz.guns.resource;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.tacz.guns.GunMod;
import cpw.mods.jarhandling.SecureJar;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.DelegatingPackResources;
import net.minecraftforge.resource.PathPackResources;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.tacz.guns.resource_legacy.CommonGunPackLoader.GSON;

public enum GunPackLoader implements RepositorySource {
    INSTANCE;
    private static final Marker MARKER = MarkerManager.getMarker("GunPackFinder");
    private Pack extensionsPack;
    private List<GunPack> gunPacks;

    @Override
    public void loadPacks(Consumer<Pack> pOnLoad) {
        pOnLoad.accept(this.extensionsPack);
    }

    public void discoverExtensions(PackType packType) {
        Path resourcePacksPath = FMLPaths.GAMEDIR.get().resolve("tacz");

        this.gunPacks = scanExtensions(resourcePacksPath);
        List<PathPackResources> extensionPacks = new ArrayList<>();

        for(GunPack gunPack : gunPacks) {
            extensionPacks.add(new PathPackResources(gunPack.name, false, gunPack.path) {
                private final SecureJar secureJar = SecureJar.from(gunPack.path);

                @NotNull
                protected Path resolve(String... paths) {
                    if (paths.length < 1) {
                        throw new IllegalArgumentException("Missing path");
                    } else {
                        return this.secureJar.getPath(String.join("/", paths));
                    }
                }

                public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
                    return super.getResource(type, location);
                }

                public void listResources(PackType type, String namespace, String path, PackResources.ResourceOutput resourceOutput) {
                    super.listResources(type, namespace, path, resourceOutput);
                }
            });
        }


        this.extensionsPack = Pack.readMetaAndCreate("tacz_resources", Component.literal("TACZ Resources"), true, (id) -> {
            return new DelegatingPackResources(id, false, new PackMetadataSection(Component.translatable("tacz.resources.modresources"),
                    SharedConstants.getCurrentVersion().getPackVersion(packType)), extensionPacks) {
                public IoSupplier<InputStream> getRootResource(String... paths) {
                    if (paths.length == 1 && paths[0].equals("pack.png")) {
                        Path logoPath = getModIcon("tacz");
                        if (logoPath != null) {
                            return IoSupplier.create(logoPath);
                        }
                    }
                    return null;
                }
            };
        }, packType, Pack.Position.BOTTOM, PackSource.BUILT_IN);
    }

    public static @Nullable Path getModIcon(String modId) {
        Optional<? extends ModContainer> m = ModList.get().getModContainerById(modId);
        if (m.isPresent()) {
            IModInfo mod = m.get().getModInfo();
            IModFile file = mod.getOwningFile().getFile();
            if (file != null) {
                Path logoPath = file.findResource("icon.png");
                if (Files.exists(logoPath)) {
                    return logoPath;
                }
            }
        }

        return null;
    }

    private static GunPack fromDirPath(Path path) throws IOException {
        Path packInfoFilePath = path.resolve("gunpack.meta.json");
        try (InputStream stream = Files.newInputStream(packInfoFilePath)) {
            PackMeta info = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackMeta.class);

            if (info == null) {
                GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}", packInfoFilePath);
                return null;
            }

            if (info.getDependencies() !=null && !modVersionAllMatch(info)) {
                GunMod.LOGGER.warn(MARKER, "Mod version mismatch: {}", packInfoFilePath);
                return null;
            }

            return new GunPack(path, info.getName());
        } catch (IOException | JsonSyntaxException | JsonIOException | InvalidVersionSpecificationException exception) {
            GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}", packInfoFilePath);
            GunMod.LOGGER.warn(exception.getMessage());
        }
        return null;
    }

    static GunPack fromZipPath(Path path)  {
        try(ZipFile zipFile = new ZipFile(path.toFile())){
            ZipEntry extDescriptorEntry = zipFile.getEntry("gunpack.meta.json");
            if (extDescriptorEntry == null) {
                GunMod.LOGGER.error(MARKER,"Failed to load extension from ZIP {}. Error: {}", path, "No gunpack.meta.json found");
                return null;
            }

            try (InputStream stream = zipFile.getInputStream(extDescriptorEntry)) {
                PackMeta info = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackMeta.class);

                if (info == null) {
                    GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}", path);
                    return null;
                }

                if (info.getDependencies() !=null && !modVersionAllMatch(info)) {
                    GunMod.LOGGER.warn(MARKER, "Mod version mismatch: {}", path);
                    return null;
                }

                return new GunPack(path, info.getName());
            } catch (IOException | JsonSyntaxException | JsonIOException | InvalidVersionSpecificationException e) {
                GunMod.LOGGER.error(MARKER,"Failed to load extension from ZIP {}. Error: {}", path, e);
                return null;
            }
        } catch (IOException e) {
            GunMod.LOGGER.error(MARKER,"Failed to load extension from ZIP {}. Error: {}", path, e);
            return null;
        }
    }

    private static List<GunPack> scanExtensions(Path extensionsPath) {
        List<GunPack> gunPacks = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(extensionsPath)){
            for (Path entry : stream) {
                GunPack gunPack = null;
                if (Files.isDirectory(entry)) {
                    gunPack = fromDirPath(entry);
                } else if (entry.toString().endsWith(".zip")) {
                    gunPack = fromZipPath(entry);
                }
                if (gunPack != null) {
                    gunPacks.add(gunPack);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return gunPacks;
    }

    private static boolean modVersionAllMatch(PackMeta info) throws InvalidVersionSpecificationException {
        HashMap<String, String> dependencies = info.getDependencies();
        for (String modId : dependencies.keySet()) {
            if (!modVersionMatch(modId, dependencies.get(modId))) {
                return false;
            }
        }
        return true;
    }

    private static boolean modVersionMatch(String modId, String version) throws InvalidVersionSpecificationException {
        VersionRange versionRange = VersionRange.createFromVersionSpec(version);
        return ModList.get().getModContainerById(modId).map(mod -> {
            ArtifactVersion modVersion = mod.getModInfo().getVersion();
            return versionRange.containsVersion(modVersion);
        }).orElse(false);
    }

    public record GunPack(Path path, String name) {
    }
}
