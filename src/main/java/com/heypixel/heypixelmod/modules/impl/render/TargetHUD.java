package com.heypixel.heypixelmod.modules.impl.render;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.impl.EventAttack;
import com.heypixel.heypixelmod.events.impl.EventRender2D;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.modules.impl.combat.Aura;
import com.heypixel.heypixelmod.utils.RenderUtils;
import com.heypixel.heypixelmod.utils.renderer.Fonts;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import com.heypixel.heypixelmod.values.impl.FloatValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.awt.Color;

@ModuleInfo(
        name = "TargetHUD",
        description = "Displays information about the target",
        category = Category.RENDER
)
public class TargetHUD extends Module {
    private Entity target;
    private Entity lastAttackedTarget;
    private long targetChangeTime = 0;
    private float animationProgress = 0f;
    private boolean animationRunning = false;
    private int animationDirection = 1; // 1 = 淡入, -1 = 淡出
    private Entity previousTarget = null;
    private long lastAnimationUpdate = 0;
    private boolean isAnimatingOut = false;

    public final BooleanValue showAuraTarget = ValueBuilder.create(this, "Show Aura Target")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final BooleanValue showAttackTarget = ValueBuilder.create(this, "Show Attack Target")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final FloatValue animationSpeed = ValueBuilder.create(this, "Animation Speed")
            .setDefaultFloatValue(1.0F)
            .setFloatStep(0.1F)
            .setMinFloatValue(0.1F)
            .setMaxFloatValue(2.0F)
            .build()
            .getFloatValue();

    public final FloatValue displayDuration = ValueBuilder.create(this, "Display Duration")
            .setDefaultFloatValue(3000.0F)
            .setFloatStep(100.0F)
            .setMinFloatValue(500.0F)
            .setMaxFloatValue(10000.0F)
            .build()
            .getFloatValue();

    @EventTarget
    public void onAttack(EventAttack event) {
        if (this.showAttackTarget.getCurrentValue() && event.getTarget() instanceof Player) {
            this.lastAttackedTarget = event.getTarget();
            this.targetChangeTime = System.currentTimeMillis();
            this.animationRunning = true;
            this.animationDirection = 1;
            this.isAnimatingOut = false;
            updateTarget();
        }
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        updateTarget();

        long currentTime = System.currentTimeMillis();
        if (lastAnimationUpdate == 0) {
            lastAnimationUpdate = currentTime;
        }

        long elapsedSinceLastUpdate = currentTime - lastAnimationUpdate;
        lastAnimationUpdate = currentTime;

        float animationSpeedFactor = Math.min(1.0f, elapsedSinceLastUpdate / 16.0f) * this.animationSpeed.getCurrentValue();

        if (!isAnimatingOut && target != this.previousTarget) {
            this.targetChangeTime = currentTime;
            this.previousTarget = target;
            this.animationRunning = true;
            this.animationDirection = target != null ? 1 : -1;

            if (target == null) {
                this.isAnimatingOut = true;
            }
        }

        if (!isAnimatingOut && target != null && currentTime - targetChangeTime > this.displayDuration.getCurrentValue()) {
            this.isAnimatingOut = true;
            this.animationRunning = true;
            this.animationDirection = -1;
            this.targetChangeTime = currentTime;
        }

        if (animationRunning) {
            long elapsedSinceChange = currentTime - targetChangeTime;
            long duration = animationDirection > 0 ? 850 : 400;

            if (elapsedSinceChange >= duration) {
                animationProgress = animationDirection > 0 ? 1f : 0f;
                animationRunning = false;

                if (animationDirection < 0) {
                    previousTarget = null;
                    lastAttackedTarget = null;
                    isAnimatingOut = false;
                }
            } else {
                float progress = (float) elapsedSinceChange / duration;

                if (animationDirection > 0) {
                    animationProgress = easeOutElastic(progress);
                } else {
                    animationProgress = 1f - easeInBack(progress);
                }
            }
        }
        Entity renderTarget = isAnimatingOut ? previousTarget : target;
        if (renderTarget instanceof LivingEntity && animationProgress > 0) {
            LivingEntity living = (LivingEntity)renderTarget;
            PoseStack stack = e.getStack();
            float x = (float)mc.getWindow().getGuiScaledWidth() / 2.0F + 10.0F;
            float y = (float)mc.getWindow().getGuiScaledHeight() / 2.0F + 10.0F;
            String targetName = renderTarget.getName().getString() + (living.isBaby() ? " (Baby)" : "");
            float width = Math.max(Fonts.harmony.getWidth(targetName, 0.4F) + 10.0F, 60.0F);
            float height = 30.0F;
            stack.pushPose();
            float centerX = x + width / 2;
            float centerY = y + height / 2;
            stack.translate(centerX, centerY, 0);
            stack.scale(animationProgress, animationProgress, 1);
            stack.translate(-centerX, -centerY, 0);
            RenderUtils.drawRoundedRect(stack, x, y, width, height, 5.0F, new Color(0, 0, 0, 100).getRGB());
            float healthBarY = y + 0;
            RenderUtils.drawRoundedRect(stack, x, healthBarY, width, 3.0F, 1.5F, new Color(0, 0, 0, 150).getRGB());
            float healthWidth = width * (living.getHealth() / living.getMaxHealth());
            if (healthWidth > 0) {
                RenderUtils.drawRoundedRect(stack, x, healthBarY, healthWidth, 3.0F, 1.5F,  new Color(150, 45, 45, 255).getRGB());
            }
            Fonts.harmony.render(stack, targetName, (double)(x + 5.0F), (double)(y + 6.0F), Color.WHITE, true, 0.35F);
            Fonts.harmony.render(
                    stack,
                    "HP: " + Math.round(living.getHealth()) + (living.getAbsorptionAmount() > 0.0F ? "+" + Math.round(living.getAbsorptionAmount()) : ""),
                    (double)(x + 5.0F),
                    (double)(y + 16.0F),
                    Color.WHITE,
                    true,
                    0.35F
            );

            stack.popPose();
        }
    }

    private void updateTarget() {
        Entity newTarget = null;

        if (this.showAuraTarget.getCurrentValue()) {
            try {
                Aura auraModule = (Aura) BlinkFix.getInstance().getModuleManager().getModule(Aura.class);
                if (auraModule != null && auraModule.isEnabled() && Aura.target != null) {
                    newTarget = Aura.target;
                }
            } catch (Exception e) {
            }
        }

        if (newTarget == null && this.showAttackTarget.getCurrentValue() && this.lastAttackedTarget != null) {
            newTarget = lastAttackedTarget;
        }

        if (newTarget != this.target && !isAnimatingOut) {
            this.target = newTarget;
            this.targetChangeTime = System.currentTimeMillis();
            this.animationRunning = true;
            this.animationDirection = newTarget != null ? 1 : -1;

            if (newTarget == null) {
                this.isAnimatingOut = true;
            }
        }
    }

    private float easeOutElastic(float x) {
        float c4 = (float) ((2 * Math.PI) / 3);
        return x == 0 ? 0 : x == 1 ? 1 : (float) (Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1);
    }

    private float easeInBack(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return c3 * x * x * x - c1 * x * x;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.lastAnimationUpdate = System.currentTimeMillis();
        this.isAnimatingOut = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.target = null;
        this.previousTarget = null;
        this.lastAttackedTarget = null;
        this.animationProgress = 0f;
        this.animationRunning = false;
        this.isAnimatingOut = false;
    }
}