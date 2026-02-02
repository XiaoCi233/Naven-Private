package tech.blinkfix.modules.impl.move;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import org.lwjgl.glfw.GLFW;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventMoveInput;
import tech.blinkfix.events.impl.EventPacket;
import tech.blinkfix.events.impl.EventRunTicks;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;

import java.util.Arrays;
import java.util.List;

@ModuleInfo(
        name = "InventoryMove",
        description = "",
        category = Category.MOVEMENT
)
public class InventoryMove extends Module {
    private final BooleanValue spoof = ValueBuilder.create(this, "spoof")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
    private static final List<KeyMapping> MOVEMENT_KEYS = Arrays.asList(
            mc.options.keyUp,
            mc.options.keyDown,
            mc.options.keyLeft,
            mc.options.keyRight,
            mc.options.keyJump
    );
    private boolean needCancel;
    private boolean isPickingItem;

    @EventTarget
    public void onPacket(EventPacket e){
        Packet<?> packet = e.getPacket();
        if (packet instanceof ServerboundContainerClosePacket closePacket && mc.screen instanceof InventoryScreen && needCancel){
            e.setCancelled(true);
        }
        if (packet instanceof ServerboundPickItemPacket || packet instanceof ServerboundSetCarriedItemPacket){
            needCancel = false;
            isPickingItem = true;
        } else {
            needCancel = true;
            isPickingItem = false;
        }
    }
    @Override
    public void onEnable() {
        super.onEnable();
        Notification notification = new Notification(NotificationLevel.INFO, "This module may cause a ban.", 10000L);
        BlinkFix.getInstance().getNotificationManager().addNotification(notification);
    }

    @EventTarget
    public void onMoveInput(EventMoveInput e){
        if (!needCancel){
            e.setForward(0);
            e.setStrafe(0);
            e.setJump(false);
        }
    }

    @EventTarget
    public void onTick(EventRunTicks event) {
        if (mc.screen == null || !isEnabled()) {
            return;
        }
        if (mc.screen instanceof ChatScreen) {
            return;
        }
        if (mc.screen instanceof InventoryScreen ) {
            updateMovementKeys();
        }
    }
    private void updateMovementKeys() {
        long windowHandle = mc.getWindow().getWindow();

        for (KeyMapping key : MOVEMENT_KEYS) {
            boolean pressed = GLFW.glfwGetKey(windowHandle, key.getKey().getValue()) == GLFW.GLFW_PRESS;
            key.setDown(pressed);
            if (key == mc.options.keyJump) {
                mc.player.setJumping(pressed);
            }
        }
    }
}