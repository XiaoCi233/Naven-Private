package com.heypixel.heypixelmod.modules.impl.combat;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.EventRunTicks;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import com.heypixel.heypixelmod.values.impl.FloatValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@ModuleInfo(
        name = "ThrowableAura",
        description = "AutoThrow uses items to KB nearby players within FoV, ignoring teammates and bots",
        category = Category.COMBAT
)
public class ThrowableAura extends Module {
    private final Minecraft mc = Minecraft.getInstance();

    // Float values
    private final FloatValue detectionRange = ValueBuilder.create(this, "Detection Range")
            .setDefaultFloatValue(8.0F)
            .setFloatStep(0.1F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(15.0F)
            .build()
            .getFloatValue();

    private final FloatValue throwRange = ValueBuilder.create(this, "Throw Range")
            .setDefaultFloatValue(6.0F)
            .setFloatStep(0.1F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(12.0F)
            .build()
            .getFloatValue();

    private final FloatValue delay = ValueBuilder.create(this, "Delay")
            .setDefaultFloatValue(0.0F)
            .setFloatStep(1.0F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(100.0F)
            .build()
            .getFloatValue();

    // Boolean values
    public final BooleanValue ignoreTeammates = ValueBuilder.create(this, "Ignore Teammates")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final BooleanValue targetPlayers = ValueBuilder.create(this, "Target Players")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final BooleanValue targetMonsters = ValueBuilder.create(this, "Target Monsters")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final BooleanValue autoSwitch = ValueBuilder.create(this, "Auto Switch")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final BooleanValue predictiveAiming = ValueBuilder.create(this, "Predictive Aiming")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final BooleanValue useOffhand = ValueBuilder.create(this, "Use Offhand")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final BooleanValue checkOtherModules = ValueBuilder.create(this, "Check Other Modules")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
    public final BooleanValue botCheck = ValueBuilder.create(this, "Bot Check")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    private int tickCounter = 0;
    private int lastSlot = -1;

    @Override
    public void onEnable() {
        tickCounter = 0;
        lastSlot = -1;
    }

    @Override
    public void onDisable() {
        if (autoSwitch.getCurrentValue() && lastSlot != -1 && mc.player != null) {
            mc.player.getInventory().selected = lastSlot;
        }
    }

    private boolean shouldWork() {
        if (!checkOtherModules.getCurrentValue()) {
            return true;
        }

        try {
            Aura auraModule = (Aura) com.heypixel.heypixelmod.BlinkFix.getInstance().getModuleManager().getModule(Aura.class);
            if (auraModule != null && auraModule.isEnabled() && Aura.target != null) {
                return false;
            }

            DelayTrack backTrackModule = (DelayTrack) com.heypixel.heypixelmod.BlinkFix.getInstance().getModuleManager().getModule(DelayTrack.class);
            if (backTrackModule != null && backTrackModule.isEnabled() && backTrackModule.btwork) {
                return false;
            }
        } catch (Exception e) {
            return true;
        }

        return true;
    }

    @EventTarget
    public void onTick(EventRunTicks event) {
        if (event.getType() != EventType.POST || mc.player == null || mc.level == null) return;

        if (!shouldWork()) {
            return;
        }

        LocalPlayer player = mc.player;
        if (tickCounter < delay.getCurrentValue()) {
            tickCounter++;
            return;
        }
        tickCounter = 0;

        Entity target = findTarget(player);
        if (target == null) return;
        if (player.distanceTo(target) > throwRange.getCurrentValue()) return;

        InteractionHand hand = findSnowballHand();
        if (hand == null) return;

        if (hand == InteractionHand.MAIN_HAND && autoSwitch.getCurrentValue()) {
            int snowballSlot = findSnowballSlot();
            if (snowballSlot == -1) return;

            if (lastSlot == -1) {
                lastSlot = player.getInventory().selected;
            }
            player.getInventory().selected = snowballSlot;
        }

        throwSnowballWithAnimation(player, target, hand);
    }

    private Entity findTarget(LocalPlayer player) {
        float range = detectionRange.getCurrentValue();
        AABB detectionBox = new AABB(
                player.getX() - range,
                player.getY() - range,
                player.getZ() - range,
                player.getX() + range,
                player.getY() + range,
                player.getZ() + range
        );

        List<Entity> allEntities = mc.level.getEntities(null, detectionBox);

        return allEntities.stream()
                .filter(entity -> isValidTarget(player, entity))
                .min((a, b) -> Double.compare(player.distanceToSqr(a), player.distanceToSqr(b)))
                .orElse(null);
    }

    private boolean isValidTarget(LocalPlayer player, Entity target) {
        if (target == player || !target.isAlive()) return false;
        if (player.distanceTo(target) > detectionRange.getCurrentValue()) return false;

        if (botCheck.getCurrentValue() && isBot(target)) {
            return false;
        }

        if (target instanceof Player) {
            Player playerTarget = (Player) target;
            return targetPlayers.getCurrentValue() && (!ignoreTeammates.getCurrentValue() || !isTeammate(player, playerTarget));
        } else if (target instanceof Monster) {
            return targetMonsters.getCurrentValue();
        }
        return false;
    }
    private boolean isBot(Entity entity) {
        try {
            AntiBots antiBotsModule = (AntiBots) BlinkFix.getInstance().getModuleManager().getModule(AntiBots.class);
            if (antiBotsModule != null && antiBotsModule.isEnabled()) {
                return AntiBots.isBotEntity(entity);
            }
        } catch (Exception e) {
        }
        return false;
    }

    private boolean isTeammate(LocalPlayer player, Player target) {
        return player.getTeam() != null &&
                target.getTeam() != null &&
                player.getTeam().getName().equals(target.getTeam().getName());
    }

    private InteractionHand findSnowballHand() {
        if (mc.player == null) return null;

        if (useOffhand.getCurrentValue() && mc.player.getOffhandItem().getItem() == Items.SNOWBALL) {
            return InteractionHand.OFF_HAND;
        }

        if (mc.player.getMainHandItem().getItem() == Items.SNOWBALL) {
            return InteractionHand.MAIN_HAND;
        }

        if (autoSwitch.getCurrentValue() && findSnowballSlot() != -1) {
            return InteractionHand.MAIN_HAND;
        }

        return null;
    }

    private int findSnowballSlot() {
        LocalPlayer player = mc.player;
        if (player == null) return -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.SNOWBALL) {
                return i;
            }
        }
        return -1;
    }

    private void throwSnowballWithAnimation(LocalPlayer player, Entity target, InteractionHand hand) {
        if (mc.gameMode == null || player == null) return;
        float originalYaw = player.getYRot();
        float originalPitch = player.getXRot();
        aimAtTarget(player, target);
        player.swing(hand);
        mc.gameMode.useItem(player, hand);
        player.setYRot(originalYaw);
        player.setXRot(originalPitch);
    }

    private void aimAtTarget(LocalPlayer player, Entity target) {
        if (player == null || target == null) return;

        Vec3 targetPos = predictiveAiming.getCurrentValue() ? calculatePredictedPosition(target) : target.getEyePosition(1.0F);
        Vec3 playerPos = player.getEyePosition(1.0F);

        double dx = targetPos.x - playerPos.x;
        double dy = targetPos.y - playerPos.y;
        double dz = targetPos.z - playerPos.z;

        double yaw = Math.atan2(dz, dx) * 180.0 / Math.PI - 90.0;
        double distance = Math.sqrt(dx * dx + dz * dz);
        double pitch = Math.atan2(dy, distance) * 180.0 / Math.PI;

        player.setYRot((float) yaw);
        player.setXRot((float) -pitch);
    }

    private Vec3 calculatePredictedPosition(Entity target) {
        Vec3 currentPos = target.getEyePosition(1.0F);
        Vec3 velocity = target.getDeltaMovement();
        double distance = mc.player.distanceTo(target);
        double timeToTarget = distance / 1.5;

        return new Vec3(
                currentPos.x + velocity.x * timeToTarget,
                currentPos.y + velocity.y * timeToTarget,
                currentPos.z + velocity.z * timeToTarget
        );
    }
}