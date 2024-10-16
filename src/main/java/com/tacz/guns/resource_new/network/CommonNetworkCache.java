package com.tacz.guns.resource_new.network;

import com.google.gson.reflect.TypeToken;
import com.tacz.guns.resource_new.index.CommonAmmoIndex;
import com.tacz.guns.resource_new.index.CommonAttachmentIndex;
import com.tacz.guns.resource_new.index.CommonGunIndex;
import com.tacz.guns.resource_new.pojo.data.attachment.AttachmentData;
import com.tacz.guns.resource_new.pojo.data.gun.GunData;
import com.tacz.guns.resource_new.CommonAssetsManager;
import com.tacz.guns.resource_new.ICommonResourceProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 网络位置的缓存<br/>
 * 用于存储从网络获取的数据
 */
public enum CommonNetworkCache implements ICommonResourceProvider {
    INSTANCE;

    public Map<ResourceLocation, GunData> gunData = new HashMap<>();
    public Map<ResourceLocation, AttachmentData> attachmentData = new HashMap<>();
    public Map<ResourceLocation, CommonGunIndex> gunIndex = new HashMap<>();
    public Map<ResourceLocation, CommonAmmoIndex> ammoIndex = new HashMap<>();
    public Map<ResourceLocation, CommonAttachmentIndex> attachmentIndex = new HashMap<>();
    public Map<ResourceLocation, Set<String>> attachmentTags = new HashMap<>();
    public Map<ResourceLocation, Set<String>> allowAttachmentTags = new HashMap<>();

    @Nullable
    @Override
    public GunData getGunData(ResourceLocation id) {
        return gunData.get(id);
    }

    @Override
    public AttachmentData getAttachmentData(ResourceLocation attachmentId) {
        return attachmentData.get(attachmentId);
    }

    @Nullable
    @Override
    public CommonGunIndex getGunIndex(ResourceLocation id) {
        return gunIndex.get(id);
    }

    @Override
    public @Nullable CommonAmmoIndex getAmmoIndex(ResourceLocation ammoId) {
        return ammoIndex.get(ammoId);
    }

    @Override
    public @Nullable CommonAttachmentIndex getAttachmentIndex(ResourceLocation attachmentId) {
        return attachmentIndex.get(attachmentId);
    }

    @Override
    public Set<Map.Entry<ResourceLocation, CommonGunIndex>> getAllGuns() {
        return gunIndex.entrySet();
    }

    @Override
    public Set<Map.Entry<ResourceLocation, CommonAmmoIndex>> getAllAmmos() {
        return ammoIndex.entrySet();
    }

    @Override
    public Set<Map.Entry<ResourceLocation, CommonAttachmentIndex>> getAllAttachments() {
        return attachmentIndex.entrySet();
    }

    @Override
    public Set<String> getAttachmentTags(ResourceLocation registryName) {
        return Set.of();
    }

    @Override
    public Set<String> getAllowAttachmentTags(ResourceLocation registryName) {
        return Set.of();
    }

    public void fromNetwork(Map<DataType, Map<ResourceLocation, String>> cache) {
        gunData.clear();
        attachmentData.clear();
        gunIndex.clear();
        ammoIndex.clear();
        attachmentIndex.clear();

        attachmentTags.clear();
        allowAttachmentTags.clear();
        // 延后处理
        Map<DataType, Map<ResourceLocation, String>> delayed = new HashMap<>();
        for (Map.Entry<DataType, Map<ResourceLocation, String>> entry : cache.entrySet()) {
            switch (entry.getKey()) {
                case GUN_INDEX:
                case AMMO_INDEX:
                case ATTACHMENT_INDEX:
                case BLOCK_INDEX:
                    delayed.put(entry.getKey(), entry.getValue());
                    break;
                default: fromNetwork(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<DataType, Map<ResourceLocation, String>> entry : delayed.entrySet()) {
            fromNetwork(entry.getKey(), entry.getValue());
        }
    }

    private <T> T parse(String json, Class<T> dataClass) {
        return CommonAssetsManager.GSON.fromJson(json, dataClass);
    }

    private void resolveAttachmentTags(Map<ResourceLocation, String> data) {
        for (Map.Entry<ResourceLocation, String> entry : data.entrySet()) {
            List<String> tags = CommonAssetsManager.GSON.fromJson(entry.getValue(), new TypeToken<>(){});
            if (entry.getKey().getPath().startsWith("allow_attachments/") && entry.getKey().getPath().length()>18) {
                ResourceLocation gunId = entry.getKey().withPath(entry.getKey().getPath().substring(18));
                allowAttachmentTags.computeIfAbsent(gunId, (v) -> new HashSet<>()).addAll(tags);
            } else {
                attachmentTags.computeIfAbsent(entry.getKey(), (v) -> new HashSet<>()).addAll(tags);
            }
        }
    }


    private void fromNetwork(DataType type, Map<ResourceLocation, String> data) {
        for (Map.Entry<ResourceLocation, String> entry : data.entrySet()) {
            switch (type) {
                case GUN_DATA -> gunData.put(entry.getKey(), parse(entry.getValue(), GunData.class));
                case GUN_INDEX -> gunIndex.put(entry.getKey(), parse(entry.getValue(), CommonGunIndex.class));
                case AMMO_INDEX -> ammoIndex.put(entry.getKey(), parse(entry.getValue(), CommonAmmoIndex.class));
                case ATTACHMENT_DATA -> attachmentData.put(entry.getKey(), parse(entry.getValue(), AttachmentData.class));
                case ATTACHMENT_INDEX -> attachmentIndex.put(entry.getKey(), parse(entry.getValue(), CommonAttachmentIndex.class));
                case ATTACHMENT_TAGS -> resolveAttachmentTags(data);
            }
        }
    }
}
