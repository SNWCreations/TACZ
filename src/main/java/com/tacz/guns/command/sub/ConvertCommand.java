package com.tacz.guns.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.tacz.guns.client.event.PlayerEnterWorld;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class ConvertCommand {
    private static final String CONVERT_NAME = "convert";

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal(CONVERT_NAME);
        reload.executes(ConvertCommand::convert);
        return reload;
    }

    private static int convert(CommandContext<CommandSourceStack> context) {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> PlayerEnterWorld::convert);
        return Command.SINGLE_SUCCESS;
    }
}
