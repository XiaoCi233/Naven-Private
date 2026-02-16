package com.surface.mod.fight.velocity;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.EventPacket;
import com.surface.events.EventPreUpdate;
import com.surface.events.EventUpdate;
import com.surface.mod.SubMod;
import com.surface.mod.fight.KillAuraModule;
import com.surface.mod.fight.VelocityModule;
import com.surface.util.player.PlayerUtils;
import com.surface.util.player.RaytraceUtils;
import com.surface.value.impl.BooleanValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class VerticalVelocity extends SubMod<VelocityModule> {

    private final BooleanValue boost = new BooleanValue("Boost", false) {
        @Override
        public boolean isVisible() {
            return getParent().isSub(VerticalVelocity.this);
        }
    };

    public VerticalVelocity(VelocityModule parent) {
        super(parent);
        getParent().registerValues(boost);
    }

    KillAuraModule aura;

    boolean velocityInput;
    double motion;

    @Override
    public void onEnable() {
        aura = (KillAuraModule) Wrapper.Instance.getModManager().getModFromName("Kill Aura");
        velocityInput = false;
        super.onEnable();
    }

    @EventTarget
    public void Pre(EventPreUpdate eventPreUpdate){
        if(boost.getValue()) {
            AxisAlignedBB player = mc.thePlayer.getEntityBoundingBox();
            if (mc.theWorld.loadedEntityList.stream().anyMatch(entity -> entity != mc.thePlayer && !entity.isDead && entity instanceof EntityLivingBase && entity.getEntityBoundingBox().intersectsWith(player))) {
                mc.thePlayer.motionX *= 1.19;
                mc.thePlayer.motionZ *= 1.19;
            }
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (velocityInput) {
            if (aura.target == null) return;
            if (RaytraceUtils.rayCast(mc.thePlayer.getLastReportedRotation(), 3.2).entityHit == aura.target && PlayerUtils.movementInput()) {
                for (int i = 0; i < 6; i++) {
                    if (mc.thePlayer.serverSprintState && this.isMoving()) {
                        mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(aura.target, C02PacketUseEntity.Action.ATTACK));
                        mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                    } else {
                        mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                        mc.thePlayer.setSprinting(false);
                        mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(aura.target, C02PacketUseEntity.Action.ATTACK));
                        mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                        mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    }
                }

                mc.thePlayer.motionX *= motion;
                mc.thePlayer.motionZ *= motion;
            }

            velocityInput = false;

        }

    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (mc.thePlayer == null) return;
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            if (((S12PacketEntityVelocity) event.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
                S12PacketEntityVelocity s12 = ((S12PacketEntityVelocity) event.getPacket());

                if (PlayerUtils.movementInput()) {
                    motion = getMotion(s12);
                }
                velocityInput = true;
            }
        }
    }

    private double getMotion(S12PacketEntityVelocity packetEntityVelocity) {
        double strength = new Vec3(packetEntityVelocity.getMotionX(), packetEntityVelocity.getMotionY(), packetEntityVelocity.getMotionZ()).lengthVector();
        double motionNoXZ;
        if (strength >= 20000D) {
            if (mc.thePlayer.onGround) {
                motionNoXZ = 0.05425D;
            } else {
                motionNoXZ = 0.065D;
            }
        } else if (strength >= 5000D) {
            if (mc.thePlayer.onGround) {
                motionNoXZ = 0.01625D;
            } else {
                motionNoXZ = 0.0452D;
            }
        } else {
            motionNoXZ = 0.0075D;
        }
        return motionNoXZ;
    }

    @Override
    public String getName() {
        return "Ignore Horizontal";
    }
}
