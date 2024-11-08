package com.tacz.guns.client.event;

import com.tacz.guns.resource.PackConvertor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PlayerEnterWorld {

    @SubscribeEvent
    public static void onPlayerEnterWorld(PlayerEvent.PlayerLoggedInEvent event) {
        File[] files = PackConvertor.FOLDER.toFile().listFiles();
        if (files != null && files.length > 0){
            Component component = Component.literal("[TACZ] 发现旧版枪包资源，是否进行转换? ").append(Component.literal("[点我转换]").withStyle(
                    Style.EMPTY.withColor(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tacz convert"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击转换"))
            )));
            event.getEntity().sendSystemMessage(component);
        }
    }
}
