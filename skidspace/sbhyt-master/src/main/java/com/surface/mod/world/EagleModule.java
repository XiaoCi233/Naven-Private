package com.surface.mod.world;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventPreUpdate;
import com.surface.mod.Mod;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class EagleModule extends Mod {

    public EagleModule() {
        super("Eagle", Category.WORLD);
    }

    @EventTarget
    public void onPre(EventPreUpdate eventPreUpdate){
        BlockPos belowPlayer = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1D, mc.thePlayer.posZ);
        if (mc.thePlayer.onGround) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            if (mc.theWorld.getBlockState(belowPlayer).getBlock() == Blocks.air) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            }
        }
    }

}
