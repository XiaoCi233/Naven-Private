package com.surface.mod.move;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventUpdate;
import com.surface.mod.Mod;
import com.surface.mod.move.noslow.GrimNoSlow;
import com.surface.util.HYTUtils;
import com.surface.util.player.PlayerUtils;
import com.surface.value.impl.BooleanValue;
import com.surface.value.impl.NumberValue;

public class FlightModule extends Mod {

    private final NumberValue speed = new NumberValue("Speed", 2, 1, 3, 0.1);

    public FlightModule() {
        super("AutoFlight", Category.MOVE);
        registerValues(speed);
    }


    @Override
    public String getModTag() {
        return speed.getValueName();
    }


    @EventTarget
    public void onUpdate(EventUpdate e) {
        if(HYTUtils.isInLobby())return;
        if(mc.thePlayer.ticksExisted < 20 || mc.thePlayer.capabilities.allowFlying) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionZ = 0;

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.motionY = speed.getValue();
            }

            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.thePlayer.motionY = -speed.getValue();
            }

            if (PlayerUtils.isMoving()) {
                PlayerUtils.setSpeed(speed.getValue() * 3);
            }
        }
    }

    @Override
    public void onDisable() {
        if(mc.thePlayer == null) return;
        mc.thePlayer.motionX = 0;
        mc.thePlayer.motionY = 0;
        mc.thePlayer.motionZ = 0;
        super.onDisable();
    }

}
