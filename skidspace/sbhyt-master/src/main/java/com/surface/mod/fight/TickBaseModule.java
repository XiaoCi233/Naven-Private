package com.surface.mod.fight;

import com.cubk.event.annotations.EventPriority;
import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventPacket;
import com.surface.events.EventUpdate;
import com.surface.mod.Mod;
import com.surface.util.NumberUtils;
import com.surface.value.impl.BooleanValue;
import com.surface.value.impl.ModeValue;
import com.surface.value.impl.NumberValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class TickBaseModule extends Mod {
    private int playerTicks = 0;
    private int smartCounter = 0;
    private boolean confirmAttack = false;
    private boolean confirmLagBack = false;

    private final ModeValue timerBoostMode = new ModeValue("Mode", "Normal", new String[]{"Normal", "Smart"});
    private final NumberValue ticksValue = new NumberValue("Ticks", 10.0, 1.0, 20.0, 1.0);
    private final NumberValue timerBoostValue = new NumberValue("Boost", 1.5, 0.01, 35.0, 0.01);
    private final NumberValue timerChargedValue = new NumberValue("Charged", 0.45, 0.05, 5.0, 0.01);
    private final NumberValue rangeValue = new NumberValue("Range", 3.5, 1.0, 5.0, 0.1) {
        @Override
        public boolean isVisible() {
            return timerBoostMode.isCurrentMode("Normal");
        }
    };
    private final NumberValue minRange = new NumberValue("Minimum Range", 1.0, 1.0, 5.0, 0.1) {
        @Override
        public boolean isVisible() {
            return timerBoostMode.isCurrentMode("Smart");
        }
    };
    private final NumberValue maxRange = new NumberValue("Maximum Range", 5.0, 1.0, 5.0, 0.1) {
        @Override
        public boolean isVisible() {
            return timerBoostMode.isCurrentMode("Smart");
        }
    };
    private final NumberValue minTickDelay = new NumberValue("Minimum Tick Delay", 5.0, 1.0, 100.0, 1.0) {
        @Override
        public boolean isVisible() {
            return timerBoostMode.isCurrentMode("Smart");
        }
    };
    private final NumberValue maxTickDelay = new NumberValue("Maximum Tick Delay", 100.0, 1.0, 100.0, 1.0) {
        @Override
        public boolean isVisible() {
            return timerBoostMode.isCurrentMode("Smart");
        }
    };
    private final BooleanValue resetlagBack = new BooleanValue("Reset on set back", false);

    public TickBaseModule() {
        super("Tick Base", Category.FIGHT);
        registerValues(timerBoostMode, ticksValue, timerBoostValue, timerChargedValue, rangeValue, minRange, maxRange, minTickDelay, maxTickDelay, resetlagBack);
    }

    @Override
    public void onEnable() {
        timerReset();
    }

    @Override
    public void onDisable() {
        timerReset();
        smartCounter = 0;
        playerTicks = 0;
    }

    @EventTarget
    @EventPriority(15)
    public void onAttack(EventPacket event) {
        if (event.getPacket() instanceof C02PacketUseEntity) {
            if (!(((C02PacketUseEntity) event.getPacket()).getEntityFromWorld(mc.theWorld) instanceof EntityLivingBase) || shouldResetTimer()) {
                timerReset();
                return;
            } else {
                confirmAttack = true;
            }

            EntityLivingBase targetEntity = (EntityLivingBase) ((C02PacketUseEntity) event.getPacket()).getEntityFromWorld(mc.theWorld);
            double entityDistance = mc.thePlayer.getDistanceToEntity2(targetEntity);
            int randomCounter = NumberUtils.getRandom(minTickDelay.getValue().intValue(), maxTickDelay.getValue().intValue());
            double randomRange = NumberUtils.getRandom(minRange.getValue(), maxRange.getValue());

            smartCounter++;

            boolean shouldSlowed;
            switch (timerBoostMode.getValue()) {
                case "Normal":
                    shouldSlowed = entityDistance <= rangeValue.getValue();
                    break;
                case "Smart":
                    shouldSlowed = smartCounter >= randomCounter && entityDistance <= randomRange;
                    break;
                default:
                    shouldSlowed = false;
                    break;
            }

            if (shouldSlowed && confirmAttack) {
                confirmAttack = false;
                playerTicks = ticksValue.getValue().intValue();
                if (resetlagBack.getValue()) {
                    confirmLagBack = true;
                }
                smartCounter = 0;
            } else {
                timerReset();
            }
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        double timerboost = NumberUtils.getRandom(0.5, 0.56);
        double charged = NumberUtils.getRandom(0.75, 0.91);

        if (playerTicks <= 0) {
            timerReset();
            return;
        }

        double tickProgress = (double) playerTicks / (double) ticksValue.getValue();
        float playerSpeed = (float) (tickProgress < timerboost ? timerBoostValue.getValue() : tickProgress < charged ? timerChargedValue.getValue() : 1f);
        float speedAdjustment = playerSpeed >= 0 ? playerSpeed : (float) (1f + ticksValue.getValue() - playerTicks);
        float adjustedTimerSpeed = Math.max(speedAdjustment, 0f);

        mc.timer.timerSpeed = adjustedTimerSpeed;
        playerTicks--;
    }

    private void timerReset() {
        mc.timer.timerSpeed = 1f;
    }

    private boolean shouldResetTimer() {
        EntityPlayerSP player = mc.thePlayer;
        return playerTicks >= 1
                || player.isSpectator() || player.isDead
                || player.isInWater() || player.isInLava()
                || player.isInWeb || player.isOnLadder()
                || player.isRiding();
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook && resetlagBack.getValue() && confirmLagBack && !shouldResetTimer()) {
            confirmLagBack = false;
            timerReset();
        }
    }
}
