package com.heypixel.heypixelmod.modules.impl.misc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.EventPacket;
import com.heypixel.heypixelmod.events.impl.PostMotionEvent;
import com.heypixel.heypixelmod.events.impl.WorldChangeEvent;
import com.heypixel.heypixelmod.files.FileManager;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.utils.ChatUtils;
import com.heypixel.heypixelmod.utils.MathUtils;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import sun.misc.Unsafe;

import static com.heypixel.heypixelmod.utils.PlayerUtils.mc;

@ModuleInfo(
        name = "Disabler",
        category = Category.MISC,
        description = "Disables some checks of the anti cheat."
)
public class Disabler extends Module {
    private final BooleanValue logging = ValueBuilder.create(this, "Logging").setDefaultBooleanValue(false).build().getBooleanValue();
    private final BooleanValue acaaimstep = ValueBuilder.create(this, "ACAAimStep").setDefaultBooleanValue(false).build().getBooleanValue();
    private final BooleanValue acaperfectrotation = ValueBuilder.create(this, "ACAPerfectRotation").setDefaultBooleanValue(false).build().getBooleanValue();
    private final BooleanValue grimDuplicateRotPlace = ValueBuilder.create(this, "GrimDuplicateRotPlace")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
    private final BooleanValue grimBadPacketsA = ValueBuilder.create(this, "BadPacketsA").setDefaultBooleanValue(false).build().getBooleanValue();
    private final BooleanValue grimBadPacketsD = ValueBuilder.create(this, "BadPacketsD").setDefaultBooleanValue(false).build().getBooleanValue();
    private final BooleanValue grimBadPacketsF = ValueBuilder.create(this, "BadPacketsF").setDefaultBooleanValue(false).build().getBooleanValue();
    private final BooleanValue grimBadPacketsY = ValueBuilder.create(this, "BadPacketsY").setDefaultBooleanValue(false).build().getBooleanValue();

    // 新增的 Post 功能相关配置和变量
    private final BooleanValue post = ValueBuilder.create(this, "Post").setDefaultBooleanValue(false).build().getBooleanValue();
    private final BooleanValue withoutPlace = ValueBuilder.create(this, "Without Place").setDefaultBooleanValue(false).build().getBooleanValue();
    public static int inGameTick; // 游戏刻计数器，通常在其他地方更新
    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();

    private float playerYaw;
    private float deltaYaw;
    private float lastPlacedDeltaYaw;
    private boolean rotated = false;
    private float lastYaw = 0.0F;
    private float lastPitch = 0.0F;
    private final Random random = new Random();
    private static final double[] PERFECT_PATTERNS = new double[]{0.1, 0.25};
    private static final double EPSILON = 1.0E-10;
    private static final Unsafe unsafe;
    private final List<Object> inventoryPacketsQueue = new ArrayList<>();
    private int lastSlot = -1;
    private boolean lastSprinting = false;
    private boolean lastSneaking = false;
    private long lastSlotChangeTime = 0;
    private long lastSprintChangeTime = 0;
    private long lastSneakChangeTime = 0;

    private void log(String message) {
        if (this.logging.getCurrentValue()) {
            ChatUtils.addChatMessage(message);
        }
    }

    // 新增：判断是否使用Post功能的条件
    private boolean usePost() {
        return post.getCurrentValue()
                && mc.player != null
                && mc.level != null
                && mc.player.isAlive()
                && inGameTick >= 20
                && (!withoutPlace.getCurrentValue());
    }

    // 新增：释放队列中的所有包并发送
    private void releasePackets() {
        if (mc.getConnection() == null || mc.player == null || mc.level == null) {
            packets.clear();
            return;
        }

        while (!packets.isEmpty()) {
            Packet<?> packet = packets.poll();
//
//             这里可以添加自定义的事件处理逻辑，如果需要的话
//             PacketSendEvent packetSendEvent = new PacketSendEvent(packet);
//             Client.INSTANCE.getEventBus().handle(packetSendEvent);
//             if (packetSendEvent.isCancelled()) continue;

            if (mc.getConnection() == null || mc.player == null || mc.level == null || mc.player.isDeadOrDying()) break;

            // 发送包
            mc.getConnection().send(packet);
        }
    }

    @Override
    public void onEnable() {
        packets.clear();
    }

    @Override
    public void onDisable() {
        releasePackets();
    }
    @EventTarget
    public void onWorldChange(WorldChangeEvent event) {
        releasePackets();
        packets.clear();
    }
    @EventTarget
    public void onPostMotion(PostMotionEvent event) {
        releasePackets();
    }

    private float normalizeYaw(float yaw) {
        while (yaw > 180.0F) {
            yaw -= 360.0F;
        }

        while (yaw < -180.0F) {
            yaw += 360.0F;
        }

        return yaw;
    }

    private boolean shouldModifyRotation(float currentYaw, float currentPitch) {
        if (this.lastYaw == 0.0F && this.lastPitch == 0.0F) {
            return false;
        } else {
            double yawDelta = (double) Math.abs(this.normalizeYaw(currentYaw - this.lastYaw));
            double pitchDelta = (double) Math.abs(currentPitch - this.lastPitch);
            boolean isStepYaw = yawDelta < 1.0E-5 && pitchDelta > 1.0;
            boolean isStepPitch = pitchDelta < 1.0E-5 && yawDelta > 1.0;
            return isStepYaw || isStepPitch;
        }
    }

    private float[] getModifiedRotation(float yaw, float pitch) {
        double yawDelta = (double) Math.abs(this.normalizeYaw(yaw - this.lastYaw));
        double pitchDelta = (double) Math.abs(pitch - this.lastPitch);
        float newYaw = yaw;
        float newPitch = pitch;
        if (yawDelta < 1.0E-5 && pitchDelta > 1.0) {
            newYaw = this.lastYaw + (float) (this.random.nextGaussian() * 0.001);
        }

        if (pitchDelta < 1.0E-5 && yawDelta > 1.0) {
            newPitch = this.lastPitch + (float) (this.random.nextGaussian() * 0.001);
        }

        return new float[]{newYaw, newPitch};
    }

    private float[] getAntiPerfectRotation(float yaw, float pitch) {
        if (this.lastYaw == 0.0F && this.lastPitch == 0.0F) {
            return new float[]{yaw, pitch};
        } else {
            double yawDelta = (double) Math.abs(this.normalizeYaw(yaw - this.lastYaw));
            double pitchDelta = (double) Math.abs(pitch - this.lastPitch);
            float newYaw = yaw;
            float newPitch = pitch;
            if (!this.isNoRotation(yawDelta) && this.isPerfectPattern(yawDelta)) {
                double jitter = this.random.nextGaussian() * 0.005;
                newYaw = yaw + (float) jitter;
            }

            if (!this.isNoRotation(pitchDelta) && this.isPerfectPattern(pitchDelta)) {
                double jitter = this.random.nextGaussian() * 0.005;
                newPitch = pitch + (float) jitter;
            }

            return new float[]{newYaw, newPitch};
        }
    }

    private boolean isNoRotation(double rotation) {
        return Math.abs(rotation) <= 1.0E-10 || this.isIntegerMultiple(360.0, rotation);
    }

    private boolean isPerfectPattern(double rotation) {
        if (!Double.isInfinite(rotation) && !Double.isNaN(rotation)) {
            for (double pattern : PERFECT_PATTERNS) {
                if (this.isIntegerMultiple(pattern, rotation)) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private boolean isIntegerMultiple(double reference, double value) {
        if (reference == 0.0) {
            return Math.abs(value) <= 1.0E-10;
        } else {
            double multiple = value / reference;
            return Math.abs(multiple - (double) Math.round(multiple)) <= 1.0E-10;
        }
    }

    public static float getPacketYRot(ServerboundMovePlayerPacket packet) {
        if (mc.gameMode == null) {
            return 0.0F;
        } else {
            Field yRotField = findField(packet.getClass(), "f_134121_");

            try {
                return yRotField.getFloat(packet);
            } catch (Exception var3) {
                FileManager.logger.error("Failed to get yrot field", var3);
                var3.printStackTrace();
                return 0.0F;
            }
        }
    }

    public static float getPacketXRot(ServerboundMovePlayerPacket packet) {
        if (mc.gameMode == null) {
            return 0.0F;
        } else {
            Field xRotField = findField(packet.getClass(), "f_134122_");

            try {
                return xRotField.getFloat(packet);
            } catch (Exception var3) {
                FileManager.logger.error("Failed to get xrot field", var3);
                var3.printStackTrace();
                return 0.0F;
            }
        }
    }
    private static Field findField(Class<?> clazz, String... fieldNames) {
        if (clazz != null && fieldNames != null && fieldNames.length != 0) {
            Exception failed = null;

            for (Class<?> currentClass = clazz; currentClass != null; currentClass = currentClass.getSuperclass()) {
                for (String fieldName : fieldNames) {
                    if (fieldName != null) {
                        try {
                            Field f = currentClass.getDeclaredField(fieldName);
                            f.setAccessible(true);
                            if ((f.getModifiers() & 16) != 0) {
                                unsafe.putInt(f, (long) unsafe.arrayBaseOffset(boolean[].class), f.getModifiers() & -17);
                            }

                            return f;
                        } catch (Exception var9) {
                            failed = var9;
                        }
                    }
                }
            }

            throw new Disabler.UnableToFindFieldException(failed);
        } else {
            throw new IllegalArgumentException("Class and fieldNames must not be null or empty");
        }
    }

    public static void setPacketYRot(ServerboundMovePlayerPacket packet, float yRot) {
        if (mc.gameMode != null) {
            Field yRotField = findField(packet.getClass(), "f_134121_");

            try {
                yRotField.setFloat(packet, yRot);
            } catch (Exception var4) {
                FileManager.logger.error("Failed to set yrot field", var4);
                var4.printStackTrace();
            }
        }
    }

    public static void setPacketXRot(ServerboundMovePlayerPacket packet, float xRot) {
        if (mc.gameMode != null) {
            Field xRotField = findField(packet.getClass(), "f_134122_");

            try {
                xRotField.setFloat(packet, xRot);
            } catch (Exception var4) {
                FileManager.logger.error("Failed to set xrot field", var4);
                var4.printStackTrace();
            }
        }
    }
    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getType() == EventType.SEND && !event.isCancelled() && mc.player != null) {
            Object packet = event.getPacket();

            // 新增：Post 功能处理 - 拦截包并加入队列
            if (usePost()) {
                // 通常这里会有选择性地过滤哪些包需要被延迟
                // 例如：只延迟移动包和动作包，而不延迟其他关键包
                if (packet instanceof ServerboundMovePlayerPacket ||
                        packet instanceof ServerboundPlayerCommandPacket) {
                    packets.add((Packet<?>) packet);
                    event.setCancelled(true);
                    return; // 拦截后不再进行后续处理
                }
            }

            if (this.grimBadPacketsA.getCurrentValue() && packet instanceof ServerboundSetCarriedItemPacket) {
                ServerboundSetCarriedItemPacket slotPacket = (ServerboundSetCarriedItemPacket) packet;
                int slot = slotPacket.getSlot();

                if (slot == lastSlot) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastSlotChangeTime < 50) {
                        event.setCancelled(true);
                        return;
                    }
                }

                lastSlot = slot;
                lastSlotChangeTime = System.currentTimeMillis();
            }
            if (this.grimBadPacketsD.getCurrentValue() && packet instanceof ServerboundMovePlayerPacket) {
                ServerboundMovePlayerPacket movePacket = (ServerboundMovePlayerPacket) packet;
                if (movePacket.hasRotation()) {
                    float pitch = movePacket.getXRot(0.0F);
                    if (pitch > 90.0F || pitch < -90.0F) {
                        float clampedPitch = Math.max(-90.0F, Math.min(90.0F, pitch));

                        if (movePacket.hasPosition()) {
                            event.setPacket(
                                    new PosRot(
                                            movePacket.getX(0.0), movePacket.getY(0.0), movePacket.getZ(0.0),
                                            movePacket.getYRot(0.0F), clampedPitch, movePacket.isOnGround()
                                    )
                            );
                        } else {
                            event.setPacket(new Rot(movePacket.getYRot(0.0F), clampedPitch, movePacket.isOnGround()));
                        }
                    }
                }
            }
            if (this.grimBadPacketsF.getCurrentValue() && packet instanceof ServerboundPlayerCommandPacket) {
                ServerboundPlayerCommandPacket commandPacket = (ServerboundPlayerCommandPacket) packet;
                ServerboundPlayerCommandPacket.Action action = commandPacket.getAction();

                boolean isSprintingAction = action == ServerboundPlayerCommandPacket.Action.START_SPRINTING ||
                        action == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING;

                if (isSprintingAction) {
                    boolean newSprintState = action == ServerboundPlayerCommandPacket.Action.START_SPRINTING;

                    if (newSprintState == lastSprinting) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastSprintChangeTime < 50) {
                            event.setCancelled(true);
                            return;
                        }
                    }

                    lastSprinting = newSprintState;
                    lastSprintChangeTime = System.currentTimeMillis();
                }
            }
            if (this.grimBadPacketsY.getCurrentValue() && packet instanceof ServerboundSetCarriedItemPacket) {
                ServerboundSetCarriedItemPacket slotPacket = (ServerboundSetCarriedItemPacket) packet;
                int slot = slotPacket.getSlot();
                if (slot < 0 || slot > 8) {
                    int clampedSlot = Math.max(0, Math.min(8, slot));
                    event.setPacket(new ServerboundSetCarriedItemPacket(clampedSlot));
                }
            }
        }
    }

    @EventTarget(3)
    public void duplicateRotPlaceDisabler(EventPacket e) {
        if (this.grimDuplicateRotPlace.currentValue && e.getType() == EventType.SEND && !e.isCancelled() && mc.player != null) {
            if (e.getPacket() instanceof ServerboundMovePlayerPacket) {
                ServerboundMovePlayerPacket packet = (ServerboundMovePlayerPacket) e.getPacket();
                if (packet.hasRotation()) {
                    if (packet.getYRot(0.0F) < 360.0F && packet.getYRot(0.0F) > -360.0F) {
                        if (packet.hasPosition()) {
                            e.setPacket(
                                    new PosRot(
                                            packet.getX(0.0), packet.getY(0.0), packet.getZ(0.0), packet.getYRot(0.0F) + 720.0F, packet.getXRot(0.0F), packet.isOnGround()
                                    )
                            );
                        } else {
                            e.setPacket(new Rot(packet.getYRot(0.0F) + 720.0F, packet.getXRot(0.0F), packet.isOnGround()));
                        }
                    }

                    float lastPlayerYaw = this.playerYaw;
                    this.playerYaw = packet.getYRot(0.0F);
                    this.deltaYaw = Math.abs(this.playerYaw - lastPlayerYaw);
                    this.rotated = true;
                    if (this.deltaYaw > 2.0F) {
                        float xDiff = Math.abs(this.deltaYaw - this.lastPlacedDeltaYaw);
                        if ((double) xDiff < 1.0E-4) {
                            this.log("Disabling DuplicateRotPlace!");
                            if (packet.hasPosition()) {
                                e.setPacket(
                                        new PosRot(
                                                packet.getX(0.0), packet.getY(0.0), packet.getZ(0.0), packet.getYRot(0.0F) + 0.002F, packet.getXRot(0.0F), packet.isOnGround()
                                        )
                                );
                            } else {
                                e.setPacket(new Rot(packet.getYRot(0.0F) + 0.002F, packet.getXRot(0.0F), packet.isOnGround()));
                            }
                        }
                    }
                }
            } else if (e.getPacket() instanceof ServerboundUseItemOnPacket && this.rotated) {
                this.lastPlacedDeltaYaw = this.deltaYaw;
                this.rotated = false;
            }
        }

        if ((this.acaaimstep.currentValue || this.acaperfectrotation.currentValue) && e.getPacket() instanceof ServerboundMovePlayerPacket movePacket) {
            float currentYaw = getPacketYRot(movePacket);
            float currentPitch = getPacketXRot(movePacket);
            boolean modified = false;
            if (this.acaaimstep.currentValue && this.shouldModifyRotation(currentYaw, currentPitch)) {
                float[] modifiedRotation = this.getModifiedRotation(currentYaw, currentPitch);
                currentYaw = modifiedRotation[0];
                currentPitch = modifiedRotation[1];
                modified = true;
            }

            if (this.acaperfectrotation.currentValue) {
                float[] antiPerfectRotation = this.getAntiPerfectRotation(currentYaw, currentPitch);
                if (antiPerfectRotation[0] != currentYaw || antiPerfectRotation[1] != currentPitch) {
                    currentYaw = antiPerfectRotation[0];
                    currentPitch = antiPerfectRotation[1];
                    modified = true;
                    this.log("PerfectRotation: Modified rotation");
                }
            }

            if (modified) {
                setPacketYRot(movePacket, currentYaw);
                setPacketXRot(movePacket, MathUtils.clampPitch_To90(currentPitch));
            }

            this.lastYaw = getPacketYRot(movePacket);
            this.lastPitch = getPacketXRot(movePacket);
        }
    }

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception var1) {
            throw new RuntimeException(var1);
        }
    }

    private static class UnableToFindFieldException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public UnableToFindFieldException(Exception e) {
            super(e);
        }
    }
}