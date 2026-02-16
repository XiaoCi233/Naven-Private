package com.surface.management;

import com.cubk.event.annotations.EventPriority;
import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.*;
import com.surface.util.struct.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class RotationManager {
    private Minecraft mc = Minecraft.getMinecraft();
    private Rotation rotation, lastRotation, targetRotation;
    private float rotationSpeed;
    private boolean modify, smoothed;
    private boolean movementFix;

    public RotationManager() {
        this.rotation = new Rotation(0, 0);
        Wrapper.Instance.getEventManager().register(this);
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation, float rotationSpeed, boolean movementFix) {
        this.targetRotation = rotation;
        this.rotationSpeed = rotationSpeed;
        this.movementFix = movementFix;

        this.modify = true;
        smoothRotation();
    }

    @EventTarget
    @EventPriority(8964)
    public void onMotion(EventUpdate event) {
        if (!modify || rotation == null || lastRotation == null || targetRotation == null) {
            rotation = lastRotation = targetRotation = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        }

        if (modify) {
            smoothRotation();
        }
    }

    @EventTarget
    @EventPriority(8964)
    public void onMovementInput(EventMovementInput event) {
        if (modify && movementFix) {
            final float yaw = rotation.getYaw();
            final float forward = event.getForward();
            final float strafe = event.getStrafe();

            final double angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(getDirection(mc.thePlayer.rotationYaw, forward, strafe)));

            if (forward == 0 && strafe == 0) return;

            float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

            for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
                for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                    if (predictedStrafe == 0 && predictedForward == 0) continue;

                    final double predictedAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(getDirection(yaw, predictedForward, predictedStrafe)));
                    final double difference = Math.abs(angle - predictedAngle);

                    if (difference < closestDifference) {
                        closestDifference = (float) difference;
                        closestForward = predictedForward;
                        closestStrafe = predictedStrafe;
                    }
                }
            }

            event.setForward(closestForward);
            event.setStrafe(closestStrafe);
        }
    }

    public static double getDirection(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    @EventTarget
    @EventPriority(8964)
    public void onLook(EventLook event) {
        if (modify) {
            event.setRotation(rotation);
        }
    }

    @EventTarget
    @EventPriority(8964)
    public void onStrafe(EventStrafe event) {
        if (modify && movementFix) {
            event.setYaw(rotation.getYaw());
        }
    }

    @EventTarget
    @EventPriority(8964)
    public void onJump(EventJump event) {
        if (modify && movementFix) {
            event.setYaw(rotation.getYaw());
        }
    }

    @EventTarget
    @EventPriority(8964)
    public void onUpdate(EventPreUpdate event) {
        if (modify) {
            event.setYaw(rotation.getYaw());
            event.setPitch(rotation.getPitch());

            if (Math.abs((rotation.getYaw() - mc.thePlayer.rotationYaw) % 360) < 1 && Math.abs((rotation.getPitch() - mc.thePlayer.rotationPitch)) < 1) {
                modify = false;

                correctDisabledRotations();
            }

            lastRotation = rotation;
        } else {
            lastRotation = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        }

        targetRotation = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        smoothed = false;
    }

    private void correctDisabledRotations() {
        final Rotation rotations = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        final Rotation fixedRotations = resetRotation(applySensitivityPatch(rotations, lastRotation));

        mc.thePlayer.rotationYaw = fixedRotations.getYaw();
        mc.thePlayer.rotationPitch = fixedRotations.getPitch();
    }

    public Rotation resetRotation(Rotation rotation) {
        if (rotation == null) {
            return null;
        }

        final float yaw = rotation.getYaw() + MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - rotation.getYaw());
        final float pitch = mc.thePlayer.rotationPitch;
        return new Rotation(yaw, pitch);
    }

    public Rotation applySensitivityPatch(final Rotation rotation, final Rotation previousRotation) {
        final float mouseSensitivity = (float) (mc.gameSettings.mouseSensitivity * (1 + Math.random() / 10000000) * 0.6F + 0.2F);
        final double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0F * 0.15D;
        final float yaw = previousRotation.getYaw() + (float) (Math.round((rotation.getYaw() - previousRotation.getYaw()) / multiplier) * multiplier);
        final float pitch = previousRotation.getPitch() + (float) (Math.round((rotation.getPitch() - previousRotation.getPitch()) / multiplier) * multiplier);
        return new Rotation(yaw, MathHelper.clamp_float(pitch, -90, 90));
    }

    private void smoothRotation() {
        if (!smoothed) {
            final float lastYaw = lastRotation.getYaw();
            final float lastPitch = lastRotation.getPitch();
            final float targetYaw = targetRotation.getYaw();
            final float targetPitch = targetRotation.getPitch();

            rotation = getSmoothRotation(new Rotation(lastYaw, lastPitch), new Rotation(targetYaw, targetPitch),
                    rotationSpeed + Math.random());

            if (movementFix) {
                mc.thePlayer.movementYaw = rotation.getYaw();
            }

            mc.thePlayer.velocityYaw = rotation.getYaw();
        }

        smoothed = true;

        mc.entityRenderer.getMouseOver(1);
    }

    public Rotation getSmoothRotation(Rotation lastRotation, Rotation targetRotation, double speed) {
        float yaw = targetRotation.getYaw();
        float pitch = targetRotation.getPitch();
        final float lastYaw = lastRotation.getYaw();
        final float lastPitch = lastRotation.getPitch();

        if (speed != 0) {
            final float rotationSpeed = (float) speed;

            final double deltaYaw = MathHelper.wrapAngleTo180_float(targetRotation.getYaw() - lastRotation.getYaw());
            final double deltaPitch = pitch - lastPitch;

            final double distance = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);
            final double distributionYaw = Math.abs(deltaYaw / distance);
            final double distributionPitch = Math.abs(deltaPitch / distance);

            final double maxYaw = rotationSpeed * distributionYaw;
            final double maxPitch = rotationSpeed * distributionPitch;

            final float moveYaw = (float) Math.max(Math.min(deltaYaw, maxYaw), -maxYaw);
            final float movePitch = (float) Math.max(Math.min(deltaPitch, maxPitch), -maxPitch);

            yaw = lastYaw + moveYaw;
            pitch = lastPitch + movePitch;
        }

        final boolean randomise = Math.random() > 0.8;

        for (int i = 1; i <= (int) (2 + Math.random() * 2); ++i) {

            if (randomise) {
                yaw += (float) ((Math.random() - 0.5) / 100000000);
                pitch -= (float) (Math.random() / 200000000);
            }

            /*
             * Fixing GCD
             */
            final Rotation rotations = new Rotation(yaw, pitch);
            final Rotation fixedRotations = applySensitivityPatch(rotations);

            /*
             * Setting rotations
             */
            yaw = fixedRotations.getYaw();
            pitch = Math.max(-90, Math.min(90, fixedRotations.getPitch()));
        }

        return new Rotation(yaw, pitch);
    }

    public Rotation applySensitivityPatch(Rotation rotation) {
        final Rotation previousRotation = mc.thePlayer.getLastReportedRotation();
        final float mouseSensitivity = (float) (mc.gameSettings.mouseSensitivity * (1 + Math.random() / 10000000) * 0.6F + 0.2F);
        final double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0F * 0.15D;
        final float yaw = previousRotation.getYaw() + (float) (Math.round((rotation.getYaw() - previousRotation.getYaw()) / multiplier) * multiplier);
        final float pitch = previousRotation.getPitch() + (float) (Math.round((rotation.getPitch() - previousRotation.getPitch()) / multiplier) * multiplier);
        return new Rotation(yaw, MathHelper.clamp_float(pitch, -90, 90));
    }
}
