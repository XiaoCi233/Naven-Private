package com.surface.mod.player;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.config.DontLoadState;
import com.surface.events.Event3D;
import com.surface.events.EventPacket;
import com.surface.events.EventTick;
import com.surface.mod.Mod;
import com.surface.util.TimerUtils;
import com.surface.util.render.RenderUtils;
import com.surface.value.impl.BooleanValue;
import com.surface.value.impl.ModeValue;
import com.surface.value.impl.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import renderassist.animations.LinearAnimation;

import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@DontLoadState
public class BlinkModule extends Mod {

    private final ModeValue releaseType = new ModeValue("Release Mode", "Latency", new String[]{"Latency", "Instant"});
    public final NumberValue speed = new NumberValue("Speed", 10, 1, 100, 1) {
        @Override
        public boolean isVisible() {
            return releaseType.isCurrentMode("Latency");
        }
    };
    public final BooleanValue slowPoll = new BooleanValue("Slow poll", true) {
        @Override
        public boolean isVisible() {
            return releaseType.isCurrentMode("Latency");
        }
    };
    public final NumberValue pollDelay = new NumberValue("Poll Delay", 100, 0, 100, 1) {
        @Override
        public boolean isVisible() {
            return releaseType.isCurrentMode("Latency") && slowPoll.getValue();
        }
    };
    public final NumberValue startPollDelay = new NumberValue("Start Poll Delay", 2000, 1000, 8000, 200) {
        @Override
        public boolean isVisible() {
            return releaseType.isCurrentMode("Latency") && slowPoll.getValue();
        }
    };
    public final BooleanValue antiAim = new BooleanValue("Anti Aim", false) {
        @Override
        public boolean isVisible() {
            return releaseType.isCurrentMode("Latency");
        }
    };
    private final ConcurrentLinkedQueue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Packet<?>> testPackets = new ConcurrentLinkedQueue<>();
    private boolean sendPacket = false;
    private double x;
    private TimerUtils timer = new TimerUtils();
    private TimerUtils timer2 = new TimerUtils();
    private double y;
    private double z;
    private double motionX;
    private double motionY;
    private double motionZ;
    private boolean pressed;
    private double latencyX;
    private double latencyY;
    private double latencyZ;
    private double latencyXAnimation;
    private double latencyYAnimation;
    private double latencyZAnimation;

    public BlinkModule() {
        super("Blink", Category.PLAYER);
        registerValues(releaseType, speed, slowPoll, pollDelay, startPollDelay, antiAim);
    }

    @Override
    public String getModTag() {
        return releaseType.getValue();
    }

    @Override
    public void onEnable() {
        if (!packets.isEmpty() && sendPacket) {
            return;
        }
        sendPacket = false;
        x = mc.thePlayer.posX;
        y = mc.thePlayer.posY;
        z = mc.thePlayer.posZ;
        latencyX = x;
        latencyY = y;
        latencyZ = z;
        motionX = mc.thePlayer.motionX;
        motionY = mc.thePlayer.motionY;
        motionZ = mc.thePlayer.motionZ;
        pressed = mc.gameSettings.keyBindSneak.pressed;

        packets.clear();
        timer2.reset();
    }

    @Override
    public void onDisable() {
        timer.reset();
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            mc.thePlayer.setPosition(x, y, z);
            mc.thePlayer.motionX = motionX;
            mc.thePlayer.motionY = motionY;
            mc.thePlayer.motionZ = motionZ;
            sendPacket = false;

            for (Packet<?> packet : packets) {
                if (packet instanceof C08PacketPlayerBlockPlacement) {
                    if (mc.theWorld.getBlockState(((C08PacketPlayerBlockPlacement) packet).getPosition()).getBlock() != null) {
                        mc.theWorld.setBlockToAir(((C08PacketPlayerBlockPlacement) packet).getPosition());
                    }
                }
            }
            mc.gameSettings.keyBindSneak.pressed = pressed;
            packets.clear();
            return;
        }
        if (releaseType.isCurrentMode("Latency") && !packets.isEmpty()) {
            sendPacket = true;

            setStateNoNotification(true);
            return;
        }
        while (!this.packets.isEmpty()) {
            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(this.packets.poll());
        }
    }

    @EventTarget
    private void onPoll(EventTick event) {
        if (antiAim.getValue()) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (entity instanceof IProjectile) {
                    if (entity.onGround) continue;
                    float dX = (float) (x - entity.posX);
                    float dY = (float) (y - entity.posY);
                    float dZ = (float) (z - entity.posZ);
                    float distance = dX * dX + dY * dY + dZ * dZ;
                    if (MathHelper.sqrt_float(distance) < 4) {
                        while (MathHelper.sqrt_float(distance) < 4 && !packets.isEmpty()) {
                            poll(2);
                        }
                    }
                }
            }
        }
        if (timer2.hasTimeElapsed(startPollDelay.getIntValue())) {
            if (slowPoll.getValue() && timer.hasTimeElapsed(pollDelay.getIntValue())) {
                poll(3);
                timer.reset();
            }
        }
    }

    private void poll(int count) {
        for (int i = 0; i < count; i++) {
            Packet<?> packet = this.packets.poll();

            if (packet instanceof C03PacketPlayer.C04PacketPlayerPosition || packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
                C03PacketPlayer wrapper = (C03PacketPlayer) packet;
                latencyX = wrapper.x;
                latencyY = wrapper.y;
                latencyZ = wrapper.z;
            }

            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
        }
    }

    @EventTarget
    private void onRender3D(Event3D event) {
        if(!packets.isEmpty()) {
            latencyXAnimation = LinearAnimation.animate((float) latencyXAnimation, (float) (latencyX - mc.getRenderManager().renderPosX),0.3f);
            latencyYAnimation = LinearAnimation.animate((float) latencyYAnimation, (float) (latencyY - mc.getRenderManager().renderPosY),0.3f);
            latencyZAnimation = LinearAnimation.animate((float) latencyZAnimation, (float) (latencyZ - mc.getRenderManager().renderPosZ),0.3f);
            final Color c = Wrapper.Instance.getClickGui().getGlobalColor();
            RenderUtils.drawBoundingBox(latencyXAnimation, latencyYAnimation, latencyZAnimation, mc.thePlayer.width - .15F, mc.thePlayer.height + .15F - (mc.thePlayer.isSneaking() ? .25F : 0), new Color(c.getRed(), c.getGreen(), c.getBlue(), 40).getRGB());
        }
    }

    @EventTarget
    private void onPacket(EventPacket event) {
        if (event.isCancelled()) return;
        if (mc.thePlayer == null) {
            packets.clear();
            sendPacket = false;
            setStateNoNotification(false);
        }


        if (event.isSendMode()) {
            if (event.getPacket() instanceof C01PacketChatMessage) {
                return;
            }
            if (sendPacket) {
                testPackets.add(event.getPacket());
                event.setCancelled(true);
                return;
            }
            packets.add(event.getPacket());
            event.setCancelled(true);
        }
    }

    private void release() {
        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(this.packets.poll());
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (sendPacket) {
            if (packets.isEmpty()) {
                while (!this.testPackets.isEmpty()) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(this.testPackets.poll());
                }
                setStateNoNotification(false);
                return;
            }
            int test = 0;
            while (!this.packets.isEmpty()) {
                test++;
                if (test >= speed.getValue())
                    break;
                poll(1);

            }
        }
    }

}
