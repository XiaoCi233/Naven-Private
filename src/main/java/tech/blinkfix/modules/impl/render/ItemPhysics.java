package tech.blinkfix.modules.impl.render;

import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.BlinkFix;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.FloatValue;

@ModuleInfo(
        name = "ItemPhysics",
        description = "Custom item physics effects",
        category = Category.RENDER
)
public class ItemPhysics extends Module {
    private final FloatValue weight = ValueBuilder.create(this, "Weight")
            .setDefaultFloatValue(0.5f)
            .setMinFloatValue(0.1f)
            .setMaxFloatValue(2.0f)
            .setFloatStep(0.1f)
            .build()
            .getFloatValue();

    private final FloatValue rotationSpeed = ValueBuilder.create(this, "RotationSpeed")
            .setDefaultFloatValue(1.0f)
            .setMinFloatValue(0.1f)
            .setMaxFloatValue(5.0f)
            .setFloatStep(0.1f)
            .build()
            .getFloatValue();

    public float getWeight() {
        return weight.getCurrentValue();
    }

    public float getRotationSpeed() {
        return rotationSpeed.getCurrentValue();
    }

    public float getHeightOffset() {
        return 0.0f;
    }

    public boolean handleEvents() {
        if (this.isEnabled()) {
            Notification notification = new Notification(NotificationLevel.INFO, "You not Admin or Beta.", 3000L);
            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            this.setEnabled(false);
            return false;
        }
        return this.isEnabled();
    }

    @Override
    public void onEnable() {
    }
}