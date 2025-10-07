package com.heypixel.heypixelmod.modules.impl.misc;

import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.EventRunTicks;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.PermissionGatedModule;
import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.ui.notification.Notification;
import com.heypixel.heypixelmod.ui.notification.NotificationLevel;
import com.heypixel.heypixelmod.utils.TimeHelper;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.FloatValue;
import dev.yalan.live.LiveClient;
import dev.yalan.live.LiveUser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@ModuleInfo(
        name = "AutoReport",
        description = "Auto Report Hacker $ GreenPlayer^_^",
        category = Category.MISC
)
public class AutoReport extends Module implements PermissionGatedModule {
    public FloatValue delay = ValueBuilder.create(this, "Delay")
            .setDefaultFloatValue(6000.0F)
            .setFloatStep(100.0F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(15000.0F)
            .build()
            .getFloatValue();

    private final TimeHelper timer = new TimeHelper();
    private final Random random = new Random();
    private final Minecraft mc = Minecraft.getInstance();

    private final Set<String> reportedPlayers = new HashSet<>();

    @EventTarget
    public void onMotion(EventRunTicks e) {
        // Permission gate: only Administrator level or rank §eBeta can use this module
        if (!hasPermission()) {
            Notification notification = new Notification(NotificationLevel.INFO, "You not Admin or Beta.", 3000L);
            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            this.setEnabled(false);
            return;
        }

        if (!mc.isSingleplayer()) {
            if (e.getType() == EventType.POST && timer.delay((double) delay.getCurrentValue())) {
                List<String> playerList = getUnreportedPlayers();

                if (!playerList.isEmpty()) {
                    String targetName = playerList.get(random.nextInt(playerList.size()));
                    reportPlayer(targetName);
                    timer.reset();
                }
            }
        }
    }

    private void reportPlayer(String playerName) {
        if (playerName == null || playerName.isEmpty()) return;

        mc.player.connection.sendChat("/report " + playerName);
        reportedPlayers.add(playerName);
    }


    private List<String> getUnreportedPlayers() {
        List<String> players = new ArrayList<>();

        if (mc.player != null && mc.player.connection != null) {
            for (PlayerInfo info : mc.player.connection.getOnlinePlayers()) {
                String name = info.getProfile().getName();

                if (!name.equals(mc.player.getGameProfile().getName()) && !reportedPlayers.contains(name)) {
                    players.add(name);
                }
            }
        }
        return players;
    }

    @Override
    public void onDisable() {
        reportedPlayers.clear();
        super.onDisable();
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
                    "§eBeta".equals(user.getRank());
        } catch (Throwable ignored) {
            return false;
        }
    }
}
