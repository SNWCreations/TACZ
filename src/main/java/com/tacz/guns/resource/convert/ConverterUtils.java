package com.tacz.guns.resource.convert;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class ConverterUtils {

    public static String relativePath(File base, File sub) {
        return base.toPath().relativize(sub.toPath()).toString();
    }

    @Nullable
    public static ResourceLocation parseEntryName(File baseDir, File file) {
        String relativePath = relativePath(baseDir, file);
        int i = relativePath.indexOf(File.separatorChar);
        if (i != -1) {
            String namespace = relativePath.substring(0, i);
            String entryName = relativePath.substring(i + 1).replace(File.separatorChar, '/');
            return new ResourceLocation(namespace, entryName);
        }
        return null;
    }

    public static String toFilePath(String namespace, String path, PackType folderType) {
        return folderType.getDirectory() + File.separator + namespace + File.separator + path;
    }

    public static String toFilePath(ResourceLocation resourceLocation, PackType folderType) {
        return toFilePath(resourceLocation.getNamespace(), resourceLocation.getPath(), folderType);
    }

    private ConverterUtils() {
    }
}
