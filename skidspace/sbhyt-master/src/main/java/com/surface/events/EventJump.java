package com.surface.events;

import com.cubk.event.impl.CancellableEvent;

public final class EventJump extends CancellableEvent {
    private float jumpMotion;
    private float yaw;

    public EventJump(float jumpMotion, float yaw) {
        this.jumpMotion = jumpMotion;
        this.yaw = yaw;
    }

    public float getJumpMotion() {
        return jumpMotion;
    }

    public void setJumpMotion(float jumpMotion) {
        this.jumpMotion = jumpMotion;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
