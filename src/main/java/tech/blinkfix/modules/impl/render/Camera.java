package tech.blinkfix.modules.impl.render;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.utils.SmoothAnimationTimer;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import net.minecraft.client.CameraType;

@ModuleInfo(
        name = "Camera",
        description = "Modifies camera behavior (e.g., ViewClip, Motion Camera).",
        category = Category.RENDER
)
public class Camera extends Module {
    public BooleanValue viewClipEnabled = ValueBuilder.create(this, "ViewClip")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
    public FloatValue scale = ValueBuilder.create(this, "Scale")
            .setMinFloatValue(0.5F).setMaxFloatValue(2.0F).setDefaultFloatValue(1.0F).setFloatStep(0.01F)
            .setVisibility(() -> this.viewClipEnabled.getCurrentValue())
            .build().getFloatValue();
    public BooleanValue animation = ValueBuilder.create(this, "Animation")
            .setDefaultBooleanValue(true)
            .setVisibility(() -> this.viewClipEnabled.getCurrentValue())
            .build().getBooleanValue();
    public FloatValue animationSpeed = ValueBuilder.create(this, "Animation Speed")
            .setMinFloatValue(0.01F).setMaxFloatValue(0.5F).setDefaultFloatValue(0.3F).setFloatStep(0.01F)
            .setVisibility(() -> this.animation.getCurrentValue() && this.viewClipEnabled.getCurrentValue())
            .build().getFloatValue();
    public BooleanValue motionCameraEnabled = ValueBuilder.create(this, "Motion Camera")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
    public FloatValue motionCameraFactor = ValueBuilder.create(this, "Smoothness")
            .setMinFloatValue(0.01F).setMaxFloatValue(0.5F)
            .setDefaultFloatValue(0.1F)
            .setFloatStep(0.01F)
            .setVisibility(() -> this.motionCameraEnabled.getCurrentValue())
            .build()
            .getFloatValue();
    public SmoothAnimationTimer personViewAnimation = new SmoothAnimationTimer(0.0F);
    public double lastMotionCamX, lastMotionCamY, lastMotionCamZ;

    @Override
    public void onDisable() {
        this.lastMotionCamX = 0.0D;
        this.lastMotionCamY = 0.0D;
        this.lastMotionCamZ = 0.0D;
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        if (this.viewClipEnabled.getCurrentValue() && this.animation.getCurrentValue()) {
            boolean isThirdPerson = mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK || mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT;
            this.personViewAnimation.target = isThirdPerson ? 100.0F : 0.0F;
            this.personViewAnimation.speed = this.animationSpeed.getCurrentValue();
            this.personViewAnimation.update(true);
        } else if (this.viewClipEnabled.getCurrentValue() && !this.animation.getCurrentValue()) {
            boolean isThirdPerson = mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK || mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT;
            this.personViewAnimation.value = isThirdPerson ? 100.0F : 0.0F;
        }
    }
}