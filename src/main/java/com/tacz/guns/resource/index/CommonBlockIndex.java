package com.tacz.guns.resource.index;

import com.tacz.guns.resource.CommonAssetManager;
import com.tacz.guns.resource.pojo.BlockIndexPOJO;
import com.tacz.guns.resource.pojo.data.block.BlockData;
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
        ResourceLocation id = index.pojo.getId();
        if(!(ForgeRegistries.ITEMS.getValue(id) instanceof BlockItem item)) {
            throw new IllegalArgumentException("BlockItem not found for " + gunIndexPOJO.getName());
        }
        index.block = item;
        BlockData data = CommonAssetManager.INSTANCE.getBlockData(gunIndexPOJO.getData());
        if (data == null) {
            throw new IllegalArgumentException("BlockData not found for " + gunIndexPOJO.getName());
        }
        index.data = data;
        return index;
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
