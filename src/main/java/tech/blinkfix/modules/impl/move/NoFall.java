// Decompiled with: CFR 0.152
// Class Version: 17
package tech.blinkfix.modules.impl.move;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.events.impl.EventMoveInput;
import tech.blinkfix.events.impl.EventPacket;
import tech.blinkfix.events.impl.EventRunTicks;
import tech.blinkfix.events.impl.EventStrafe;
import tech.blinkfix.events.impl.EventUpdate;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.PermissionGatedModule;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.BlinkFix;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.FloatValue;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

@ModuleInfo(name = "NoFall", category = Category.MOVEMENT, description = "Prevent fall damage")
public class NoFall extends Module  {
    private final FloatValue fallDistance = ValueBuilder.create(this, "Fall Distance")
            .setDefaultFloatValue(3.0f)
            .setFloatStep(0.1f)
            .setMinFloatValue(3.0f)
            .setMaxFloatValue(15.0f)
            .build()
            .getFloatValue();

    private double previousFallDistance;
    private boolean isLagged = false;
    private boolean shouldHandleFall = false;
    private boolean shouldSendLagPacket = false;
    private boolean shouldJump = false;

    @Override
    public void onEnable() {

        Notification notification = new Notification(NotificationLevel.INFO, "This module may cause a ban.", 10000L);
        BlinkFix.getInstance().getNotificationManager().addNotification(notification);
        resetState();
    }

    @Override
    public void onDisable() {
        resetState();
    }

    private void resetState() {
        isLagged = false;
        shouldHandleFall = false;
        shouldSendLagPacket = false;
        shouldJump = false;
    }

    private boolean shouldBlockJump() {
        return shouldHandleFall || shouldJump;
    }

    @EventTarget
    public void onTick(EventRunTicks event) {

        if (event.getType() == EventType.POST || mc.player == null) {
            return;
        }

        if (shouldBlockJump()) {
            mc.options.keyJump.setDown(false);
        }

        previousFallDistance = mc.player.onGround() ? 0.0 : mc.player.fallDistance;

        if (isLagged && shouldHandleFall) {
            shouldJump = true;
            shouldHandleFall = false;
            isLagged = false;
        }
    }

    @EventTarget
    public void onLivingUpdate(EventUpdate event) {
        if (shouldBlockJump() && mc.options != null) {
            mc.options.keyJump.setDown(false);
        }
    }

    @EventTarget
    public void onStrafe(EventStrafe event) {
        if (mc.player.onGround() && shouldJump) {
            mc.player.jumpFromGround();
            shouldJump = false;
        }
    }

    @EventTarget
    public void onMoveInput(EventMoveInput event) {
        if (shouldBlockJump()) {
            event.setJump(false);
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (event.getType() == EventType.POST) {
            return;
        }

        if (!shouldHandleFall &&
                mc.player.fallDistance > fallDistance.getCurrentValue() &&
                !event.isOnGround()) {
            shouldHandleFall = true;
            isLagged = false;
            shouldSendLagPacket = false;
        }

        if (shouldHandleFall && mc.player.fallDistance < 3.0f) {
            event.setOnGround(false);

            if (!shouldSendLagPacket) {
                mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(
                        event.getX() - 1000.0,
                        event.getY(),
                        event.getZ(),
                        false
                ));
                shouldSendLagPacket = true;
            }
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getType() == EventType.SEND) {
            if (shouldHandleFall &&
                    shouldSendLagPacket &&
                    !isLagged &&
                    event.getPacket() instanceof ServerboundMovePlayerPacket) {
                event.setCancelled(true);
            }
            
        } else if (shouldHandleFall &&
                event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            isLagged = true;
        }
    }

}