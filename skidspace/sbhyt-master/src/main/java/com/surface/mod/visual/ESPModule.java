package com.surface.mod.visual;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.Event2D;
import com.surface.events.EventWorldLoad;
import com.surface.mod.Mod;
import com.surface.mod.fight.KillAuraModule;
import com.surface.mod.world.HackerDetectorModule;
import com.surface.render.font.FontManager;
import com.surface.util.HYTUtils;
import com.surface.util.render.RenderUtils;
import com.surface.util.render.WorldToScreenUtils;
import com.surface.util.struct.ShitData;
import com.surface.value.impl.BooleanValue;
import com.surface.value.impl.ColorValue;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ESPModule extends Mod {

    private final BooleanValue health = new BooleanValue("Health bar", true);
    private final ColorValue healthColor1 = new ColorValue("Health Color 1", Color.GREEN) {
        @Override
        public boolean isVisible() {
            return health.getValue();
        }
    }.rainbow(true);
    private final ColorValue healthColor2 = new ColorValue("Health Color 2", Color.RED) {
        @Override
        public boolean isVisible() {
            return health.getValue();
        }
    }.rainbow(true);

    public final BooleanValue username = new BooleanValue("Username", true);
    private final BooleanValue item = new BooleanValue("Item", true);
    private final BooleanValue shitDetector = new BooleanValue("Artifact", true);
    private final Map<UUID, ShitData> artifacts = new HashMap<>();

    public ESPModule() {
        super("ESP", Category.VISUAL);
        registerValues(health, healthColor1, healthColor2, username, item, shitDetector);
    }

    @EventTarget
    public void onWorldLoad(EventWorldLoad event) {
        this.artifacts.clear();
    }

    @EventTarget
    public void onRender2D(Event2D event) {
        FontManager.WQY.setFontSize(18);
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == mc.thePlayer) continue;
            if (entity instanceof EntityPlayer) {

                EntityPlayer entityPlayer = (EntityPlayer) entity;
                if (shitDetector.getValue()) {
                    if (!artifacts.containsKey(entityPlayer.getUniqueID()))
                        artifacts.put(entityPlayer.getUniqueID(), new ShitData());

                    ShitData data = artifacts.get(entityPlayer.getUniqueID());

                    if (HYTUtils.isHoldingGodAxe(entityPlayer) && !data.shouldDisplay.contains(ShitData.axe))
                        data.shouldDisplay.add(ShitData.axe);
                    if (HYTUtils.isHoldingKBBall(entityPlayer) && !data.shouldDisplay.contains(ShitData.ball))
                        data.shouldDisplay.add(ShitData.ball);
                    if (HYTUtils.isHoldingEnchantedGoldenApple(entityPlayer) && !data.shouldDisplay.contains(ShitData.apple))
                        data.shouldDisplay.add(ShitData.apple);
                    if (HYTUtils.isHoldingFireball(entityPlayer) && !data.shouldDisplay.contains(ShitData.fireball))
                        data.shouldDisplay.add(ShitData.fireball);
                    if (HYTUtils.isStrength(entityPlayer) > 0) {
                        if (!data.shouldDisplay.contains(ShitData.strength))
                            data.shouldDisplay.add(ShitData.strength);
                    } else {
                        data.shouldDisplay.remove(ShitData.strength);
                    }
                    if (HYTUtils.hasEatenGoldenApple(entityPlayer) > 0) {
                        if (!data.shouldDisplay.contains(ShitData.effect))
                            data.shouldDisplay.add(ShitData.effect);
                    } else {
                        data.shouldDisplay.remove(ShitData.effect);
                    }
                }

                WorldToScreenUtils.onRender2D(event, entity, (posX, posY, finalX, endPosY) -> {
                    if (health.getValue()) {
                        final double hpHeight = (endPosY - posY) * Math.min(Math.max(entityPlayer.getHealth() / entityPlayer.getMaxHealth(), 0), 1);
                        if (entityPlayer.getHealth() > 0) {
                            Gui.drawRect((float) (posX - 4), (float) (posY - .5), (float) (posX - 2), (float) (endPosY + .5), new Color(0, 0, 0, 70).getRGB());
                            RenderUtils.drawVGradientRect(posX - 3.5, endPosY - hpHeight, 1, hpHeight, healthColor1.getRGB(), healthColor2.getRGB());
                        }
                    }

                    HackerDetectorModule hackerDetector = (HackerDetectorModule) Wrapper.Instance.getModManager().getModFromName("Hacker Detector");
                    KillAuraModule aura = (KillAuraModule) Wrapper.Instance.getModManager().getModFromName("Kill Aura");
                    if (username.getValue()) {
                        FontManager.WQY.drawStringWithShadow(((aura.isOnSameTeam(entityPlayer) && aura.sbteams.getValue()) || (aura.armorTeam(entityPlayer) && aura.armorteams.getValue()) ? EnumChatFormatting.GREEN + "Teammate " + EnumChatFormatting.RESET : "") + (hackerDetector.isHacker(entityPlayer) ? EnumChatFormatting.RED + "Hacker " + EnumChatFormatting.RESET : "") + entityPlayer.getDisplayName().getFormattedText() + " " + entityPlayer.cheatingVL, posX + ((finalX - posX) / 2f) - FontManager.WQY.getStringWidth2(entityPlayer.getDisplayName().getFormattedText()) / 2f, posY - FontManager.WQY.getHeight() - 2, -1);
                    }
                    if (item.getValue()) {
                        if (entityPlayer.getCurrentEquippedItem() != null)
                            FontManager.WQY.drawStringWithShadow(entityPlayer.getCurrentEquippedItem().getDisplayName(), posX + ((finalX - posX) / 2f) - FontManager.WQY.getStringWidth2(entityPlayer.getCurrentEquippedItem().getDisplayName()) / 2f, endPosY + 2, -1);
                    }
                    if (shitDetector.getValue()) {
                        ShitData data = artifacts.get(entityPlayer.getUniqueID());
                        float y = (float) posY;
                        for (ItemStack stack : data.shouldDisplay) {
                            RenderUtils.drawItemStack(stack, (float) finalX, y);
                            y += 18;
                        }
                    }
                });
            }
        }
    }
}
