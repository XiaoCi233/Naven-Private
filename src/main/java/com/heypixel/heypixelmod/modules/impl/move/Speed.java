package com.heypixel.heypixelmod.modules.impl.move;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import com.heypixel.heypixelmod.values.impl.FloatValue;
import com.heypixel.heypixelmod.values.impl.ModeValue;
import com.heypixel.heypixelmod.events.impl.EventRunTicks;
import com.heypixel.heypixelmod.events.impl.EventMoveInput;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.api.EventTarget;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

@ModuleInfo(
        name = "Speed",
        description = "Make you move speed faster",
        category = Category.MOVEMENT
)
public class Speed extends Module {
    private static final float DEFAULT_TICK_SPEED = 1.0F;
    private static final float SPEED_TICK_MULTIPLIER = 1.004f;
    private static final float FRICTION_MULTIPLIER = 1.002F;
    public static final Speed INSTANCE = new Speed();
    private double lastPosX, lastPosZ;
    private long lastUpdateTime;
    private double currentBPS = 0.0;
    private int jumpCooldown = 0;
    private boolean wasJumping = false;
    public double getCurrentBPS() {
        return currentBPS;
    }

    public ModeValue mode = ValueBuilder.create(this, "Mode")
            .setDefaultModeIndex(0)
            .setModes("Legit", "Collision")
            .build()
            .getModeValue();

    public final FloatValue grimAddHitBoxes = ValueBuilder.create(this, "Collision Range")
            .setVisibility(() -> mode.isCurrentMode("Collision"))
            .setDefaultFloatValue(10.0F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(20.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();

    public final FloatValue collisionBoost = ValueBuilder.create(this, "Collision Boost Test")
            .setVisibility(() -> mode.isCurrentMode("Collision"))
            .setDefaultFloatValue(0.08F)
            .setMinFloatValue(0.01F)
            .setMaxFloatValue(0.5F)
            .setFloatStep(0.01F)
            .build()
            .getFloatValue();

    public BooleanValue autoJump = ValueBuilder.create(this, "AutoJump")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public ModeValue jumpMode = ValueBuilder.create(this, "Jump Mode")
            .setVisibility(this.autoJump::getCurrentValue)
            .setDefaultModeIndex(0)
            .setModes("Continuous", "Hold Space")
            .build()
            .getModeValue();

    public BooleanValue onlyOnGround = ValueBuilder.create(this, "Only On Ground")
            .setVisibility(this.autoJump::getCurrentValue)
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
    public BooleanValue blinkCheck = ValueBuilder.create(this, "Blink Check")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
    public BooleanValue waterCheck = ValueBuilder.create(this, "Water Check")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    private float customTickSpeed = DEFAULT_TICK_SPEED;

    @Override
    public void onEnable() {
        super.onEnable();
        customTickSpeed = DEFAULT_TICK_SPEED;
        if (mc.player != null) {
            lastPosX = mc.player.getX();
            lastPosZ = mc.player.getZ();
        }
        lastUpdateTime = System.currentTimeMillis();
        jumpCooldown = 0;
        wasJumping = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        customTickSpeed = DEFAULT_TICK_SPEED;
        if (wasJumping && mc.options.keyJump != null) {
            KeyMapping.set(mc.options.keyJump.getKey(), false);
            wasJumping = false;
        }
    }

    public boolean shouldFollow() {
        return true;
    }
    private boolean shouldDisableSpeed() {
        if (blinkCheck.getCurrentValue()) {
            try {
                Blink blinkModule =
                        (Blink)
                                BlinkFix.getInstance()
                                        .getModuleManager()
                                        .getModule(Blink.class);

                if (blinkModule != null && blinkModule.isEnabled()) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        if (waterCheck.getCurrentValue() && mc.player != null) {
            if (mc.player.isInWater() || mc.player.isInWaterRainOrBubble()) {
                return true;
            }
        }

        return false;
    }

    @EventTarget
    public void onRunTicks(EventRunTicks event) {
        if (!isEnabled() || event.getType() != EventType.PRE)
            return;
        if (shouldDisableSpeed()) {
            updateBPS();
            this.setSuffix(String.format("%.2f (Disabled)", currentBPS));
            return;
        }

        if (mode.isCurrentMode("Legit")) {
            customTickSpeed = SPEED_TICK_MULTIPLIER;
        } else if (mode.isCurrentMode("Collision")) {
            handleCollisionMode();
        }

        handleAutoJump();
        updateBPS();
        this.setSuffix(String.format("%.2f", currentBPS));
    }

    private void handleCollisionMode() {
        if (mc.player == null || mc.level == null) return;
        boolean isMoving = mc.options.keyUp.isDown() || mc.options.keyDown.isDown() ||
                mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();
        if (!isMoving) return;
        int collisions = 0;
        AABB playerBox = mc.player.getBoundingBox();
        double shrinkValue = grimAddHitBoxes.getCurrentValue() / 10.0;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == null || entity == mc.player || !(entity instanceof LivingEntity)) continue;
            AABB entityBox = entity.getBoundingBox().deflate(shrinkValue, 0.0, shrinkValue);
            if (playerBox.intersects(entityBox)) {
                collisions++;
            }
        }
        if (collisions > 0) {
            float yaw = mc.player.getYRot();
            double radYaw = Math.toRadians(yaw);
            double moveX = -Math.sin(radYaw) * mc.player.zza + Math.cos(radYaw) * mc.player.xxa;
            double moveZ = Math.cos(radYaw) * mc.player.zza + Math.sin(radYaw) * mc.player.xxa;
            double angle = Math.atan2(moveX, moveZ);
            float rotationYaw = (float) angle;
            double boost = collisionBoost.getCurrentValue() * collisions;
            mc.player.setDeltaMovement(
                    mc.player.getDeltaMovement().x + Math.sin(rotationYaw) * boost,
                    mc.player.getDeltaMovement().y,
                    mc.player.getDeltaMovement().z + Math.cos(rotationYaw) * boost
            );
        }
    }

    private void handleAutoJump() {
        if (!autoJump.getCurrentValue() || mc.player == null || mc.options == null) return;

        if (jumpCooldown > 0) {
            jumpCooldown--;
        }

        boolean shouldJump = (!onlyOnGround.getCurrentValue() || mc.player.onGround());
        boolean isMovingKeyPressed =
                mc.options.keyUp.isDown() ||
                        mc.options.keyDown.isDown() ||
                        mc.options.keyLeft.isDown() ||
                        mc.options.keyRight.isDown();

        if (shouldJump && jumpCooldown <= 0 && isMovingKeyPressed) {
            if (jumpMode.isCurrentMode("Continuous")) {
                mc.player.jumpFromGround();
                jumpCooldown = 5;
            } else if (jumpMode.isCurrentMode("Hold Space")) {
                if (mc.options.keyJump != null) {
                    KeyMapping.set(mc.options.keyJump.getKey(), true);
                    wasJumping = true;
                }
            }
        } else if (jumpMode.isCurrentMode("Hold Space") && wasJumping && (!shouldJump || !isMovingKeyPressed)) {
            KeyMapping.set(mc.options.keyJump.getKey(), false);
            wasJumping = false;
        }
    }

    private void updateBPS() {
        if (mc.player == null) return;

        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastUpdateTime;
        if (timeDiff > 0) {
            double deltaX = mc.player.getX() - lastPosX;
            double deltaZ = mc.player.getZ() - lastPosZ;
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            double seconds = timeDiff / 1000.0;
            currentBPS = seconds > 0 ? horizontalDistance / seconds : 0;
            lastPosX = mc.player.getX();
            lastPosZ = mc.player.getZ();
            lastUpdateTime = currentTime;
        }
    }

    @EventTarget
    public void onMoveInput(EventMoveInput event) {
        if (!isEnabled())
            return;
        if (shouldDisableSpeed()) {
            return;
        }

        if (mode.isCurrentMode("Legit")) {
            double newSlowdown = event.getSneakSlowDownMultiplier() * FRICTION_MULTIPLIER;
            event.setSneakSlowDownMultiplier(newSlowdown);
            applySpeedModification(event);
        }
    }

    private void applySpeedModification(EventMoveInput event) {
        float forward = event.getForward() * customTickSpeed;
        float strafe = event.getStrafe() * customTickSpeed;

        forward = Math.max(-1.0F, Math.min(1.0F, forward));
        strafe = Math.max(-1.0F, Math.min(1.0F, strafe));

        event.setForward(forward);
        event.setStrafe(strafe);
    }
}