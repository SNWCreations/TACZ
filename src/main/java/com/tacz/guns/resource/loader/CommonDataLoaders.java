package com.tacz.guns.resource.loader;

import com.tacz.guns.resource.filter.RecipeFilter;
import com.tacz.guns.resource_new.network.DataType;
import com.tacz.guns.resource_new.pojo.data.block.BlockData;

public class CommonDataLoaders {
    public static final DataLoader<RecipeFilter> RECIPE_FILTER = new DataLoader<>(DataType.RECIPE_FILTER,
            RecipeFilter.class,
            "RecipeFilter",
            "filters");

    public static final DataLoader<BlockData> BLOCKS = new DataLoader<>(DataType.BLOCK_DATA,
            BlockData.class,
            "BlockData",
            "blocks/data");
}
