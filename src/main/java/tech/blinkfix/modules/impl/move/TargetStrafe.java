package tech.blinkfix.modules.impl.move;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventMoveInput;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.impl.combat.Aura;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import net.minecraft.world.entity.Entity;
@ModuleInfo(
        name = "TargetStrafe",
        description = "Automatically moves forward to follow the Aura target(Maybe failed)",
        category = Category.MOVEMENT
)
public class TargetStrafe extends Module {
    public FloatValue range = ValueBuilder.create(this, "Range")
            .setDefaultFloatValue(5.0F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(6.0F)
            .setFloatStep(0.1F)
            .build()
            .getFloatValue();

    public FloatValue switchDelay = ValueBuilder.create(this, "SwitchDelay")
            .setDefaultFloatValue(1000.0F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(10000.0F)
            .setFloatStep(100.0F)
            .build()
            .getFloatValue();

    public BooleanValue collisionSmart = ValueBuilder.create(this, "Collision Smart")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    private Entity currentTarget = null;
    private long lastTargetSwitchTime = 0;
    private boolean isIgnoringRange = false;

    @EventTarget
    public void onMoveInput(EventMoveInput event) {
//        if (!hasPermission()) {
//            Notification notification = new Notification(NotificationLevel.INFO, "You not Admin or Beta.", 3000L);
//            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
//            this.setEnabled(false);
//            return;
//        }
        Aura aura = (Aura) BlinkFix.getInstance().getModuleManager().getModule(Aura.class);
        if (aura == null || !aura.isEnabled()) {
            currentTarget = null;
            isIgnoringRange = false;
            return;
        }

        Entity auraTarget = Aura.getTarget();
        if (auraTarget == null) {
            currentTarget = null;
            isIgnoringRange = false;
            return;
        }
        boolean shouldIgnoreRange = false;
        if (collisionSmart.getCurrentValue()) {
            try {
                Speed speedModule = (Speed) BlinkFix.getInstance().getModuleManager().getModule(Speed.class);
                if (speedModule != null && speedModule.isEnabled() &&
                        speedModule.mode.isCurrentMode("Collision") &&
                        speedModule.getCurrentBPS() > 8.0) {
                    shouldIgnoreRange = true;
                }
            } catch (Exception e) {
            }
        }
        if (!shouldIgnoreRange && mc.player != null &&
                mc.player.distanceTo(auraTarget) > range.getCurrentValue()) {
            currentTarget = null;
            isIgnoringRange = false;
            return;
        }
        if (shouldIgnoreRange) {
            isIgnoringRange = true;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTarget == null || currentTarget != auraTarget) {
            if (currentTime - lastTargetSwitchTime >= switchDelay.getCurrentValue()) {
                currentTarget = auraTarget;
                lastTargetSwitchTime = currentTime;
                isIgnoringRange = shouldIgnoreRange;
            } else {
                return;
            }
        }
        if (isIgnoringRange) {
            this.setSuffix("Smart");
        } else {
            this.setSuffix(null);
        }

        event.setForward(1.0F);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        currentTarget = null;
        isIgnoringRange = false;
        this.setSuffix(null);
    }

//    @Override
//    public void onEnable() {
//        if (!hasPermission()) {
//            Notification notification = new Notification(NotificationLevel.INFO, "You not Admin or Beta.", 3000L);
//            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
//            this.setEnabled(false);
//        }
//    }
//
//    @Override
//    public boolean hasPermission() {
//        try {
//            LiveClient client = LiveClient.INSTANCE;
//            if (client == null || client.liveUser == null) {
//                return false;
//            }
//            LiveUser user = client.liveUser;
//            return user.getLevel() == LiveUser.Level.ADMINISTRATOR ||
//                    user.getLevel() == LiveUser.Level.BETA;
//        } catch (Throwable ignored) {
//            return false;
//        }
//    }
}