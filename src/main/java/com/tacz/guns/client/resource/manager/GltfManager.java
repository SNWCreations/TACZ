package com.tacz.guns.client.resource.manager;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.client.animation.gltf.AnimationStructure;
import com.tacz.guns.api.client.animation.statemachine.vmlib.LuaAnimationConstant;
import com.tacz.guns.client.animation.statemachine.vmlib.LuaGunAnimationConstant;
import com.tacz.guns.client.resource.pojo.animation.gltf.RawAnimationStructure;
import com.tacz.guns.client.resource.ClientAssetsManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;


public class GltfManager extends SimplePreparableReloadListener<Map<ResourceLocation, AnimationStructure>> {
    private static final Marker MARKER = MarkerManager.getMarker("GltfAnimationLoader");
    private static final Globals globals = JsePlatform.standardGlobals();
    static {
        // 装载 lib
        new LuaAnimationConstant().install(globals);
        new LuaGunAnimationConstant().install(globals);
    }

    private final Map<ResourceLocation, AnimationStructure> dataMap = Maps.newHashMap();
    private final FileToIdConverter filetoidconverter = new FileToIdConverter("animations", ".gltf");

    @Override
    @NotNull
    protected Map<ResourceLocation, AnimationStructure> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<ResourceLocation, AnimationStructure> output = Maps.newHashMap();
        for(Map.Entry<ResourceLocation, Resource> entry : filetoidconverter.listMatchingResources(pResourceManager).entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

            try (Reader reader = entry.getValue().openAsReader()) {
                RawAnimationStructure rawStructure = ClientAssetsManager.GSON.fromJson(reader, RawAnimationStructure.class);
                AnimationStructure animationStructure = new AnimationStructure(rawStructure);
                output.put(resourcelocation1, animationStructure);
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                GunMod.LOGGER.warn(MARKER, "Failed to read gltf animation file: {}", resourcelocation);
            }
        }
        return output;
    }

    @Override
    protected void apply(Map<ResourceLocation, AnimationStructure> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        dataMap.clear();
        dataMap.putAll(pObject);
    }

    public AnimationStructure getGltfAnimation(ResourceLocation id) {
        return dataMap.get(id);
    }
}
