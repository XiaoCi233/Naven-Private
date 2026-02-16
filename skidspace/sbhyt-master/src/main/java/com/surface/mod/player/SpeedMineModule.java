package com.surface.mod.player;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventPacket;
import com.surface.events.EventUpdate;
import com.surface.mod.Mod;
import com.surface.value.impl.NumberValue;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class SpeedMineModule extends Mod {
    private final NumberValue speed = new NumberValue("Speed", 1.1, 1.0, 3.0, 0.1);

    private EnumFacing facing;
    private BlockPos pos;
    private boolean boost = false;
    private float damage = 0.0F;
    public SpeedMineModule() {
        super("SpeedMine", Category.PLAYER);
        registerValues(speed);
    }

    @Override
    public String getModTag() {
        return speed.getValue().toString();
    }

    @EventTarget
    private void onPacket(EventPacket e) {
        if(e.isSendMode()) {
            if (e.getPacket() instanceof C07PacketPlayerDigging) {
                if (((C07PacketPlayerDigging) e.getPacket()).getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                    boost = true;
                    pos = ((C07PacketPlayerDigging) e.getPacket()).getPosition();
                    facing = ((C07PacketPlayerDigging) e.getPacket()).getFacing();
                    damage = 0.0F;
                } else if ((((C07PacketPlayerDigging) e.getPacket()).getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK)
                        || (((C07PacketPlayerDigging) e.getPacket()).getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK)) {
                    boost = false;
                    pos = null;
                    facing = null;
                }
            }
        }
    }

    @EventTarget
    private void onUpdate(EventUpdate e) {

        if (mc.playerController.extendedReach()) {
            mc.playerController.blockHitDelay = 0;
        }else if (pos != null && boost) {
            IBlockState blockState = mc.theWorld.getBlockState(pos);
            damage += blockState.getBlock().getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, pos) * speed.getValue();
            if (damage >= 1.0F) {
                mc.theWorld.setBlockState(pos, Blocks.air.getDefaultState(), 11);

                   mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, pos, facing));
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, facing));
                damage = 0.0F;
                boost = false;
            }
        }
    }
}