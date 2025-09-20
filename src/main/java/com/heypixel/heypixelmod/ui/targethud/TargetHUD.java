package com.heypixel.heypixelmod.ui.targethud;

import com.heypixel.heypixelmod.events.impl.EventRender2D;
import com.heypixel.heypixelmod.modules.impl.render.HUD;
import com.heypixel.heypixelmod.utils.RenderUtils;
import com.heypixel.heypixelmod.utils.StencilUtils;
import com.heypixel.heypixelmod.utils.renderer.Fonts;
import com.heypixel.heypixelmod.utils.renderer.HealthBarAnimator;
import com.heypixel.heypixelmod.utils.renderer.HealthParticle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
}
//BlinkFix风风光光操你妈