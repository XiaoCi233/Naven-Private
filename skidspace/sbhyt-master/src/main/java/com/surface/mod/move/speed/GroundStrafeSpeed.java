package com.surface.mod.move.speed;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventPreUpdate;
import com.surface.events.EventStrafe;
import com.surface.events.EventTick;
import com.surface.mod.SubMod;
import com.surface.mod.move.SpeedModule;
import com.surface.util.player.PlayerUtils;

public class GroundStrafeSpeed extends SubMod<SpeedModule> {
    private float lastYaw = -1;

    public GroundStrafeSpeed(SpeedModule parent) {
        super(parent);
    }

    @Override
    public void onEnable() {
        lastYaw = -1;
    }

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindJump.pressed = false;
    }

    @EventTarget
    public void onUpdate(EventTick event) {
        mc.thePlayer.motionX *= 1.0001;
        mc.thePlayer.motionZ *= 1.0001;
    }


    @EventTarget
    public void onUpdate(EventPreUpdate event) {
        event.setYaw(lastYaw);
    }

    @EventTarget
    public void onStrafe(EventStrafe event) {
        if (mc.thePlayer.onGround) {
            lastYaw = PlayerUtils.getMoveYaw();
        } else {
            event.setYaw(lastYaw);
        }
    }

    @Override
    public String getName() {
        return "Ground Strafe";
    }
}
