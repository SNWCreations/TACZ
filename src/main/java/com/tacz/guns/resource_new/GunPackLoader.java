package com.tacz.guns.resource_new;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.tacz.guns.GunMod;
import com.tacz.guns.client.resource_new.pojo.PackInfo;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.tacz.guns.resource.CommonGunPackLoader.GSON;

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
                public boolean isHidden() {
                    return false;
                }

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
        Path packInfoFilePath = path.resolve("pack.json");
        try (InputStream stream = Files.newInputStream(packInfoFilePath)) {
            PackInfo info = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackInfo.class);

            if (info == null) {
                GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}", packInfoFilePath);
                return null;
            }

            return new GunPack(path, info.getName());
        } catch (IOException | JsonSyntaxException | JsonIOException exception) {
            GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}", packInfoFilePath);
            GunMod.LOGGER.warn(exception.getMessage());
        }
        return null;
    }

    private static List<GunPack> scanExtensions(Path extensionsPath) {
        List<GunPack> gunPacks = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(extensionsPath)){
            for (Path entry : stream) {
                GunPack gunPack;
                if (Files.isDirectory(entry)) {
                    try {
                        gunPack = fromDirPath(entry);
                        if (gunPack != null) {
                            gunPacks.add(gunPack);
                        }
                    } catch (Exception var8) {
                        //                            throw new InvalidExtensionException(entry, re);
                    }
                } else if (entry.toString().endsWith(".zip")) {
                    try {
//                            extension = ExtensionRegistry.Extension.fromZipPath(entry);
//                            if (extension != null) {
//                                extensions.add(extension);
//                            }
                    } catch (Exception var7) {
                        //                            throw new InvalidExtensionException(entry, re);
                    }
                }
            }
        } catch (IOException var10) {
            var10.printStackTrace();
        }

        return gunPacks;
    }
}
