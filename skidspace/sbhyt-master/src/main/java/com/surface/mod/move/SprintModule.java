package com.surface.mod.move;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.EventTick;
import com.surface.mod.Mod;
import com.surface.mod.world.ScaffoldModule;

public class SprintModule extends Mod {
    public SprintModule() {
        super("Sprint", Category.MOVE);
    }

    @EventTarget
    public void onTick(EventTick event) {
        ScaffoldModule scaffold = (ScaffoldModule) Wrapper.Instance.getModManager().getModFromName("Scaffold");
        if(scaffold.isEnable()) {
            mc.thePlayer.setSprinting(false);
        }else{
            mc.gameSettings.keyBindSprint.pressed = true;
        }
    }

}
