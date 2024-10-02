package com.tacz.guns.client.resource.pojo.display.block;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;

public class BlockDisplay {
    @SerializedName("model")
    private ResourceLocation modelLocation;
    @SerializedName("texture")
    private ResourceLocation modelTexture;
    @SerializedName("transforms")
    private ItemTransforms transforms;

    public ResourceLocation getModelLocation() {
        return modelLocation;
    }

    public ResourceLocation getModelTexture() {
        return modelTexture;
    }

    public ItemTransforms getTransforms() {
        return transforms;
    }
}
