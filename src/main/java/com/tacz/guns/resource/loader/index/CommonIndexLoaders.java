package com.tacz.guns.resource.loader.index;

import com.tacz.guns.resource.CommonGunPackLoader;
import com.tacz.guns.resource_new.index.CommonBlockIndex;
import com.tacz.guns.resource.loader.IndexLoader;
import com.tacz.guns.resource_new.network.DataType;
import com.tacz.guns.resource_new.pojo.BlockIndexPOJO;

public class CommonIndexLoaders {
    public static final IndexLoader<BlockIndexPOJO, CommonBlockIndex> BLOCK = new IndexLoader<>(DataType.BLOCK_INDEX,
            BlockIndexPOJO.class,
            CommonBlockIndex.class,
            "BlockIndex",
            "blocks/index",
            CommonGunPackLoader.BLOCK_INDEX::put,
            CommonBlockIndex::getInstance);
}
