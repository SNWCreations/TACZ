package com.tacz.guns.resource_new.index;

import com.google.common.base.Preconditions;
import com.tacz.guns.resource.CommonAssetManager;
import com.tacz.guns.resource_new.pojo.BlockIndexPOJO;
import com.tacz.guns.resource_new.pojo.data.block.BlockData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.registries.ForgeRegistries;

public class CommonBlockIndex {

    private BlockIndexPOJO pojo;
    private BlockItem block;
    private BlockData data;

    public static CommonBlockIndex getInstance(BlockIndexPOJO gunIndexPOJO) throws IllegalArgumentException {
        CommonBlockIndex index = new CommonBlockIndex();
        index.pojo = gunIndexPOJO;
        checkIndex(gunIndexPOJO, index);
        checkData(gunIndexPOJO, index);
        return index;
    }

    private static void checkIndex(BlockIndexPOJO block, CommonBlockIndex index) {
        ResourceLocation id = index.pojo.getId();
        Preconditions.checkArgument(block != null, "index object file is empty");
        if(!(ForgeRegistries.ITEMS.getValue(id) instanceof BlockItem item)) {
            throw new IllegalArgumentException("BlockItem not found for " + block.getName());
        }
        index.block = item;
    }

    private static void checkData(BlockIndexPOJO block, CommonBlockIndex index) {
        ResourceLocation pojoData = block.getData();
        Preconditions.checkArgument(pojoData != null, "index object missing pojoData field");
        BlockData data = CommonAssetManager.INSTANCE.getBlockData(pojoData);
        Preconditions.checkArgument(data != null, "there is no corresponding data file");
        index.data = data;
    }

    public BlockIndexPOJO getPojo() {
        return pojo;
    }

    public BlockItem getBlock() {
        return block;
    }

    public BlockData getData() {
        return data;
    }
}
