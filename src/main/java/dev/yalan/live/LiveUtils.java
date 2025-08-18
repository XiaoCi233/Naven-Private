package dev.yalan.live;

import net.minecraft.client.Minecraft;
import java.util.UUID;

public class LiveUtils {
    private static final Minecraft mc = Minecraft.getInstance();
    public static String getCurrentUsername() {
        if (mc.player != null) {
            LiveUser user = LiveClient.INSTANCE.getLiveUserMap().get(mc.player.getUUID());
            if (user != null) {
                return user.getName(); // 获取用户名
            }
        }
        return "Unknown";
    }
}
