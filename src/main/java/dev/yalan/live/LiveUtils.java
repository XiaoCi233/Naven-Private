package dev.yalan.live;

import net.minecraft.client.Minecraft;

public class LiveUtils {
    private static final Minecraft mc = Minecraft.getInstance();
    public static String getCurrentUsername() {
        return LiveClient.INSTANCE.liveUser.getName();
    }
}
