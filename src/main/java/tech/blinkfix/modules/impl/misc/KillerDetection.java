package tech.blinkfix.modules.impl.misc;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.events.impl.EventRespawn;
import tech.blinkfix.events.impl.EventUpdate;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.utils.ChatUtils;
import tech.blinkfix.utils.RenderUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@ModuleInfo(
        name = "KillerDetection",
        description = "KillerDetection you",
        category = Category.MISC
)

public class KillerDetection extends Module {
    public KillerDetection() {
    }

    private final Set<String> detectedKillers = new HashSet<>();

    @EventTarget
    private void onUpdate(EventUpdate event) {
        if (mc.level == null || mc.player == null) {
            return;
        }

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player player) {
                if (player == mc.player) {
                    continue;
                }

                String playerName = player.getName().getString();

                if (detectedKillers.contains(playerName)) {
                    continue;
                }

                ItemStack mainHandItem = player.getMainHandItem();

                if (mainHandItem.getItem() == Items.IRON_SWORD || mainHandItem.getItem() == Items.DIAMOND_SWORD || mainHandItem.getItem() instanceof SwordItem) {
                    ChatUtils.addChatMessage("[KillerDetection] Player " + playerName + " It's a killer!");
                    Notification notification = new Notification(NotificationLevel.ERROR, "The killer is " + playerName, 6000L);
                    BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                    detectedKillers.add(playerName);
                }
            }
        }
    }

    @EventTarget
    private void onRender3D(EventRender2D event) {
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (detectedKillers.contains(entity.getName().getString())) {
                RenderUtils.drawRoundedRect(event.getStack(), new Color(255, 0, 0, 50));
            }
        }
    }

    @EventTarget
    private void onWorld(EventRespawn event) {
        detectedKillers.clear();
    }
}
