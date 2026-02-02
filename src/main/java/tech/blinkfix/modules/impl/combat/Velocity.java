package tech.blinkfix.modules.impl.combat;

import tech.blinkfix.BlinkFix;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventHandlePacket;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.events.impl.EventRespawn;
import tech.blinkfix.events.impl.EventRunTicks;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.impl.move.Scaffold;
import tech.blinkfix.utils.ChatUtils;
import tech.blinkfix.utils.TimeHelper;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.values.impl.ModeValue;

import java.util.concurrent.LinkedBlockingQueue;

@ModuleInfo(name = "Velocity"
        ,description = "Reduces knockback."
        ,category = Category.COMBAT)
public class Velocity extends Module {
    private final LinkedBlockingQueue<Packet<ClientGamePacketListener>> packets = new LinkedBlockingQueue<>();
    ModeValue mode = ValueBuilder.create(this, "Mode").setModes("Grim Reduce", "Jump Reset").build().getModeValue();
    public BooleanValue log = ValueBuilder.create(this, "Logging").setDefaultBooleanValue(false).build().getBooleanValue();
    FloatValue reduceCount  = ValueBuilder.create(this,"Reduce Count").setDefaultFloatValue(4F)
            .setFloatStep(1F)
            .setMinFloatValue(1F)
            .setMaxFloatValue(10.0F)
            .build()
            .getFloatValue();
    FloatValue attack  = ValueBuilder.create(this,"Attack Count").setDefaultFloatValue(2F)
            .setFloatStep(1F)
            .setMinFloatValue(1F)
            .setMaxFloatValue(5.0F)
            .build()
            .getFloatValue();
    BooleanValue Lag = ValueBuilder.create(this,"Lag Cooldown").setDefaultBooleanValue(true).build().getBooleanValue();
    public boolean velocityInput = false;
    private TimeHelper timeHelper = new TimeHelper();

    public void reset() {
        if (mc.getConnection() != null) {
            attackCount = 0;
            ReduceCount = 0;
            timeHelper.reset();
            velocityInput = false;
            ProcessPacket();
        }
    }
    private boolean S08;

    public static int ReduceCount = 0;

    private TimeHelper PacketInBoundTimer = new TimeHelper();
    public static boolean kbPacket;

    public int attackCount;
    @Override
    public void onEnable() {
        this.reset();
        timeHelper.reset();
    }

    @Override
    public void onDisable() {
        this.reset();
        timeHelper.reset();
    }

    private void log(String message) {
        if (this.log.getCurrentValue()) {
            ChatUtils.addChatMessage(message);
        }
    }
    private void ProcessPacket(){
        try {
            while (!packets.isEmpty()) {
                Packet<ClientGamePacketListener> packet = packets.poll();
                if (packet != null) {
                    ChatUtils.addChatMessage("Process Packet : " + packets.size());
                    packet.handle(mc.player.connection);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventTarget
    public void onWorld(EventRespawn eventRespawn) {
        this.reset();
    }
    public static boolean Attacking;
    private boolean needStore;
    @EventTarget
    public void onTick(EventRunTicks eventRunTicks) {
        if (mc.player == null)return;
        this.setSuffix(mode.getCurrentMode());
        if (S08 && timeHelper.delay(1000)) {
            S08 = false;
        }
        // if (!Naven.getInstance().getModuleManager().getModule(Backtrack.class).isEnabled()) {

        //}
        if (mc.getConnection() != null && mc.gameMode != null && mc.player.hurtTime != 0) {
            if (mode.isCurrentMode("Grim Reduce") && Aura.target != null && kbPacket && attackCount > 0 && velocityInput && !BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled()) {
                if (ReduceCount > reduceCount.getCurrentValue()) return;
                if (!check()) {
                    if (Lag.getCurrentValue() && S08) return;
                    if (Aura.target != null && mc.player.distanceTo(Aura.target) <= 3.1) {
                        for (int i = 0; i < this.attackCount; i++) {
                            mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(Aura.target, false));
                            Attacking = true;
                            mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(0.6, 1, 0.6));
                            ChatUtils.addChatMessage(String.valueOf(attackCount));
                            ReduceCount++;
                            mc.player.setSprinting(false);
                            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                        }
                        this.attackCount = 0;
                    }
                }
                if (check()){
                    log("Ignore: Player is in Web\\Water\\Lava\\Ladder\\UseItem!");
                }
                Attacking = false;
            }
        }
        if (mode.isCurrentMode("Jump Reset")&& mc.player != null) {
            if (mc.player.hurtTime > 9 && mc.player.onGround() && !mc.player.isInWater() && !mc.player.isInLava() && !mc.player.isOnFire()) {
                mc.player.jumpFromGround();
            }
        }
        if (ReduceCount > reduceCount.getCurrentValue()){
            ReduceCount = 0;
        }
    }
    public static int direction = 1;
    @EventTarget
    public void onMotion(EventMotion e) {
        if (mc.player != null && e.getType() == EventType.PRE) {

        }
    }
    private boolean RayTraceTarget(Entity target) {
        Vec3 playerEyes = mc.player.getEyePosition(1.0F);
        Vec3 targetEyes = target.getEyePosition(1.0F);
        BlockHitResult result = mc.level.clip(new ClipContext(
                playerEyes,
                targetEyes,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.player
        ));
        return result.getType() == HitResult.Type.MISS ||
                result.getLocation().distanceTo(playerEyes) > playerEyes.distanceTo(targetEyes);
    }

    @EventTarget
    public void onPacket(EventHandlePacket e) {
        Packet<?> packet = e.getPacket();
        if (mc.player != null && mc.getConnection() != null && mc.gameMode != null && !mc.player.isUsingItem()) {
            if (mode.isCurrentMode("Grim Reduce")) {
                if (packet instanceof ClientboundDamageEventPacket) {
                    ClientboundDamageEventPacket damagePacket = (ClientboundDamageEventPacket) packet;
                    velocityInput = damagePacket.entityId() == mc.player.getId();
                }
                if (packet instanceof ClientboundPlayerPositionPacket && Aura.target != null) {
                    if (Lag.getCurrentValue()) {
                        S08 = true;
                    }
                    log("Velocity : Receive S08!");
                }
                if (packet instanceof ClientboundSetEntityMotionPacket velocityPacket) {
                    if (velocityPacket.getId() != mc.player.getId()) return;
                    kbPacket = true;
                    double X = velocityPacket.getXa() / 8000d;
                    double Z = velocityPacket.getZa() / 8000d;
                    double speed = Math.sqrt(X * X + Z * Z);
                    if (velocityInput && speed != 0.00) {
                        ChatUtils.addChatMessage("Speed : " + String.format("%.2f", speed));
                    }
                    if (speed < 0.04){
                        log("Ignore: Speed is too low!");
                        attackCount = 0;
                    } else {
                        attackCount = (int) attack.getCurrentValue();
                    }
                }
            }
        }
    }

    private boolean check(){
        return mc.player.isInLava() || mc.player.isInWater() || mc.player.isDeadOrDying() || mc.player.isUsingItem();
    }
}