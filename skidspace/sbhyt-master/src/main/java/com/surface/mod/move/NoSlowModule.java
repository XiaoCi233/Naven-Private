package com.surface.mod.move;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.EventPreUpdate;
import com.surface.events.EventSlowdown;
import com.surface.events.EventWorldLoad;
import com.surface.interfaces.ItemChecker;
import com.surface.mod.Mod;
import com.surface.mod.fight.KillAuraModule;
import com.surface.mod.move.noslow.GrimNoSlow;
import com.surface.util.player.PlayerUtils;
import com.surface.value.impl.BooleanValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.Map;

public class NoSlowModule extends Mod implements ItemChecker {

    private final BooleanValue sword = new BooleanValue("Sword", true);
    private final BooleanValue food = new BooleanValue("Food", true);
    private final BooleanValue bow = new BooleanValue("Bow", true);
    private final BooleanValue web = new BooleanValue("Web", true);
    private final BooleanValue liquid = new BooleanValue("Liquid", true);
    public static boolean shouldCancelWater;
    GrimNoSlow grimNoSlow;

    public NoSlowModule() {
        super("No Slow", Category.MOVE);
        grimNoSlow = new GrimNoSlow(this);
        regitserSubModules(grimNoSlow);
        registerValues(sword, food, bow, web, liquid);
    }


    @Override
    public void onDisable() {
        shouldCancelWater = false;
    }

    @EventTarget
    public void onWorldLoad(EventWorldLoad e) {
        shouldCancelWater = false;
    }

    @EventTarget
    public void onUpdate(EventPreUpdate event) {
        if (web.getValue()) {
                Map<BlockPos, Block> searchBlock = PlayerUtils.searchBlocks(2);
                for (Map.Entry<BlockPos, Block> block : searchBlock.entrySet()) {
                    if (mc.theWorld.getBlockState(block.getKey()).getBlock() instanceof BlockWeb) {
                        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, block.getKey(), EnumFacing.DOWN));
                        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, block.getKey(), EnumFacing.DOWN));
                    }
                }
                mc.thePlayer.isInWeb = false;
        }
        if (liquid.getValue()) {
            shouldCancelWater = false;

            Map<BlockPos, Block> searchBlock = PlayerUtils.searchBlocks(2);
            for (Map.Entry<BlockPos, Block> block : searchBlock.entrySet()) {
                boolean checkBlock = mc.theWorld.getBlockState(block.getKey()).getBlock() == Blocks.water
                        || mc.theWorld.getBlockState(block.getKey()).getBlock() == Blocks.flowing_water
                        || mc.theWorld.getBlockState(block.getKey()).getBlock() == Blocks.lava
                        || mc.theWorld.getBlockState(block.getKey()).getBlock() == Blocks.flowing_lava;
                if (checkBlock) {
                    shouldCancelWater = true;
                    mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, block.getKey(), EnumFacing.DOWN));
                    mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, block.getKey(), EnumFacing.DOWN));
                }
            }
        }
    }

    @EventTarget
    public void onSlow(EventSlowdown event) {
        if (this.subMode.getValue().equalsIgnoreCase("Grim")) {
            KillAuraModule killAuraModule = (KillAuraModule) Wrapper.Instance.getModManager().getModFromName("Kill Aura");
            if (sword.getValue() && (hasSword() || killAuraModule.blocking))
                event.setCancelled(!grimNoSlow.shouldSlow);
            if (bow.getValue() && hasBow())
                event.setCancelled(!grimNoSlow.shouldSlow);
            if (food.getValue() && hasFood())
                event.setCancelled(!grimNoSlow.shouldSlow);
            if (mc.thePlayer.isUsingItem() && mc.thePlayer.moveForward > 0) {
                mc.thePlayer.setSprinting(true);
            }
        } else {
            if (sword.getValue() && hasSword())
                event.setCancelled(true);
            if (bow.getValue() && hasBow())
                event.setCancelled(true);
            if (food.getValue() && hasFood())
                event.setCancelled(true);
        }
    }
}