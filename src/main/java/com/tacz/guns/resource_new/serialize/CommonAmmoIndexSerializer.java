package com.tacz.guns.resource_new.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tacz.guns.resource_new.index.CommonAmmoIndex;
import com.tacz.guns.resource_new.index.CommonGunIndex;
import com.tacz.guns.resource_new.pojo.AmmoIndexPOJO;
import com.tacz.guns.resource_new.pojo.GunIndexPOJO;

import java.lang.reflect.Type;

public class CommonAmmoIndexSerializer implements JsonDeserializer<CommonAmmoIndex> {
    @Override
    public CommonAmmoIndex deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            AmmoIndexPOJO pojo = context.deserialize(json, AmmoIndexPOJO.class);
            return CommonAmmoIndex.getInstance(pojo);
        } catch (IllegalArgumentException e) {
            throw new JsonParseException(e.getMessage());
        }
    }
}
