package com.heypixel.heypixelmod.modules.impl.move;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.EventMotion;
import com.heypixel.heypixelmod.events.impl.EventPacket;
import com.heypixel.heypixelmod.events.impl.EventSlowdown;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.utils.NetworkUtils;
import com.heypixel.heypixelmod.values.Value;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import com.heypixel.heypixelmod.values.impl.FloatValue;
import com.heypixel.heypixelmod.values.impl.ModeValue;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(
        name = "NoSlow",
        description = "NoSlowDown",
        category = Category.MOVEMENT
)
public class NoSlow extends Module {
    private float previousTimer = 1.0F;
    private ModeValue mode;
    private FloatValue timerValue;

    // TimerNoC03模式的高级调节参数
    private BooleanValue timerNoC03Enabled;
    private FloatValue timerNoC03TimerValue;
    private BooleanValue timerNoC03AutoRelease;
    private FloatValue timerNoC03ReleaseDelay;
    private BooleanValue timerNoC03ReleaseOnHurt;

    // 用于存储C03包的队列
    private final Queue<Packet<?>> cachedPackets = new ConcurrentLinkedQueue<>();
    private boolean isCachingPackets = false;
    private long cachingStartTime = 0;

    public NoSlow() {
        setupValues();
    }

    private void setupValues() {
        mode = ValueBuilder.create(this, "Mode")
                .setModes("Original", "Timer", "TimerNoC03")
                .setDefaultModeIndex(0)
                .setOnUpdate((value) -> {
                    if (this.isEnabled()) {
                        if (mode.isCurrentMode("Timer")) {
                            previousTimer = BlinkFix.TICK_TIMER;
                            BlinkFix.TICK_TIMER = timerValue.getCurrentValue();
                        } else if (mode.isCurrentMode("TimerNoC03") && timerNoC03Enabled.getCurrentValue()) {
                            previousTimer = BlinkFix.TICK_TIMER;
                            BlinkFix.TICK_TIMER = timerNoC03TimerValue.getCurrentValue();
                        } else {
                            BlinkFix.TICK_TIMER = previousTimer;
                        }
                    }
                })
                .build()
                .getModeValue();

        timerValue = ValueBuilder.create(this, "Timer")
                .setVisibility(() -> mode.isCurrentMode("Timer"))
                .setDefaultFloatValue(1.0F)
                .setMinFloatValue(0.1F)
                .setMaxFloatValue(2.0F)
                .setFloatStep(0.1F)
                .build()
                .getFloatValue();

        // TimerNoC03模式的配置参数
        timerNoC03Enabled = ValueBuilder.create(this, "TimerNoC03 Enabled")
                .setVisibility(() -> mode.isCurrentMode("TimerNoC03"))
                .setDefaultBooleanValue(true)
                .build()
                .getBooleanValue();

        timerNoC03TimerValue = ValueBuilder.create(this, "TimerNoC03 Timer")
                .setVisibility(() -> mode.isCurrentMode("TimerNoC03") && timerNoC03Enabled.getCurrentValue())
                .setDefaultFloatValue(1.0F)
                .setMinFloatValue(0.1F)
                .setMaxFloatValue(2.0F)
                .setFloatStep(0.1F)
                .build()
                .getFloatValue();

        timerNoC03AutoRelease = ValueBuilder.create(this, "TimerNoC03 Auto Release")
                .setVisibility(() -> mode.isCurrentMode("TimerNoC03") && timerNoC03Enabled.getCurrentValue())
                .setDefaultBooleanValue(true)
                .build()
                .getBooleanValue();

        timerNoC03ReleaseDelay = ValueBuilder.create(this, "TimerNoC03 Release Delay (ms)")
                .setVisibility(() -> mode.isCurrentMode("TimerNoC03") && timerNoC03Enabled.getCurrentValue() && timerNoC03AutoRelease.getCurrentValue())
                .setDefaultFloatValue(0.0F)
                .setMinFloatValue(0.0F)
                .setMaxFloatValue(1000.0F)
                .setFloatStep(10.0F)
                .build()
                .getFloatValue();

        timerNoC03ReleaseOnHurt = ValueBuilder.create(this, "TimerNoC03 Release On Hurt")
                .setVisibility(() -> mode.isCurrentMode("TimerNoC03") && timerNoC03Enabled.getCurrentValue())
                .setDefaultBooleanValue(false)
                .build()
                .getBooleanValue();
    }

    @EventTarget
    public void onSlow(EventSlowdown eventSlowdown) {
        if (mode.isCurrentMode("Original")) {
            if (mc.player.getUseItemRemainingTicks() % 2 != 0 && mc.player.getUseItemRemainingTicks() <= 30) {
                eventSlowdown.setSlowdown(false);
                mc.player.setSprinting(true);
            }
        } else if (mode.isCurrentMode("Timer")) {
            if (mc.player.getUseItemRemainingTicks() % 2 != 0 && mc.player.getUseItemRemainingTicks() <= 30) {
                eventSlowdown.setSlowdown(false);
                mc.player.setSprinting(true);
            }
        } else if (mode.isCurrentMode("TimerNoC03")) {
            if (timerNoC03Enabled.getCurrentValue() &&
                    mc.player.getUseItemRemainingTicks() % 2 != 0 && mc.player.getUseItemRemainingTicks() <= 30) {
                eventSlowdown.setSlowdown(false);
                mc.player.setSprinting(true);
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (event.getType() == EventType.PRE && this.isEnabled()) {
            // 检查玩家是否正在使用物品
            if (mc.player.getUseItemRemainingTicks() > 0) {
                // 在使用物品时应用不同的timer值
                if (mode.isCurrentMode("Timer")) {
                    BlinkFix.TICK_TIMER = timerValue.getCurrentValue();
                } else if (mode.isCurrentMode("TimerNoC03") && timerNoC03Enabled.getCurrentValue()) {
                    BlinkFix.TICK_TIMER = timerNoC03TimerValue.getCurrentValue();
                    // 如果是TimerNoC03模式，开始缓存包
                    if (!isCachingPackets) {
                        isCachingPackets = true;
                        cachingStartTime = System.currentTimeMillis();
                    }
                }
            } else {
                // 当不使用物品时，恢复正常的timer值
                if (mode.isCurrentMode("Timer")) {
                    BlinkFix.TICK_TIMER = 1.0F;
                } else if (mode.isCurrentMode("TimerNoC03") && timerNoC03Enabled.getCurrentValue()) {
                    BlinkFix.TICK_TIMER = 1.0F;

                    // 如果是TimerNoC03模式且之前在缓存包，根据设置决定是否发送所有缓存的包
                    if (isCachingPackets && timerNoC03AutoRelease.getCurrentValue()) {
                        // 检查是否达到释放延迟时间
                        long elapsedTime = System.currentTimeMillis() - cachingStartTime;
                        if (elapsedTime >= timerNoC03ReleaseDelay.getCurrentValue()) {
                            sendCachedPackets();
                            isCachingPackets = false;
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onUpdate(EventMotion event) {
        // 检查是否需要因为受伤而释放包
        if (this.isEnabled() && mode.isCurrentMode("TimerNoC03") && timerNoC03Enabled.getCurrentValue() &&
                timerNoC03ReleaseOnHurt.getCurrentValue() && isCachingPackets && mc.player.hurtTime > 0) {
            sendCachedPackets();
            isCachingPackets = false;
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        // 在TimerNoC03模式下缓存C03包
        if (this.isEnabled() && mode.isCurrentMode("TimerNoC03") && timerNoC03Enabled.getCurrentValue() && isCachingPackets) {
            if (event.getPacket() instanceof ServerboundMovePlayerPacket) {
                event.setCancelled(true);
                cachedPackets.offer(event.getPacket());
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mode.isCurrentMode("Timer")) {
            previousTimer = BlinkFix.TICK_TIMER;
            BlinkFix.TICK_TIMER = timerValue.getCurrentValue();
        } else if (mode.isCurrentMode("TimerNoC03") && timerNoC03Enabled.getCurrentValue()) {
            previousTimer = BlinkFix.TICK_TIMER;
            BlinkFix.TICK_TIMER = timerNoC03TimerValue.getCurrentValue();
        }

        // 启用时清空缓存
        cachedPackets.clear();
        isCachingPackets = false;
        cachingStartTime = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mode.isCurrentMode("Timer")) {
            BlinkFix.TICK_TIMER = previousTimer;
        } else if (mode.isCurrentMode("TimerNoC03") && timerNoC03Enabled.getCurrentValue()) {
            BlinkFix.TICK_TIMER = previousTimer;
        }

        // 禁用时发送所有缓存的包
        sendCachedPackets();
        cachedPackets.clear();
        isCachingPackets = false;
        cachingStartTime = 0;
    }

    /**
     * 发送所有缓存的包
     */
    private void sendCachedPackets() {
        while (!cachedPackets.isEmpty()) {
            Packet<?> packet = cachedPackets.poll();
            if (packet != null) {
                NetworkUtils.sendPacketNoEvent(packet);
            }
        }
    }
}