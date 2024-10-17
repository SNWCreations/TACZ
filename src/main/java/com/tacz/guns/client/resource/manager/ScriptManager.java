package com.tacz.guns.client.resource.manager;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.client.animation.statemachine.vmlib.LuaAnimationConstant;
import com.tacz.guns.client.animation.statemachine.vmlib.LuaGunAnimationConstant;
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
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class ScriptManager extends SimplePreparableReloadListener<Map<ResourceLocation, LuaTable>> {
    private static final Marker MARKER = MarkerManager.getMarker("ScriptLoader");
    private static final Globals globals = JsePlatform.standardGlobals();
    static {
        // 装载 lib
        new LuaAnimationConstant().install(globals);
        new LuaGunAnimationConstant().install(globals);
    }

    private final Map<ResourceLocation, LuaTable> dataMap = Maps.newHashMap();
    private final FileToIdConverter filetoidconverter = new FileToIdConverter("scripts", ".lua");

    @Override
    @NotNull
    protected Map<ResourceLocation, LuaTable> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<ResourceLocation, LuaTable> output = Maps.newHashMap();
        for(Map.Entry<ResourceLocation, Resource> entry : filetoidconverter.listMatchingResources(pResourceManager).entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

            try (Reader reader = entry.getValue().openAsReader()) {
                LuaValue chunk = globals.load(reader, resourcelocation1.getNamespace() + "_" + resourcelocation1.getPath());
                LuaTable table = chunk.call().checktable();
                output.put(resourcelocation1, table);
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                GunMod.LOGGER.warn(MARKER, "Failed to read script file: {}, entry: {}", resourcelocation, entry);
            }
        }
        return output;
    }

    @Override
    protected void apply(Map<ResourceLocation, LuaTable> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        dataMap.clear();
        dataMap.putAll(pObject);
    }

    public LuaTable getScript(ResourceLocation id) {
        return dataMap.get(id);
    }
}
