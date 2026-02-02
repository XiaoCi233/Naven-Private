//package com.heypixel.heypixelmod.modules.impl.render;
//
//import modules.tech.blinkfix.Category;
//import modules.tech.blinkfix.Module;
//import modules.tech.blinkfix.ModuleInfo;
//import utils.tech.blinkfix.RenderUtils;
//import renderer.utils.tech.blinkfix.Fonts;
//import impl.values.tech.blinkfix.BooleanValue;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.Entity;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import java.awt.Color;
//@ModuleInfo(
//        name = "Widget",
//        description = "Automatically attacks entities",
//        category = Category.RENDER
//)
//
//public class Widget extends Module {
//
//    private BooleanValue enabled;
//    private float animationProgress = 1.0F; // 动画进度，0~1，可根据需求增加动画逻辑
//
//    public void TargetHUDWidget(BooleanValue enabled) {
//        this.enabled = enabled;
//    }
//
//    public Widget() {
//        this.enabled = enabled;
//    }
//
//    public void render(PoseStack stack, Entity target) {
//        if (!enabled.getCurrentValue() || target == null) return;
//        if (!(target instanceof LivingEntity living)) return;
//
//        Minecraft mc = Minecraft.getInstance();
//        Font font = mc.font;
//
//        // 固定位置
//        float x = mc.getWindow().getGuiScaledWidth() / 2.0F + 10.0F;
//        float y = mc.getWindow().getGuiScaledHeight() / 2.0F + 10.0F;
//
//        // 显示名字，带 Baby 标识
//        String targetName = target.getName().getString() + (living.isBaby() ? " (Baby)" : "");
//
//        // 背景宽度
//        float width = Math.max(font.width(targetName) + 10.0F, 60.0F);
//        float height = 30.0F;
//
//        // 渲染动画缩放
//        stack.pushPose();
//        float centerX = x + width / 2;
//        float centerY = y + height / 2;
//        stack.translate(centerX, centerY, 0);
//        stack.scale(animationProgress, animationProgress, 1);
//        stack.translate(-centerX, -centerY, 0);
//
//        // 背景矩形（可以换成你自己的 RenderUtils）
//        RenderUtils.drawRoundedRect(stack, x, y, width, height, 5.0F, HUD.headerColor);
//
//        // 血量条
//        float healthRatio = living.getHealth() / living.getMaxHealth();
//        RenderUtils.fillBound(stack, x, y + height - 3, width * healthRatio, 3, HUD.headerColor);
//
//        stack.popPose();
//
//        // 渲染文字
//        Fonts.harmony.render(stack, targetName, (int) (x + 5), (int) (y + 6), Color.WHITE.getRGB());
//        Fonts.harmony.render(stack,
//                "HP: " + Math.round(living.getHealth()) +
//                        (living.getAbsorptionAmount() > 0 ? "+" + Math.round(living.getAbsorptionAmount()) : ""),
//                (int) (x + 5),
//                (int) (y + 17),
//                Color.WHITE.getRGB()
//        );
//    }
//
//}
