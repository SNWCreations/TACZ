package com.tacz.guns.client.resource_new.pojo.display.attachment;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;

public class AttachmentLod {
    @SerializedName("model")
    private ResourceLocation modelLocation;
    @SerializedName("texture")
    private ResourceLocation modelTexture;

    public ResourceLocation getModelLocation() {
        return modelLocation;
    }

    public ResourceLocation getModelTexture() {
        return modelTexture;
    }
}
