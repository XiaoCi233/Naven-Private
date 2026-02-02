package tech.blinkfix.modules.impl.move;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.events.impl.EventRunTicks;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.api.EventTarget;

@ModuleInfo(
        name = "Fly",
        description = "Allows you to fly freely",
        category = Category.MOVEMENT
)
public class Fly extends Module {
    private boolean wasFlying = false;

    public final FloatValue flySpeed = ValueBuilder.create(this, "Fly Speed")
            .setDefaultFloatValue(1.0F)
            .setMinFloatValue(0.1F)
            .setMaxFloatValue(5.0F)
            .setFloatStep(0.1F)
            .build()
            .getFloatValue();

    public final FloatValue verticalSpeed = ValueBuilder.create(this, "Vertical Speed")
            .setDefaultFloatValue(0.5F)
            .setMinFloatValue(0.1F)
            .setMaxFloatValue(2.0F)
            .setFloatStep(0.1F)
            .build()
            .getFloatValue();

    @Override
    public void onEnable() {
            super.onEnable();
            Notification notification = new Notification(NotificationLevel.INFO, "This module may cause a ban.", 10000L);
            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
        super.onEnable();
        if (mc.player != null) {
            wasFlying = mc.player.getAbilities().flying;
            mc.player.getAbilities().flying = false; // Disable vanilla flying if enabled
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null) {
            // Reset player motion when disabling
            mc.player.setDeltaMovement(0, 0, 0);
            mc.player.getAbilities().flying = wasFlying;
        }
    }

    @EventTarget
    public void onRunTicks(EventRunTicks event) {
        if (!isEnabled() || event.getType() != EventType.PRE || mc.player == null)
            return;

        // Check if player is in water or lava and cancel flying if true
        if (mc.player.isInWater() || mc.player.isInLava()) {
            return;
        }

        // Get movement direction based on key presses
        float forward = 0;
        float strafe = 0;
        float vertical = 0;

        if (mc.options.keyUp.isDown()) forward += 1;
        if (mc.options.keyDown.isDown()) forward -= 1;
        if (mc.options.keyLeft.isDown()) strafe += 1;
        if (mc.options.keyRight.isDown()) strafe -= 1;
        if (mc.options.keyJump.isDown()) vertical += 1;
        if (mc.options.keyShift.isDown()) vertical -= 1;

        // Normalize movement for diagonal movement
        if (forward != 0 && strafe != 0) {
            forward *= 0.707f; // sqrt(2)/2
            strafe *= 0.707f;
        }

        // Calculate movement vector based on player's rotation
        float yaw = mc.player.getYRot();
        double radYaw = Math.toRadians(yaw);

        double motionX = (-Math.sin(radYaw) * forward + Math.cos(radYaw) * strafe) * flySpeed.getCurrentValue();
        double motionZ = (Math.cos(radYaw) * forward + Math.sin(radYaw) * strafe) * flySpeed.getCurrentValue();
        double motionY = vertical * verticalSpeed.getCurrentValue();

        // Apply movement
        mc.player.setDeltaMovement(motionX, motionY, motionZ);

        // Update module suffix with current speed
    }
}