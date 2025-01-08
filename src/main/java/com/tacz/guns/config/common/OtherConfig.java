package com.tacz.guns.config.common;

import net.minecraft.Util;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class OtherConfig {
    public static ForgeConfigSpec.BooleanValue DEFAULT_PACK_DEBUG;
    public static ForgeConfigSpec.IntValue TARGET_SOUND_DISTANCE;
    public static ForgeConfigSpec.DoubleValue SERVER_HITBOX_OFFSET;
    public static ForgeConfigSpec.BooleanValue SERVER_HITBOX_LATENCY_FIX;
    public static ForgeConfigSpec.DoubleValue SERVER_HITBOX_LATENCY_MAX_SAVE_MS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> PACK_UPGRADE_BLACKLIST;

    public static void init(ForgeConfigSpec.Builder builder) {
        builder.push("other");

        builder.comment("Deprecated: now move to .minecraft/tacz/tacz-pre.toml or <your version>/tacz/tacz-pre.toml");
        builder.comment("When enabled, the reload command will not overwrite the default model file under config");
        DEFAULT_PACK_DEBUG = builder.define("DefaultPackDebug", false);

        builder.comment("The farthest sound distance of the target, including minecarts type");
        TARGET_SOUND_DISTANCE = builder.defineInRange("TargetSoundDistance", 128, 0, Integer.MAX_VALUE);

        builder.comment("The pack names in the list will be ignored during legacy pack upgrade");
        PACK_UPGRADE_BLACKLIST = builder.defineList("PackUpgradeBlackList", Util.make(() -> {
            ArrayList<String> list = new ArrayList<>();
            list.add("tacz_default_gun");
            return list;
        }), String.class::isInstance);

        serverConfig(builder);

        builder.pop();
    }

    /**
     * 这些配置不加入 cloth config api 中
     */
    private static void serverConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("DEV: Server hitbox offset (If the hitbox is ahead, fill in a negative number)");
        SERVER_HITBOX_OFFSET = builder.defineInRange("ServerHitboxOffset", 3, -Double.MAX_VALUE, Double.MAX_VALUE);

        builder.comment("Server hitbox latency fix");
        SERVER_HITBOX_LATENCY_FIX = builder.define("ServerHitboxLatencyFix", true);

        builder.comment("The maximum latency (in milliseconds) for the server hitbox latency fix saved");
        SERVER_HITBOX_LATENCY_MAX_SAVE_MS = builder.defineInRange("ServerHitboxLatencyMaxSaveMs", 1000, 250, Double.MAX_VALUE);
    }
}