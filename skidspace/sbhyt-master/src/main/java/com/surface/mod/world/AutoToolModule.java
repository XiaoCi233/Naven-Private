package com.surface.mod.world;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventPreUpdate;
import com.surface.mod.Mod;

public class AutoToolModule extends Mod {

    public AutoToolModule() {
        super("AutoTool", Category.WORLD);
    }

    private int oldSlot;
    private int tick;

    @EventTarget
    public void onPre(EventPreUpdate eventPreUpdate){
        if (mc.playerController.isBreakingBlock()) {
            tick++;

            if (tick == 1) {
                oldSlot = mc.thePlayer.inventory.currentItem;
            }

            mc.thePlayer.updateTool(mc.objectMouseOver.getBlockPos());
        } else if (tick > 0) {

                mc.thePlayer.inventory.currentItem = oldSlot;

            tick = 0;
        }
    }

}
