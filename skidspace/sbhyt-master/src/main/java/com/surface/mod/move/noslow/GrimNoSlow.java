package com.surface.mod.move.noslow;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.EventPacket;
import com.surface.events.EventPreUpdate;
import com.surface.mod.SubMod;
import com.surface.mod.fight.KillAuraModule;
import com.surface.mod.move.NoSlowModule;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.MovingObjectPosition;

import static com.surface.util.player.PlayerUtils.isHoldingPotionAndSword;


public class GrimNoSlow extends SubMod<NoSlowModule> {
    public boolean shouldSlow;
    KillAuraModule aura = (KillAuraModule) Wrapper.Instance.getModManager().getModFromName("Kill Aura");
    public GrimNoSlow(NoSlowModule parent) {
        super(parent);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        shouldSlow = true;
    }

    @EventTarget
    public void onPacketReceive(EventPacket event) {
        if (mc.thePlayer == null) return;
        if (event.isReceiveMode()) {
            Packet<?> packet = event.getPacket();
            Block block = null;
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                block = mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock();
            if (isHoldingPotionAndSword(mc.thePlayer.getHeldItem(), true, true)
                    && mc.thePlayer.isUsingItem() && (block == null || block != Blocks.chest)) {
                if (packet instanceof S30PacketWindowItems) {
                    event.setCancelled(true);
                    shouldSlow = false;
                }
                if (packet instanceof S2FPacketSetSlot) {
                    event.setCancelled(true);
                }
            }
        }

    }

    @EventTarget
    public void onUpdate(EventPreUpdate event) {
        if (mc.thePlayer == null) return;
        if (mc.thePlayer.isUsingItem() && isHoldingPotionAndSword(mc.thePlayer.getHeldItem(), true, true) && !aura.blocking) {
            if (mc.thePlayer.getItemInUseMaxCount() % 4 == 0) {
                mc.thePlayer.sendQueue.addToSendQueue(new C0EPacketClickWindow(0, 36, 0, 2, new ItemStack(Block.getBlockById(166)), (short) 0));
            }
        }
    }

    @EventTarget
    public void onPacketSend(EventPacket event) {
        if (mc.thePlayer == null) return;
        if (event.isSendMode()) {
            final Packet<?> packet = event.getPacket();
            if (isHoldingPotionAndSword(mc.thePlayer.getHeldItem(), true, true)) {
                if (packet instanceof C08PacketPlayerBlockPlacement) {
                    shouldSlow = true;
                }
                if (packet instanceof C07PacketPlayerDigging && ((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                    shouldSlow = true;
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Grim";
    }
}
