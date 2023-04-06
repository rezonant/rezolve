package com.astronautlabs.mc.rezolve.common;

import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Errors {
    private static final Logger LOGGER = LogManager.getLogger(RezolveMod.ID);

    public static void reportException(Level level, Exception e, String message, Object... params) {
        message = String.format(message, params);

        LOGGER.error(message, params);
        LOGGER.error("Details:");
        LOGGER.error(e);

        CommandSource chat;

        if (level.isClientSide) {
            chat = Minecraft.getInstance().player;
        } else {
            chat = level.getServer();
        }

        chat.sendSystemMessage(Component.empty()
                .append(Component.literal("Error: ").withStyle(ChatFormatting.DARK_RED))
                .append(Component.literal(message))
        );
        chat.sendSystemMessage(Component.literal(String.format("[%s] %s", e.getClass().getName(), e.getMessage())));

        Throwable throwable = e.getCause();
        while (throwable != null) {
            chat.sendSystemMessage(
                    Component.literal(
                            String.format("    Caused by [%s] %s",
                                    throwable.getClass().getName(),
                                    throwable.getMessage()
                            )
                    ).withStyle(ChatFormatting.GRAY)
            );

            throwable = e.getCause();
        }
    }
}
