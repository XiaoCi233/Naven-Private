package tech.blinkfix.modules.impl.combat;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.impl.*;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.ui.targethud.TargetESP;
import tech.blinkfix.ui.targethud.TargetHUD;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.impl.misc.KillSay;
import tech.blinkfix.modules.impl.misc.Teams;
import tech.blinkfix.modules.impl.move.Blink;
import tech.blinkfix.modules.impl.move.Stuck;
import tech.blinkfix.utils.*;
import tech.blinkfix.utils.rotation.RotationManager;
import tech.blinkfix.utils.rotation.RotationUtils;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.values.impl.ModeValue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ModuleInfo(
        name = "KillAura",
        description = "Automatically attacks entities",
        category = Category.COMBAT
)
public class Aura extends Module {
    private static final float[] targetColorRed = new float[]{0.78431374F, 0.0F, 0.0F, 0.23529412F};
    private static final float[] targetColorGreen = new float[]{0.0F, 0.78431374F, 0.0F, 0.23529412F};
    public static Entity target;
    public static Entity aimingTarget;
    public static List<Entity> targets = new ArrayList<>();
    public static Vector2f rotation;
    BooleanValue targetEsp = ValueBuilder.create(this, "Target ESP").setDefaultBooleanValue(true).build().getBooleanValue();
//    ModeValue targetEspMode = ValueBuilder.create(this, "Target Esp Mode").setModes("Box", "Rectangle").build().getModeValue();
    BooleanValue attackPlayer = ValueBuilder.create(this, "Attack Player").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue attackInvisible = ValueBuilder.create(this, "Attack Invisible").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue attackAnimals = ValueBuilder.create(this, "Attack Animals").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue attackMobs = ValueBuilder.create(this, "Attack Mobs").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue multi = ValueBuilder.create(this, "Multi Attack").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue infSwitch = ValueBuilder.create(this, "Infinity Switch").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue preferBaby = ValueBuilder.create(this, "Prefer Baby").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue moreParticles = ValueBuilder.create(this, "More Particles").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue eat = ValueBuilder.create(this, "Attack When Offhand Using").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue cooldown = ValueBuilder.create(this, "Slowdown Attack").setDefaultBooleanValue(false).build().getBooleanValue();
//    BooleanValue fakeAutoBlock = ValueBuilder.create(this, "AutoBlock").setDefaultBooleanValue(false).build().getBooleanValue();
    FloatValue aimRange = ValueBuilder.create(this, "Aim Range")
            .setDefaultFloatValue(5.0F)
            .setFloatStep(0.1F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(6.0F)
            .build()
            .getFloatValue();
    FloatValue aps = ValueBuilder.create(this, "Attack Per Second")
            .setDefaultFloatValue(10.0F)
            .setFloatStep(1.0F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(20.0F)
            .build()
            .getFloatValue();
    FloatValue switchSize = ValueBuilder.create(this, "Switch Size")
            .setDefaultFloatValue(1.0F)
            .setFloatStep(1.0F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(5.0F)
            .setVisibility(() -> !this.infSwitch.getCurrentValue())
            .build()
            .getFloatValue();
    FloatValue switchAttackTimes = ValueBuilder.create(this, "Switch Delay (Attack Times)")
            .setDefaultFloatValue(1.0F)
            .setFloatStep(1.0F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(10.0F)
            .build()
            .getFloatValue();
    FloatValue fov = ValueBuilder.create(this, "FoV")
            .setDefaultFloatValue(360.0F)
            .setFloatStep(1.0F)
            .setMinFloatValue(10.0F)
            .setMaxFloatValue(360.0F)
            .build()
            .getFloatValue();
    FloatValue hurtTime = ValueBuilder.create(this, "Hurt Time")
            .setDefaultFloatValue(10.0F)
            .setFloatStep(1.0F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(10.0F)
            .build()
            .getFloatValue();
    ModeValue priority = ValueBuilder.create(this, "Priority").setModes("Health", "FoV", "Range", "None").build().getModeValue();
    BooleanValue targetHud = ValueBuilder.create(this, "Target HUD").setDefaultBooleanValue(true).build().getBooleanValue();
    public ModeValue TargetESPStyle = ValueBuilder.create(this, "TargetEsp Style")
            .setVisibility(this.targetEsp::getCurrentValue)
            .setDefaultModeIndex(0)
            .setModes("BlinkFix", "rectangle","Newrectangle", "maoniang")
            .build()
            .getModeValue();
    ModeValue targetHudStyle = ValueBuilder.create(this, "Target HUD Style")
            .setModes("BlinkFix", "BlinkFix-XD", "MoonLight", "Rise", "Lite","Exhibition","LastBlinkFix","Myau","Jello","Client")
            .setDefaultModeIndex(0)
            .setVisibility(() -> Aura.this.targetHud.getCurrentValue())
            .build()
            .getModeValue();
//    public final ModeValue movefix = ValueBuilder.create(this, "Move Fix").setDefaultModeIndex(1).setModes("Strict", "Silent").build().getModeValue();

    RotationUtils.Data lastRotationData;
    RotationUtils.Data rotationData;
    int attackTimes = 0;
    float attacks = 0.0F;
    private int index;
    private static float rotationAngle = 0.0F;
    private static float currentRotationSpeed = 0.7F;
    private static float targetRotationSpeed = 0.7F;
    private static int rotationSpeedTickCounter = 0;
    private static int rotationDirectionTickCounter = 0;
    private static int rotationDirection = 1;

    private static float currentSizeMultiplier = 1.0F;
    private static float targetSizeMultiplier = 1.0F;
    private static int sizeTickCounter = 0;
    private static final Random random = new Random();
    private Vector4f blurMatrix;
    private float lastApsValue = 0.0F;
    private boolean hasShownHighApsWarning = false;

    public double prevCircleStep;
    public double circleStep;

    public static Entity getTarget() {
        return target;
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        this.blurMatrix = null;
        if (target instanceof LivingEntity && this.targetHud.getCurrentValue()) {
            LivingEntity living = (LivingEntity) target;
            e.getStack().pushPose();
            float x = (float) mc.getWindow().getGuiScaledWidth() / 2.0F + 10.0F;
            float y = (float) mc.getWindow().getGuiScaledHeight() / 2.0F + 10.0F;

            this.blurMatrix = TargetHUD.render(e.getGuiGraphics(), living, this.targetHudStyle.getCurrentMode(), x, y);

            e.getStack().popPose();
        }
    }
    @EventTarget
    public void onRender(EventRender e) {
        if (this.targetEsp.getCurrentValue()) {
            TargetESP.render(
                    e,
                    targets,
                    target,
                    this.TargetESPStyle.getCurrentMode()
            );
        }
    }
    private static void renderNitroStyle(EventRender e) {

    }


    boolean isHoldingJump = InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyJump.getDefaultKey().getValue());
    @Override
    public void onEnable() {
        rotation = null;
        this.index = 0;
        target = null;
        aimingTarget = null;
        targets.clear();
        lastApsValue = aps.getCurrentValue();
        hasShownHighApsWarning = false;

        // Check if APS is greater than 15 when enabling Aura
        if (aps.getCurrentValue() > 15.0F) {
            Notification notification = new Notification(NotificationLevel.INFO, "Your CPS value is too high and may result in a ban.", 10000L);
            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            hasShownHighApsWarning = true;
        }
    }

    @Override
    public void onDisable() {
        target = null;
        aimingTarget = null;
        super.onDisable();
    }
    @EventTarget
    public void onRespawn(EventRespawn e) {
        target = null;
        aimingTarget = null;
        this.toggle();
    }

    @EventTarget
    public void onAttackSlowdown(EventAttackSlowdown e) {
        if (mc.player == null) return;

        ItemStack mainHandItem = mc.player.getMainHandItem();
        if (InventoryUtils.isKBItem(mainHandItem)){
            return;
        }
        e.setCancelled(true);
    }

    @EventTarget
    public void onMotion(EventRunTicks event) {
        if (event.getType() == EventType.PRE && mc.player != null) {
            boolean isSwitch = this.switchSize.getCurrentValue() > 1.0F;
            this.setSuffix(this.multi.getCurrentValue() ? "Multi" : (isSwitch ? "Switch" : "Single"));
            if (mc.screen instanceof AbstractContainerScreen
                    || BlinkFix.getInstance().getModuleManager().getModule(Stuck.class).isEnabled()
                    || InventoryUtils.shouldDisableFeatures()) {
                target = null;
                aimingTarget = null;
                this.rotationData = null;
                rotation = null;
                this.lastRotationData = null;
                targets.clear();
                return;
            }



            this.updateAttackTargets();
            aimingTarget = this.shouldPreAim();
            this.lastRotationData = this.rotationData;
            this.rotationData = null;
            if (aimingTarget != null) {
                this.rotationData = RotationUtils.getRotationDataToEntity(aimingTarget);
                if (this.rotationData.getRotation() != null) {
                    rotation = this.rotationData.getRotation();
                } else {
                    rotation = null;
                }
            }

            if (targets.isEmpty()) {
                target = null;
                return;
            }
            if (target == null){
                blocked = false;
            }

            if (this.index > targets.size() - 1) {
                this.index = 0;
            }

            if (targets.size() > 1
                    && ((float)this.attackTimes >= this.switchAttackTimes.getCurrentValue() || this.rotationData != null && this.rotationData.getDistance() > 3.0)) {
                this.attackTimes = 0;

                for (int i = 0; i < targets.size(); i++) {
                    this.index++;
                    if (this.index > targets.size() - 1) {
                        this.index = 0;
                    }

                    Entity nextTarget = targets.get(this.index);
                    RotationUtils.Data data = RotationUtils.getRotationDataToEntity(nextTarget);
                    if (data.getDistance() <= 3.0) {
                        break;
                    }
                }
            }

            if (this.index > targets.size() - 1 || !isSwitch) {
                this.index = 0;
            }

            target = targets.get(this.index);
            Velocity velocity = (Velocity) BlinkFix.getInstance().getModuleManager().getModule(Velocity.class);
            int CurrentAps = Velocity.ReduceCount > 0 ? (int) (10 - velocity.attack.getCurrentValue()) : (int) aps.getCurrentValue();
            this.attacks = this.attacks + CurrentAps / 20.0F;
            if (CurrentAps != lastApsValue) {
                if (CurrentAps > 15.0F && (!hasShownHighApsWarning || lastApsValue <= 15.0F)) {
                    Notification notification = new Notification(NotificationLevel.INFO, "Your CPS value is too high and may result in a ban.", 10000L);
                    BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                    hasShownHighApsWarning = true;
                } else if (CurrentAps <= 12.0F) {
                    hasShownHighApsWarning = false;
                }
                lastApsValue = CurrentAps;
            }
        }
    }
    public boolean blink;
    private boolean blocking;
    private boolean swapped = false;
    private int currentTick;
    @EventTarget
    public void onClick(EventClick e) {
        boolean allowOffhandAttack = this.eat.getCurrentValue();
        boolean mainHandUsing = !mc.player.getUseItem().isEmpty();
        boolean attackReady = !cooldown.getCurrentValue() ||
                mc.player.getAttackStrengthScale(0.5F) >= 1.0F;

        if (mc.screen == null
                && BlinkFix.skipTasks.isEmpty()
                && !NetworkUtils.isServerLag()
                && !BlinkFix.getInstance().getModuleManager().getModule(Blink.class).isEnabled()
                && (allowOffhandAttack || !mainHandUsing)
                && attackReady) {
            while (this.attacks >= 1.0F) {
                this.doAttack();
                this.attacks--;
            }
        }
    }


    public Entity shouldPreAim() {
        Entity target = Aura.target;
        if (target == null) {
            List<Entity> aimTargets = this.getTargets();
            if (!aimTargets.isEmpty()) {
                target = aimTargets.get(0);
            }
        }

        return target;
    }
    public static boolean blocked;
    public void doAttack() {
//        if (fakeAutoBlock.getCurrentValue()){
//            blocked = true;
//        }
        if (!targets.isEmpty()) {
            HitResult hitResult = mc.hitResult;
            if (hitResult.getType() == Type.ENTITY) {
                EntityHitResult result = (EntityHitResult)hitResult;
                if (AntiBots.isBot(result.getEntity())) {
                    ChatUtils.addChatMessage("Attacking Bot!");
                    return;
                }
            }

            if (this.multi.getCurrentValue()) {
                int attacked = 0;

                for (Entity entity : targets) {
                    if (RotationUtils.getDistance(entity, mc.player.getEyePosition(), RotationManager.rotations) < 3.0) {
                        this.attackEntity(entity);
                        if (++attacked >= 2) {
                            break;
                        }
                    }
                }
            } else if (hitResult.getType() == Type.ENTITY) {
                EntityHitResult result = (EntityHitResult)hitResult;
                this.attackEntity(result.getEntity());
            }
        }
    }

    public void updateAttackTargets() {
        targets = this.getTargets();
    }

    public boolean isValidTarget(Entity entity) {
        if (entity == mc.player) {
            return false;
        } else if (entity instanceof LivingEntity living) {
            if (living instanceof BlinkingPlayer) {
                return false;
            } else {
                AntiBots module = (AntiBots)BlinkFix.getInstance().getModuleManager().getModule(AntiBots.class);
                if (module == null || !module.isEnabled() || !AntiBots.isBot(entity) && !AntiBots.isBedWarsBot(entity)) {
                    if (Teams.isSameTeam(living)) {
                        return false;
                    } else if (FriendManager.isFriend(living)) {
                        return false;
                    } else if (living.isDeadOrDying() || living.getHealth() <= 0.0F) {
                        return false;
                    } else if (entity instanceof ArmorStand) {
                        return false;
                    } else if (entity.isInvisible() && !this.attackInvisible.getCurrentValue()) {
                        return false;
                    } else if (entity instanceof Player && (!this.attackPlayer.getCurrentValue())) {
                        return false;
                    } else if (!(entity instanceof Player) || !((double)entity.getBbWidth() < 0.5) && !living.isSleeping()) {
                        if ((entity instanceof Mob || entity instanceof Slime || entity instanceof Bat || entity instanceof AbstractGolem)
                                && !this.attackMobs.getCurrentValue()) {
                            return false;
                        } else if ((entity instanceof Animal || entity instanceof Squid) && !this.attackAnimals.getCurrentValue()) {
                            return false;
                        } else {
                            return entity instanceof Villager && !this.attackAnimals.getCurrentValue() ? false : !(entity instanceof Player) || !entity.isSpectator();
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public boolean isValidAttack(Entity entity) {
        if (!this.isValidTarget(entity)) {
            return false;
        } else if (entity instanceof LivingEntity && (float)((LivingEntity)entity).hurtTime > this.hurtTime.getCurrentValue()) {
            return false;
        } else {
            Vec3 closestPoint = RotationUtils.getClosestPoint(mc.player.getEyePosition(), entity.getBoundingBox());
            double distance = closestPoint.distanceTo(mc.player.getEyePosition());
            if (distance > (double)this.aimRange.getCurrentValue()) {
                return false;
            }

            if (!RotationUtils.inFoV(entity, this.fov.getCurrentValue() / 2.0F)) {
                return false;
            }

            if (!canSeeEntity(entity)) {
                return false;
            }

            return true;
        }
    }

    private boolean canSeeEntity(Entity entity) {
        Vec3 playerEyes = mc.player.getEyePosition();
        Vec3 entityPos = entity.getBoundingBox().getCenter();

        HitResult hitResult = mc.level.clip(new ClipContext(
                playerEyes,
                entityPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.player
        ));

        return hitResult.getType() == Type.MISS ||
                (hitResult.getType() == Type.ENTITY && ((EntityHitResult)hitResult).getEntity() == entity);
    }

    public void attackEntity(Entity entity) {
        if (cooldown.getCurrentValue() && mc.player.getAttackStrengthScale(0.5F) < 1.0F) {
            return;
        }
        this.attackTimes++;
        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();
        mc.player.setYRot(RotationManager.rotations.x);
        mc.player.setXRot(RotationManager.rotations.y);

        if (entity instanceof Player && !AntiBots.isBot(entity)) {
            KillSay.attackedPlayers.add(entity.getName().getString());
        }

        mc.gameMode.attack(mc.player, entity);
        mc.player.swing(InteractionHand.MAIN_HAND);

        if (this.moreParticles.getCurrentValue()) {
            mc.player.magicCrit(entity);
            mc.player.crit(entity);
        }

        mc.player.setYRot(currentYaw);
        mc.player.setXRot(currentPitch);
    }

    private List<Entity> getTargets() {
        Stream<Entity> stream = StreamSupport.<Entity>stream(mc.level.entitiesForRendering().spliterator(), true)
                .filter(entity -> entity instanceof Entity)
                .filter(this::isValidAttack);
        List<Entity> possibleTargets = stream.collect(Collectors.toList());
        if (this.priority.isCurrentMode("Range")) {
            possibleTargets.sort(Comparator.comparingDouble(o -> (double)o.distanceTo(mc.player)));
        } else if (this.priority.isCurrentMode("FoV")) {
            possibleTargets.sort(
                    Comparator.comparingDouble(o -> (double)RotationUtils.getDistanceBetweenAngles(RotationManager.rotations.x, RotationUtils.getRotations(o).x))
            );
        } else if (this.priority.isCurrentMode("Health")) {
            possibleTargets.sort(Comparator.comparingDouble(o -> o instanceof LivingEntity living ? (double)living.getHealth() : 0.0));
        }

        if (this.preferBaby.getCurrentValue() && possibleTargets.stream().anyMatch(entity -> entity instanceof LivingEntity && ((LivingEntity)entity).isBaby())) {
            possibleTargets.removeIf(entity -> !(entity instanceof LivingEntity) || !((LivingEntity)entity).isBaby());
        }

        possibleTargets.sort(Comparator.comparing(o -> o instanceof EndCrystal ? 0 : 1));
        return this.infSwitch.getCurrentValue()
                ? possibleTargets
                : possibleTargets.subList(0, (int)Math.min((float)possibleTargets.size(), this.switchSize.getCurrentValue()));
    }

//    public boolean shouldAutoBlock() {
//        return this.isEnabled() && this.fakeAutoBlock.getCurrentValue() && aimingTarget != null;
//    }
}
