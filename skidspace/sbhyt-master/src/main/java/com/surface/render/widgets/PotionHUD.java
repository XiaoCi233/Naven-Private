package com.surface.render.widgets;

import com.surface.render.font.FontManager;
import com.surface.util.render.RenderUtils;
import com.surface.util.render.shader.ShaderElement;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import renderassist.animations.LinearAnimation;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.surface.Wrapper.mc;

public class PotionHUD extends Widget {
    public PotionHUD() {
        super(150, 100, "PotionHUD");
    }


    private final Map<Integer, Integer> potionMaxDurations = new HashMap<>();
    private float heightanimation = 0;
    List<PotionEffect> effects = new ArrayList<>();

    @Override
    public void render(int mouseX, int mouseY, float renderPartialTicks) {
        setX(6);
        setY(26);
        FontManager.WQY.setFontSize(14);
        effects = mc.thePlayer.getActivePotionEffects().stream()
                .sorted(Comparator.comparingInt((PotionEffect it) -> FontManager.WQY.getStringWidth(
                        get(it))
                ))
                .collect(Collectors.toList());
        int offsetX = 22;
        int offsetY = 14;
        int i2 = 16;
        final ArrayList<Integer> needRemove = new ArrayList<Integer>();
        for (final Map.Entry<Integer, Integer> entry : this.potionMaxDurations.entrySet()) {
            if (mc.thePlayer.getActivePotionEffect(Potion.potionTypes[entry.getKey()]) == null) {
                needRemove.add(entry.getKey());
            }
        }
        for (final int id : needRemove) {
            this.potionMaxDurations.remove(id);
        }
        for (final PotionEffect effect : effects) {
            if (!this.potionMaxDurations.containsKey(effect.getPotionID()) || this.potionMaxDurations.get(effect.getPotionID()) < effect.getDuration()) {
                this.potionMaxDurations.put(effect.getPotionID(), effect.getDuration());
            }
        }
        float width = 28;
        float height = effects.size() * 32;
        heightanimation = LinearAnimation.animate(heightanimation, height, 0.5f);
        if (!effects.isEmpty()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            int l = 32;

            ShaderElement.addBlurTask(() -> RenderUtils.drawRoundedRect(x, y, width, (int) heightanimation, 5F, new Color(0, 0, 0, 144).getRGB()));

            RenderUtils.drawRoundedRect(x, y, width, (int) heightanimation, 5F, new Color(0, 0, 0, 144).getRGB());

            RenderUtils.startGlScissor((int) x, (int) y, (int) width, (int) heightanimation);
            for (PotionEffect potioneffect : effects) {
                Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                if (effects.contains(potioneffect)) {
                    if (potioneffect.getDuration() > effects.get(effects.indexOf(potioneffect)).getDuration()) {
                        potioneffect.animation.addRipple(x, y, 100, 0.5f);
                    }
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                if (potion.hasStatusIcon()) {
                    mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                    int i1 = potion.getStatusIconIndex();
                    GlStateManager.enableBlend();

                    mc.ingameGUI.drawTexturedModalRect((x + offsetX) - 17, (y + i2) - offsetY + 2, 0 + i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                }

                String s = Potion.getDurationString(potioneffect);
                FontManager.WQY.drawCenteredString(s, width - 8, (y + i2 + 17) - offsetY + 2, -1);
                potioneffect.animation.draw(() -> RenderUtils.drawRoundedRect(x, y, width, (int) heightanimation, 5F, new Color(255, 255, 255, 255).getRGB()));

                i2 += l;
            }
            RenderUtils.stopGlScissor();
        }
    }

    private String get(PotionEffect potioneffect) {
        Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
        String s1 = I18n.format(potion.getName(), new Object[0]);
        s1 = s1 + " " + intToRomanByGreedy(potioneffect.getAmplifier() + 1);
        return s1;
    }

    private String intToRomanByGreedy(int num) {
        final int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        final String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < values.length && num >= 0; i++)
            while (values[i] <= num) {
                num -= values[i];
                stringBuilder.append(symbols[i]);
            }

        return stringBuilder.toString();
    }
}
