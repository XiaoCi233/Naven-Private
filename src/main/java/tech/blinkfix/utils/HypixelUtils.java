package tech.blinkfix.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public class HypixelUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean isHypixel() {
        if (mc.getCurrentServer() == null) return false;
        ServerData serverData = mc.getCurrentServer();
        return serverData != null &&
                (serverData.ip.toLowerCase().contains("hypixel.net") ||
                        serverData.ip.toLowerCase().contains("hypixel.io"));
    }

    public static boolean isHypixelLobby() {
        if (!isHypixel()) return false;
        return mc.level != null &&
                (mc.level.dimension().location().getPath().contains("lobby") ||
                        mc.player != null && mc.player.getScoreboard() != null &&
                                mc.player.getScoreboard().getDisplayObjective(1) != null &&
                                mc.player.getScoreboard().getDisplayObjective(1).getFormattedDisplayName().getString().toLowerCase().contains("lobby"));
    }
}