package tech.blinkfix.modules.impl.combat;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.*;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.impl.move.Blink;
import tech.blinkfix.modules.impl.render.HUD;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.utils.TimerUtils;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.values.impl.ModeValue;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.concurrent.LinkedBlockingQueue;
import tech.blinkfix.utils.RenderUtils;
import tech.blinkfix.utils.SmoothAnimationTimer;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import tech.blinkfix.utils.renderer.Fonts;
import tech.blinkfix.utils.renderer.text.CustomTextRenderer;
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
    private static final int mainColor = (HUD.headerColor);
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
        Module blinkModule = BlinkFix.getInstance().getModuleManager().getModule(Blink.class);
        return blinkModule != null && blinkModule.isEnabled();
    }

    @Override
    public void onEnable() {
        // Check if AntiKB is enabled with Attack Reduce mode
        Module antiKBModule = BlinkFix.getInstance().getModuleManager().getModule(AntiKB.class);
        if (antiKBModule != null && antiKBModule.isEnabled()) {
            AntiKB antiKB = (AntiKB) antiKBModule;
            if (antiKB.mode.isCurrentMode("Attack Reduce")) {
                Notification notification = new Notification(NotificationLevel.INFO, "Fakelag cannot be enabled at the same time as Attack Reduce mode in AntiKB.", 10000L);
                BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            }
        }

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
                if (hurtCount >= (int) hurtCountThreshold.getCurrentValue()) {
                    send();
                    hurtCount = 0;
                    if (this.log.getCurrentValue()) {
                        mc.player.sendSystemMessage(Component.literal("§cFakeLag §7Under attack, release all packages."));
                    }
                }
            }
            lastHurtTime = currentHurtTime;
        }
        Aura aura = (Aura) BlinkFix.getInstance().getModuleManager().getModule(Aura.class);
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
            if (hasAttacked && packets.size() >= (int) maxPackets.getCurrentValue() && autoReleaseAtMax.getCurrentValue()) {
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

        if (EventType.RECEIVE == event.getType()) {
            Packet<?> packet = event.getPacket();
            if (mc.player == null || mc.level == null) return;

            if (entity != null) {
                if (packet instanceof ClientboundPingPacket) {
                    event.setCancelled(true);
                    packets.add((Packet<PacketListener>) packet);
                    progress.target = Mth.clamp((float) packets.size() / (int) maxPackets.getCurrentValue() * 100.0F, 0.0F, 100.0F);
                    if (hasAttacked && packets.size() >= (int) maxPackets.getCurrentValue() && autoReleaseAtMax.getCurrentValue()) {
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
                        progress.target = Mth.clamp((float) packets.size() / (int) maxPackets.getCurrentValue() * 100.0F, 0.0F, 100.0F);
                        if (hasAttacked && packets.size() >= (int) maxPackets.getCurrentValue() && autoReleaseAtMax.getCurrentValue()) {
                            send();
                        }
                    }
                }
                if (packet instanceof ClientboundMoveEntityPacket moveEntityPacket && moveEntityPacket.getEntity(mc.level) == entity) {
                    event.setCancelled(true);
                    packets.add((Packet<PacketListener>) packet);
                    progress.target = Mth.clamp((float) packets.size() / (int) maxPackets.getCurrentValue() * 100.0F, 0.0F, 100.0F);
                    if (hasAttacked && packets.size() >= (int) maxPackets.getCurrentValue() && autoReleaseAtMax.getCurrentValue()) {
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
                    progress.target = Mth.clamp((float) packets.size() / (int) maxPackets.getCurrentValue() * 100.0F, 0.0F, 100.0F);
                    if (hasAttacked && packets.size() >= (int) maxPackets.getCurrentValue() && autoReleaseAtMax.getCurrentValue()) {
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
        // FakeLag 进度条 Shadow 渲染
        if (e.getType() == EventType.SHADOW && !isBlinkEnabled() && entity != null) {
            int baseX = mc.getWindow().getGuiScaledWidth() / 2 - 50;
            int baseY = mc.getWindow().getGuiScaledHeight() / 2 + 15;
            int x = (int) (baseX + renderPosX.getCurrentValue());
            int y = (int) (baseY + renderPosY.getCurrentValue());
            float progressBarWidth = 100.0F;
            float progressBarHeight = 5.0F;
            
            // 进度条背景 Shadow
            RenderUtils.drawRoundedRect(e.getStack(), (float) x, (float) y, progressBarWidth, progressBarHeight, 2.0F, Integer.MIN_VALUE);
            // 进度条填充 Shadow
            RenderUtils.drawRoundedRect(e.getStack(), (float) x, (float) y, progress.value, progressBarHeight, 2.0F, Integer.MIN_VALUE);
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
            int x = (int) (baseX + renderPosX.getCurrentValue());
            int y = (int) (baseY + renderPosY.getCurrentValue());
            progress.update(true);
            float progressBarWidth = 100.0F;
            float progressBarHeight = 5.0F;
            RenderUtils.drawRoundedRect(e.getStack(), (float) x, (float) y, progressBarWidth, progressBarHeight, 2.0F, 0x80000000);
            RenderUtils.drawRoundedRect(e.getStack(), (float) x, (float) y, progress.value, 5.0F, 2.0F, mainColor);
            CustomTextRenderer font = Fonts.harmony;
            String targetText = "Target: " + entity.getName().getString();
            String packetText = "Tracking: " + packets.size() + "/" + (int) maxPackets.getCurrentValue();
            String hurtText = "Attack: " + hurtCount + "/" + (int) hurtCountThreshold.getCurrentValue();

            font.render(e.getStack(), targetText, (double) (x), (double) (y - 20), java.awt.Color.WHITE, true, 0.4);
            font.render(e.getStack(), packetText, (double) (x), (double) (y - 30), java.awt.Color.WHITE, true, 0.4);
            if (releaseOnHurt.getCurrentValue()) {
                font.render(e.getStack(), hurtText, (double) (x), (double) (y - 40), java.awt.Color.WHITE, true, 0.4);
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
                .move((double) partialTicks * motionX, (double) partialTicks * motionY, (double) partialTicks * motionZ)
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
/*
package com.heypixel.heypixelmod.modules.impl.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tech.blinkfix.BlinkFix;
import api.events.tech.blinkfix.EventTarget;
import types.api.events.tech.blinkfix.EventType;
import com.heypixel.heypixelmod.events.impl.*;
import modules.tech.blinkfix.Category;
import modules.tech.blinkfix.Module;
import modules.tech.blinkfix.ModuleInfo;
import move.impl.modules.tech.blinkfix.Scaffold;
import utils.tech.blinkfix.RenderUtils;
import utils.tech.blinkfix.SmoothAnimationTimer;
import values.tech.blinkfix.ValueBuilder;
import impl.values.tech.blinkfix.BooleanValue;
import impl.values.tech.blinkfix.FloatValue;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import static combat.impl.modules.tech.blinkfix.FakeLag.Stage.*;

@ModuleInfo(
        name = "FakeLag",
        description = "FakeLag",
        category = Category.COMBAT
)
public class FakeLag extends Module {
    private final LinkedBlockingQueue<Packet<ClientGamePacketListener>> packets = new LinkedBlockingQueue<>();
    private Vec3 realTargetPosition = new Vec3(0.0D, 0.0D, 0.0D);
    private final FloatValue backtrackRange = ValueBuilder.create(this, "Backtrack Range").setDefaultFloatValue(5.0F).setFloatStep(0.1F).setMinFloatValue(1.0F).setMaxFloatValue(6.0F).build().getFloatValue();
    private final FloatValue maxRange = ValueBuilder.create(this, "Max Range").setDefaultFloatValue(7.0F).setFloatStep(0.1F).setMinFloatValue(1.0F).setMaxFloatValue(10.0F).build().getFloatValue();
    private final FloatValue backtrackTime = ValueBuilder.create(this, "Backtrack Time").setDefaultFloatValue(250.0F).setFloatStep(10.0F).setMinFloatValue(50.0F).setMaxFloatValue(2000.0F).build().getFloatValue();
    private final BooleanValue rest = ValueBuilder.create(this, "Reset On HurtTime").setDefaultBooleanValue(true).build().getBooleanValue();
    private final FloatValue resetDelay = ValueBuilder.create(this, "Reset Delay").setDefaultFloatValue(200.0F).setFloatStep(1.0F).setMinFloatValue(0.0F).setMaxFloatValue(500.0F).build().getFloatValue();
    private final FloatValue hurttime = ValueBuilder.create(this, "Reset Hurttime").setDefaultFloatValue(8.0F).setFloatStep(1.0F).setMinFloatValue(1.0F).setMaxFloatValue(10.0F).build().getFloatValue();
    private final BooleanValue renderEnemyPos = ValueBuilder.create(this, "Render Target Position").setDefaultBooleanValue(true).build().getBooleanValue();
    private final BooleanValue delayVelocity = ValueBuilder.create(this, "Delay Velocity").setDefaultBooleanValue(true).build().getBooleanValue();

    private long lastResetTime = 0;
    private boolean isInCooldown = false;
    private boolean isBacktracking = false;
    private final SmoothAnimationTimer progress = new SmoothAnimationTimer(0.0F, 0.2F);
    private long backtrackStartTime = 0;
    private final Map<UUID, EntityPosition> entityPositions = new HashMap<>();
    private UUID currentTargetId = null;
    private Stage Stage;
    private final Map<UUID, TrackedEnemy> trackedEnemies = new HashMap<>();

    // 反射字段
    private Field moveEntityPacketEntityIdField;
    private Field moveEntityPacketXaField;
    private Field moveEntityPacketYaField;
    private Field moveEntityPacketZaField;

    private static class EntityPosition {
        double x, y, z;
        long timestamp;

        EntityPosition(double x, double y, double z, long timestamp) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.timestamp = timestamp;
        }
    }

    private static class TrackedEnemy {
        Vec3 serverPosition;
        long lastUpdateTime;

        TrackedEnemy(Vec3 initialPosition) {
            this.serverPosition = initialPosition;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        void updatePosition(Vec3 newPosition) {
            this.serverPosition = newPosition;
            this.lastUpdateTime = System.currentTimeMillis();
        }
    }

    public FakeLag() {
        // 初始化反射字段
        try {
            moveEntityPacketEntityIdField = ClientboundMoveEntityPacket.class.getDeclaredField("entityId");
            moveEntityPacketXaField = ClientboundMoveEntityPacket.class.getDeclaredField("xa");
            moveEntityPacketYaField = ClientboundMoveEntityPacket.class.getDeclaredField("ya");
            moveEntityPacketZaField = ClientboundMoveEntityPacket.class.getDeclaredField("za");

            // 设置字段可访问
            moveEntityPacketEntityIdField.setAccessible(true);
            moveEntityPacketXaField.setAccessible(true);
            moveEntityPacketYaField.setAccessible(true);
            moveEntityPacketZaField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        resetBacktrack();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        resetBacktrack();
        super.onDisable();
    }

    private void resetBacktrack() {
        if (mc.player == null || mc.level == null) return;
        isBacktracking = false;
        backtrackStartTime = 0;
        lastResetTime = System.currentTimeMillis();
        isInCooldown = true;

        if (currentTargetId != null) {
            Entity target = mc.level.getPlayerByUUID(currentTargetId);
            if (target != null) {
                entityPositions.put(currentTargetId, new EntityPosition(
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        System.currentTimeMillis()
                ));
            }
        }

        entityPositions.clear();
        trackedEnemies.clear();
        try {
            while (!packets.isEmpty()) {
                Packet<ClientGamePacketListener> packet = packets.poll();
                if (packet != null) {
                    packet.handle(mc.player.connection);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentTargetId = null;
    }

    @EventTarget
    public void onClick(EventAttack event) {
        if (!isEnabled() || isInCooldown) return;

        Entity target = Aura.target;
        if (target instanceof Player && mc.player != null) {
            double distance = mc.player.distanceTo(target);
            if (distance > backtrackRange.getCurrentValue() && !isBacktracking && !((Player) target).isDeadOrDying()) {
                startBacktrack(target);
            }
        }
    }

    private void startBacktrack(Entity target) {
        isBacktracking = true;
        backtrackStartTime = System.currentTimeMillis();
        this.Stage = TRACKING;
        currentTargetId = target.getUUID();
        entityPositions.put(target.getUUID(), new EntityPosition(
                target.getX(),
                target.getY(),
                target.getZ(),
                System.currentTimeMillis()
        ));
        trackedEnemies.put(target.getUUID(), new TrackedEnemy(target.position()));
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (!isEnabled() || !isBacktracking) return;
        Packet<?> packet = event.getPacket();
        this.progress.target = Mth.clamp(backtrackStartTime / backtrackTime.getCurrentValue() * 100.0F, 0.0F, 100.0F);
        if (packet instanceof ClientboundPlayerPositionPacket) {
            resetBacktrack();
            return;
        }

        if (packet instanceof ClientboundPingPacket){
            event.setCancelled(true);
            packets.add((Packet<ClientGamePacketListener>) packet);
        }

        if (delayVelocity.getCurrentValue() && packet instanceof ClientboundSetEntityMotionPacket velocityPacket){
            // 直接访问公共字段
            if (velocityPacket.getId() != mc.player.getId()) return;
            event.setCancelled(true);
            packets.add((Packet<ClientGamePacketListener>) packet);
        }
        if (packet instanceof ClientboundTeleportEntityPacket teleportPacket && isBacktracking) {
            // 直接访问公共字段
            Vec3 newPos = new Vec3(teleportPacket.getX(), teleportPacket.getY(), teleportPacket.getZ());
            UUID entityUUID = getUUIDFromEntityId(teleportPacket.getId());
            if (entityUUID != null && trackedEnemies.containsKey(entityUUID)) {
                trackedEnemies.get(entityUUID).updatePosition(newPos);
            }
            event.setCancelled(true);
            packets.add((Packet<ClientGamePacketListener>) packet);
        } else if (packet instanceof ClientboundMoveEntityPacket movePacket && isBacktracking) {
            try {
                // 使用反射访问protected字段
                int entityId = (int) moveEntityPacketEntityIdField.get(movePacket);
                short xa = (short) moveEntityPacketXaField.get(movePacket);
                short ya = (short) moveEntityPacketYaField.get(movePacket);
                short za = (short) moveEntityPacketZaField.get(movePacket);

                realTargetPosition.add(xa, ya, za);
                UUID entityUUID = getUUIDFromEntityId(entityId);
                if (entityUUID != null && trackedEnemies.containsKey(entityUUID)) {
                    TrackedEnemy trackedEnemy = trackedEnemies.get(entityUUID);
                    Vec3 lastKnownPos = trackedEnemy.serverPosition;
                    double newX = lastKnownPos.x + (double) xa / 4096.0;
                    double newY = lastKnownPos.y + (double) ya / 4096.0;
                    double newZ = lastKnownPos.z + (double) za / 4096.0;
                    trackedEnemy.updatePosition(new Vec3(newX, newY, newZ));
                }
                event.setCancelled(true);
                this.Stage = STORE;
                packets.add((Packet<ClientGamePacketListener>) packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D e) {
        if (!isEnabled() || !isBacktracking) return;

        int windowWidth = mc.getWindow().getGuiScaledWidth();
        int windowHeight = mc.getWindow().getGuiScaledHeight();

        int x = windowWidth / 2 - 50;
        int y = windowHeight / 2 + 40;
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - backtrackStartTime;
        float progressPercent = Mth.clamp((float) elapsedTime / backtrackTime.getCurrentValue(), 0.0F, 1.0F);
        this.progress.target = progressPercent * 100.0F;
        this.progress.update(true);
        RenderUtils.drawRoundedRect(e.getStack(), (float) x, (float) y, 100.0F, 5.0F, 2.0F, Integer.MIN_VALUE);
        RenderUtils.drawRoundedRect(e.getStack(), (float) x, (float) y, this.progress.value, 5.0F, 2.0F, new Color(150, 45, 45, 255).getRGB());
        e.getGuiGraphics().drawString(mc.font,"Tracking...", x + 27, (int) y - 10, new Color(255,255,255).getRGB());
    }

    private UUID getUUIDFromEntityId(int entityId) {
        if (mc.level == null) return null;
        Entity entity = mc.level.getEntity(entityId);
        return entity != null ? entity.getUUID() : null;
    }

    @EventTarget
    public void onTick(EventRunTicks event) {
        if (!isEnabled() || event.getType() != EventType.PRE || mc.player == null) return;

        if (isInCooldown && System.currentTimeMillis() - lastResetTime > resetDelay.getCurrentValue()) {
            this.Stage = IDLE;
            isInCooldown = false;
        }
        if (isBacktracking) {
            long currentTime = System.currentTimeMillis();
            long maxBacktrackTime = (long) backtrackTime.getCurrentValue();

            if (currentTargetId != null && mc.level != null) {
                TrackedEnemy trackedEnemy = trackedEnemies.get(currentTargetId);
                if (trackedEnemy != null) {
                    Vec3 serverPos = trackedEnemy.serverPosition;
                    double actualDistance = mc.player.position().distanceTo(serverPos);

                    if (actualDistance > maxRange.getCurrentValue()) {
                        resetBacktrack();
                        return;
                    }
                    if (actualDistance < backtrackRange.getCurrentValue()) {
                        resetBacktrack();
                        return;
                    }
                }
            }
            if (rest.getCurrentValue()) {
                if (mc.player.hurtTime > hurttime.getCurrentValue()) {
                    resetBacktrack();
                }
            }

            if (currentTime - backtrackStartTime > maxBacktrackTime) {
                resetBacktrack();
            }
            if (Aura.target == null) {
                resetBacktrack();
            }
        }
        long currentTime = System.currentTimeMillis();
        trackedEnemies.keySet().removeIf(uuid -> {
            TrackedEnemy trackedEnemy = trackedEnemies.get(uuid);
            return trackedEnemy == null || (currentTime - trackedEnemy.lastUpdateTime) > 1000;
        });
        if (delayVelocity.getCurrentValue()) {
            rest.setCurrentValue(false);
        }
        if (BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled() || mc.player.isUsingItem()) {
            isBacktracking = false;
            resetBacktrack();
        }
    }

    @EventTarget
    public void onRender(EventRender event) {
        if (mc.player == null || mc.getConnection() == null || mc.gameMode == null || mc.level == null) {
            return;
        }
        if (mc.screen != null) return;
        if (!isEnabled()) return;
        if (renderEnemyPos.getCurrentValue() && isBacktracking && !trackedEnemies.isEmpty()) {
            renderEntityPositions(event);
        }
    }

    private void renderEntityPositions(EventRender event) {
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = event.getPMatrixStack();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionShader);

        RenderSystem.setShaderColor(255, 255, 255, 0.4f);

        for (TrackedEnemy trackedEnemy : trackedEnemies.values()) {
            if (trackedEnemy == null) return;

            poseStack.pushPose();
            Vec3 enemyPos = trackedEnemy.serverPosition;
            double renderX = enemyPos.x() - cameraPos.x();
            double renderY = enemyPos.y() - cameraPos.y();
            double renderZ = enemyPos.z() - cameraPos.z();
            poseStack.translate(renderX, renderY, renderZ);
            AABB enemyBox = new AABB(-0.3, 0, -0.3, 0.3, 1.8, 0.3);
            RenderUtils.drawSolidBox(enemyBox, poseStack);

            poseStack.popPose();
        }

        RenderSystem.setShaderColor(0.0f, 1.0f, 0.0f, 1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static enum Stage {
        TRACKING,
        STORE,
        IDLE;
    }
}
*/