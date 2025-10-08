package com.heypixel.heypixelmod.modules.impl.misc;

import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.PermissionGatedModule;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.ui.notification.Notification;
import com.heypixel.heypixelmod.ui.notification.NotificationLevel;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import dev.yalan.live.LiveClient;
import dev.yalan.live.LiveUser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

@ModuleInfo(
        name = "AutoPlay",
        description = "AutoPlay Game",
        category = Category.MISC
)
public class AutoPlay extends Module implements PermissionGatedModule {
    private final Minecraft mc = Minecraft.getInstance();

    BooleanValue gameover = ValueBuilder.create(this, "Game Over Check")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    BooleanValue gamestart = ValueBuilder.create(this, "Game Start Check")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
    public void onTitlePacket(ClientboundSetTitleTextPacket packet) {
        if (!isEnabled() || !gamestart.getCurrentValue()) return;
        if (!hasPermission()) {
            Notification notification = new Notification(NotificationLevel.INFO, "You not Admin or Beta.", 3000L);
            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            this.setEnabled(false);
            return;
        }

        Component titleComponent = packet.getText();
        if (titleComponent != null) {
            String title = titleComponent.getString();
            if (title.contains("稍等片刻，等待其他玩家")) {
                sendAgainCommand();
            }
        }
    }
    public void onSubtitlePacket(ClientboundSetSubtitleTextPacket packet) {
        if (!isEnabled() || !gamestart.getCurrentValue()) return;
        if (!hasPermission()) {
            Notification notification = new Notification(NotificationLevel.INFO, "You not Admin or Beta.", 3000L);
            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            this.setEnabled(false);
            return;
        }

        Component subtitleComponent = packet.getText();
        if (subtitleComponent != null) {
            String subtitle = subtitleComponent.getString();
            if (subtitle.contains("稍等片刻，等待其他玩家")) {
                sendAgainCommand();
            }
        }
    }
    public void onSystemChatPacket(ClientboundSystemChatPacket packet) {
        if (!isEnabled() || !gameover.getCurrentValue()) return;
        if (!hasPermission()) {
            Notification notification = new Notification(NotificationLevel.INFO, "You not Admin or Beta.", 3000L);
            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            this.setEnabled(false);
            return;
        }

        Component message = packet.content();
        if (message != null) {
            String text = message.getString();
            if (text.contains("游戏结束，请对此地图") ||
                    (mc.player != null && mc.player.isSpectator())) {
                sendAgainCommand();
            }
        }
    }
    private void sendAgainCommand() {
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.sendCommand("again");
        }
    }

    @Override
    public boolean hasPermission() {
        try {
            LiveClient client = LiveClient.INSTANCE;
            if (client == null || client.liveUser == null) {
                return false;
            }
            LiveUser user = client.liveUser;
            return user.getLevel() == LiveUser.Level.ADMINISTRATOR ||
                    user.getLevel() == LiveUser.Level.BETA;
        } catch (Throwable ignored) {
            return false;
        }
    }
}