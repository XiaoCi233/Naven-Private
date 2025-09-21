package com.heypixel.heypixelmod.modules.impl.combat;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.*;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.modules.impl.render.HUD;
import com.heypixel.heypixelmod.utils.TimerUtils;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import com.heypixel.heypixelmod.values.impl.FloatValue;
import com.heypixel.heypixelmod.values.impl.ModeValue;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.concurrent.LinkedBlockingQueue;
import com.heypixel.heypixelmod.utils.RenderUtils;
import com.heypixel.heypixelmod.utils.SmoothAnimationTimer;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import com.heypixel.heypixelmod.utils.renderer.Fonts;
import com.heypixel.heypixelmod.utils.renderer.text.CustomTextRenderer;
import org.joml.Vector4f;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

@ModuleInfo(name = "FakeLag", description = "FakeLag", category = Category.COMBAT)
public class FakeLag extends Module {
    ModeValue targetMode = ValueBuilder.create(this, "TargetMode")
            .setModes("Range", "Attack")
            .setDefaultModeIndex(1)
            .build().getModeValue();

    ModeValue renderMode = ValueBuilder.create(this, "RenderMode")
            .setModes("Box", "Wireframe", "None")
            .setDefaultModeIndex(0)
            .build().getModeValue();

    FloatValue range = ValueBuilder.create(this, "Range")
            .setDefaultFloatValue(3)
            .setFloatStep(1)
            .setMinFloatValue(0.1F)
            .setMaxFloatValue(6)
            .build()
            .getFloatValue();
    public BooleanValue log = ValueBuilder.create(this, "DevLog").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue sendVelocity = ValueBuilder.create(this, "Velocity").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue autoReleaseAtMax = ValueBuilder.create(this, "AutoReleaseAtMax").setDefaultBooleanValue(true).build().getBooleanValue();

    public FloatValue maxPackets = ValueBuilder.create(this, "MaxPackets")
            .setDefaultFloatValue(55.0F)
            .setMinFloatValue(10.0F)
            .setMaxFloatValue(200.0F)
            .setFloatStep(1.0F)
            .build().getFloatValue();

    public BooleanValue releaseOnHurt = ValueBuilder.create(this, "ReleaseOnHurt")
            .setDefaultBooleanValue(false)
            .build().getBooleanValue();

    public FloatValue hurtCountThreshold = ValueBuilder.create(this, "HurtCountThreshold")
            .setDefaultFloatValue(1.0F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(10.0F)
            .setFloatStep(1.0F)
            .setVisibility(() -> releaseOnHurt.getCurrentValue())
            .build().getFloatValue();

    public FloatValue renderPosX = ValueBuilder.create(this, "RenderX")
            .setDefaultFloatValue(0.0F)
            .setMinFloatValue(-1000.0F)
            .setMaxFloatValue(1000.0F)
            .setFloatStep(1.0F)
            .build().getFloatValue();

    public FloatValue renderPosY = ValueBuilder.create(this, "RenderY")
            .setDefaultFloatValue(0.0F)
            .setMinFloatValue(-1000.0F)
            .setMaxFloatValue(1000.0F)
            .setFloatStep(1.0F)
            .build().getFloatValue();

    public FloatValue boxColorRed = ValueBuilder.create(this, "Box Red")
            .setDefaultFloatValue(0.3F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(1.0F)
            .setFloatStep(0.01F)
            .setVisibility(() -> renderMode.isCurrentMode("Box"))
            .build().getFloatValue();

    public FloatValue boxColorGreen = ValueBuilder.create(this, "Box Green")
            .setDefaultFloatValue(0.13F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(1.0F)
            .setFloatStep(0.01F)
            .setVisibility(() -> renderMode.isCurrentMode("Box"))
            .build().getFloatValue();

    public FloatValue boxColorBlue = ValueBuilder.create(this, "Box Blue")
            .setDefaultFloatValue(0.58F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(1.0F)
            .setFloatStep(0.01F)
            .setVisibility(() -> renderMode.isCurrentMode("Box"))
            .build().getFloatValue();

    public FloatValue boxColorAlpha = ValueBuilder.create(this, "Box Alpha")
            .setDefaultFloatValue(0.34F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(1.0F)
            .setFloatStep(0.01F)
            .setVisibility(() -> renderMode.isCurrentMode("Box"))
            .build().getFloatValue();

    public FloatValue wireframeWidth = ValueBuilder.create(this, "Wireframe Width")
            .setDefaultFloatValue(1.5F)
            .setMinFloatValue(0.5F)
            .setMaxFloatValue(5.0F)
            .setFloatStep(0.1F)
            .setVisibility(() -> renderMode.isCurrentMode("Wireframe"))
            .build().getFloatValue();

    private final SmoothAnimationTimer progress = new SmoothAnimationTimer(0.0F, 0.2F);
    private static final int mainColor =(HUD.headerColor);
    private final LinkedBlockingQueue<Packet<PacketListener>> packets = new LinkedBlockingQueue<>();
    private Entity entity = null;
    private Entity oldEntity = null;
    private LocalPlayer vec3 = null;
    private TimerUtils timer = new TimerUtils();
    private boolean hasAttacked = false;
    private TimerUtils attackLogTimer = new TimerUtils();
    private int hurtCount = 0;
    private int lastHurtTime = 0;
    private Vector4f blurMatrix;

    private boolean isBlinkEnabled() {
        Module blinkModule = BlinkFix.getInstance().getModuleManager().getModule("Blink");
        return blinkModule != null && blinkModule.isEnabled();
    }

    @Override
    public void onEnable() {
        packets.clear();
        vec3 = null;
        entity = null;
        progress.value = 0.0F;
        progress.target = 0.0F;
        hasAttacked = false;
        hurtCount = 0;
        lastHurtTime = 0;
        blurMatrix = null;
    }

    @Override
    public void onDisable() {
        send();
    }

    @EventTarget
    public void onAttack(EventClick event) {
        if (isBlinkEnabled()) {
            return;
        }

        Aura aura = (Aura) BlinkFix.getInstance().getModuleManager().getModule(Aura.class);
        if (aura.target != null) {
            if (oldEntity != aura.target) {
                send();
            }
            entity = aura.target;
            hasAttacked = true;
            if (vec3 == null) {
                vec3 = mc.gameMode.createPlayer(mc.level, new StatsCounter(), new ClientRecipeBook());
                vec3.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                vec3.setSprinting(entity.isSprinting());
                vec3.setShiftKeyDown(entity.isShiftKeyDown());
            }
            oldEntity = aura.target;

            if (attackLogTimer.hasTimePassed(500L)) {
                if (this.log.getCurrentValue())
                mc.player.sendSystemMessage(Component.literal("§aFakeLag §7 has Attack target: §f" + entity.getName().getString()));
                attackLogTimer.reset();
            }
        }
    }

    @EventTarget
    public void onUpdate(EventRunTicks event) {
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;
        if (isBlinkEnabled()) {
            return;
        }

        if (releaseOnHurt.getCurrentValue()) {
            int currentHurtTime = mc.player.hurtTime;

            if (currentHurtTime > lastHurtTime && currentHurtTime > 0) {
                hurtCount++;
                if (hurtCount >= (int)hurtCountThreshold.getCurrentValue()) {
                    send();
                    hurtCount = 0;
                    if (this.log.getCurrentValue()) {
                        mc.player.sendSystemMessage(Component.literal("§cFakeLag §7Under attack, release all packages."));
                    }
                }
            }
            lastHurtTime = currentHurtTime;
        }
        Aura aura = (Aura)BlinkFix.getInstance().getModuleManager().getModule(Aura.class);
        if (entity != null && aura.target == null) {
            entity = null;
            vec3 = null;
            hasAttacked = false;
        }

        if (vec3 != null) {
            if ((mc.player.distanceTo(vec3) > range.getCurrentValue() || (mc.player.fallDistance - entity.fallDistance) > 2.5F) && timer.hasTimePassed(50L)) {
                timer.reset();
                send();
            }
            if (hasAttacked && packets.size() >= (int)maxPackets.getCurrentValue() && autoReleaseAtMax.getCurrentValue()) {
                send();
            }
        } else {
            send();
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (isBlinkEnabled()) {
            return;
        }

        if(EventType.RECEIVE == event.getType()) {
            Packet<?> packet = event.getPacket();
            if (mc.player == null || mc.level == null) return;

            if (entity != null) {
                if (packet instanceof ClientboundPingPacket) {
                    event.setCancelled(true);
                    packets.add((Packet<PacketListener>) packet);
                    progress.target = Mth.clamp((float) packets.size() / (int)maxPackets.getCurrentValue() * 100.0F, 0.0F, 100.0F);
                    if (hasAttacked && packets.size() >= (int)maxPackets.getCurrentValue() && autoReleaseAtMax.getCurrentValue()) {
                        send();
                    }
                }
                if (packet instanceof ClientboundPlayerPositionPacket) {
                    entity = null;
                    vec3 = null;
                    hasAttacked = false;
                    hurtCount = 0;
                }
                if (packet instanceof ClientboundSetEntityMotionPacket entityMotionPacket && entityMotionPacket.getId() == mc.player.getId()) {
                    if (sendVelocity.getCurrentValue()) {
                        event.setCancelled(true);
                        packets.add((Packet<PacketListener>) packet);
                        progress.target = Mth.clamp((float) packets.size() / (int)maxPackets.getCurrentValue() * 100.0F, 0.0F, 100.0F);
                        if (hasAttacked && packets.size() >= (int)maxPackets.getCurrentValue() && autoReleaseAtMax.getCurrentValue()) {
                            send();
                        }
                    }
                }
                if (packet instanceof ClientboundMoveEntityPacket moveEntityPacket && moveEntityPacket.getEntity(mc.level) == entity) {
                    event.setCancelled(true);
                    packets.add((Packet<PacketListener>) packet);
                    progress.target = Mth.clamp((float) packets.size() / (int)maxPackets.getCurrentValue() * 100.0F, 0.0F, 100.0F);
                    if (hasAttacked && packets.size() >= (int)maxPackets.getCurrentValue() && autoReleaseAtMax.getCurrentValue()) {
                        send();
                    }
                    if (entity != null) {
                        if (!entity.isControlledByLocalInstance()) {
                            if (moveEntityPacket.hasPosition()) {
                                VecDeltaCodec vecdeltacodec = vec3.getPositionCodec();
                                Vec3 vec3 = vecdeltacodec.decode((long) moveEntityPacket.getXa(), (long) moveEntityPacket.getYa(), (long) moveEntityPacket.getZa());
                                vecdeltacodec.setBase(vec3);
                                this.vec3.moveTo(vec3.x, vec3.y, vec3.z, moveEntityPacket.getyRot(), moveEntityPacket.getxRot());
                            }
                        }
                    }
                }
                if (packet instanceof ClientboundTeleportEntityPacket teleportEntityPacket && teleportEntityPacket.getId() == entity.getId()) {
                    event.setCancelled(true);
                    packets.add((Packet<PacketListener>) packet);
                    progress.target = Mth.clamp((float) packets.size() / (int)maxPackets.getCurrentValue() * 100.0F, 0.0F, 100.0F);
                    if (hasAttacked && packets.size() >= (int)maxPackets.getCurrentValue() && autoReleaseAtMax.getCurrentValue()) {
                        send();
                    }
                    if (entity != null) {
                        double d0 = teleportEntityPacket.getX();
                        double d1 = teleportEntityPacket.getY();
                        double d2 = teleportEntityPacket.getZ();
                        vec3.syncPacketPositionCodec(d0, d1, d2);
                        if (!entity.isControlledByLocalInstance()) {
                            float f = (float) (teleportEntityPacket.getyRot() * 360) / 256.0F;
                            float f1 = (float) (teleportEntityPacket.getxRot() * 360) / 256.0F;
                            vec3.lerpTo(d0, d1, d2, f, f1, 3, true);
                            vec3.setOnGround(teleportEntityPacket.isOnGround());
                        }
                    }
                }
                if (packet instanceof ClientboundSetEntityMotionPacket entityMotionPacket && entityMotionPacket.getId() == entity.getId()) {
                    vec3.lerpMotion((double) entityMotionPacket.getXa() / 8000.0D, (double) entityMotionPacket.getYa() / 8000.0D, (double) entityMotionPacket.getZa() / 8000.0D);
                }
            }
        }
    }

    @EventTarget
    public void onWorldChange(EventMotion event) {
        if (mc.player == null) return;
        if (mc.player.tickCount <= 1 && EventType.PRE == event.getType()) {
            send();
            hurtCount = 0;
        }
    }

    @EventTarget
    public void onShader(EventShader e) {
        if (this.blurMatrix != null) {
            RenderUtils.drawRoundedRect(e.getStack(), this.blurMatrix.x(), this.blurMatrix.y(), this.blurMatrix.z(), this.blurMatrix.w(), 3.0F, 1073741824);
        }
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        if (isBlinkEnabled()) {
            return;
        }

        if (entity != null) {
            int baseX = mc.getWindow().getGuiScaledWidth() / 2 - 50;
            int baseY = mc.getWindow().getGuiScaledHeight() / 2 + 15;
            int x = (int)(baseX + renderPosX.getCurrentValue());
            int y = (int)(baseY + renderPosY.getCurrentValue());
            progress.update(true);
            float progressBarWidth = 100.0F;
            float progressBarHeight = 5.0F;
            RenderUtils.drawRoundedRect(e.getStack(), (float)x, (float)y, progressBarWidth, progressBarHeight, 2.0F, 0x80000000);
            RenderUtils.drawRoundedRect(e.getStack(), (float)x, (float)y, progress.value, 5.0F, 2.0F, mainColor);
            CustomTextRenderer font = Fonts.harmony;
            String targetText = "Target: " + entity.getName().getString();
            String packetText = "Tracking: " + packets.size() + "/" + (int)maxPackets.getCurrentValue();
            String hurtText = "Attack: " + hurtCount + "/" + (int)hurtCountThreshold.getCurrentValue();

            font.render(e.getStack(), targetText, (double)(x), (double)(y - 20), java.awt.Color.WHITE, true, 0.4);
            font.render(e.getStack(), packetText, (double)(x), (double)(y - 30), java.awt.Color.WHITE, true, 0.4);
            if (releaseOnHurt.getCurrentValue()) {
                font.render(e.getStack(), hurtText, (double)(x), (double)(y - 40), java.awt.Color.WHITE, true, 0.4);
            }
        }
    }

    @EventTarget
    public void onRender(EventRender e) {
        if (isBlinkEnabled() || renderMode.isCurrentMode("None") || entity == null || vec3 == null) {
            return;
        }

        PoseStack stack = e.getPMatrixStack();
        float partialTicks = e.getRenderPartialTicks();

        stack.pushPose();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(2848);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderUtils.applyRegionalRenderOffset(stack);

        double motionX = entity.getX() - entity.xo;
        double motionY = entity.getY() - entity.yo;
        double motionZ = entity.getZ() - entity.zo;

        Vec3 backtrackPos = new Vec3(vec3.getX(), vec3.getY(), vec3.getZ());
        AABB boundingBox = entity.getBoundingBox()
                .move(-motionX, -motionY, -motionZ)
                .move((double)partialTicks * motionX, (double)partialTicks * motionY, (double)partialTicks * motionZ)
                .move(backtrackPos.x - entity.getX(), backtrackPos.y - entity.getY(), backtrackPos.z - entity.getZ());

        if (renderMode.isCurrentMode("Box")) {
            RenderSystem.setShaderColor(
                    boxColorRed.getCurrentValue(),
                    boxColorGreen.getCurrentValue(),
                    boxColorBlue.getCurrentValue(),
                    boxColorAlpha.getCurrentValue()
            );
            RenderUtils.drawSolidBox(boundingBox, stack);
        } else if (renderMode.isCurrentMode("Wireframe")) {
            GL11.glLineWidth(wireframeWidth.getCurrentValue());
            RenderSystem.setShaderColor(0.3F, 0.13F, 0.58F, 0.8F);
            RenderUtils.drawOutlinedBox(boundingBox, stack);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(2848);
        stack.popPose();
    }

    private void send() {
        progress.target = 0.0F;
        progress.value = 0.0F;
        vec3 = null;
        entity = null;
        hasAttacked = false;
        hurtCount = 0;
        while (!packets.isEmpty()) {
            Packet<PacketListener> packet = packets.poll();
            packet.handle(mc.getConnection());
        }
    }
}