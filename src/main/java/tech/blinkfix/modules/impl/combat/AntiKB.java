package tech.blinkfix.modules.impl.combat;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventPacket;
import tech.blinkfix.events.impl.EventUpdate;
import tech.blinkfix.events.impl.EventMoveInput;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.values.impl.ModeValue;
import tech.blinkfix.utils.ChatUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.Entity;

@ModuleInfo(
        name = "AntiKB",
        description = "Reduces Knock Back.",
        category = Category.COMBAT
)
public class AntiKB extends Module {
    public final ModeValue mode = ValueBuilder.create(this, "Mode")
            .setDefaultModeIndex(0)
            .setModes("Attack Reduce", "Jump Reset")
            .build()
            .getModeValue();

    private final ModeValue attackMode = ValueBuilder.create(this, "Attack Mode")
            .setDefaultModeIndex(0)
            .setModes("OneTime", "Test")
            .setVisibility(() -> mode.isCurrentMode("Attack Reduce"))
            .build()
            .getModeValue();

    private final FloatValue attacks = ValueBuilder.create(this, "Attack Count")
            .setDefaultFloatValue(2.0F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(5.0F)
            .setFloatStep(1.0F)
            .setVisibility(() -> mode.isCurrentMode("Attack Reduce"))
            .build()
            .getFloatValue();

    private final FloatValue jumpTick = ValueBuilder.create(this, "Jump Reset Tick")
            .setDefaultFloatValue(1.0F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(9.0F)
            .setFloatStep(1.0F)
            .setVisibility(() -> mode.isCurrentMode("Jump Reset"))
            .build()
            .getFloatValue();

    private final BooleanValue Logging = ValueBuilder.create(this, "Logging")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    private Entity targetEntity;
    private boolean velocityInput = false;
    private boolean attacked = false;
    private int JumpResetTicks = 0;
    private double currentKnockbackSpeed = 0.0;
    private int attackQueue = 0;
    private boolean receiveDamage = false;
    private BackTrack backTrackModule;
    private FakeLag fakeLagModule;
    private DelayTrack delayTrackModule;
    public boolean shouldVelo;
    private String lastMode = "";


    @Override
    public void onEnable() {
        super.onEnable();
        lastMode = mode.getCurrentMode();
        backTrackModule = (BackTrack) BlinkFix.getInstance().getModuleManager().getModule(BackTrack.class);
        fakeLagModule = (FakeLag) BlinkFix.getInstance().getModuleManager().getModule(FakeLag.class);
        delayTrackModule = (DelayTrack) BlinkFix.getInstance().getModuleManager().getModule(DelayTrack.class);
        
        // Check if FakeLag is enabled when enabling AntiKB with Attack Reduce mode
        if (mode.isCurrentMode("Attack Reduce")) {
            if (fakeLagModule != null && fakeLagModule.isEnabled()) {
                Notification notification = new Notification(NotificationLevel.INFO, "Fakelag cannot be enabled at the same time as Attack Reduce mode in AntiKB.", 10000L);
                BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            }
        }
    }

    @Override
    public void onDisable() {
        this.velocityInput = false;
        this.attacked = false;
        this.JumpResetTicks = 0;
        this.targetEntity = null;
        this.currentKnockbackSpeed = 0.0;
        this.attackQueue = 0;
        this.receiveDamage = false;
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (mc.level == null || mc.player == null) return;

        Packet<?> packet = event.getPacket();

        if (packet instanceof ClientboundDamageEventPacket damagePacket) {
            if (damagePacket.entityId() == mc.player.getId()) {
                this.receiveDamage = true;
            }
        }

        if (packet instanceof ClientboundSetEntityMotionPacket velocityPacket) {
            if (velocityPacket.getId() != mc.player.getId()) {
                return;
            }

            this.velocityInput = true;
            this.targetEntity = Aura.target;

            double x = (double) velocityPacket.getXa() / 8000.0;
            double z = (double) velocityPacket.getZa() / 8000.0;
            double speed = Math.sqrt(x * x + z * z);
            this.currentKnockbackSpeed = speed;

            if (this.mode.isCurrentMode("Attack Reduce")) {
                if (this.receiveDamage) {
                    this.receiveDamage = false;
                    this.attackQueue = (int) this.attacks.getCurrentValue();

                    if (this.Logging.getCurrentValue()) {
                        ChatUtils.addChatMessage("Speed: " + String.format("%.2f", speed));
                    }
                }
            } else if (this.mode.isCurrentMode("Jump Reset")) {
                this.JumpResetTicks = (int) this.jumpTick.getCurrentValue();
                if (this.Logging.getCurrentValue()) {
                    ChatUtils.addChatMessage("Jump Reset scheduled in " + this.JumpResetTicks + " ticks");
                }
            }
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.player == null) return;

        // Check if mode changed to Attack Reduce and FakeLag is enabled
        String currentMode = mode.getCurrentMode();
        if (!currentMode.equals(lastMode) && currentMode.equals("Attack Reduce")) {
            if (fakeLagModule != null && fakeLagModule.isEnabled()) {
                Notification notification = new Notification(NotificationLevel.INFO, "Fakelag cannot be enabled at the same time as Attack Reduce mode in Velocity.", 50000L);
                BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            }
            lastMode = currentMode;
        } else if (!currentMode.equals(lastMode)) {
            lastMode = currentMode;
        }

        if (mc.player.hurtTime == 0) {
            this.velocityInput = false;
            this.currentKnockbackSpeed = 0.0;
        }

        if (this.JumpResetTicks > 0) {
            this.JumpResetTicks--;
        }
        if (mc.player.distanceTo(this.targetEntity) > 3) {
            this.reset();
            return;
        }
        if (this.mode.isCurrentMode("Attack Reduce") && this.targetEntity != null && this.attackQueue > 0) {
            if (this.attackMode.isCurrentMode("OneTime")) {
                for (; this.attackQueue >= 1; this.attackQueue--) {
                    mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(this.targetEntity, false));
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(0.6, 1, 0.6));
                    mc.player.setSprinting(true);
//               mc.player.swing(InteractionHand.MAIN_HAND);
                }
            } else if (this.attackMode.isCurrentMode("Test")) {
                for (; this.attackQueue >= 1; this.attackQueue--) {
                    mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(this.targetEntity, false));
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(0.7776, 1, 0.7776));
                    mc.player.setSprinting(true);
//               mc.player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
    }
    private void reset() {
        this.shouldVelo = false;
        this.targetEntity = null;
    }
    @EventTarget
    public void onMoveInput(EventMoveInput event) {
        if (mc.player != null && this.mode.isCurrentMode("Jump Reset") &&
                mc.player.onGround() && this.JumpResetTicks == 1) {
            event.setJump(true);
            this.JumpResetTicks = 0;
            if (this.Logging.getCurrentValue()) {
                ChatUtils.addChatMessage("Jump reset activated");
            }
        }
        //suffix
//         if (mode.isCurrentMode("Attack Reduce")) {
//            this.setSuffix("Attack Reduce");
//         } else if (mode.isCurrentMode("Jump Reset")) {
//            this.setSuffix("Jump Reset");
//      }
    }
}