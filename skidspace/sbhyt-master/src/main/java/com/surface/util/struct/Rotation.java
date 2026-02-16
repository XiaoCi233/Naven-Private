package com.surface.util.struct;

import net.minecraft.entity.Entity;

public class Rotation {

    private float yaw, pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation(Entity entity) {
        this.yaw = entity.rotationYaw;
        this.pitch = entity.rotationPitch;
    }

    public void add(Rotation rotation) {
        this.yaw += rotation.getYaw();
        this.pitch += rotation.getPitch();
    }

    public Rotation createAdded(Rotation rotation) {
        return new Rotation(this.yaw + rotation.getYaw(), this.pitch + rotation.getPitch());
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
