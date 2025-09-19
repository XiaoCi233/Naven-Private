package dev.yalan.live;

import net.minecraft.client.Minecraft;
import java.util.UUID;

public class LiveUtils {
    private static final Minecraft mc = Minecraft.getInstance();
    public static String getCurrentUsername() {
        return LiveClient.INSTANCE.liveUser.getName();
    }
}
