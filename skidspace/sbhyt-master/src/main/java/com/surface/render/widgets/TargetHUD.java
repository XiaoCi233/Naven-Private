package com.surface.render.widgets;

import com.surface.Wrapper;
import com.surface.mod.fight.KillAuraModule;
import com.surface.render.font.FontManager;
import com.surface.util.NumberUtils;
import com.surface.util.render.RenderUtils;
import com.surface.util.render.shader.ShaderElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.EnumChatFormatting;
import renderassist.animations.LinearAnimation;
import renderassist.rendering.BasicRendering;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TargetHUD extends Widget {
    private static final Minecraft mc = Minecraft.getMinecraft();
    ;

    public TargetHUD() {
        super(mc.getResolution().getScaledWidth() / 2f + 10, mc.getResolution().getScaledHeight() / 2f + 10, "Target HUD");
    }

    @Override
    public void render(int mouseX, int mouseY, float renderPartialTicks) {
        setX(mc.getResolution().getScaledWidth() / 2f + 10);
        setY(mc.getResolution().getScaledHeight() / 2f + 10);

        KillAuraModule killAura = (KillAuraModule) Wrapper.Instance.getModManager().getModFromName("Kill Aura");
        final float[] fractions = new float[]{0.0F, 0.5F, 1.0F};
        final Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};
        assert killAura != null;
        if (!killAura.isEnable()) return;
        if (killAura.targets.isEmpty()) return;

        float targetY = 0;

        List<EntityLivingBase> targets = new ArrayList<>(killAura.targets);


        for (EntityLivingBase entity : targets) {
            if (entity instanceof AbstractClientPlayer) {
                float y = targetY + this.y;
                AbstractClientPlayer target = (AbstractClientPlayer) entity;

                String displayName = entity.getName();
                double width = Math.max(100.0, 34.0 + FontManager.TAHOMA.getStringWidth(displayName));
                RenderUtils.drawRoundedRect(x, y, (float) width, 30, 6, new Color(0, 0, 0, 80).getRGB());
                ShaderElement.addBlurTask(() -> RenderUtils.drawRoundedRect(x, y, (float) width, 30, 6, new Color(0, 0, 0, 144).getRGB()));
                float absorptionAmount = entity.getAbsorptionAmount();
                int headColor = RenderUtils.reAlpha(Color.RED.getRGB(), target.hurtTime * 10);

                boolean hasNoSkin = true;

                NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(target.getUniqueID());

                if (playerInfo != null && playerInfo.getLocationSkin() != null) {
                    hasNoSkin = false;
                    BasicRendering.glColor(-1);

                    target.scaleAnimation = 60 + (40 - target.hurtTime * 2);

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x + 4 + 11, y + 4 + 11, 0);
                    GlStateManager.scale(target.scaleAnimation / 100, target.scaleAnimation / 100, 1);
                    GlStateManager.translate(-(x + 4 + 11), -(y + 4 + 11), 0);

                    GlStateManager.enableBlend();
                    mc.getTextureManager().bindTexture(playerInfo.getLocationSkin());
                    Gui.drawScaledCustomSizeModalRect(x + 4.0f, y + 4.0f, 8.0f, 8.0f, 8.0f, 8.0f, 22.0f, 22.0f, 64.0f, 64.0f);
                    if (target.isWearing(EnumPlayerModelParts.HAT)) {
                        Gui.drawScaledCustomSizeModalRect(x + 4.0f, y + 4.0f, 40.0f, 8.0f, 8.0f, 8.0f, 22.0f, 22.0f, 64.0f, 64.0f);
                    }
                    BasicRendering.drawRect(x + 4.0, y + 4.0, 22, 22, headColor);
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }

                if (hasNoSkin) {
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(1.5, 1.5, 1.5);
                    GlStateManager.popMatrix();
                }

                FontManager.WQY.setFontSize(18);
                FontManager.WQY.drawString(displayName, x + 30.0, y + 2.0, -1);

                RenderUtils.drawRoundedRect(x + 30.0, y + 13.0, (float) (width - 33f), 4, 3, new Color(0, 0, 0, 50).getRGB());

                if (!Double.isNaN(target.getHealth())) {
                    target.healthAnimation = LinearAnimation.animate(target.healthAnimation, (float) (((target.getHealth() + absorptionAmount) / (target.getMaxHealth() + absorptionAmount)) * (width - 33.0)), 16f / mc.getDebugFPS());
                    final float progress = target.getHealth() / target.getMaxHealth();
                    final Color healthColor = target.getHealth() >= 0.0f ? RenderUtils.blendColors(fractions, colors, progress).brighter() : Color.RED;
                    RenderUtils.drawRoundedRect(x + 30.0, y + 13.0, (float) (((target.getHealth() + absorptionAmount) / (target.getMaxHealth() + absorptionAmount)) * (width - 33.0)), 4, 2, healthColor.getRGB());
                    RenderUtils.drawRoundedRect(x + 30.0, y + 13.0, target.healthAnimation, 4, 2, RenderUtils.reAlpha(healthColor.getRGB(), 100));
                }

                StringBuilder healthText = new StringBuilder();

                healthText.append(NumberUtils.roundToString(1, target.getHealth())).append("HP");

                if (absorptionAmount != 0.0f) {
                    healthText.append("  ").append(EnumChatFormatting.YELLOW)
                            .append(NumberUtils.roundToString(1, absorptionAmount)).append("HP");
                }

                FontManager.TAHOMA.drawString(healthText.toString(), x + 30.0, y + 19.0, -1);

                targetY += 34;
            }
        }
    }
}
