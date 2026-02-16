package com.surface.mod.move;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventPreUpdate;
import com.surface.mod.Mod;
import com.surface.value.impl.ModeValue;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class VClipModule extends Mod {
    private final ModeValue mode = new ModeValue("Mode", "High Jump", new String[]{"High Jump", "Low Jump"});

    public VClipModule() {
        super("Vertical Clip", Category.MOVE);
        registerValues(mode);
    }

    @EventTarget
    public void onMotion(EventPreUpdate e) {
        if (mc.thePlayer.ticksExisted <= 2) {
            BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 2, mc.thePlayer.posZ);
            BlockPos higherPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 3, mc.thePlayer.posZ);
            if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.glass || mc.theWorld.getBlockState(higherPos).getBlock() == Blocks.glass) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + (mode.isCurrentMode("High Jump") ? 4 : 6), mc.thePlayer.posZ);
            }
        }
    }
}