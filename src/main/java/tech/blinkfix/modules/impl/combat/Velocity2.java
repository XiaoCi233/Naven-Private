////package com.heypixel.heypixelmod.modules.impl.combat;
////
////import tech.blinkfix.BlinkFix;
////import api.events.tech.blinkfix.EventTarget;
////import types.api.events.tech.blinkfix.EventType;
////import com.heypixel.heypixelmod.events.impl.*;
////import modules.tech.blinkfix.Category;
////import modules.tech.blinkfix.Module;
////import modules.tech.blinkfix.ModuleInfo;
////import move.impl.modules.tech.blinkfix.Scaffold;
////import com.heypixel.heypixelmod.utils.*;
////import rotation.utils.tech.blinkfix.RotationManager;
////import values.tech.blinkfix.ValueBuilder;
////import impl.values.tech.blinkfix.BooleanValue;
////import impl.values.tech.blinkfix.FloatValue;
////import impl.values.tech.blinkfix.ModeValue;
////import net.minecraft.network.protocol.Packet;
////import net.minecraft.network.protocol.game.*;
////import net.minecraft.world.InteractionHand;
////import net.minecraft.world.entity.Entity;
////import net.minecraft.world.entity.LivingEntity;
////import net.minecraft.world.entity.player.Player;
////import net.minecraft.world.phys.AABB;
////import net.minecraft.world.phys.BlockHitResult;
////import net.minecraft.world.phys.EntityHitResult;
////import net.minecraft.world.phys.HitResult;
////import org.joml.Vector2d;
////import xyz.gay.accessors.mixin.LocalPlayerAccessor;
////
////import java.util.Optional;
////import java.util.concurrent.LinkedBlockingDeque;
////
////@ModuleInfo(
////        name = "Velocity",
////        description = "Reduces Knock Back.",
////        category = Category.COMBAT
////)
////public class Velocity extends Module {
////    private final ModeValue mode = ValueBuilder.create(this, "Mode")
////            .setDefaultModeIndex(0)
////            .setModes("GrimNoXZ", "JumpReset","GrimTest","Grim","GrimFull")
////            .build()
////            .getModeValue();
////
////    private final ModeValue noXZMode = ValueBuilder.create(this, "NoXZ Mode")
////            .setDefaultModeIndex(0)
////            .setModes("OneTime", "PerTick")
////            .setVisibility(() -> mode.isCurrentMode("GrimNoXZ"))
////            .build()
////            .getModeValue();
////
////    private final FloatValue attacks = ValueBuilder.create(this, "Attack Count")
////            .setDefaultFloatValue(2.0F)
////            .setMinFloatValue(1.0F)
////            .setMaxFloatValue(5.0F)
////            .setFloatStep(1.0F)
////            .setVisibility(() -> mode.isCurrentMode("GrimNoXZ"))
////            .build()
////            .getFloatValue();
////    private final FloatValue skips = ValueBuilder.create(this, "FullTicks")
////            .setDefaultFloatValue(3.0F)
////            .setFloatStep(1.0F)
////            .setMinFloatValue(2.0F)
////            .setMaxFloatValue(10.0F)
////            .setVisibility(() -> mode.isCurrentMode("GrimFull"))
////            .build()
////            .getFloatValue();
////
////    private final FloatValue jumpTick = ValueBuilder.create(this, "JumpResetTick")
////            .setDefaultFloatValue(1.0F)
////            .setMinFloatValue(0.0F)
////            .setMaxFloatValue(5.0F)
////            .setFloatStep(1.0F)
////            .setVisibility(() -> mode.isCurrentMode("JumpReset"))
////            .build()
////            .getFloatValue();
////
////    private final BooleanValue Logging = ValueBuilder.create(this, "Logging")
////            .setDefaultBooleanValue(false)
////            .build()
////            .getBooleanValue();
////
////
////
////    private Entity targetEntity;
////    private boolean velocityInput = false;
////    private boolean attacked = false;
////    private int jumpResetTicks = 0;
////    private double currentKnockbackSpeed = 0.0;
////    private int attackQueue = 0;
////    private boolean receiveDamage = false;
////    private int slowdownTicks = 0;
////    private boolean velocity = false;
////    private LivingEntity entity;
////    private boolean sprint = false;
////    private final TickTimeHelper timer = new TickTimeHelper();
////    private int delayVelocity = 0;
////    private int velocityTicks = 0;
////    LinkedBlockingDeque<Packet<ClientGamePacketListener>> inBound = new LinkedBlockingDeque<>();
////    BlockHitResult result = null;
////    private boolean nextMovement;
////    private int direction = 1;
////    private int preC0f = 0;
////    private int grimAction = 0;
////    private Optional<LivingEntity> findEntity() {
////        HitResult hitResult = mc.hitResult;
////        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
////            Entity entity = ((EntityHitResult)hitResult).getEntity();
////            if (entity instanceof Player) {
////                return Optional.of((LivingEntity)entity);
////            }
////        }
////
////        return Optional.empty();
////    }
////
////    @Override
////    public void onDisable() {
////        this.velocityInput = false;
////        this.attacked = false;
////        this.jumpResetTicks = 0;
////        this.targetEntity = null;
////        this.currentKnockbackSpeed = 0.0;
////        this.attackQueue = 0;
////        this.receiveDamage = false;
////    }
////    @EventTarget
////    public void onMotion(EventMotion eventMotion) {
////        this.setSuffix(mode.getCurrentMode());
////        if (this.mode.isCurrentMode("Grim")) {
////            if (eventMotion.getType() == EventType.PRE) {
////                this.slowdownTicks--;
////                if (this.slowdownTicks == 0) {
////                    tech.blinkfix.BlinkFix.TICK_TIMER = 1.0F;
////                } else if (this.slowdownTicks > 0) {
////                    ChatUtils.addChatMessage("Slowdown Ticks: " + this.slowdownTicks);
////                    BlinkFix.TICK_TIMER = 1.0F / this.slowdownTicks;
////                }
////            }
////        }
////    }
////    @EventTarget
////    public void onEnable() {
////        super.onEnable();
//////        if (this.mode.isCurrentMode("GrimNoXZ")) {
//////            this.setSuffix("GrimNoXZ");
//////        }else if (this.mode.isCurrentMode("JumpReset")) {
//////            this.setSuffix("JumpReset");
//////        }else if (this.mode.isCurrentMode("Grim")) {
//////            this.setSuffix("Grim");
//////        }else if (this.mode.isCurrentMode("GrimFull")) {
//////            this.setSuffix("GrimFull");
//////        }
////    }
////    @EventTarget
////    public void onMotion(EventClick e) {
////        this.setSuffix(mode.getCurrentMode());
////        if (this.mode.isCurrentMode("GrimFull")) {
////            if (this.velocity) {
////                if (this.sprint && !((LocalPlayerAccessor) mc.player).isWasSprinting()) {
////                    this.velocity = false;
////                    return;
////                }
////
////                this.velocity = false;
////                if (this.sprint) {
////                    float currentYaw = mc.player.getYRot();
////                    float currentPitch = mc.player.getXRot();
////                    mc.player.setYRot(RotationManager.rotations.x);
////                    mc.player.setXRot(RotationManager.rotations.y);
////
////                    for (int i = 0; i < this.attacks.getCurrentValue(); i++) {
////                        mc.gameMode.attack(mc.player, this.entity);
////                        mc.player.swing(InteractionHand.MAIN_HAND);
////                    }
////
////                    mc.player.setYRot(currentYaw);
////                    mc.player.setXRot(currentPitch);
////                }
////
////                if (!this.sprint) {
////                }
////            }
////        }
////    }
////
////    @EventTarget
////    public void onPacket(EventPacket event) {
////        if (mc.level == null || mc.player == null) return;
////
////        Packet<?> packet = event.getPacket();
////
////        if (packet instanceof ClientboundDamageEventPacket) {
////            ClientboundDamageEventPacket damagePacket = (ClientboundDamageEventPacket)packet;
////            if (damagePacket.entityId() == mc.player.getId()) {
////                this.receiveDamage = true;
////            }
////        }
////
////        if (packet instanceof ClientboundSetEntityMotionPacket) {
////            ClientboundSetEntityMotionPacket velocityPacket = (ClientboundSetEntityMotionPacket)packet;
////            if (velocityPacket.getId() != mc.player.getId()) {
////                return;
////            }
////
////            this.velocityInput = true;
////            this.targetEntity = Aura.target;
////
////            if (this.mode.isCurrentMode("GrimNoXZ")) {
////                if (this.receiveDamage) {
////                    this.receiveDamage = false;
////                    this.attackQueue = (int)this.attacks.getCurrentValue();
////
////                    if (this.Logging.getCurrentValue()) {
////                        ChatUtils.addChatMessage("NoXZ Queue set: " + this.attackQueue + " attacks");
////                    }
////                }
////            } else if (this.mode.isCurrentMode("JumpReset")) {
////                this.jumpResetTicks = (int)this.jumpTick.getCurrentValue();
////                if (this.Logging.getCurrentValue()) {
////                    ChatUtils.addChatMessage("JumpReset scheduled in " + this.jumpResetTicks + " ticks");
////                }
////
////            }
////        }
////    }
////
////    @EventTarget
////    public void onUpdate(EventUpdate event) {
////        if (mc.player == null) return;
////
////        if (mc.player.hurtTime == 0) {
////            this.velocityInput = false;
////            this.currentKnockbackSpeed = 0.0;
////        }
////
////        if (this.jumpResetTicks > 0) {
////            this.jumpResetTicks--;
////        }
////
////        if (this.mode.isCurrentMode("GrimNoXZ") && this.targetEntity != null && this.attackQueue > 0) {
////            if (this.noXZMode.isCurrentMode("OneTime")) {
////                for (; this.attackQueue >= 1; this.attackQueue--) {
////                    mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(this.targetEntity, false));
////                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(0.6, 1, 0.6));
////                    mc.player.setSprinting(false);
////                    mc.player.swing(InteractionHand.MAIN_HAND);
////                }
////                if (this.Logging.getCurrentValue()) {
////                    ChatUtils.addChatMessage("NoXZ OneTime attacks executed");
////                }
////            } else if (this.noXZMode.isCurrentMode("PerTick")) {
////                if (this.attackQueue >= 1) {
////                    mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(this.targetEntity, false));
////                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(0.6, 1, 0.6));
////                    mc.player.setSprinting(false);
////                    mc.player.swing(InteractionHand.MAIN_HAND);
////
////                    if (this.Logging.getCurrentValue()) {
////                        ChatUtils.addChatMessage("NoXZ PerTick attack executed, remaining: " + (this.attackQueue - 1));
////                    }
////                }
////                this.attackQueue--;
////            }
////        }
////    }
////
////    @EventTarget
////    public void onMoveInput(EventMoveInput event) {
////        if (mc.player != null && this.mode.isCurrentMode("JumpReset") &&
////                mc.player.onGround() && this.jumpResetTicks == 1) {
////            event.setJump(true);
////            this.jumpResetTicks = 0;
////            if (this.Logging.getCurrentValue()) {
////                ChatUtils.addChatMessage("Jump reset activated");
////            }
////        }
////    }
////@EventTarget(0)
////public void onPacket(EventHandlePacket e) {
////    try {
////        if (mc.player != null && !e.isCancelled()) {
////            if (e.getPacket() instanceof ClientboundSetEntityMotionPacket && this.timer.delay(3)) {
////                ClientboundSetEntityMotionPacket packet = (ClientboundSetEntityMotionPacket)e.getPacket();
////                if (mc.player.getId() == packet.getId()) {
////                    double x = packet.getXa() / 8000.0;
////                    double z = packet.getZa() / 8000.0;
////                    double speed = Math.sqrt(x * x + z * z);
////                    Optional<LivingEntity> targetEntity = this.findEntity();
////                    if (this.mode.isCurrentMode("GrimFull")
////                            && !tech.blinkfix.BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled()
////                            && mc.player.getUseItem().isEmpty()
////                            && mc.screen == null
////                            && targetEntity.isPresent()) {
////                        this.entity = targetEntity.get();
////                        this.sprint = ((LocalPlayerAccessor)mc.player).isWasSprinting();
////                        if (this.sprint) {
////                            e.setCancelled(true);
////                            if (this.Logging.getCurrentValue()) {
////                                ChatUtils.addChatMessage("Vel: " + (float)Math.round(speed * 100.0) / 100.0F);
////                            }
////
////                            x *= Math.pow(0.6, this.attacks.getCurrentValue());
////                            z *= Math.pow(0.6, this.attacks.getCurrentValue());
////                            mc.player.setDeltaMovement(x, packet.getYa() / 8000.0, z);
////                            this.velocity = true;
////                            this.timer.reset();
////                        }
////                    }
////
////                    if (this.mode.isCurrentMode("GrimFull") && mc.player.tickCount > 120) {
////                        double horizontalStrength = new Vector2d(packet.getXa(), packet.getZa()).length();
////                        if (horizontalStrength <= 1000.0) {
////                            return;
////                        }
////
////                        if (packet.getYa() < 0) {
////                            return;
////                        }
////
////                        this.delayVelocity = (int)this.skips.getCurrentValue();
////                        this.velocityTicks = 0;
////                        this.velocity = true;
////                        e.setCancelled(true);
////                    }
////                }
////            }
////
////            if (this.mode.isCurrentMode("GrimFull") && mc.player.tickCount > 120) {
////                Packet<?> packet = e.getPacket();
////                if (packet instanceof ClientboundPlayerPositionPacket wrapped) {
////                    while (!this.inBound.isEmpty()) {
////                        this.inBound.poll().handle(mc.player.connection);
////                    }
////                }
////
////                if (packet instanceof ClientboundPingPacket pingPacket) {
////                    if (Math.abs(this.preC0f - pingPacket.getId()) == 1) {
////                        this.grimAction = pingPacket.getId();
////                    }
////
////                    this.preC0f = pingPacket.getId();
////                    if (this.grimAction != pingPacket.getId() && Math.abs(this.grimAction - pingPacket.getId()) > 10 && mc.player.hurtTime > 0) {
////                        mc.player.hurtTime = 0;
////                        e.setCancelled(true);
////                        return;
////                    }
////                }
////
////                if (this.delayVelocity > 0 && this.velocity) {
////                    if (packet instanceof ClientboundSystemChatPacket) {
////                        return;
////                    }
////
////                    if (e.getPacket().getClass().getSimpleName().startsWith("C")
////                            && !(packet instanceof ClientboundSetEntityMotionPacket)
////                            && !(packet instanceof ClientboundExplodePacket)
////                            && !(packet instanceof ClientboundSetTimePacket)
////                            && !(packet instanceof ClientboundMoveEntityPacket)
////                            && !(packet instanceof ClientboundTeleportEntityPacket)
////                            && !(packet instanceof ClientboundSoundPacket)
////                            && !(packet instanceof ClientboundSetHealthPacket)
////                            && !(packet instanceof ClientboundPlayerPositionPacket)
////                            && !(packet instanceof ClientboundSystemChatPacket)) {
////                        e.setCancelled(true);
////                        this.inBound.add((Packet<ClientGamePacketListener>)packet);
////                        if (packet instanceof ClientboundPingPacket) {
////                            this.delayVelocity--;
////                            if (this.delayVelocity == 0) {
////                                this.delayVelocity++;
////                                BlockHitResult blockRayTraceResult = (BlockHitResult) PlayerUtils.pickCustom(4.5, mc.player.getYRot(), 90.0F);
////                                if (blockRayTraceResult == null) {
////                                    return;
////                                }
////
////                                if (BlockUtils.isAirBlock(blockRayTraceResult.getBlockPos())) {
////                                    return;
////                                }
////
////                                AABB aabb = new AABB(blockRayTraceResult.getBlockPos().above());
////                                if (!mc.player.getBoundingBox().intersects(aabb)) {
////                                    return;
////                                }
////
////                                this.delayVelocity--;
////
////                                while (!this.inBound.isEmpty()) {
////                                    this.inBound.poll().handle(mc.player.connection);
////                                }
////
////                                this.result = new BlockHitResult(
////                                        blockRayTraceResult.getLocation(), blockRayTraceResult.getDirection(), blockRayTraceResult.getBlockPos(), false
////                                );
////                                this.direction = (int)(this.direction * -0.1);
////                                float pitch = (float)(MathUtils.getRandomDoubleInRange(89.1F, 90.0) - MathUtils.getRandomDoubleInRange(0.002F, 1.0E-14));
////                                float yaw = (float)(((LocalPlayerAccessor)mc.player).getYRotLast() - MathUtils.getRandomDoubleInRange(0.002F, 0.004F));
////                                ((LocalPlayerAccessor)mc.player).setYRotLast(yaw);
////                                ((LocalPlayerAccessor)mc.player).setXRotLast(pitch);
////                                float currentYaw = mc.player.getYRot();
////                                mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, mc.player.onGround()));
////                                mc.player
////                                        .connection
////                                        .send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, this.result, MathUtils.getRandomIntInRange(0, 2)));
////                                if (this.Logging.getCurrentValue()) {
////                                    ChatUtils.addChatMessage("Send");
////                                }
////
////                                ((IMixinMinecraft)mc).setSkipTicks((int)this.skips.getCurrentValue());
////                                mc.hitResult = this.result;
////                                this.delayVelocity = 0;
////                                this.result = null;
////                                this.velocity = false;
////                                this.nextMovement = true;
////                            }
////                        }
////                    }
////                }
////
////                if (packet instanceof ServerboundMovePlayerPacket
////                        && !(packet instanceof ServerboundMovePlayerPacket.Pos)
////                        && !(packet instanceof ServerboundMovePlayerPacket.Rot)
////                        && !(packet instanceof ServerboundMovePlayerPacket.PosRot)
////                        && this.nextMovement) {
////                    mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.onGround()));
////                    e.setCancelled(true);
////                    this.nextMovement = false;
////                }
////            }
////        }
////    } catch (Exception var12) {
////        var12.printStackTrace();
////    }
////}
////}
//package com.heypixel.heypixelmod.modules.impl.combat;
//
//import tech.blinkfix.BlinkFix;
//import api.events.tech.blinkfix.EventTarget;
//import types.api.events.tech.blinkfix.EventType;
//import com.heypixel.heypixelmod.events.impl.*;
//import modules.tech.blinkfix.Category;
//import modules.tech.blinkfix.Module;
//import modules.tech.blinkfix.ModuleInfo;
//import move.impl.modules.tech.blinkfix.Scaffold;
//import com.heypixel.heypixelmod.utils.*;
//import rotation.utils.tech.blinkfix.RotationManager;
//import values.tech.blinkfix.ValueBuilder;
//import impl.values.tech.blinkfix.BooleanValue;
//import impl.values.tech.blinkfix.FloatValue;
//import impl.values.tech.blinkfix.ModeValue;
//import tech.airfoundation.obfuscate.jnic.JNICInclude;
//import net.minecraft.network.protocol.Packet;
//import net.minecraft.network.protocol.game.*;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.level.ClipContext;
//import net.minecraft.world.phys.*;
//import org.joml.Vector2d;
//import xyz.gay.accessors.mixin.LocalPlayerAccessor;
//
//import java.util.Optional;
//import java.util.concurrent.LinkedBlockingDeque;
//@JNICInclude
//
//@ModuleInfo(
//        name = "Velocity",
//        description = "Reduces Knock Back.",
//        category = Category.COMBAT
//)
//public class Velocity extends Module {
//    private final ModeValue mode = ValueBuilder.create(this, "Mode")
//            .setDefaultModeIndex(0)
//            .setModes("GrimNoXZ", "JumpReset","Grim","GrimFull","GrimTest","Mix")
//            .build()
//            .getModeValue();
//
//    private final ModeValue noXZMode = ValueBuilder.create(this, "NoXZ Mode")
//            .setDefaultModeIndex(0)
//            .setModes("OneTime", "PerTick")
//            .setVisibility(() -> mode.isCurrentMode("GrimNoXZ"))
//            .build()
//            .getModeValue();
//
//    private final FloatValue attacks = ValueBuilder.create(this, "Attack Count")
//            .setDefaultFloatValue(2.0F)
//            .setMinFloatValue(1.0F)
//            .setMaxFloatValue(5.0F)
//            .setFloatStep(1.0F)
//            .setVisibility(() -> mode.isCurrentMode("GrimNoXZ"))
//            .build()
//            .getFloatValue();
//
//    private final FloatValue skips = ValueBuilder.create(this, "FullTicks")
//            .setDefaultFloatValue(3.0F)
//            .setFloatStep(1.0F)
//            .setMinFloatValue(2.0F)
//            .setMaxFloatValue(10.0F)
//            .setVisibility(() -> mode.isCurrentMode("GrimFull"))
//            .build()
//            .getFloatValue();
//
//    private final FloatValue jumpTick = ValueBuilder.create(this, "JumpResetTick")
//            .setDefaultFloatValue(1.0F)
//            .setMinFloatValue(0.0F)
//            .setMaxFloatValue(5.0F)
//            .setFloatStep(1.0F)
//            .setVisibility(() -> mode.isCurrentMode("JumpReset"))
//            .build()
//            .getFloatValue();
//
//    // Mix Mode
//    private final FloatValue attackTick = ValueBuilder.create(this, "AttackTick")
//            .setDefaultFloatValue(6.0F)
//            .setFloatStep(1.0F)
//            .setMinFloatValue(1.0F)
//            .setMaxFloatValue(10.0F)
//            .setVisibility(() -> mode.isCurrentMode("Mix"))
//            .build()
//            .getFloatValue();
//
//    private final BooleanValue Logging = ValueBuilder.create(this, "Logging")
//            .setDefaultBooleanValue(false)
//            .build()
//            .getBooleanValue();
//
//    // GrimTest specific values
//    private final FloatValue reduceCount = ValueBuilder.create(this,"Reduce Count")
//            .setDefaultFloatValue(4F)
//            .setFloatStep(1F)
//            .setMinFloatValue(1F)
//            .setMaxFloatValue(10.0F)
//            .setVisibility(() -> mode.isCurrentMode("GrimTest"))
//            .build()
//            .getFloatValue();
//
//    private final FloatValue attack = ValueBuilder.create(this,"Attack Count")
//            .setDefaultFloatValue(2F)
//            .setFloatStep(1F)
//            .setMinFloatValue(1F)
//            .setMaxFloatValue(5.0F)
//            .setVisibility(() -> mode.isCurrentMode("GrimTest"))
//            .build()
//            .getFloatValue();
//
//    private final BooleanValue raytrace = ValueBuilder.create(this,"Best")
//            .setDefaultBooleanValue(true)
//            .setVisibility(() -> mode.isCurrentMode("GrimTest"))
//            .build()
//            .getBooleanValue();
//
//    private final BooleanValue Lag = ValueBuilder.create(this,"S08 Disabler")
//            .setDefaultBooleanValue(true)
//            .setVisibility(() -> mode.isCurrentMode("GrimTest"))
//            .build()
//            .getBooleanValue();
//
//    private final BooleanValue boost = ValueBuilder.create(this,"Attack Boost")
//            .setDefaultBooleanValue(true)
//            .setVisibility(() -> mode.isCurrentMode("GrimTest"))
//            .build()
//            .getBooleanValue();
//
//    // GrimTest specific fields
//    private final java.util.concurrent.LinkedBlockingQueue<Packet<ClientGamePacketListener>> packets = new java.util.concurrent.LinkedBlockingQueue<>();
//    private boolean velocityInput = false;
//    private boolean S08;
//    public static int ReduceCount = 0;
//    public int attackCount;
//    private boolean needStore;
//    private final TimeHelper timeHelper = new TimeHelper();
//    private final TimeHelper PacketInBoundTimer = new TimeHelper();
//    public static boolean kbPacket;
//    public static boolean Attacking;
//
//    // Existing fields
//    private Entity targetEntity;
//    private boolean attacked = false;
//    private int jumpResetTicks = 0;
//    private double currentKnockbackSpeed = 0.0;
//    private int attackQueue = 0;
//    private boolean receiveDamage = false;
//    private int slowdownTicks = 0;
//    private boolean velocity = false;
//    private LivingEntity entity;
//    private boolean sprint = false;
//    private final TickTimeHelper tickTimer = new TickTimeHelper();
//    private int delayVelocity = 0;
//    private int velocityTicks = 0;
//    LinkedBlockingDeque<Packet<ClientGamePacketListener>> inBound = new LinkedBlockingDeque<>();
//    BlockHitResult result = null;
//    private boolean nextMovement;
//    private int direction = 1;
//    private int preC0f = 0;
//    private int grimAction = 0;
//
//    private Optional<LivingEntity> findEntity() {
//        HitResult hitResult = mc.hitResult;
//        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
//            Entity entity = ((EntityHitResult)hitResult).getEntity();
//            if (entity instanceof Player) {
//                return Optional.of((LivingEntity)entity);
//            }
//        }
//        return Optional.empty();
//    }
//
//    @Override
//    public void onEnable() {
//        super.onEnable();
//        if (mode.isCurrentMode("GrimTest")) {
//            this.reset();
//            timeHelper.reset();
//        }
//    }
//
//    @Override
//    public void onDisable() {
//        // Reset all modes
//        this.velocityInput = false;
//        this.attacked = false;
//        this.jumpResetTicks = 0;
//        this.targetEntity = null;
//        this.currentKnockbackSpeed = 0.0;
//        this.attackQueue = 0;
//        this.receiveDamage = false;
//
//        // Reset GrimTest specific fields
//        if (mode.isCurrentMode("GrimTest")) {
//            this.reset();
//            timeHelper.reset();
//        }
//    }
//
//    // GrimTest specific methods
//    public void reset() {
//        if (mc.getConnection() != null) {
//            attackCount = 0;
//            ReduceCount = 0;
//            timeHelper.reset();
//            velocityInput = false;
//            ProcessPacket();
//        }
//    }
//
//    private void log(String message) {
//        if (this.Logging.getCurrentValue()) {
//            ChatUtils.addChatMessage(message);
//        }
//    }
//
//    private void ProcessPacket(){
//        try {
//            while (!packets.isEmpty()) {
//                Packet<ClientGamePacketListener> packet = packets.poll();
//                if (packet != null) {
//                    packet.handle(mc.player.connection);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void cnm(){
//        if (mc.player != null && !packets.isEmpty()){
//            packets.clear();
//        }
//    }
//
//    private boolean check(){
//        return mc.player.isInLava() || mc.player.isInWater() || mc.player.isDeadOrDying() || mc.player.isUsingItem();
//    }
//
//    private boolean RayTraceTarget(Entity target) {
//        Vec3 playerEyes = mc.player.getEyePosition(1.0F);
//        Vec3 targetEyes = target.getEyePosition(1.0F);
//        BlockHitResult result = mc.level.clip(new ClipContext(
//                playerEyes,
//                targetEyes,
//                ClipContext.Block.COLLIDER,
//                ClipContext.Fluid.NONE,
//                mc.player
//        ));
//        return result.getType() == HitResult.Type.MISS ||
//                result.getLocation().distanceTo(playerEyes) > playerEyes.distanceTo(targetEyes);
//    }
//
//    @EventTarget
//    public void onWorld(EventRespawn eventRespawn) {
//        if (mode.isCurrentMode("GrimTest")) {
//            this.reset();
//        }
//    }
//
//    @EventTarget
//    public void onTick(EventRunTicks eventRunTicks) {
//        if (mc.player == null) return;
//
//        this.setSuffix(mode.getCurrentMode());
//
//        // GrimTest tick logic
//        if (mode.isCurrentMode("GrimTest")) {
//            if (S08 && timeHelper.delay(1000)) {
//                S08 = false;
//            }
//
//            if (mc.getConnection() != null && mc.gameMode != null && mc.player.hurtTime != 0) {
//                if (Aura.target != null && kbPacket && attackCount > 0 && velocityInput && !tech.blinkfix.BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled()) {
//                    if (ReduceCount > reduceCount.getCurrentValue()) return;
//                    if (!check()) {
//                        if (Lag.getCurrentValue() && S08) return;
//                        if (RayTraceTarget(Aura.target) && raytrace.getCurrentValue())
//                            if (mc.player.distanceTo(Aura.target) <= 3.3 && mc.player.distanceTo(Aura.target) >= 3.0){
//                                needStore = true;
//                            }
//                        if (mc.player.distanceTo(Aura.target) <= 3.02) {
//                            needStore = false;
//                            if (!packets.isEmpty()) {
//                                ProcessPacket();
//                            }
//                            for (;this.attackCount >= 1; this.attackCount--) {
//                                mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(Aura.target, false));
//                                Attacking = true;
//                                mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(0.6, 1, 0.6));
//                                ReduceCount++;
//                                mc.player.setSprinting(false);
//                                mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
//                            }
//                        }
//                    }
//                    Attacking = false;
//                }
//            }
//
//            if (ReduceCount > reduceCount.getCurrentValue()){
//                timeHelper.delay(200);
//                ReduceCount = 0;
//            }
//
//            if (!packets.isEmpty() && !needStore) {
//                ProcessPacket();
//            }
//
//            if (Aura.target != null && mc.player.distanceTo(Aura.target) > 3.3 && PacketInBoundTimer.delay(1500)){
//                cnm();
//            }
//        }
//
//        // JumpReset tick logic
//        if (mode.isCurrentMode("JumpReset") && mc.player != null) {
//            if (mc.player.hurtTime > 9 && mc.player.onGround() && !mc.player.isInWater() && !mc.player.isInLava() && !mc.player.isOnFire()) {
//                mc.player.jumpFromGround();
//            }
//        }
//
//        // Mix tick logic
//        if (mode.isCurrentMode("Mix")) {
//            mixUpdateHandler();
//        }
//
//        // Existing tick logic for other modes
//        if (mc.player.hurtTime == 0) {
//            this.velocityInput = false;
//            this.currentKnockbackSpeed = 0.0;
//        }
//
//        if (this.jumpResetTicks > 0) {
//            this.jumpResetTicks--;
//        }
//
//        if (this.mode.isCurrentMode("GrimNoXZ") && this.targetEntity != null && this.attackQueue > 0) {
//            if (this.noXZMode.isCurrentMode("OneTime")) {
//                for (; this.attackQueue >= 1; this.attackQueue--) {
//                    mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(this.targetEntity, false));
//                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(0.6, 1, 0.6));
//                    mc.player.setSprinting(false);
//                    mc.player.swing(InteractionHand.MAIN_HAND);
//                }
////                if (this.Logging.getCurrentValue()) {
////                    ChatUtils.addChatMessage("NoXZ OneTime attacks executed");
////                }
//            } else if (this.noXZMode.isCurrentMode("PerTick")) {
//                if (this.attackQueue >= 1) {
//                    mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(this.targetEntity, false));
//                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(0.6, 1, 0.6));
//                    mc.player.setSprinting(false);
//                    mc.player.swing(InteractionHand.MAIN_HAND);
//
////                    if (this.Logging.getCurrentValue()) {
////                        ChatUtils.addChatMessage("NoXZ PerTick attack executed, remaining: " + (this.attackQueue - 1));
////                    }
//                }
//                this.attackQueue--;
//            }
//        }
//    }
//
//    @EventTarget
//    public void onMotion(EventMotion eventMotion) {
//        this.setSuffix(mode.getCurrentMode());
//
//        // Grim motion logic
//        if (this.mode.isCurrentMode("Grim")) {
//            if (eventMotion.getType() == EventType.PRE) {
//                this.slowdownTicks--;
//                if (this.slowdownTicks == 0) {
//                    tech.blinkfix.BlinkFix.TICK_TIMER = 1.0F;
//                } else if (this.slowdownTicks > 0) {
//                    ChatUtils.addChatMessage("Slowdown Ticks: " + this.slowdownTicks);
//                    BlinkFix.TICK_TIMER = 1.0F / this.slowdownTicks;
//                }
//            }
//        }
//
//    }
//
//    @EventTarget
//    public void onMotion(EventClick e) {
//        this.setSuffix(mode.getCurrentMode());
//        if (this.mode.isCurrentMode("GrimFull")) {
//            if (this.velocity) {
//                if (this.sprint && !((LocalPlayerAccessor) mc.player).isWasSprinting()) {
//                    this.velocity = false;
//                    return;
//                }
//
//                this.velocity = false;
//                if (this.sprint) {
//                    float currentYaw = mc.player.getYRot();
//                    float currentPitch = mc.player.getXRot();
//                    mc.player.setYRot(RotationManager.rotations.x);
//                    mc.player.setXRot(RotationManager.rotations.y);
//
//                    for (int i = 0; i < this.attacks.getCurrentValue(); i++) {
//                        mc.gameMode.attack(mc.player, this.entity);
//                        mc.player.swing(InteractionHand.MAIN_HAND);
//                    }
//
//                    mc.player.setYRot(currentYaw);
//                    mc.player.setXRot(currentPitch);
//                }
//            }
//        }
//    }
//
//    @EventTarget
//    public void onPacket(EventPacket event) {
//        if (mc.level == null || mc.player == null) return;
//
//        Packet<?> packet = event.getPacket();
//
//        if (packet instanceof ClientboundDamageEventPacket) {
//            ClientboundDamageEventPacket damagePacket = (ClientboundDamageEventPacket)packet;
//            if (damagePacket.entityId() == mc.player.getId()) {
//                this.receiveDamage = true;
//            }
//        }
//
//        if (packet instanceof ClientboundSetEntityMotionPacket) {
//            ClientboundSetEntityMotionPacket velocityPacket = (ClientboundSetEntityMotionPacket)packet;
//            if (velocityPacket.getId() != mc.player.getId()) {
//                return;
//            }
//
//            this.velocityInput = true;
//            this.targetEntity = Aura.target;
//
//            if (this.mode.isCurrentMode("GrimNoXZ")) {
//                if (this.receiveDamage) {
//                    this.receiveDamage = false;
//                    this.attackQueue = (int)this.attacks.getCurrentValue();
//                    double X = velocityPacket.getXa() / 8000d;
//                    double Z = velocityPacket.getZa() / 8000d;
//                    double speed = Math.sqrt(X * X + Z * Z);
//                    if (this.Logging.getCurrentValue()) {
//                        ChatUtils.addChatMessage("NoxzTick: " + (float)Math.round(speed * 100.0) / 100.0F);
//                    }
////                    if (this.Logging.getCurrentValue()) {
////                        ChatUtils.addChatMessage("NoXZ Queue set: " + this.attackQueue + " attacks");
////                    }
//                }
//            } else if (this.mode.isCurrentMode("JumpReset")) {
//                this.jumpResetTicks = (int)this.jumpTick.getCurrentValue();
//                if (this.Logging.getCurrentValue()) {
//                    ChatUtils.addChatMessage("JumpReset " + this.jumpResetTicks + " ticks");
//                }
//            }
//        }
//    }
//
//    @EventTarget
//    public void onHandlePacket(EventHandlePacket e) {
//        if (mc.player == null) return;
//
//        Packet<?> packet = e.getPacket();
//
//        // GrimTest packet handling
//        if (mode.isCurrentMode("GrimTest") && mc.getConnection() != null && mc.gameMode != null && !mc.player.isUsingItem()) {
//            if (packet instanceof ClientboundDamageEventPacket) {
//                ClientboundDamageEventPacket damagePacket = (ClientboundDamageEventPacket) packet;
//                if (damagePacket.entityId() == mc.player.getId()) {
//                    velocityInput = true;
//                } else {
//                    velocityInput = false;
//                }
//            }
//            if (packet instanceof ClientboundPlayerPositionPacket && Aura.target != null) {
//                if (Lag.getCurrentValue()) {
//                    S08 = true;
//                }
////                log("Velocity : Receive S08!");
//            }
//            if (packet instanceof ClientboundSetEntityMotionPacket velocityPacket) {
//                if (velocityPacket.getId() != mc.player.getId()) return;
//                kbPacket = true;
//                double X = velocityPacket.getXa() / 8000d;
//                double Z = velocityPacket.getZa() / 8000d;
//                double Y = velocityPacket.getYa() / 8000d;
//                double speed = Math.sqrt(X * X + Z * Z);
//                if (this.Logging.getCurrentValue()) {
//                    ChatUtils.addChatMessage("VelocityTest: " + (float)Math.round(speed * 100.0) / 100.0F);
//                }
//                attackCount = (int) attack.getCurrentValue();
//            }
//        }
//
//        // Existing packet handling for other modes
//        try {
//            if (!e.isCancelled()) {
//                if (packet instanceof ClientboundSetEntityMotionPacket && this.tickTimer.delay(3)) {
//                    ClientboundSetEntityMotionPacket velocityPacket = (ClientboundSetEntityMotionPacket) packet;
//                    if (mc.player.getId() == velocityPacket.getId()) {
//                        double x = velocityPacket.getXa() / 8000.0;
//                        double z = velocityPacket.getZa() / 8000.0;
//                        double speed = Math.sqrt(x * x + z * z);
//                        Optional<LivingEntity> targetEntity = this.findEntity();
//                        if (this.mode.isCurrentMode("GrimFull")
//                                && !tech.blinkfix.BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled()
//                                && mc.player.getUseItem().isEmpty()
//                                && mc.screen == null
//                                && targetEntity.isPresent()) {
//                            this.entity = targetEntity.get();
//                            this.sprint = ((LocalPlayerAccessor)mc.player).isWasSprinting();
//                            if (this.sprint) {
//                                e.setCancelled(true);
//                                if (this.Logging.getCurrentValue()) {
//                                    ChatUtils.addChatMessage("Vel: " + (float)Math.round(speed * 100.0) / 100.0F);
//                                }
//
//                                x *= Math.pow(0.6, this.attacks.getCurrentValue());
//                                z *= Math.pow(0.6, this.attacks.getCurrentValue());
//                                mc.player.setDeltaMovement(x, velocityPacket.getYa() / 8000.0, z);
//                                this.velocity = true;
//                                this.tickTimer.reset();
//                            }
//                        }
//
//                        if (this.mode.isCurrentMode("GrimFull") && mc.player.tickCount > 120) {
//                            double horizontalStrength = new Vector2d(velocityPacket.getXa(), velocityPacket.getZa()).length();
//                            if (horizontalStrength <= 1000.0) {
//                                return;
//                            }
//
//                            if (velocityPacket.getYa() < 0) {
//                                return;
//                            }
//
//                            this.delayVelocity = (int)this.skips.getCurrentValue();
//                            this.velocityTicks = 0;
//                            this.velocity = true;
//                            e.setCancelled(true);
//                        }
//                    }
//                }
//
//                if (this.mode.isCurrentMode("GrimFull") && mc.player.tickCount > 120) {
//                    if (packet instanceof ClientboundPlayerPositionPacket) {
//                        while (!this.inBound.isEmpty()) {
//                            this.inBound.poll().handle(mc.player.connection);
//                        }
//                    }
//
//                    if (packet instanceof ClientboundPingPacket pingPacket) {
//                        if (Math.abs(this.preC0f - pingPacket.getId()) == 1) {
//                            this.grimAction = pingPacket.getId();
//                        }
//
//                        this.preC0f = pingPacket.getId();
//                        if (this.grimAction != pingPacket.getId() && Math.abs(this.grimAction - pingPacket.getId()) > 10 && mc.player.hurtTime > 0) {
//                            mc.player.hurtTime = 0;
//                            e.setCancelled(true);
//                            return;
//                        }
//                    }
//
//                    if (this.delayVelocity > 0 && this.velocity) {
//                        if (packet instanceof ClientboundSystemChatPacket) {
//                            return;
//                        }
//
//                        if (packet.getClass().getSimpleName().startsWith("C")
//                                && !(packet instanceof ClientboundSetEntityMotionPacket)
//                                && !(packet instanceof ClientboundExplodePacket)
//                                && !(packet instanceof ClientboundSetTimePacket)
//                                && !(packet instanceof ClientboundMoveEntityPacket)
//                                && !(packet instanceof ClientboundTeleportEntityPacket)
//                                && !(packet instanceof ClientboundSoundPacket)
//                                && !(packet instanceof ClientboundSetHealthPacket)
//                                && !(packet instanceof ClientboundPlayerPositionPacket)
//                                && !(packet instanceof ClientboundSystemChatPacket)) {
//                            e.setCancelled(true);
//                            this.inBound.add((Packet<ClientGamePacketListener>)packet);
//                            if (packet instanceof ClientboundPingPacket) {
//                                this.delayVelocity--;
//                                if (this.delayVelocity == 0) {
//                                    this.delayVelocity++;
//                                    BlockHitResult blockRayTraceResult = (BlockHitResult) PlayerUtils.pickCustom(4.5, mc.player.getYRot(), 90.0F);
//                                    if (blockRayTraceResult == null) {
//                                        return;
//                                    }
//
//                                    if (BlockUtils.isAirBlock(blockRayTraceResult.getBlockPos())) {
//                                        return;
//                                    }
//
//                                    AABB aabb = new AABB(blockRayTraceResult.getBlockPos().above());
//                                    if (!mc.player.getBoundingBox().intersects(aabb)) {
//                                        return;
//                                    }
//
//                                    this.delayVelocity--;
//
//                                    while (!this.inBound.isEmpty()) {
//                                        this.inBound.poll().handle(mc.player.connection);
//                                    }
//
//                                    this.result = new BlockHitResult(
//                                            blockRayTraceResult.getLocation(), blockRayTraceResult.getDirection(), blockRayTraceResult.getBlockPos(), false
//                                    );
//                                    this.direction = (int)(this.direction * -0.1);
//                                    float pitch = (float)(MathUtils.getRandomDoubleInRange(89.1F, 90.0) - MathUtils.getRandomDoubleInRange(0.002F, 1.0E-14));
//                                    float yaw = (float)(((LocalPlayerAccessor)mc.player).getYRotLast() - MathUtils.getRandomDoubleInRange(0.002F, 0.004F));
//                                    ((LocalPlayerAccessor)mc.player).setYRotLast(yaw);
//                                    ((LocalPlayerAccessor)mc.player).setXRotLast(pitch);
//                                    float currentYaw = mc.player.getYRot();
//                                    mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, mc.player.onGround()));
//                                    mc.player
//                                            .connection
//                                            .send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, this.result, MathUtils.getRandomIntInRange(0, 2)));
//                                    if (this.Logging.getCurrentValue()) {
//                                        ChatUtils.addChatMessage("Send");
//                                    }
//
//                                    ((IMixinMinecraft)mc).setSkipTicks((int)this.skips.getCurrentValue());
//                                    mc.hitResult = this.result;
//                                    this.delayVelocity = 0;
//                                    this.result = null;
//                                    this.velocity = false;
//                                    this.nextMovement = true;
//                                }
//                            }
//                        }
//                    }
//
//                    if (packet instanceof ServerboundMovePlayerPacket
//                            && !(packet instanceof ServerboundMovePlayerPacket.Pos)
//                            && !(packet instanceof ServerboundMovePlayerPacket.Rot)
//                            && !(packet instanceof ServerboundMovePlayerPacket.PosRot)
//                            && this.nextMovement) {
//                        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.onGround()));
//                        e.setCancelled(true);
//                        this.nextMovement = false;
//                    }
//                }
//            }
//        } catch (Exception var12) {
//            var12.printStackTrace();
//        }
//    }
//
//    @EventTarget
//    public void onPacketSend(EventPacket e){
//        if (!mode.isCurrentMode("GrimTest")) return;
//        if (e.getType() != EventType.SEND) return;
//
//        Packet<?> packet = e.getPacket();
//        if (packet instanceof ServerboundInteractPacket && needStore && boost.getCurrentValue()){
//            e.setCancelled(true);
//            packets.add((Packet<ClientGamePacketListener>) packet);
//        }
//        if (packet instanceof ServerboundSwingPacket && needStore && boost.getCurrentValue()){
//            e.setCancelled(true);
//            packets.add((Packet<ClientGamePacketListener>) packet);
//        }
//        if (packet instanceof ClientboundSystemChatPacket chatPacket){
//            String sb = chatPacket.content().getString();
//            if (sb.contains("你当前的操作疑似作弊行为")){
//                // tips++;
//            }
//        }
//    }
//
//    @EventTarget
//    public void onMoveInput(EventMoveInput event) {
//        if (mc.player != null && this.mode.isCurrentMode("JumpReset") &&
//                mc.player.onGround() && this.jumpResetTicks == 1) {
//            event.setJump(true);
//            this.jumpResetTicks = 0;
//            if (this.Logging.getCurrentValue()) {
//                ChatUtils.addChatMessage("Jump reset activated");
//            }
//        }
//    }
//
//    /**
//     * Mix mode update handler - attacks target when hurtTime reaches attackTick
//     */
//    private void mixUpdateHandler() {
//        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.getConnection() == null) return;
//        if (mc.player.tickCount <= 20) return;
//        if (mc.player.hurtTime >= attackTick.getCurrentValue()) {
//            HitResult hitResult = mc.hitResult;
//            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
//                EntityHitResult result = (EntityHitResult) hitResult;
//                Entity entity = result.getEntity();
//                if (AntiBots.isBot(entity) || !(entity instanceof Player)) {
//                    if (this.Logging.getCurrentValue()) {
//                        ChatUtils.addChatMessage("Mix: Not player.");
//                    }
//                    return;
//                }
//                mc.gameMode.attack(mc.player, result.getEntity());
//                mc.player.swing(InteractionHand.MAIN_HAND);
//            }
//        }
//    }
//}