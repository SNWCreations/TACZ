package com.tacz.guns.resource_new;

import com.tacz.guns.resource_new.index.CommonAmmoIndex;
import com.tacz.guns.resource_new.index.CommonAttachmentIndex;
import com.tacz.guns.resource_new.index.CommonGunIndex;
import com.tacz.guns.resource_new.pojo.data.attachment.AttachmentData;
import com.tacz.guns.resource_new.pojo.data.gun.GunData;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface ICommonResourceProvider {
    @Nullable GunData getGunData(ResourceLocation id);

    @Nullable AttachmentData getAttachmentData(ResourceLocation attachmentId);

    @Nullable CommonGunIndex getGunIndex(ResourceLocation gunId);

    @Nullable CommonAmmoIndex getAmmoIndex(ResourceLocation ammoId);

    @Nullable CommonAttachmentIndex getAttachmentIndex(ResourceLocation attachmentId);

    Set<Map.Entry<ResourceLocation, CommonGunIndex>> getAllGuns();

    Set<Map.Entry<ResourceLocation, CommonAmmoIndex>> getAllAmmos();

    Set<Map.Entry<ResourceLocation, CommonAttachmentIndex>> getAllAttachments();

    Set<String> getAttachmentTags(ResourceLocation registryName);

    Set<String> getAllowAttachmentTags(ResourceLocation registryName);
}
