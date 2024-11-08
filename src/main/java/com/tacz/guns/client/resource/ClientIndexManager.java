package com.tacz.guns.client.resource;

import com.google.common.collect.Maps;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.index.ClientAmmoIndex;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.client.resource.index.ClientBlockIndex;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.resource.manager.ScriptManager;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.AmmoIndexPOJO;
import com.tacz.guns.resource.pojo.AttachmentIndexPOJO;
import com.tacz.guns.resource.pojo.BlockIndexPOJO;
import com.tacz.guns.resource.pojo.GunIndexPOJO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ClientIndexManager {
    public static final Map<ResourceLocation, ClientGunIndex> GUN_INDEX = Maps.newHashMap();
    public static final Map<ResourceLocation, ClientAmmoIndex> AMMO_INDEX = Maps.newHashMap();
    public static final Map<ResourceLocation, ClientAttachmentIndex> ATTACHMENT_INDEX = Maps.newHashMap();
    public static final Map<ResourceLocation, ClientBlockIndex> BLOCK_INDEX = Maps.newHashMap();

    public static void reload() {
        GUN_INDEX.clear();
        AMMO_INDEX.clear();
        ATTACHMENT_INDEX.clear();
        BLOCK_INDEX.clear();
        ScriptManager.reloadGlobal();
        loadGunIndex();
        loadAmmoIndex();
        loadAttachmentIndex();
        loadBlockIndex();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && IGun.mainhandHoldGun(player)) {
            AttachmentPropertyManager.postChangeEvent(player, player.getMainHandItem());
        }
    }

    public static void loadGunIndex() {
        TimelessAPI.getAllCommonGunIndex().forEach(index -> {
            ResourceLocation id = index.getKey();
            GunIndexPOJO pojo = index.getValue().getPojo();
            try {
                GUN_INDEX.put(id, ClientGunIndex.getInstance(pojo));
            } catch (IllegalArgumentException exception) {
                GunMod.LOGGER.warn("{} index file read fail!", id);
                exception.printStackTrace();
            }
        });
    }

    public static void loadAmmoIndex() {
        TimelessAPI.getAllCommonAmmoIndex().forEach(index -> {
            ResourceLocation id = index.getKey();
            AmmoIndexPOJO pojo = index.getValue().getPojo();
            try {
                AMMO_INDEX.put(id, ClientAmmoIndex.getInstance(pojo));
            } catch (IllegalArgumentException exception) {
                GunMod.LOGGER.warn("{} index file read fail!", id);
                exception.printStackTrace();
            }
        });
    }

    public static void loadAttachmentIndex() {
        TimelessAPI.getAllCommonAttachmentIndex().forEach(index -> {
            ResourceLocation id = index.getKey();
            AttachmentIndexPOJO pojo = index.getValue().getPojo();
            try {
                ATTACHMENT_INDEX.put(id, ClientAttachmentIndex.getInstance(id, pojo));
            } catch (IllegalArgumentException exception) {
                GunMod.LOGGER.warn("{} index file read fail!", id);
                exception.printStackTrace();
            }
        });
    }

    public static void loadBlockIndex() {
        TimelessAPI.getAllCommonBlockIndex().forEach(index -> {
            ResourceLocation id = index.getKey();
            BlockIndexPOJO pojo = index.getValue().getPojo();
            try {
                BLOCK_INDEX.put(id, ClientBlockIndex.getInstance(pojo));
            } catch (IllegalArgumentException exception) {
                GunMod.LOGGER.warn("{} index file read fail!", id);
                exception.printStackTrace();
            }
        });
    }

    public static Set<Map.Entry<ResourceLocation, ClientGunIndex>> getAllGuns() {
        return GUN_INDEX.entrySet();
    }

    public static Set<Map.Entry<ResourceLocation, ClientAmmoIndex>> getAllAmmo() {
        return AMMO_INDEX.entrySet();
    }

    public static Set<Map.Entry<ResourceLocation, ClientAttachmentIndex>> getAllAttachments() {
        return ATTACHMENT_INDEX.entrySet();
    }

    public static Set<Map.Entry<ResourceLocation, ClientBlockIndex>> getAllBlocks() {
        return BLOCK_INDEX.entrySet();
    }
}
