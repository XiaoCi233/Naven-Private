package tech.blinkfix.ui.CooldownBar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import tech.blinkfix.utils.RenderUtils;
import tech.blinkfix.utils.SmoothAnimationTimer;
import tech.blinkfix.utils.renderer.Fonts;

import java.awt.*;

public class CooldownBar {
    private static final Minecraft mc = Minecraft.getInstance();
    private final static int mainColor = new Color(150, 45, 45, 255).getRGB();
    private final SmoothAnimationTimer animation = new SmoothAnimationTimer(0, 0.2f);
    private final SmoothAnimationTimer yAnimation = new SmoothAnimationTimer(0, 0.2f);
    private final SmoothAnimationTimer startAnimation = new SmoothAnimationTimer(0, 0.3f);

    private long time, createTime;
    private String title;

    public CooldownBar(long time, String title) {
        this.time = time;
        this.createTime = System.currentTimeMillis();
        this.title = title;
    }

    public float getState() {
        return (System.currentTimeMillis() - createTime) / (float) time;
    }

    public void render(GuiGraphics guiGraphics, PoseStack poseStack) {
        float state = getState();

        if (state >= 1.0f) {
            startAnimation.target = 0;
        } else {
            startAnimation.target = 60;
        }

        startAnimation.update(true);
        animation.target = state;
        animation.update(true);
        poseStack.pushPose();
        float width = Fonts.opensans.getWidth(title, 0.35f);
        Fonts.opensans.render(poseStack, title, mc.getWindow().getGuiScaledWidth()/2f - 50 + 50 - width / 2f, yAnimation.value + 75, Color.WHITE, true, 0.35f);

        RenderUtils.drawRoundedRect(poseStack, 0, 13, 100, 5, 2, 0x80000000);

        float progressWidth = 100 * (1 - Math.min(animation.value, 1.0f));
        if (progressWidth > 0) {
            RenderUtils.drawRoundedRect(poseStack, 0, 13, progressWidth, 5, 2, mainColor);
        }

        poseStack.popPose();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createTime > time && yAnimation.isAnimationDone(true) && startAnimation.isAnimationDone(true);
    }

    public SmoothAnimationTimer getYAnimation() {
        return yAnimation;
    }
}
