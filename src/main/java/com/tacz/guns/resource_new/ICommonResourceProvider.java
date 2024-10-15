package com.tacz.guns.resource_new;

import com.tacz.guns.resource_new.index.CommonAmmoIndex;
import com.tacz.guns.resource_new.index.CommonGunIndex;
import com.tacz.guns.resource_new.pojo.data.attachment.AttachmentData;
import com.tacz.guns.resource_new.pojo.data.gun.GunData;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface ICommonResourceProvider {
    @Nullable GunData getGunData(ResourceLocation id);

    @Nullable CommonGunIndex getGunIndex(ResourceLocation gunId);

    @Nullable CommonAmmoIndex getAmmoIndex(ResourceLocation ammoId);

    Set<Map.Entry<ResourceLocation, CommonGunIndex>> getAllGuns();

    Set<Map.Entry<ResourceLocation, CommonAmmoIndex>> getAllAmmos();

    AttachmentData getAttachmentData(ResourceLocation dataId);
}
