package com.tacz.guns.resource_new;

import java.nio.file.Path;

public class GunPack {
    public Path path;
    public String name;

    public GunPack(Path entry, String name) {
        this.path = entry;
    }
}
