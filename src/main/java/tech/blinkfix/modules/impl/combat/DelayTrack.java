package tech.blinkfix.modules.impl.combat;

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
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.*;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.impl.move.Scaffold;
import tech.blinkfix.utils.RenderUtils;
import tech.blinkfix.utils.SmoothAnimationTimer;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import xyz.gay.mixin.accessors.ClientboundMoveEntityPacketAccessor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import static tech.blinkfix.modules.impl.combat.DelayTrack.Stage.*;

@ModuleInfo(
        name = "Backtrack",
        description = "",
        category = Category.COMBAT
)
public class DelayTrack extends Module {
    private static final float[] color = new float[]{0.78431374F, 0.0F, 0.0F, 0.39215686F};
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
            if (velocityPacket.getId() != mc.player.getId()) return;
            event.setCancelled(true);
            packets.add((Packet<ClientGamePacketListener>) packet);
        }
        if (packet instanceof ClientboundTeleportEntityPacket teleportPacket && isBacktracking) {
            Vec3 newPos = new Vec3(teleportPacket.getX(), teleportPacket.getY(), teleportPacket.getZ());
            UUID entityUUID = getUUIDFromEntityId(teleportPacket.getId());
            if (entityUUID != null && trackedEnemies.containsKey(entityUUID)) {
                trackedEnemies.get(entityUUID).updatePosition(newPos);
            }
            event.setCancelled(true);
            packets.add((Packet<ClientGamePacketListener>) packet);
        } else if (packet instanceof ClientboundMoveEntityPacket movePacket && isBacktracking) {
            ClientboundMoveEntityPacketAccessor accessor = (ClientboundMoveEntityPacketAccessor) movePacket;
            realTargetPosition.add(accessor.getXa(), accessor.getYa(), accessor.getZa());
            UUID entityUUID = getUUIDFromEntityId(accessor.getEntityId());
            if (entityUUID != null && trackedEnemies.containsKey(entityUUID)) {
                TrackedEnemy trackedEnemy = trackedEnemies.get(entityUUID);
                Vec3 lastKnownPos = trackedEnemy.serverPosition;
                double newX = lastKnownPos.x + (double)accessor.getXa() / 4096.0;
                double newY = lastKnownPos.y + (double)accessor.getYa() / 4096.0;
                double newZ = lastKnownPos.z + (double)accessor.getZa() / 4096.0;
                trackedEnemy.updatePosition(new Vec3(newX, newY, newZ));
            }
            event.setCancelled(true);
            this.Stage = STORE;
            packets.add((Packet<ClientGamePacketListener>) packet);
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
                    if (actualDistance < backtrackRange.getCurrentValue()){
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
            if (Aura.target == null){
                resetBacktrack();
            }
        }
        long currentTime = System.currentTimeMillis();
        trackedEnemies.keySet().removeIf(uuid -> {
            TrackedEnemy trackedEnemy = trackedEnemies.get(uuid);
            return trackedEnemy == null || (currentTime - trackedEnemy.lastUpdateTime) > 1000;
        });
        if (delayVelocity.getCurrentValue()){
            rest.setCurrentValue(false);
        }
        if (BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled() || mc.player.isUsingItem()){
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

        RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);

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

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
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
