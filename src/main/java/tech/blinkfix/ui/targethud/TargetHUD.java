package tech.blinkfix.ui.targethud;

import tech.blinkfix.modules.impl.render.HUD;
import tech.blinkfix.utils.RenderUtils;
import tech.blinkfix.utils.StencilUtils;
import tech.blinkfix.utils.renderer.Fonts;
import tech.blinkfix.utils.renderer.HealthBarAnimator;
import tech.blinkfix.utils.renderer.HealthParticle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector4f;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import tech.blinkfix.utils.TimeHelper;
import tech.blinkfix.utils.SmoothAnimationTimer;
import net.minecraft.world.item.Items;
import net.minecraft.client.gui.screens.ChatScreen;

public class TargetHUD {
    private static Entity previousTarget;
    private static boolean animationRunning;
    private static long animationStartTime;
    private static int animationDirection;
    private static Map<UUID, Float> previousHealthMap = new WeakHashMap<UUID, Float>();
    private Map<LivingEntity, Long> damageTimeMap = new WeakHashMap<>();
    private static float animationProgress;
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Map<UUID, HealthBarAnimator> healthAnimators = new HashMap<>();
    private static final Map<UUID, List<HealthParticle>> playerParticles = new HashMap<>();
    private static final Map<UUID, Float> lastHealth = new HashMap<>();
    private static final Random random = new Random();
    
    // LastNaven mode state
    private static Player lastNavenTarget;
    private static final TimeHelper lastNavenTimer = new TimeHelper();
    private static final SmoothAnimationTimer lastNavenTargetAnimation = new SmoothAnimationTimer(0, 0.3f);
    private static final SmoothAnimationTimer lastNavenHealthAnimation = new SmoothAnimationTimer(0, 0.2f);
    private static ItemStack[] lastNavenInventorySnapshot = null;
    private static int lastNavenGappleCount = 0;
    private static Player lastNavenTargetPlayer = null;
    
    // Jello style state
    private static final Map<UUID, SmoothAnimationTimer> jelloHealthBars = new HashMap<>();
    private static final Map<UUID, SmoothAnimationTimer> jelloHeadSizes = new HashMap<>();
    private static final Map<UUID, SmoothAnimationTimer> jelloHeadXYs = new HashMap<>();
    
    // MCP style state
    private static final Map<UUID, SmoothAnimationTimer> mcpAnimations = new HashMap<>();
    private static final Map<UUID, Float> mcpHealthAnimations = new HashMap<>();

    public static Vector4f render(GuiGraphics graphics, LivingEntity living, String style, float x, float y) {
        if ("Naven".equals(style)) {
            return renderNavenStyle(graphics, living, x, y);
        } else if ("Naven-XD".equals(style)) {
            return renderNewStyle(graphics, living, x, y);
        } else if ("MoonLight".equals(style)) {
            return renderMoonLightV2Style(graphics, living, x, y);
        } else if ("Rise".equals(style)) {
            return renderRise(graphics, living, x, y);
        }else if ("Lite".equals(style)) {
            return renderLite(graphics, living, x, y);
        } else if ("Myau".equals(style)) {
            return renderMyauStyle(graphics, living, x, y);
        } else if ("Exhibition".equals(style)) {
            return renderExhibitionStyle(graphics, living, x, y);
        } else if ("LastNaven".equals(style)) {
            return renderLastNaven(graphics, living, x, y);
        } else if ("Jello".equals(style)) {
            return renderJelloStyle(graphics, living, x, y);
        } else if ("Client".equals(style)) {
            return renderMCPStyle(graphics, living, x, y);
        }
        return null;
    }

    private static Vector4f renderLite(GuiGraphics graphics, LivingEntity living, float x, float y) {
        long currentTime = System.currentTimeMillis();
        Entity target = living;

        if (target != previousTarget) {
            animationStartTime = currentTime;
            previousTarget = target;
            animationRunning = true;
            animationDirection = target != null ? 1 : -1;
        }

        if (animationRunning) {
            long elapsed = currentTime - animationStartTime;
            long duration = animationDirection > 0 ? 850 : 400;

            if (elapsed >= duration) {
                animationProgress = animationDirection > 0 ? 1f : 0f;
                animationRunning = false;

                if (animationDirection < 0) {
                    previousTarget = null;
                }
            } else {
                float progress = (float) elapsed / duration;
                if (animationDirection > 0) {
                    animationProgress = easeOutElastic(progress);
                } else {
                    animationProgress = 1f - easeInBack(progress);
                }
            }
        }

        Entity renderTarget = target != null ? target : previousTarget;
        if (!(renderTarget instanceof LivingEntity) || animationProgress <= 0) {
            return null;
        }

        LivingEntity livingTarget = (LivingEntity) renderTarget;
        String targetName = livingTarget.getName().getString() + (livingTarget.isBaby() ? " (Baby)" : "");
        float avatarSize = 24.0F;
        float avatarPadding = 5.0F;
        float width = Math.max(Fonts.harmony.getWidth(targetName, 0.4f) + 20.0F + avatarSize + avatarPadding, 100.0F);
        float height = 40.0F;
        Vector4f blurMatrix = new Vector4f(x, y, width, height);
        PoseStack stack = graphics.pose();
        stack.pushPose();
        float centerX = x + width / 2;
        float centerY = y + height / 2;
        stack.translate(centerX, centerY, 0);
        stack.scale(animationProgress, animationProgress, 1);
        stack.translate(-centerX, -centerY, 0);
        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(stack, x, y, width, height, 5.0F, HUD.headerColor);
        StencilUtils.erase(true);
        RenderUtils.fillBound(stack, x, y, width, height, HUD.bodyColor);
        float avatarX = x + avatarPadding;
        float avatarY = y + (height - avatarSize) / 2.0F;
        float textX = x + avatarSize + avatarPadding * 2;
        Fonts.harmony.render(stack, targetName, textX, y + 6.0, Color.WHITE, true, 0.35);
        Fonts.harmony.render(stack, "HP: " + Math.round(livingTarget.getHealth()) +
                        (livingTarget.getAbsorptionAmount() > 0.0F ? "+" + Math.round(livingTarget.getAbsorptionAmount()) : ""),
                textX, y + 17.0, Color.WHITE, true, 0.35);
        float progressBarY = y + 30.0F;
        float progressBarWidth = width - avatarSize - avatarPadding * 3;
        float progressBarHeight = 4.0F;
        float currentHealth = livingTarget.getHealth() / livingTarget.getMaxHealth();
        float previousHealth = getPreviousHealth(livingTarget);
        RenderUtils.drawRoundedRect(stack, textX, progressBarY, progressBarWidth, progressBarHeight, 2.0F, new Color(100, 100, 100, 150).getRGB());

        int healthColor = new Color(200, 45, 45, 200).getRGB();
        int damageColor = new Color(150, 150, 150, 180).getRGB();

        if (previousHealth > currentHealth) {
            float damageWidth = progressBarWidth * (previousHealth - currentHealth);
            float damageX = textX + progressBarWidth * currentHealth;
            if (damageWidth > 0) {
                damageWidth += 2.0F;
                damageX -= 1.0F;
                if (damageWidth < 4.0F) damageWidth = 4.0F;
                RenderUtils.drawRoundedRect(stack, damageX, progressBarY, damageWidth, progressBarHeight, 2.0F, damageColor);
            }
        }

        if (currentHealth > 0) {
            float healthWidth = progressBarWidth * currentHealth;
            if (healthWidth < 4.0F) healthWidth = 4.0F;
            float extendedHealthWidth = healthWidth + 1.0F;
            RenderUtils.drawRoundedRect(stack, textX, progressBarY, extendedHealthWidth, progressBarHeight, 2.0F, healthColor);
        }

        StencilUtils.dispose();
        drawAvatar(graphics, livingTarget, avatarX, avatarY, avatarSize);
        stack.popPose();

        updatePreviousHealth(livingTarget, currentHealth);

        return blurMatrix;
    }

    private static void drawAvatar(GuiGraphics graphics, LivingEntity entity, float x, float y, float size) {
        PoseStack stack = graphics.pose();
        boolean isHit = entity.hurtTime > 0;
        int bgColor = isHit ? new Color(200, 50, 50, 200).getRGB() : new Color(50, 50, 50, 200).getRGB();
        RenderUtils.drawCircle(stack, x + size / 2, y + size / 2, size / 2 + 1, bgColor);
        try {
            ResourceLocation skinTexture = getEntitySkinTexture(entity);
            graphics.blit(
                    skinTexture,
                    (int) x,
                    (int) y,
                    (int) size,
                    (int) size,
                    8, 8, 8, 8,
                    64, 64
            );

        } catch (Exception ex) {
            RenderUtils.drawCircle(stack, x + size / 2, y + size / 2, size / 2, new Color(150, 150, 150, 255).getRGB());
        }
    }

    private static ResourceLocation getEntitySkinTexture(LivingEntity entity) {
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
    private static float getPreviousHealth(LivingEntity entity) {
        return previousHealthMap.getOrDefault(entity.getUUID(), entity.getHealth() / entity.getMaxHealth());
    }
    private static void updatePreviousHealth(LivingEntity entity, float currentHealth) {
        previousHealthMap.put(entity.getUUID(), currentHealth);
    }

    private static Vector4f renderNavenStyle(GuiGraphics graphics, LivingEntity living, float x, float y) {
        long currentTime = System.currentTimeMillis();
        Entity target = living;

        if (target != previousTarget) {
            animationStartTime = currentTime;
            previousTarget = target;
            animationRunning = true;
            animationDirection = target != null ? 1 : -1;
        }

        if (animationRunning) {
            long elapsed = currentTime - animationStartTime;
            long duration = animationDirection > 0 ? 850 : 400;

            if (elapsed >= duration) {
                animationProgress = animationDirection > 0 ? 1f : 0f;
                animationRunning = false;

                if (animationDirection < 0) {
                    previousTarget = null;
                }
            } else {
                float progress = (float) elapsed / duration;
                if (animationDirection > 0) {
                    animationProgress = easeOutElastic(progress);
                } else {
                    animationProgress = 1f - easeInBack(progress);
                }
            }
        }

        Entity renderTarget = target != null ? target : previousTarget;
        if (!(renderTarget instanceof LivingEntity) || animationProgress <= 0) {
            return null;
        }

        LivingEntity livingTarget = (LivingEntity) renderTarget;
        String targetName = livingTarget.getName().getString() + (livingTarget.isBaby() ? " (Baby)" : "");
        float width = Math.max(Fonts.harmony.getWidth(targetName, 0.4F) + 10.0F, 60.0F);
        Vector4f blurMatrix = new Vector4f(x, y, width, 30.0F);
        PoseStack stack = graphics.pose();
        stack.pushPose();
        float centerX = x + width / 2;
        float centerY = y + 15;
        stack.translate(centerX, centerY, 0);
        stack.scale(animationProgress, animationProgress, 1);
        stack.translate(-centerX, -centerY, 0);
        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(stack, x, y, width, 30.0F, 5.0F, HUD.headerColor);
        StencilUtils.erase(true);
        RenderUtils.fillBound(stack, x, y, width, 30.0F, HUD.bodyColor);
        RenderUtils.fillBound(stack, x, y, width * (livingTarget.getHealth() / livingTarget.getMaxHealth()), 3.0F, HUD.headerColor);
        StencilUtils.dispose();
        Fonts.harmony.render(stack, targetName, (double) (x + 5.0F), (double) (y + 6.0F), Color.WHITE, true, 0.35F);
        Fonts.harmony.render(
                stack,
                "HP: " + Math.round(livingTarget.getHealth()) + (livingTarget.getAbsorptionAmount() > 0.0F ? "+" + Math.round(livingTarget.getAbsorptionAmount()) : ""),
                (double) (x + 5.0F),
                (double) (y + 17.0F),
                Color.WHITE,
                true,
                0.35F
        );

        stack.popPose();

        return blurMatrix;
    }
    private float easeOutCubic(float x) {
        return (float) (1 - Math.pow(1 - x, 3));
    }
    private static float easeOutElastic(float x) {
        float c4 = (float) ((2 * Math.PI) / 3);
        return x == 0 ? 0 : x == 1 ? 1 : (float) (Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1);
    }

    private static float easeInBack(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return c3 * x * x * x - c1 * x * x;
    }

    private static Vector4f renderNewStyle(GuiGraphics graphics, LivingEntity living, float x, float y) {
        float hudWidth = 140.0F;
        float hudHeight = 50.0F;
        Vector4f blurMatrix = new Vector4f(x, y, hudWidth, hudHeight);

        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(graphics.pose(), x, y, hudWidth, hudHeight, 8.0F, 0x80000000);
        StencilUtils.erase(true);
        RenderUtils.fillBound(graphics.pose(), x, y, hudWidth, hudHeight, 0x80000000);
        StencilUtils.dispose();

        String targetName = living.getName().getString() + (living.isBaby() ? " (Baby)" : "");
        float nameX = x + 10.0F;
        float nameY = y + 8.0F;
        Fonts.harmony.render(graphics.pose(), "Name: " + targetName, (double)nameX, (double)nameY, Color.WHITE, true, 0.30F);

        String healthText = "HP: " + Math.round(living.getHealth()) + (living.getAbsorptionAmount() > 0.0F ? "+" + Math.round(living.getAbsorptionAmount()) : "");
        float healthTextX = x + 10.0F;
        float healthTextY = y + 20.0F;
        Fonts.harmony.render(graphics.pose(), healthText, (double)healthTextX, (double)healthTextY, Color.WHITE, true, 0.30F);

        float healthBarWidth = 120.0F;
        float healthBarHeight = 6.0F;
        float healthBarX = x + 10.0F;
        float healthBarY = y + 36.0F;

        if (healthBarX + healthBarWidth > x + hudWidth) {
            healthBarWidth = hudWidth - 20.0F;
        }

        RenderUtils.drawRoundedRect(graphics.pose(), healthBarX, healthBarY, healthBarWidth, healthBarHeight, 4.0F, 0x80FFFFFF);

        float healthRatio = living.getHealth() / living.getMaxHealth();
        if (healthRatio > 1.0F) healthRatio = 1.0F;
        float currentHealthWidth = healthBarWidth * healthRatio;

        if (currentHealthWidth > 0) {
            RenderUtils.fillBound(graphics.pose(), healthBarX, healthBarY, currentHealthWidth, healthBarHeight, 0xFFFFFFFF);
        }

        return blurMatrix;
    }

    private static Vector4f renderMoonLightV2Style(GuiGraphics graphics, LivingEntity living, float x, float y) {
        float mlHudWidth = 150.0F;
        float mlHudHeight = 35.0F;
        Vector4f blurMatrix = new Vector4f(x, y, mlHudWidth, mlHudHeight);

        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(graphics.pose(), x, y, mlHudWidth, mlHudHeight, 4.0F, 0x80000000);
        StencilUtils.erase(true);
        RenderUtils.fillBound(graphics.pose(), x, y, mlHudWidth, mlHudHeight, 0x80000000);
        StencilUtils.dispose();

        String mlTargetName = living.getName().getString() + (living.isBaby() ? " (Baby)" : "");
        float mlNameX = x + 8.0F;
        float mlNameY = y + 8.0F;
        Fonts.harmony.render(graphics.pose(), mlTargetName, (double) mlNameX, (double) mlNameY, Color.WHITE, true, 0.30F);

        String mlHealthText = Math.round(living.getHealth()) + "/" + Math.round(living.getMaxHealth());
        float mlHealthTextX = x + 8.0F;
        float mlHealthTextY = y + 20.0F;
        Fonts.harmony.render(graphics.pose(), mlHealthText, (double) mlHealthTextX, (double) mlHealthTextY, Color.WHITE, true, 0.30F);

        float mlCircleX = x + mlHudWidth - 20.0F;
        float mlCircleY = y + mlHudHeight / 2.0F;
        float mlCircleRadius = 10.0F;
        float mlHealthPercent = Math.min(1.0f, Math.max(0.0f, living.getHealth() / living.getMaxHealth()));

        RenderUtils.drawHealthRing(
                graphics.pose(),
                mlCircleX,
                mlCircleY,
                mlCircleRadius,
                2.5F,
                mlHealthPercent
        );

        return blurMatrix;
    }

    private static Vector4f renderRise(GuiGraphics graphics, LivingEntity living, float x, float y) {
        float hudWidth = 160.0F;
        float hudHeight = 45.0F;
        float avatarSize = 32.0F;
        float padding = 4.0F;

        Vector4f blurMatrix = new Vector4f(x, y, hudWidth, hudHeight);


        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(graphics.pose(), x, y, hudWidth, hudHeight, 6.0F, 0x70000000);
        StencilUtils.erase(true);
        RenderUtils.fillBound(graphics.pose(), x, y, hudWidth, hudHeight, 0x70000000);
        StencilUtils.dispose();


        float currentTotalHealth = living.getHealth() + living.getAbsorptionAmount();
        float previousHealth = lastHealth.getOrDefault(living.getUUID(), currentTotalHealth);

        if (currentTotalHealth < previousHealth) {
            int particleCount = random.nextInt(6) + 8;
            float avatarX = x + padding;
            float avatarY = y + (hudHeight - avatarSize) / 2;

            List<HealthParticle> particles = playerParticles.computeIfAbsent(living.getUUID(), k -> new CopyOnWriteArrayList<>());
            for (int i = 0; i < particleCount; i++) {
                particles.add(new HealthParticle(avatarX + avatarSize / 2, avatarY + avatarSize / 2));
            }
        }

        lastHealth.put(living.getUUID(), currentTotalHealth);

        List<HealthParticle> particles = playerParticles.get(living.getUUID());
        if (particles != null) {
            for (HealthParticle particle : particles) {
                particle.update();
                particle.render(graphics);
            }
            particles.removeIf(HealthParticle::isDead);
        }


        float avatarX = x + padding;
        float avatarY = y + (hudHeight - avatarSize) / 2;
        RenderUtils.drawRoundedRect(graphics.pose(), avatarX, avatarY, avatarSize, avatarSize, 4.0F, Color.WHITE.getRGB());

        ResourceLocation skinLocation = null;
        if (living instanceof Player player) {
            PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(player.getUUID());
            if (playerInfo != null) {
                skinLocation = playerInfo.getSkinLocation();
            }
        }

        if (skinLocation != null) {
            if (living instanceof Player player) {
                graphics.blit(skinLocation, (int) avatarX, (int) avatarY, (int) avatarSize, (int) avatarSize, 8, 8, 8, 8, 64, 64);
                graphics.blit(skinLocation, (int) avatarX, (int) avatarY, (int) avatarSize, (int) avatarSize, 40, 8, 8, 8, 64, 64);
            } else {
                graphics.blit(skinLocation, (int) avatarX, (int) avatarY, (int) avatarSize, (int) avatarSize, 0, 0, 16, 16, 16, 16);
            }
        } else {

            String noneText = "NONE";
            float noneTextWidth = Fonts.harmony.getWidth(noneText, 0.30F);
            float noneTextHeight = (float) Fonts.harmony.getHeight(true, 0.30F);
            float noneTextX = avatarX + (avatarSize - noneTextWidth) / 2.0F;
            float noneTextY = avatarY + (avatarSize - noneTextHeight) / 2.0F;
            Fonts.harmony.render(graphics.pose(), noneText, (double) noneTextX, (double) noneTextY, Color.WHITE, true, 0.30F);
        }


        String targetName = living.getName().getString() + (living.isBaby() ? " (Baby)" : "");
        float textX = x + avatarSize + padding * 2;
        float textY = y + padding + 2;
        Fonts.harmony.render(graphics.pose(), "Name: " + targetName, (double) textX, (double) textY, Color.WHITE, true, 0.30F);

        float health = living.getHealth();
        float maxHealth = living.getMaxHealth();
        float absorption = living.getAbsorptionAmount();

        HealthBarAnimator animator = healthAnimators.computeIfAbsent(living.getUUID(), k -> new HealthBarAnimator(health + absorption, 4.0F));
        animator.update(health + absorption);
        float animatedHealth = animator.getDisplayedHealth();

        String healthText = "HP: " + String.format("%.0f", animatedHealth) + " / " + String.format("%.0f", maxHealth);
        float healthTextY = (float) (textY + Fonts.harmony.getHeight(true, 0.30F) + 2.0F);
        Fonts.harmony.render(graphics.pose(), healthText, (double) textX, (double) healthTextY, Color.WHITE, true, 0.30F);


        float healthBarX = x + avatarSize + padding * 2;
        float healthBarY = y + hudHeight - padding - 8;
        float healthBarWidth = hudWidth - (healthBarX - x) - padding;
        float healthBarHeight = 6.0F;
        float cornerRadius = 4.0F;

        float healthRatio = animatedHealth / maxHealth;
        if (healthRatio > 1.0F) healthRatio = 1.0F;
        float currentHealthWidth = healthBarWidth * healthRatio;


        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);


        RenderUtils.drawRoundedRect(graphics.pose(), healthBarX, healthBarY, healthBarWidth, healthBarHeight, cornerRadius, 0x80404040);


        if (currentHealthWidth > 0) {

            float foregroundRadius = Math.min(cornerRadius, currentHealthWidth / 2);


            RenderUtils.drawRoundedRect(
                    graphics.pose(),
                    healthBarX,
                    healthBarY,
                    currentHealthWidth,
                    healthBarHeight,
                    foregroundRadius,
                    0xFF962D2D
            );
        }
        return blurMatrix;
    }
    private static Vector4f renderExhibitionStyle(GuiGraphics graphics, LivingEntity living, float x, float y) {
        float hudWidth = 170.0F;
        float hudHeight = 60.0F;
        float avatarSize = 40.0F;
        float padding = 5.0F;
        RenderUtils.fill(graphics.pose(), x, y, x + hudWidth, y + hudHeight, new Color(50, 50, 50, 200).getRGB()); // 外层边框
        RenderUtils.fill(graphics.pose(), x + 1, y + 1, x + hudWidth - 1, y + hudHeight - 1, new Color(50, 50, 50, 200).getRGB()); // 加粗边框
        RenderUtils.fill(graphics.pose(), x + 2, y + 2, x + hudWidth - 2, y + hudHeight - 2, new Color(50, 50, 50, 200).getRGB()); // 加粗边框
        RenderUtils.fill(graphics.pose(), x + 3, y + 3, x + hudWidth - 3, y + hudHeight - 3, new Color(25, 25, 25, 240).getRGB());
        float avatarX = x + padding + 3;
        float modelSize = avatarSize - 6.0F;
        float avatarY = y + (hudHeight - modelSize) / 2.0f;
        RenderUtils.fill(graphics.pose(), avatarX - 2, avatarY - 2, avatarX + modelSize + 2, avatarY + modelSize + 2, new Color(10, 10, 10).getRGB());
        RenderUtils.fill(graphics.pose(), avatarX - 2, avatarY - 4, avatarX + modelSize + 2, avatarY - 3, new Color(50, 50, 50).getRGB()); // 上边框更长
        RenderUtils.fill(graphics.pose(), avatarX - 2, avatarY + modelSize + 3, avatarX + modelSize + 2, avatarY + modelSize + 4, new Color(50, 60, 60).getRGB()); // 下边框更长
        RenderUtils.fill(graphics.pose(), avatarX - 2, avatarY - 3, avatarX - 1, avatarY + modelSize + 3, new Color(50, 50, 50).getRGB()); // 左边框
        RenderUtils.fill(graphics.pose(), avatarX + modelSize + 1, avatarY - 3, avatarX + modelSize + 2, avatarY + modelSize + 3, new Color(60, 60, 60).getRGB()); // 右边框
        drawPlayerModel(graphics, living, avatarX, avatarY, modelSize);
        float textX = x + avatarSize + padding * 2 + 2;
        float textY = y + padding + 2;
        String targetName = living.getName().getString();
        Fonts.harmony.render(graphics.pose(), targetName, (double) textX, (double) textY, Color.WHITE, true, 0.35F);
        float health = living.getHealth();
        float maxHealth = living.getMaxHealth();
        float absorption = living.getAbsorptionAmount();

        float healthBarWidth = hudWidth - (avatarSize + padding * 3) - 4;
        float healthBarHeight = 5.0F;
        float healthBarX = textX;
        float healthBarY = textY + (float)Fonts.harmony.getHeight(true, 0.35F) + padding;

        int numSegments = 10;
        float gap = 1.0f;
        float totalGapWidth = gap * (numSegments - 1);
        float segmentWidth = (healthBarWidth - totalGapWidth) / numSegments;

        float healthPerSegment = maxHealth / numSegments;
        float currentHealthAmount = health + absorption;
        float healthRatioForColor = (health + absorption) / maxHealth;
        Color healthColor = getHealthColor(healthRatioForColor);
        for (int i = 0; i < numSegments; i++) {
            float segmentX = healthBarX + i * (segmentWidth + gap);
            RenderUtils.fill(graphics.pose(), segmentX, healthBarY, segmentX + segmentWidth, healthBarY + healthBarHeight, new Color(50, 50, 50).getRGB());

            if (currentHealthAmount > 0) {
                float fillWidth = segmentWidth;
                if (currentHealthAmount < healthPerSegment) {
                    fillWidth = segmentWidth * (currentHealthAmount / healthPerSegment);
                }

                RenderUtils.fill(graphics.pose(), segmentX, healthBarY, segmentX + fillWidth, healthBarY + healthBarHeight, healthColor.getRGB());

                currentHealthAmount -= healthPerSegment;
            }
        }
        float itemY = healthBarY + healthBarHeight + 5.0F;
        renderPlayerItems(graphics, living, textX, itemY);

        return new Vector4f(x, y, hudWidth, hudHeight);
    }

    private static void drawPlayerModel(GuiGraphics graphics, LivingEntity living, float x, float y, float size) {
        com.mojang.blaze3d.vertex.PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x + size / 2.0F, y + size / 2F + (size * 0.7f * living.getBbHeight()) / 2F, 100.0F);
        poseStack.scale(size * 0.7f, size * 0.7f, -size * 0.7f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(-30.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(10.0F));
        EntityRenderDispatcher entityRenderDispatcher = mc.getEntityRenderDispatcher();
        entityRenderDispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(living, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, graphics.bufferSource(), 15728880);
        });
        graphics.flush();
        entityRenderDispatcher.setRenderShadow(true);
        poseStack.popPose();
    }

    private static void renderPlayerItems(GuiGraphics graphics, LivingEntity living, float x, float y) {
        if (living instanceof Player player) {
            float itemSize = 16.0F;
            float currentX = x;
            for (int i = 3; i >= 0; i--) {
                ItemStack armorStack = player.getInventory().getArmor(i);
                if (!armorStack.isEmpty()) {
                    graphics.renderItem(armorStack, (int) currentX, (int) y);
                    graphics.renderItemDecorations(mc.font, armorStack, (int) currentX, (int) y);
                    currentX += itemSize + 2.0F;
                }
            }
            ItemStack mainHandStack = player.getMainHandItem();
            if (!mainHandStack.isEmpty()) {
                graphics.renderItem(mainHandStack, (int) currentX, (int) y);
                graphics.renderItemDecorations(mc.font, mainHandStack, (int) currentX, (int) y);
            }
            ItemStack offHandStack = player.getOffhandItem();
            if (!offHandStack.isEmpty()) {
                graphics.renderItem(offHandStack, (int) (currentX + itemSize + 2.0F), (int) y);
                graphics.renderItemDecorations(mc.font, offHandStack, (int) (currentX + itemSize + 2.0F), (int) y);
            }
        }
    }

    private static Color getHealthColor(float healthRatio) {
        if (healthRatio > 0.6) {
            return new Color(0, 255, 0);
        } else if (healthRatio > 0.3) {
            return new Color(255, 255, 0);
        } else {
            return new Color(255, 0, 0);
        }
    }
    
    /**
     * Set target for LastNaven mode (called from EventAttack)
     */
    public static void setLastNavenTarget(Player target) {
        if (target != lastNavenTarget) {
            lastNavenTarget = target;
            if (target != null) {
                float percent = target.getHealth() / target.getMaxHealth();
                lastNavenHealthAnimation.target = lastNavenHealthAnimation.value = percent;
            }
            lastNavenTargetAnimation.target = 1;
            lastNavenTimer.reset();
        }
    }
    
    /**
     * Update LastNaven target from chat screen or update timer
     */
    public static void updateLastNaven() {
        if (mc.screen instanceof ChatScreen && mc.player != null) {
            lastNavenTarget = mc.player;
            float percent = lastNavenTarget.getHealth() / lastNavenTarget.getMaxHealth();
            lastNavenHealthAnimation.target = lastNavenHealthAnimation.value = percent;
            lastNavenTargetAnimation.target = 1;
            lastNavenTimer.reset();
        }
        
        if (lastNavenTargetAnimation.target > 0 && lastNavenTimer.delay(1500)) {
            lastNavenTargetAnimation.target = 0;
        }
        
        if (lastNavenTargetAnimation.target > 0 && (lastNavenTarget != null && (lastNavenTarget.isDeadOrDying() || lastNavenTarget.getHealth() <= 0))) {
            lastNavenTargetAnimation.target = 0;
        }
    }
    
    private static Vector4f renderLastNaven(GuiGraphics graphics, LivingEntity living, float x, float y) {
        // Use the tracked target if available, otherwise use the passed living entity
        Player targetPlayer = lastNavenTarget != null ? lastNavenTarget : (living instanceof Player ? (Player)living : null);
        if (targetPlayer == null) {
            return null;
        }
        
        updateLastNaven();
        lastNavenTargetAnimation.update(true);
        
        if (lastNavenTargetAnimation.value > 0.08) {
            String name = targetPlayer.getName().getString();
            
            int width = Math.max(120, (int)Fonts.harmony.getWidth(name, 0.35) + 53);
            float animatedWidth = width * lastNavenTargetAnimation.value;
            float animatedHeight = 66 * lastNavenTargetAnimation.value;
            
            int rgb = new Color(23, 22, 38, 200).getRGB();
            PoseStack stack = graphics.pose();
            
            // Render shader blur area (white rounded rect)
            if (lastNavenTargetAnimation.value > 0.08) {
                RenderUtils.drawRoundedRect(stack, x + (width - animatedWidth) / 2f, y + (56 - animatedHeight) / 2f + 5, animatedWidth, animatedHeight - 5, 6, 0xFFFFFFFF);
            }
            
            // Main body
            StencilUtils.write(false);
            RenderUtils.drawRoundedRect(stack, x + (width - animatedWidth) / 2f, y + (56 - animatedHeight) / 2f, animatedWidth, animatedHeight, 6, 0xFFFFFFFF);
            StencilUtils.erase(true);
            RenderUtils.drawRoundedRect(stack, x, y, width, 85, 6, rgb);
            
            // Render player skin
            if (mc.getConnection() != null) {
                PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(targetPlayer.getUUID());
                if (playerInfo != null) {
                    ResourceLocation skinLocation = playerInfo.getSkinLocation();
                    graphics.blit(skinLocation, (int)(x + 9), (int)(y + 14), 32, 32, 8, 8, 8, 8, 64, 64);
                    // Render hat layer - always render second layer for hat (simplified)
                    graphics.blit(skinLocation, (int)(x + 9), (int)(y + 14), 32, 32, 40, 8, 8, 8, 64, 64);
                }
            }
            
            // Render text
            Fonts.harmony.render(stack, name, x + 46, y + 7, Color.WHITE, true, 0.35);
            
            String healthText = "Health: " + String.format("%.1f", targetPlayer.getHealth()) + 
                    (targetPlayer.getAbsorptionAmount() > 0 ? "+" + String.format("%.1f", targetPlayer.getAbsorptionAmount()) : "");
            Fonts.opensans.render(stack, healthText, x + 46, y + 19, Color.WHITE, true, 0.35);
            
            // Distance
            float distance = mc.player != null ? mc.player.distanceTo(targetPlayer) : 0;
            Fonts.opensans.render(stack, "Distance: " + String.format("%.1f", distance), x + 46, y + 27, Color.WHITE, true, 0.35);
            
            // Blocking/Using status
            String blocking;
            ItemStack mainHand = targetPlayer.getMainHandItem();
            boolean isBlocking = targetPlayer.isUsingItem() && mainHand.getItem() == Items.SHIELD;
            
            if (!mainHand.isEmpty() && mainHand.getItem() instanceof net.minecraft.world.item.SwordItem) {
                blocking = isBlocking ? "Blocking (100%)" : "Not Blocking (0%)";
            } else {
                blocking = targetPlayer.isUsingItem() ? "Using" : "Not Using";
            }
            Fonts.opensans.render(stack, blocking, x + 46, y + 35, Color.WHITE, true, 0.35);
            
            // Gapples count
            if (targetPlayer != mc.player) {
                int currentGappleCount = 0;
                for (int i = 0; i < targetPlayer.getInventory().getContainerSize(); i++) {
                    ItemStack stack2 = targetPlayer.getInventory().getItem(i);
                    if (!stack2.isEmpty() && stack2.getItem() == Items.GOLDEN_APPLE) {
                        currentGappleCount += stack2.getCount();
                    }
                }
                
                if (lastNavenTargetPlayer != targetPlayer) {
                    lastNavenGappleCount = currentGappleCount;
                    lastNavenTargetPlayer = targetPlayer;
                    lastNavenInventorySnapshot = takeInventorySnapshot(targetPlayer);
                } else if (hasInventoryChanged(targetPlayer)) {
                    lastNavenGappleCount = currentGappleCount;
                    lastNavenInventorySnapshot = takeInventorySnapshot(targetPlayer);
                }
            }
            
            if (lastNavenGappleCount > 0) {
                Fonts.opensans.render(stack, "Gapples: " + lastNavenGappleCount, x + 46, y + 43, Color.WHITE, true, 0.35);
            }
            
            // Health bar
            lastNavenHealthAnimation.target = targetPlayer.getHealth() / targetPlayer.getMaxHealth();
            lastNavenHealthAnimation.update(true);
            RenderUtils.drawRoundedRect(stack, x + 8, y + 53, (width - 16) * lastNavenHealthAnimation.value, 4, 2, new Color(148, 42, 43).getRGB());
            
            StencilUtils.dispose();
            
            return new Vector4f(x, y, width, 85);
        }
        
        return null;
    }
    
    private static ItemStack[] takeInventorySnapshot(Player player) {
        ItemStack[] snapshot = new ItemStack[player.getInventory().getContainerSize()];
        for (int i = 0; i < snapshot.length; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            snapshot[i] = stack != null && !stack.isEmpty() ? stack.copy() : null;
        }
        return snapshot;
    }
    
    private static boolean hasInventoryChanged(Player player) {
        if (lastNavenInventorySnapshot == null) return true;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack current = player.getInventory().getItem(i);
            ItemStack last = i < lastNavenInventorySnapshot.length ? lastNavenInventorySnapshot[i] : null;
            if (!current.isEmpty() && current.getItem() == Items.GOLDEN_APPLE) {
                if (last == null || last.isEmpty() || last.getCount() != current.getCount()) {
                    return true;
                }
            }
        }
        return false;
    }
    private static Vector4f renderMyauStyle(GuiGraphics graphics, LivingEntity living, float x, float y) {
        String targetName = living.getName().getString();
        float selfTotal = mc.player != null ? (mc.player.getHealth() + mc.player.getAbsorptionAmount()) : 0.0F;
        float targetAbs = living.getAbsorptionAmount();
        float targetTotal = living.getHealth() + targetAbs;

        String hpText = String.format("HP: %d%s", Math.round(living.getHealth()), targetAbs > 0.0F ? "+" + Math.round(targetAbs) : "");
        String status = "D";
        if (selfTotal > 0.0F) {
            if (targetTotal < selfTotal) status = "W"; else if (targetTotal > selfTotal) status = "L";
        }
        String diffText = selfTotal > 0.0F ? String.format("%s%d", (int) Math.signum(Math.round(selfTotal - targetTotal)) >= 0 ? "+" : "", Math.round(selfTotal - targetTotal)) : "0";

        float titleScale = 0.35F;
        float infoScale = 0.30F;

        float nameW = Fonts.harmony.getWidth(targetName, titleScale);
        float hpW = Fonts.harmony.getWidth(hpText, infoScale);
        float statusW = Fonts.harmony.getWidth(status, infoScale);
        float diffW = Fonts.harmony.getWidth(diffText, infoScale);

        float paddingH = 8.0F;
        float paddingV = 6.0F;
        float lineGap = 4.0F;
        float hudHeight = 26.0F;
        float textGap = 8.0F;

        float contentW = Math.max(nameW + textGap + statusW, hpW + textGap + diffW);
        float hudWidth = Math.max(120.0F, contentW + paddingH * 2.0F);

        int bg = new Color(0, 0, 0, 140).getRGB();
        int outline = HUD.headerColor;
        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(graphics.pose(), x, y, hudWidth, hudHeight, 5.0F, outline);
        StencilUtils.erase(true);
        RenderUtils.fillBound(graphics.pose(), x, y, hudWidth, hudHeight, bg);
        StencilUtils.dispose();

        float nameX = x + paddingH;
        float nameY = y + paddingV;
        Fonts.harmony.drawString(graphics.pose(), targetName, (double) nameX, (double) nameY, Color.WHITE, true, titleScale);
        Fonts.harmony.drawString(graphics.pose(), status, (double) (x + hudWidth - paddingH - statusW), (double) nameY, new Color(255, 200, 60), true, infoScale);

        float hpX = x + paddingH;
        float hpY = nameY + (float) Fonts.harmony.getHeight(true, titleScale) + lineGap;
        Fonts.harmony.drawString(graphics.pose(), hpText, (double) hpX, (double) hpY, Color.WHITE, true, infoScale);
        Fonts.harmony.drawString(graphics.pose(), diffText, (double) (x + hudWidth - paddingH - diffW), (double) hpY, new Color(200, 200, 200), true, infoScale);

        float barX = x + paddingH;
        float barW = hudWidth - paddingH * 2.0F;
        float barH = 3.0F;
        float barY = y + hudHeight - paddingV - barH;
        float ratio = Math.min(1.0F, Math.max(0.0F, living.getHealth() / Math.max(1.0F, living.getMaxHealth())));
        int barBg = new Color(255, 255, 255, 90).getRGB();
        RenderUtils.drawRoundedRect(graphics.pose(), barX, barY, barW, barH, 2.0F, barBg);
        if (ratio > 0) {
            float fillW = barW * ratio;
            int fill = new Color(102, 204, 255).getRGB();
            RenderUtils.drawRoundedRect(graphics.pose(), barX, barY, fillW, barH, Math.min(2.0F, fillW / 2.0F), fill);
        }

        return new Vector4f(x, y, hudWidth, hudHeight);
    }
    
    private static Vector4f renderJelloStyle(GuiGraphics graphics, LivingEntity living, float x, float y) {
        UUID entityUUID = living.getUUID();
        
        // Initialize animation timers if needed
        SmoothAnimationTimer healthBar = jelloHealthBars.computeIfAbsent(entityUUID, k -> new SmoothAnimationTimer(0, 0.4f));
        SmoothAnimationTimer headSize = jelloHeadSizes.computeIfAbsent(entityUUID, k -> {
            SmoothAnimationTimer timer = new SmoothAnimationTimer(0, 0.5f);
            timer.speed = 0.5f;
            return timer;
        });
        SmoothAnimationTimer headXY = jelloHeadXYs.computeIfAbsent(entityUUID, k -> {
            SmoothAnimationTimer timer = new SmoothAnimationTimer(0, 0.5f);
            timer.speed = 0.5f;
            return timer;
        });
        
        // Update animations
        healthBar.target = living.getHealth() / living.getMaxHealth();
        healthBar.update(true);
        
        if (living instanceof Player && ((Player) living).hurtTime > 5) {
            headSize.target = 4;
            headXY.target = 2;
        } else {
            headSize.target = 0;
            headXY.target = 0;
        }
        headSize.update(true);
        headXY.update(true);
        
        PoseStack stack = graphics.pose();
        stack.pushPose();
        
        String targetName = living.getName().getString() + (living.isBaby() ? " (Baby)" : "");
        float width = Math.max(Fonts.harmony.getWidth(targetName, 0.4F) + 46.0F, 125.0F);
        Vector4f blurMatrix = new Vector4f(x, y, width, 40.0f);
        
        // Render background
        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(stack, x, y, width, 40.0f, 5.0F, HUD.headerColor);
        StencilUtils.erase(true);
        RenderUtils.fillBound(stack, x, y, 40.0f, 40.0f, HUD.headerColor);
        RenderUtils.fillBound(stack, x + 40.0f, y, width - 40.0f, 40.0f, HUD.bodyColor);
        StencilUtils.dispose();
        
        // Render text
        Color textColor = new Color(200, 200, 200, 255);
        Fonts.harmony.render(stack, targetName, x + 43.0f, y + 5.0F, Color.white, true, 0.35F);
        Fonts.harmony.render(
                stack,
                Math.round(living.getHealth()) + (living.getAbsorptionAmount() > 0.0F ? "+" + Math.round(living.getAbsorptionAmount()) : "") + "Health",
                x + 43.0f,
                y + (28.0f - Fonts.harmony.getHeight(true, 0.325f)),
                textColor,
                true,
                0.325F
        );
        
        // Render health bar
        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(stack, x + 43.0f, y + 30.0f, width - 46.0f, 5.0F, 2.0f, -1);
        StencilUtils.erase(true);
        RenderUtils.fillBound(stack, x + 43.0f, y + 30.0f, width - 46.0f, 5.0F, new Color(255, 255, 255, 75).getRGB());
        RenderUtils.fillBound(stack, x + 43.0f, y + 30.0f, (width - 46.0f) * healthBar.value, 5.0f, textColor.getRGB());
        StencilUtils.dispose();
        
        // Render player head
        if (living instanceof AbstractClientPlayer player) {
            float headX = x + 3.0f + headXY.value;
            float headY = y + 3.0f + headXY.value;
            float headSizeValue = 34.0f - headSize.value;
            
            StencilUtils.write(false);
            RenderUtils.drawRoundedRect(stack, headX, headY, headSizeValue, headSizeValue, 5.0F, -1);
            StencilUtils.erase(true);
            
            // Render player head using skin texture
            try {
                ResourceLocation skinTexture = player.getSkinTextureLocation();
                graphics.blit(
                        skinTexture,
                        (int) headX,
                        (int) headY,
                        (int) headSizeValue,
                        (int) headSizeValue,
                        8, 8, 8, 8,
                        64, 64
                );
                // Render hat layer
                graphics.blit(
                        skinTexture,
                        (int) headX,
                        (int) headY,
                        (int) headSizeValue,
                        (int) headSizeValue,
                        40, 8, 8, 8,
                        64, 64
                );
            } catch (Exception ex) {
                RenderUtils.drawCircle(stack, headX + headSizeValue / 2, headY + headSizeValue / 2, headSizeValue / 2, new Color(150, 150, 150, 255).getRGB());
            }
            
            StencilUtils.dispose();
        }
        
        stack.popPose();
        
        return blurMatrix;
    }
    
    /**
     * 渲染 MCP 模式（Murasame 风格）
     */
    private static Vector4f renderMCPStyle(GuiGraphics graphics, LivingEntity living, float x, float y) {
        UUID entityUUID = living.getUUID();
        
        // 初始化动画计时器
        SmoothAnimationTimer animation = mcpAnimations.computeIfAbsent(entityUUID, k -> new SmoothAnimationTimer(0, 0.3f));
        animation.target = 1.0f;
        animation.update(true);
        float aniStep = animation.value;
        
        // 初始化血量动画
        float healthRatio = living.getHealth() / living.getMaxHealth();
        float currentHealthAnim = mcpHealthAnimations.getOrDefault(entityUUID, healthRatio);
        float targetHealthAnim = healthRatio;
        currentHealthAnim += (targetHealthAnim - currentHealthAnim) * 0.1f; // 平滑动画
        mcpHealthAnimations.put(entityUUID, currentHealthAnim);
        
        // 如果动画进度太低，不渲染
        if (aniStep < 0.01f) {
            return null;
        }
        
        float width = 140.0f;
        float height = 34.0f;
        float scale = 0.7f + 0.3f * aniStep;
        float alpha = Math.max(0.01f, Math.min(1.0f, aniStep));
        
        PoseStack stack = graphics.pose();
        stack.pushPose();
        
        // 应用缩放和位置变换
        stack.translate(x + width / 2, y + height / 2, 0);
        stack.scale(scale, scale, 1.0f);
        stack.translate(-(x + width / 2), -(y + height / 2), 0);
        
        // 背景颜色（带透明度）
        Color bgColor = new Color(20, 20, 20, (int)(90 * alpha));
        
        // 绘制圆角矩形背景
        RenderUtils.drawRoundedRect(stack, x, y, width, height, 2.0f, bgColor.getRGB());
        
        // 绘制玩家模型（使用 Stencil）
        float playerWidth = height - 8.0f;
        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(stack, x + 4, y + 4, playerWidth, playerWidth, 3.0f, -1);
        StencilUtils.erase(true);
        drawPlayerModel(graphics, living, x + 4, y + 4, playerWidth);
        StencilUtils.dispose();
        
        // 计算渐变颜色（基于时间）- 减慢速度
        long time = System.currentTimeMillis();
        float timeOffset1 = ((time * 1 + 2) % 800) / 200.0f;
        float timeOffset2 = ((time * 1 + 2 + 50) % 800) / 200.0f;
        
        // 使用鲜艳的红色渐变（类似 arraylist）
        Color color1 = new Color(255, 50, 50, (int)(255 * alpha));  // 亮红色
        Color color2 = new Color(200, 0, 0, (int)(255 * alpha));    // 深红色
        
        // 计算渐变颜色
        Color gradientColor1 = getGradientColor(color1, color2, timeOffset1, alpha);
        Color gradientColor2 = getGradientColor(color1, color2, timeOffset2, alpha);
        
        // 绘制血条背景
        float healthBarX = x + 35;
        float healthBarY = y + 20;
        float healthBarWidth = width - playerWidth - 13;
        float healthBarHeight = 8.0f;
        
        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(stack, healthBarX, healthBarY, healthBarWidth, healthBarHeight, 3.0f, 
            new Color(50, 0, 0, (int)(100 * alpha)).getRGB());
        StencilUtils.erase(true);
        RenderUtils.fillBound(stack, healthBarX, healthBarY, healthBarWidth, healthBarHeight, 
            new Color(50, 0, 0, (int)(100 * alpha)).getRGB());
        StencilUtils.dispose();
        
        // 绘制血条（渐变）
        float healthBarFillWidth = healthBarWidth * currentHealthAnim;
        if (healthBarFillWidth > 0) {
            StencilUtils.write(false);
            RenderUtils.drawRoundedRect(stack, healthBarX, healthBarY, healthBarFillWidth, healthBarHeight, 3.0f, -1);
            StencilUtils.erase(true);
            RenderUtils.drawHorizontalGradientRect(stack, healthBarX, healthBarY, healthBarFillWidth, healthBarHeight, 
                gradientColor1.getRGB(), gradientColor2.getRGB());
            StencilUtils.dispose();
        }
        
        // 绘制左侧渐变条
        RenderUtils.drawHorizontalGradientRect(stack, x, y + height / 2 - 5, 2, 10, 
            gradientColor1.getRGB(), gradientColor2.getRGB());
        
        // 绘制玩家名称
        String name = living.getName().getString();
        Color nameColor = new Color(255, 255, 255, (int)(255 * alpha));
        Fonts.harmony.render(stack, name, x + 35, y + 10, nameColor, true, 0.35);
        
        stack.popPose();
        
        return new Vector4f(x, y, width, height);
    }
    
    /**
     * 获取渐变颜色（用于 MCP 模式）
     */
    private static Color getGradientColor(Color color1, Color color2, float offset, float alpha) {
        float progress = (float)(Math.sin(offset * Math.PI * 2) * 0.5 + 0.5);
        int red = (int)(color1.getRed() + (color2.getRed() - color1.getRed()) * progress);
        int green = (int)(color1.getGreen() + (color2.getGreen() - color1.getGreen()) * progress);
        int blue = (int)(color1.getBlue() + (color2.getBlue() - color1.getBlue()) * progress);
        return new Color(red, green, blue, (int)(255 * alpha));
    }
}
