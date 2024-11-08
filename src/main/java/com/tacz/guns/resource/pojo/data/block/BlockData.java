package com.tacz.guns.resource.pojo.data.block;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.resource.filter.RecipeFilter;

import javax.annotation.Nullable;

public class BlockData {
    @Nullable
    @SerializedName("filter")
    private RecipeFilter filter;

    @Nullable
    public RecipeFilter getFilter() {
        return filter;
    }
}
