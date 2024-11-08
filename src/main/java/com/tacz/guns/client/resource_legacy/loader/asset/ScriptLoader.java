package com.tacz.guns.client.resource_legacy.loader.asset;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.client.animation.statemachine.vmlib.LuaAnimationConstant;
import com.tacz.guns.client.animation.statemachine.vmlib.LuaGunAnimationConstant;
import com.tacz.guns.client.resource_legacy.ClientAssetManager;
import com.tacz.guns.util.TacPathVisitor;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ScriptLoader {
    private static final Globals globals = JsePlatform.standardGlobals();
    static {
        // 装载 lib
        new LuaAnimationConstant().install(globals);
        new LuaGunAnimationConstant().install(globals);
    }
    private static final Marker MARKER = MarkerManager.getMarker("ScriptLoader");
    private static final Pattern SCRIPTS_PATTERN = Pattern.compile("^(\\w+)/scripts/client/([\\w/]+)\\.lua$");

    public static boolean load(ZipFile zipFile, String zipPath) {
        Matcher matcher = SCRIPTS_PATTERN.matcher(zipPath);
        if (matcher.find()) {
            String namespace = matcher.group(1);
            String path = matcher.group(2);
            ZipEntry entry = zipFile.getEntry(zipPath);
            if (entry == null) {
                GunMod.LOGGER.warn(MARKER, "{} file don't exist", zipPath);
                return false;
            }
            try (InputStream zipEntryStream = zipFile.getInputStream(entry)) {
                ResourceLocation registryName = new ResourceLocation(namespace, path);
                LuaValue chunk = globals.load(zipEntryStream, namespace + "_" + path, "t", globals);
                LuaTable table = chunk.call().checktable();
                ClientAssetManager.INSTANCE.putScript(registryName, table);
                return true;
            } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                GunMod.LOGGER.warn(MARKER, "Failed to read script file: {}, entry: {}", zipFile, entry);
                exception.printStackTrace();
            }
        }
        return false;
    }

    public static void load(File root) {
        Path scriptPath = root.toPath().resolve("scripts/client");
        if (Files.isDirectory(scriptPath)) {
            TacPathVisitor visitor = new TacPathVisitor(scriptPath.toFile(), root.getName(), ".lua", (id, file) -> {
                try (InputStream stream = Files.newInputStream(file)) {
                    LuaValue chunk = globals.load(stream, id.getNamespace() + "_" + id.getPath(), "t", globals);
                    LuaTable table = chunk.call().checktable();
                    ClientAssetManager.INSTANCE.putScript(id, table);
                } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                    GunMod.LOGGER.warn(MARKER, "Failed to read script file: {}", file);
                    exception.printStackTrace();
                }
            });
            try {
                Files.walkFileTree(scriptPath, visitor);
            } catch (IOException e) {
                GunMod.LOGGER.warn(MARKER, "Failed to walk file tree: {}", scriptPath);
                e.printStackTrace();
            }
        }
    }
}
