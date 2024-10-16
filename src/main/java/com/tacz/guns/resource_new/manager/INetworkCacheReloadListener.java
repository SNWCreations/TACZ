package com.tacz.guns.resource_new.manager;

import com.tacz.guns.resource_new.network.DataType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Map;

public interface INetworkCacheReloadListener extends PreparableReloadListener {
    Map<ResourceLocation, String> getNetworkCache();

    DataType getType();
}
