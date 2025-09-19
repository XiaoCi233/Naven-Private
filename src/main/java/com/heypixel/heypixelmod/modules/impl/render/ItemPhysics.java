package com.heypixel.heypixelmod.modules.impl.render;

import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.FloatValue;

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
        return this.isEnabled();
    }
}