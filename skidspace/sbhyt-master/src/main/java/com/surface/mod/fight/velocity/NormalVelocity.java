package com.surface.mod.fight.velocity;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventPacket;
import com.surface.mod.SubMod;
import com.surface.mod.fight.VelocityModule;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class NormalVelocity extends SubMod<VelocityModule> {
    public NormalVelocity(VelocityModule parent) {
        super(parent);
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            if (((S12PacketEntityVelocity) event.getPacket()).getEntityID() == mc.thePlayer.getEntityId())
                event.setCancelled(true);
        }
    }

    @Override
    public String getName() {
        return "Normal";
    }
}
