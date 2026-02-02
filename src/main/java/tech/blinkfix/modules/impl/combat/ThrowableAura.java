package tech.blinkfix.modules.impl.combat;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventClick;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.impl.misc.Teams;
import tech.blinkfix.modules.impl.move.Blink;
import tech.blinkfix.modules.impl.move.Scaffold;
import tech.blinkfix.modules.impl.move.Stuck;
import tech.blinkfix.utils.FriendManager;
import tech.blinkfix.utils.NetworkUtils;
import tech.blinkfix.utils.PacketUtils;
import tech.blinkfix.utils.TimeHelper;
import tech.blinkfix.utils.rotation.RotationUtils;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.values.impl.ModeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.util.Comparator;
import java.util.Optional;

@ModuleInfo(
        name = "ThrowableAura",
        description = "Automatically throw snowballs and eggs.",
        category = Category.COMBAT
)
public class ThrowableAura extends Module {
    private final Minecraft mc = Minecraft.getInstance();

    private final ModeValue mode = ValueBuilder.create(this, "Mode")
            .setDefaultModeIndex(0)
            .setModes("Packet", "Legit")
            .build()
            .getModeValue();

    private final FloatValue minDistance = ValueBuilder.create(this, "Min Distance")
            .setDefaultFloatValue(5.0F)
            .setFloatStep(1.0F)
            .setMinFloatValue(3.0F)
            .setMaxFloatValue(30.0F)
            .build()
            .getFloatValue();

    private final FloatValue maxDistance = ValueBuilder.create(this, "Max Distance")
            .setDefaultFloatValue(10.0F)
            .setFloatStep(1.0F)
            .setMinFloatValue(3.0F)
            .setMaxFloatValue(30.0F)
            .build()
            .getFloatValue();

    private final FloatValue delay = ValueBuilder.create(this, "Delay")
            .setDefaultFloatValue(500.0F)
            .setFloatStep(50.0F)
            .setMinFloatValue(50.0F)
            .setMaxFloatValue(2000.0F)
            .build()
            .getFloatValue();

    private final FloatValue fov = ValueBuilder.create(this, "Fov")
            .setDefaultFloatValue(180.0F)
            .setMaxFloatValue(360.0F)
            .setFloatStep(10.0F)
            .build()
            .getFloatValue();

    public final BooleanValue useOffhand = ValueBuilder.create(this, "Use Offhand")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    private final TimeHelper timer = new TimeHelper();
    public Vector2f rotation;
    public int rotationSet;
    private int swapBack = -1;
    private boolean shouldSwapBack = false;
    private int throwableHotbar = -1;

    @EventTarget
    public void onTick(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            if (shouldSwapBack && swapBack != -1 && mc.player != null) {
                mc.player.getInventory().selected = swapBack;
                shouldSwapBack = false;
                swapBack = -1;
                return;
            }

            if (mc.player == null || mc.level == null) return;

            // Check if other modules are enabled
            if (BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled() ||
                    BlinkFix.getInstance().getModuleManager().getModule(Stuck.class).isEnabled()) {
                rotationSet = 0;
                return;
            }

            // Check DelayTrack (Backtrack)
            DelayTrack delayTrackModule = (DelayTrack) BlinkFix.getInstance().getModuleManager().getModule(DelayTrack.class);
            if (delayTrackModule != null && delayTrackModule.isEnabled()) {
                rotationSet = 0;
                return;
            }
            
            // Check BackTrack module
            BackTrack backTrackModule = (BackTrack) BlinkFix.getInstance().getModuleManager().getModule(BackTrack.class);
            if (backTrackModule != null && backTrackModule.isEnabled()) {
                rotationSet = 0;
                return;
            }

            Optional<Player> targetOpt = getTarget();
            if (!targetOpt.isPresent() || !RotationUtils.inFoV(targetOpt.get(), fov.getCurrentValue())) {
                rotationSet = 0;
                return;
            }

            rotation = null;
            throwableHotbar = -1;

            // Check offhand first if enabled
            if (useOffhand.getCurrentValue() && mc.player.getOffhandItem().getItem() == Items.SNOWBALL ||
                    useOffhand.getCurrentValue() && mc.player.getOffhandItem().getItem() == Items.EGG) {
                // Offhand has throwable, use it
                if (--rotationSet == 0) {
                    throwItem(targetOpt.get(), InteractionHand.OFF_HAND);
                    shouldSwapBack = false;
                } else if (timer.delay(delay.getCurrentValue())) {
                    rotation = getRotationToEntity(targetOpt.get());
                    timer.reset();
                }
            return;
        }

            // Check main hand
            if (mc.player.getMainHandItem().getItem() == Items.SNOWBALL ||
                    mc.player.getMainHandItem().getItem() == Items.EGG) {
                if (--rotationSet == 0) {
                    throwItem(targetOpt.get(), InteractionHand.MAIN_HAND);
                    shouldSwapBack = false;
                } else if (timer.delay(delay.getCurrentValue())) {
                    rotation = getRotationToEntity(targetOpt.get());
                    timer.reset();
                }
            return;
        }

            // Find throwable in hotbar
            for (int hotbar = 0; hotbar < 9; hotbar++) {
                ItemStack stack = mc.player.getInventory().getItem(hotbar);
                if (!stack.isEmpty() && (stack.getItem() == Items.EGG || stack.getItem() == Items.SNOWBALL)) {
                    throwableHotbar = hotbar;
                    break;
                }
            }

            if (throwableHotbar != -1) {
                if (--rotationSet == 0) {
                    int originalHotbar = mc.player.getInventory().selected;
                    boolean shouldSwap = originalHotbar != throwableHotbar;

                    if (shouldSwap) {
                        mc.player.getInventory().selected = throwableHotbar;
                        swapBack = originalHotbar;
                    }

                    throwItem(targetOpt.get(), InteractionHand.MAIN_HAND);
                    shouldSwapBack = shouldSwap;
                } else if (targetOpt.isPresent() && timer.delay(delay.getCurrentValue())) {
                    // Check if player is holding incompatible items
                    ItemStack heldItem = mc.player.getMainHandItem();
                    if (heldItem.isEmpty() || 
                        (!(heldItem.getItem() instanceof net.minecraft.world.item.EnderpearlItem) &&
                         !(heldItem.getItem() instanceof net.minecraft.world.item.BowItem) &&
                         !heldItem.getItem().isEdible() &&
                         !(heldItem.getItem() instanceof net.minecraft.world.item.PotionItem))) {
                        
                        Blink blink = (Blink) BlinkFix.getInstance().getModuleManager().getModule(Blink.class);
                        Stuck stuck = (Stuck) BlinkFix.getInstance().getModuleManager().getModule(Stuck.class);
                        
                        if ((blink == null || !blink.isEnabled()) && 
                            (stuck == null || !stuck.isEnabled())) {
                            rotation = getRotationToEntity(targetOpt.get());
                            timer.reset();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (shouldSwapBack && swapBack != -1 && mc.player != null) {
            mc.player.getInventory().selected = swapBack;
            shouldSwapBack = false;
            swapBack = -1;
        }
        super.onDisable();
    }

    @EventTarget
    public void asb(EventClick e) {
        // Empty handler as in original
    }

    private void throwItem(Player target, InteractionHand hand) {
        if (mc.player == null || mc.gameMode == null) return;

        Vector2f rotations = getRotationToEntity(target);
        if (rotations == null) return;

        float originalYaw = mc.player.getYRot();
        float originalPitch = mc.player.getXRot();

        // Set rotation
        mc.player.setYRot(rotations.x);
        mc.player.setXRot(rotations.y);

        if (mode.isCurrentMode("Packet")) {
            // Packet mode: send packets directly
            if (mc.getConnection() != null) {
                PacketUtils.sendSequencedPacket(id -> new ServerboundUseItemPacket(hand, id));
                NetworkUtils.sendSwingPacket(hand);
            }
        } else {
            // Legit mode: use gameMode
            mc.player.swing(hand);
            mc.gameMode.useItem(mc.player, hand);
        }

        // Restore rotation
        mc.player.setYRot(originalYaw);
        mc.player.setXRot(originalPitch);
    }

    private Vector2f getRotationToEntity(Entity target) {
        if (target == null || mc.player == null) return null;

        // Get predicted position
        double distanceToEnt = mc.player.distanceTo(target);
        Vec3 targetPos = target.position();
        Vec3 lastPos = new Vec3(target.xOld, target.yOld, target.zOld);
        Vec3 velocity = targetPos.subtract(lastPos);
        
        double predictX = targetPos.x + velocity.x * (distanceToEnt * 0.8);
        double predictY = targetPos.y + velocity.y * (distanceToEnt * 0.8);
        double predictZ = targetPos.z + velocity.z * (distanceToEnt * 0.8);

        Vec3 playerEyePos = mc.player.getEyePosition(1.0F);
        double x = predictX - playerEyePos.x;
        double h = predictY + 1.2 - (playerEyePos.y);
        double z = predictZ - playerEyePos.z;

        double h1 = Math.sqrt(x * x + z * z);

        float yaw = (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        
        // Calculate trajectory angle for throwable (snowball/egg)
        // Using simplified physics: v0 = 0.6, g = 0.03 (approximate for snowball)
        float pitch = calculateTrajectoryAngle((float) h1, (float) h, 0.6f, 0.03f);

        return new Vector2f(yaw, pitch);
    }

    private float calculateTrajectoryAngle(float horizontalDist, float verticalDist, float velocity, float gravity) {
        // Simplified trajectory calculation
        // Using quadratic formula to solve for pitch angle
        double v2 = velocity * velocity;
        double discriminant = v2 * v2 - gravity * (gravity * horizontalDist * horizontalDist + 2 * verticalDist * v2);
        
        if (discriminant < 0) {
            // No solution, use direct angle
            return (float) -Math.toDegrees(Math.atan2(verticalDist, horizontalDist));
        }
        
        double sqrtDisc = Math.sqrt(discriminant);
        double tanAngle = (v2 - sqrtDisc) / (gravity * horizontalDist);
        
        return (float) -Math.toDegrees(Math.atan(tanAngle));
    }

    private Optional<Player> getTarget() {
        if (mc.player == null || mc.level == null) return Optional.empty();

        return mc.level.players().stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .filter(e -> e != mc.player)
                .filter(e -> !Teams.isSameTeam(e))
                .filter(e -> !FriendManager.isFriend(e))
                .filter(e -> !AntiBots.isBot(e))
                .filter(e -> !e.isInvisible())
                .filter(e -> {
                    double horizontalDist = Math.sqrt(
                            Math.pow(e.getX() - mc.player.getX(), 2) +
                            Math.pow(e.getZ() - mc.player.getZ(), 2)
                    );
                    return horizontalDist <= maxDistance.getCurrentValue() && 
                           horizontalDist >= minDistance.getCurrentValue();
                })
                .filter(e -> {
                    // Check if entity can be seen (simplified check)
                    Vec3 eyePos = mc.player.getEyePosition(1.0F);
                    Vec3 targetPos = e.getEyePosition(1.0F);
                    net.minecraft.world.phys.BlockHitResult result = mc.level.clip(
                            new net.minecraft.world.level.ClipContext(
                                    eyePos, targetPos,
                                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                                    mc.player
                            )
                    );
                    return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS;
                })
                .min(Comparator.comparingDouble(e -> mc.player.distanceToSqr(e)));
    }
}
