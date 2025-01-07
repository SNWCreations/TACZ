package com.tacz.guns.resource.convert.folder;

import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.IOException;

public interface FileOp {
    boolean run(File baseDir, File file, ResourceLocation fileResourceLocation) throws IOException;

    default FileOp andThen(FileOp op) {
        return (baseDir, file, fileResourceLocation) -> {
            boolean self = FileOp.this.run(baseDir, file, fileResourceLocation);
            boolean after = op.run(baseDir, file, fileResourceLocation);
            return self && after;
        };
    }
}
