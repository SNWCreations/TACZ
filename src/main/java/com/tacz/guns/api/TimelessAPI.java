package com.tacz.guns.api;

import com.tacz.guns.api.client.other.IThirdPersonAnimation;
import com.tacz.guns.api.client.other.ThirdPersonManager;
import com.tacz.guns.client.resource.ClientGunPackLoader;
import com.tacz.guns.client.resource_new.index.ClientAmmoIndex;
import com.tacz.guns.client.resource_new.index.ClientAttachmentIndex;
import com.tacz.guns.client.resource_new.index.ClientBlockIndex;
import com.tacz.guns.client.resource_new.index.ClientGunIndex;
import com.tacz.guns.client.resource_new.ClientIndexManager;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.resource.CommonAssetManager;
import com.tacz.guns.resource.CommonGunPackLoader;
import com.tacz.guns.resource_new.index.CommonAmmoIndex;
import com.tacz.guns.resource_new.index.CommonAttachmentIndex;
import com.tacz.guns.resource_new.index.CommonBlockIndex;
import com.tacz.guns.resource_new.index.CommonGunIndex;
import com.tacz.guns.resource_new.CommonAssetsManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class TimelessAPI {
    @OnlyIn(Dist.CLIENT)
    public static Optional<ClientGunIndex> getClientGunIndex(ResourceLocation gunId) {
        return Optional.ofNullable(ClientIndexManager.GUN_INDEX.get(gunId));
    }

    @OnlyIn(Dist.CLIENT)
    public static Optional<ClientAttachmentIndex> getClientAttachmentIndex(ResourceLocation attachmentId) {
        return ClientGunPackLoader.getAttachmentIndex(attachmentId);
    }

    @OnlyIn(Dist.CLIENT)
    public static Optional<ClientAmmoIndex> getClientAmmoIndex(ResourceLocation ammoId) {
        return Optional.ofNullable(ClientIndexManager.AMMO_INDEX.get(ammoId));
    }

    @OnlyIn(Dist.CLIENT)
    public static Optional<ClientBlockIndex> getClientBlockIndex(ResourceLocation blockId) {
        return ClientGunPackLoader.getBlockIndex(blockId);
    }

    @OnlyIn(Dist.CLIENT)
    public static Set<Map.Entry<ResourceLocation, ClientGunIndex>> getAllClientGunIndex() {
        return ClientGunPackLoader.getAllGuns();
    }

    @OnlyIn(Dist.CLIENT)
    public static Set<Map.Entry<ResourceLocation, ClientAmmoIndex>> getAllClientAmmoIndex() {
        return ClientGunPackLoader.getAllAmmo();
    }

    @OnlyIn(Dist.CLIENT)
    public static Set<Map.Entry<ResourceLocation, ClientAttachmentIndex>> getAllClientAttachmentIndex() {
        return ClientGunPackLoader.getAllAttachments();
    }

    public static Optional<CommonBlockIndex> getCommonBlockIndex(ResourceLocation blockId) {
        return CommonGunPackLoader.getBlockIndex(blockId);
    }

    public static Optional<CommonGunIndex> getCommonGunIndex(ResourceLocation gunId) {
        return Optional.ofNullable(CommonAssetsManager.get().getGunIndex(gunId));
    }

    public static Optional<CommonAttachmentIndex> getCommonAttachmentIndex(ResourceLocation attachmentId) {
        return CommonGunPackLoader.getAttachmentIndex(attachmentId);
    }

    public static Optional<CommonAmmoIndex> getCommonAmmoIndex(ResourceLocation ammoId) {
        return Optional.ofNullable(CommonAssetsManager.get().getAmmoIndex(ammoId));
    }

    public static Optional<GunSmithTableRecipe> getRecipe(ResourceLocation recipeId) {
        return CommonAssetManager.INSTANCE.getRecipe(recipeId);
    }

    public static Set<Map.Entry<ResourceLocation, CommonBlockIndex>> getAllCommonBlockIndex() {
        return CommonGunPackLoader.getAllBlocks();
    }

    public static Set<Map.Entry<ResourceLocation, CommonGunIndex>> getAllCommonGunIndex() {
        return CommonAssetsManager.get().getAllGuns();
    }

    public static Set<Map.Entry<ResourceLocation, CommonAmmoIndex>> getAllCommonAmmoIndex() {
        return CommonAssetsManager.get().getAllAmmos();
    }

    public static Set<Map.Entry<ResourceLocation, CommonAttachmentIndex>> getAllCommonAttachmentIndex() {
        return CommonGunPackLoader.getAllAttachments();
    }

    public static Map<ResourceLocation, GunSmithTableRecipe> getAllRecipes() {
        return CommonAssetManager.INSTANCE.getAllRecipes();
    }

    public static void registerThirdPersonAnimation(String name, IThirdPersonAnimation animation) {
        ThirdPersonManager.register(name, animation);
    }
}
