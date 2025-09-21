package com.heypixel.heypixelmod.modules.impl.combat;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.EventAttackSlowdown;
import com.heypixel.heypixelmod.events.impl.EventClick;
import com.heypixel.heypixelmod.events.impl.EventRender;
import com.heypixel.heypixelmod.events.impl.EventRender2D;
import com.heypixel.heypixelmod.events.impl.EventRespawn;
import com.heypixel.heypixelmod.events.impl.EventRunTicks;
import com.heypixel.heypixelmod.events.impl.EventShader;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.modules.impl.misc.KillSay;
import com.heypixel.heypixelmod.modules.impl.misc.Teams;
import com.heypixel.heypixelmod.modules.impl.move.Blink;
import com.heypixel.heypixelmod.modules.impl.move.Stuck;
import com.heypixel.heypixelmod.modules.impl.render.HUD;
import com.heypixel.heypixelmod.utils.*;
import com.heypixel.heypixelmod.utils.renderer.Fonts;
import com.heypixel.heypixelmod.utils.rotation.RotationManager;
import com.heypixel.heypixelmod.utils.rotation.RotationUtils;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import com.heypixel.heypixelmod.values.impl.FloatValue;
import com.heypixel.heypixelmod.values.impl.ModeValue;
import com.heypixel.heypixelmod.ui.targethud.TargetHUD;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Interface;


@ModuleInfo(
        name = "Aura",
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
    private long targetChangeTime = 0;
    private long animationStartTime = 0;
    private float animationProgress = 0f;
    private boolean animationRunning = false;
    private Entity previousTarget = null;
    private int animationDirection = 1; // 1 = 淡入, -1 = 淡出
    BooleanValue targetHud = ValueBuilder.create(this, "Target HUD").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue targetEsp = ValueBuilder.create(this, "Target ESP").setDefaultBooleanValue(true).build().getBooleanValue();
    ModeValue targetespStyle = ValueBuilder.create(this, "Target ESP Style")
            .setModes("Naven", "Rectangle")
            .setDefaultModeIndex(0)
            .setVisibility(() -> Aura.this.targetEsp.getCurrentValue())
            .build()
            .getModeValue();
    BooleanValue attackPlayer = ValueBuilder.create(this, "Attack Player").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue attackInvisible = ValueBuilder.create(this, "Attack Invisible").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue attackAnimals = ValueBuilder.create(this, "Attack Animals").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue attackMobs = ValueBuilder.create(this, "Attack Mobs").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue multi = ValueBuilder.create(this, "Multi Attack").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue infSwitch = ValueBuilder.create(this, "Infinity Switch").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue preferBaby = ValueBuilder.create(this, "Prefer Baby").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue moreParticles = ValueBuilder.create(this, "More Particles").setDefaultBooleanValue(false).build().getBooleanValue();
    public BooleanValue fakeAutoblock = ValueBuilder.create(this, "张铁男Fake Autoblock").setDefaultBooleanValue(false).build().getBooleanValue();
    FloatValue aimRange = ValueBuilder.create(this, "Aim Range").setDefaultFloatValue(5.0F).setFloatStep(0.1F).setMinFloatValue(1.0F).setMaxFloatValue(6.0F).build().getFloatValue();
    FloatValue aps = ValueBuilder.create(this, "Attack Per Second").setDefaultFloatValue(10.0F).setFloatStep(1.0F).setMinFloatValue(1.0F).setMaxFloatValue(20.0F).build().getFloatValue();
    FloatValue switchSize = ValueBuilder.create(this, "Switch Size").setDefaultFloatValue(1.0F).setFloatStep(1.0F).setMinFloatValue(1.0F).setMaxFloatValue(5.0F).setVisibility(() -> !this.infSwitch.getCurrentValue()).build().getFloatValue();
    FloatValue switchAttackTimes = ValueBuilder.create(this, "Switch Delay (Attack Times)").setDefaultFloatValue(1.0F).setFloatStep(1.0F).setMinFloatValue(1.0F).setMaxFloatValue(10.0F).build().getFloatValue();
    FloatValue fov = ValueBuilder.create(this, "FoV").setDefaultFloatValue(360.0F).setFloatStep(1.0F).setMinFloatValue(10.0F).setMaxFloatValue(360.0F).build().getFloatValue();
    FloatValue hurtTime = ValueBuilder.create(this, "Hurt Time").setDefaultFloatValue(10.0F).setFloatStep(1.0F).setMinFloatValue(0.0F).setMaxFloatValue(10.0F).build().getFloatValue();
    ModeValue priority = ValueBuilder.create(this, "Priority").setModes("Health", "FoV", "Range", "None").build().getModeValue();
    ModeValue targetHudStyle = ValueBuilder.create(this, "Target HUD Style")
            .setModes("Naven", "Naven-XD", "MoonLight", "Rise","Lite")
            .setDefaultModeIndex(0)
            .setVisibility(() -> Aura.this.targetHud.getCurrentValue())
            .build()
            .getModeValue();
    RotationUtils.Data lastRotationData;
    RotationUtils.Data rotationData;
    int attackTimes = 0;
    float attacks = 0.0F;
    private int index;
    private Vector4f blurMatrix;
    public static Aura instance;
    private void putRainbowVertex(BufferBuilder buffer, float x, float y, float z, int offset) {
        int rgb = getRainbowColor(offset);
        float r = (rgb >> 16 & 0xFF) / 255.0F;
        float g = (rgb >> 8 & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;
        buffer.vertex(x, y, z).color(r, g, b, 1.0F).endVertex();
    }

    private int getRainbowColor(int offset) {
        float hue = (System.currentTimeMillis() + offset) % 3000L / 3000.0f;
        return Color.HSBtoRGB(hue, 1.0f, 1.0f);
    }
    public Aura() {
        instance = this;
    }

    @EventTarget
    public void onShader(EventShader e) {
        if (this.blurMatrix != null && this.targetHud.getCurrentValue()) {
            RenderUtils.drawRoundedRect(e.getStack(), this.blurMatrix.x(), this.blurMatrix.y(), this.blurMatrix.z(), this.blurMatrix.w(), 3.0F, 1073741824);
        }
    }
    public static Entity getTarget() {
        return target;
    }
    @EventTarget
    public void onRender(EventRender e) {
        if (this.targetespStyle.isCurrentMode("Naven")) {
            if (this.targetEsp.getCurrentValue()) {
                PoseStack stack = e.getPMatrixStack();
                float partialTicks = e.getRenderPartialTicks();
                stack.pushPose();
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glDisable(2929);
                GL11.glDepthMask(false);
                GL11.glEnable(2848);
                RenderSystem.setShader(GameRenderer::getPositionShader);
                RenderUtils.applyRegionalRenderOffset(stack);

                for (Entity entity : targets) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity) entity;
                        float[] color = target == living ? targetColorRed : targetColorGreen;
                        stack.pushPose();
                        RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
                        double motionX = entity.getX() - entity.xo;
                        double motionY = entity.getY() - entity.yo;
                        double motionZ = entity.getZ() - entity.zo;
                        AABB boundingBox = entity.getBoundingBox().move(-motionX, -motionY, -motionZ).move((double) partialTicks * motionX, (double) partialTicks * motionY, (double) partialTicks * motionZ);
                        RenderUtils.drawSolidBox(boundingBox, stack);
                        stack.popPose();
                    }
                }

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glDisable(3042);
                GL11.glEnable(2929);
                GL11.glDepthMask(true);
                GL11.glDisable(2848);
                stack.popPose();
            }
        } else if (this.targetespStyle.isCurrentMode("Rectangle")) {
            if (this.targetEsp.getCurrentValue() && target instanceof LivingEntity living) {
                PoseStack stack = e.getPMatrixStack();
                float partialTicks = e.getRenderPartialTicks();

                stack.pushPose();
                RenderSystem.enableBlend();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);

                RenderUtils.applyRegionalRenderOffset(stack);

                float x = (float) (living.getX() - mc.getEntityRenderDispatcher().camera.getPosition().x);
                float y = (float) (living.getY() + living.getBbHeight() + 0.5 - mc.getEntityRenderDispatcher().camera.getPosition().y);
                float z = (float) (living.getZ() - mc.getEntityRenderDispatcher().camera.getPosition().z);

                stack.pushPose();
                stack.translate(x, y, z);
                stack.scale(0.8f, 0.8f, 0.8f);

                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder buffer = tessellator.getBuilder();

                buffer.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                putRainbowVertex(buffer, -0.5f, 0.0f, -0.5f, 0);
                putRainbowVertex(buffer, 0.5f, 0.0f, -0.5f, 500);
                putRainbowVertex(buffer, 0.5f, 0.0f, 0.5f, 1000);
                putRainbowVertex(buffer, -0.5f, 0.0f, 0.5f, 1500);
                putRainbowVertex(buffer, -0.5f, 0.0f, -0.5f, 2000); // 闭合

                tessellator.end();

                stack.popPose();

                // reset
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
                stack.popPose();
            }
        }
    }
//        private void drawAvatar(EventRender2D e, LivingEntity entity, float x, float y, float size) {
//        boolean isHit = entity.hurtTime > 0;
//        int bgColor = isHit ? new Color(200, 50, 50, 200).getRGB() : new Color(50, 50, 50, 200).getRGB();
//        RenderUtils.drawCircle(e.getStack(), x + size / 2, y + size / 2, size / 2 + 1, bgColor);
//        try {
//            ResourceLocation skinTexture = this.getEntitySkinTexture(entity);
//            RenderSystem.setShaderTexture(0, skinTexture);
//            RenderSystem.setShader(GameRenderer::getPositionTexShader);
//            RenderSystem.enableBlend();
//            Matrix4f matrix = e.getStack().last().pose();
//            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
//            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//            float u1 = 8.0F / 64.0F;
//            float v1 = 8.0F / 64.0F;
//            float u2 = 16.0F / 64.0F;
//            float v2 = 16.0F / 64.0F;
//            bufferBuilder.vertex(matrix, x, y + size, 0).uv(u1, v2).endVertex();
//            bufferBuilder.vertex(matrix, x + size, y + size, 0).uv(u2, v2).endVertex();
//            bufferBuilder.vertex(matrix, x + size, y, 0).uv(u2, v1).endVertex();
//            bufferBuilder.vertex(matrix, x, y, 0).uv(u1, v1).endVertex();
//            BufferUploader.drawWithShader(bufferBuilder.end());
//        } catch (Exception ex) {
//            RenderUtils.drawCircle(e.getStack(), x + size / 2, y + size / 2, size / 2, new Color(150, 150, 150, 255).getRGB());
//        }
//    }
    private ResourceLocation getEntitySkinTexture(LivingEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        if (entity instanceof AbstractClientPlayer) {
            return ((AbstractClientPlayer) entity).getSkinTextureLocation();
        } else if (entity instanceof Player) {
            Player player = (Player) entity;
            return DefaultPlayerSkin.getDefaultSkin(player.getUUID());
        } else {
            return minecraft.player.getSkinTextureLocation();
        }
    }
    private Map<LivingEntity, Float> previousHealthMap = new WeakHashMap<>();
    private Map<LivingEntity, Long> damageTimeMap = new WeakHashMap<>();
    private float getPreviousHealth(LivingEntity entity) {
        return previousHealthMap.getOrDefault(entity, entity.getHealth() / entity.getMaxHealth());
    }
    private void updatePreviousHealth(LivingEntity entity, float currentHealth){
        float previous = previousHealthMap.getOrDefault(entity, currentHealth);
        if (previous > currentHealth) {
            damageTimeMap.put(entity, System.currentTimeMillis());
        }
        long damageTime = damageTimeMap.getOrDefault(entity, 0L);
        if (System.currentTimeMillis() - damageTime < 1000) {
            float newPrevious = Math.max(currentHealth, previous - 0.01F);
            previousHealthMap.put(entity, newPrevious);
        } else {
            previousHealthMap.put(entity, currentHealth);
        }
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        this.blurMatrix = null;
        if (target instanceof LivingEntity && this.targetHud.getCurrentValue()) {
            LivingEntity living = (LivingEntity)target;
            e.getStack().pushPose();
            float x = (float)mc.getWindow().getGuiScaledWidth() / 2.0F + 10.0F;
            float y = (float)mc.getWindow().getGuiScaledHeight() / 2.0F + 10.0F;

            this.blurMatrix = TargetHUD.render(e.getGuiGraphics(), living, this.targetHudStyle.getCurrentMode(), x, y);

            e.getStack().popPose();
        }
    }

    public void onEnable() {
        rotation = null;
        this.index = 0;
        target = null;
        aimingTarget = null;
        targets.clear();
    }

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
        e.setCancelled(true);
    }

    @EventTarget
    public void onMotion(EventRunTicks event) {
        if (event.getType() == EventType.PRE && mc.player != null) {
            if (mc.screen instanceof AbstractContainerScreen || BlinkFix.getInstance().getModuleManager().getModule(Stuck.class).isEnabled() || InventoryUtils.shouldDisableFeatures()) {
                target = null;
                aimingTarget = null;
                this.rotationData = null;
                rotation = null;
                this.lastRotationData = null;
                targets.clear();
                return;
            }

            boolean isSwitch = this.switchSize.getCurrentValue() > 1.0F;
            this.setSuffix(this.multi.getCurrentValue() ? "Multi" : (isSwitch ? "Switch" : "Single"));
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

            if (this.index > targets.size() - 1) {
                this.index = 0;
            }

            if (targets.size() > 1 && ((float)this.attackTimes >= this.switchAttackTimes.getCurrentValue() || this.rotationData != null && this.rotationData.getDistance() > (double)3.0F)) {
                this.attackTimes = 0;

                for(int i = 0; i < targets.size(); ++i) {
                    ++this.index;
                    if (this.index > targets.size() - 1) {
                        this.index = 0;
                    }

                    Entity nextTarget = (Entity)targets.get(this.index);
                    RotationUtils.Data data = RotationUtils.getRotationDataToEntity(nextTarget);
                    if (data.getDistance() < (double)3.0F) {
                        break;
                    }
                }
            }

            if (this.index > targets.size() - 1 || !isSwitch) {
                this.index = 0;
            }

            target = (Entity)targets.get(this.index);
            this.attacks += this.aps.getCurrentValue() / 20.0F;
        }

    }

    @EventTarget
    public void onClick(EventClick e) {
        if (mc.player.getUseItem().isEmpty() && mc.screen == null && BlinkFix.skipTasks.isEmpty() && !NetworkUtils.isServerLag() && !BlinkFix.getInstance().getModuleManager().getModule(Blink.class).isEnabled()) {
            while(this.attacks >= 1.0F) {
                this.doAttack();
                --this.attacks;
            }
        } else {
            this.attacks = 0.0F;
        }

    }

    public boolean shouldAutoBlock() {
        return this.isEnabled() && this.fakeAutoblock.getCurrentValue() && aimingTarget != null;
    }

    public Entity shouldPreAim() {
        Entity target = Aura.target;
        if (target == null) {
            List<Entity> aimTargets = this.getTargets();
            if (!aimTargets.isEmpty()) {
                target = (Entity)aimTargets.get(0);
            }
        }

        return target;
    }

    public void doAttack() {
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

                for(Entity entity : targets) {
                    if (RotationUtils.getDistance(entity, mc.player.getEyePosition(), RotationManager.rotations) < (double)3.0F) {
                        this.attackEntity(entity);
                        ++attacked;
                        if (attacked >= 2) {
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
        } else if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            if (living instanceof BlinkingPlayer) {
                return false;
            } else {
                AntiBots module = (AntiBots)BlinkFix.getInstance().getModuleManager().getModule(AntiBots.class);
                if (module == null || !module.isEnabled() || !AntiBots.isBot(entity) && !AntiBots.isBedWarsBot(entity)) {
                    if (Teams.isSameTeam(living)) {
                        return false;
                    } else if (FriendManager.isFriend((Entity)living)) {
                        return false;
                    } else if (!living.isDeadOrDying() && !(living.getHealth() <= 0.0F)) {
                        if (entity instanceof ArmorStand) {
                            return false;
                        } else if (entity.isInvisible() && !this.attackInvisible.getCurrentValue()) {
                            return false;
                        } else if (entity instanceof Player && !this.attackPlayer.getCurrentValue()) {
                            return false;
                        } else if (!(entity instanceof Player) || !((double)entity.getBbWidth() < (double)0.5F) && !living.isSleeping()) {
                            if ((entity instanceof Mob || entity instanceof Slime || entity instanceof Bat || entity instanceof AbstractGolem) && !this.attackMobs.getCurrentValue()) {
                                return false;
                            } else if ((entity instanceof Animal || entity instanceof Squid) && !this.attackAnimals.getCurrentValue()) {
                                return false;
                            } else if (entity instanceof Villager && !this.attackAnimals.getCurrentValue()) {
                                return false;
                            } else {
                                return !(entity instanceof Player) || !entity.isSpectator();
                            }
                        } else {
                            return false;
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
            return closestPoint.distanceTo(mc.player.getEyePosition()) > (double)this.aimRange.getCurrentValue() ? false : RotationUtils.inFoV(entity, this.fov.getCurrentValue() / 2.0F);
        }
    }

    public void attackEntity(Entity entity) {
        ++this.attackTimes;
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
        Stream<Entity> stream = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), true).filter((entity) -> entity instanceof Entity).filter(this::isValidAttack);
        List<Entity> possibleTargets = (List)stream.collect(Collectors.toList());
        if (this.priority.isCurrentMode("Range")) {
            possibleTargets.sort(Comparator.comparingDouble((o) -> (double)o.distanceTo(mc.player)));
        } else if (this.priority.isCurrentMode("FoV")) {
            possibleTargets.sort(Comparator.comparingDouble((o) -> (double)RotationUtils.getDistanceBetweenAngles(RotationManager.rotations.x, RotationUtils.getRotations(o).x)));
        } else if (this.priority.isCurrentMode("Health")) {
            possibleTargets.sort(Comparator.comparingDouble((o) -> {
                if (o instanceof LivingEntity living) {
                    return (double)living.getHealth();
                } else {
                    return (double)0.0F;
                }
            }));
        }

        if (this.preferBaby.getCurrentValue() && possibleTargets.stream().anyMatch((entity) -> entity instanceof LivingEntity && ((LivingEntity)entity).isBaby())) {
            possibleTargets.removeIf((entity) -> !(entity instanceof LivingEntity) || !((LivingEntity)entity).isBaby());
        }

        possibleTargets.sort(Comparator.comparing((o) -> o instanceof EndCrystal ? 0 : 1));
        return this.infSwitch.getCurrentValue() ? possibleTargets : possibleTargets.subList(0, (int)Math.min((float)possibleTargets.size(), this.switchSize.getCurrentValue()));
    }
    private int applyAlpha(int color, float alpha) {
        int a = (int)(((color >> 24) & 0xFF) * alpha);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    private float easeOutCubic(float x) {
        return (float) (1 - Math.pow(1 - x, 3));
    }
    private float easeOutElastic(float x) {
        float c4 = (float) ((2 * Math.PI) / 3);
        return x == 0 ? 0 : x == 1 ? 1 : (float) (Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1);
    }

    private float easeInBack(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return c3 * x * x * x - c1 * x * x;
    }
}