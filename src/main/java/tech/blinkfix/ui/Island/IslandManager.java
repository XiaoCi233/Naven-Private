package tech.blinkfix.ui.Island;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ServerData;
import dev.yalan.live.LiveClient;
import tech.blinkfix.BlinkFix;
import tech.blinkfix.utils.AnimationUtils;
import tech.blinkfix.utils.RenderUtils;
import tech.blinkfix.utils.SmoothAnimationTimer;
import tech.blinkfix.utils.StencilUtils;
import tech.blinkfix.utils.renderer.Fonts;
import tech.blinkfix.events.impl.FpsConfig;
import org.apache.commons.lang3.StringUtils;
import tech.blinkfix.modules.impl.render.Island;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class IslandManager {
    protected static final Minecraft mc = Minecraft.getInstance();

    protected static final SmoothAnimationTimer animW = new SmoothAnimationTimer(Fonts.tenacity.getWidth("BlinkFix | " + "Username" + " | " + "111" + " Fps | " + "HH:mm:ss",0.4) + 12, 0.5f);
    protected static final SmoothAnimationTimer animH = new SmoothAnimationTimer((float) Fonts.tenacity.getHeight(false, 0.4f), 0.5f);

    private static float animWVelocity = 0f;
    private static float animHVelocity = 0f;

    private final SmoothAnimationTimer posX = new SmoothAnimationTimer(0, 0.5f);
    private final SmoothAnimationTimer posY = new SmoothAnimationTimer(0, 0.5f);

    private final List<IslandContent> contents = new ArrayList<>();

    public IslandManager() {
    }
    
    /**
     * 获取 Island 模块的偏移值
     */
    private float[] getOffsets() {
        try {
            Island islandModule =
                (Island) BlinkFix.getInstance()
                    .getModuleManager().getModule(Island.class);
            if (islandModule != null) {
                return new float[]{
                    islandModule.xOffset.getCurrentValue(),
                    islandModule.yOffset.getCurrentValue()
                };
            }
        } catch (Exception ignored) {
        }
        // 如果无法获取模块，返回默认值（0偏移）
        return new float[]{0.0f, 0.0f};
    }

    public void addContent(IslandContent content) {
        this.contents.add(content);
    }

    public void removeContent(IslandContent content) {
        this.contents.remove(content);
    }

    /**
     * 获取当前应该显示的内容（优先级最高的可见内容）
     */
    private IslandContent getActiveContent() {
        return contents.stream()
                .filter(IslandContent::shouldDisplay)
                .max(Comparator.comparingInt(IslandContent::getPriority))
                .orElse(null);
    }

    /**
     * 获取当前活动内容（优先级最高的可见内容）的尺寸
     */
    private float[] getActiveDimensions() {
        IslandContent activeContent = getActiveContent();

        if (activeContent != null) {
            // 如果有活动内容，使用活动内容的尺寸
            return new float[]{activeContent.getWidth(), activeContent.getHeight()};
        } else {
            String username = LiveClient.INSTANCE.liveUser != null ? LiveClient.INSTANCE.liveUser.getName() : "Player";
            // 使用 FpsConfig 获取显示的 FPS（支持 Fake FPS）
            int actualFps = Integer.parseInt(StringUtils.split(mc.fpsString, " ")[0]);
            int displayedFps = FpsConfig.getDisplayedFps(actualFps);
            String text = ChatFormatting.RED + "BlinkFix" + ChatFormatting.WHITE + " · " + username + ChatFormatting.WHITE + " · " + getCurrentServerIP() + ChatFormatting.WHITE + " · "  + displayedFps + " Fps";
            return new float[]{Fonts.tenacity.getWidth(text,0.4) + 12, (float) Fonts.tenacity.getHeight(false, 0.4f) + 10};
        }
    }

    public void renderShader(GuiGraphics graphics) {
        IslandContent activeContent = getActiveContent();
        if (activeContent == null) {
            // 显示默认背景
            RenderUtils.drawRoundedRect(graphics.pose(), posX.value, posY.value, animW.value, animH.value, 8, new Color(0, 0, 0, 160).getRGB());
        } else {
            // 显示活动内容的背景
            RenderUtils.drawRoundedRect(graphics.pose(), posX.value, posY.value, animW.value, animH.value, 8, new Color(0, 0, 0, 160).getRGB());
        }
    }

    public void render(GuiGraphics graphics) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float[] dimensions = getActiveDimensions();
        float targetWidth = dimensions[0];
        float targetHeight = dimensions[1];

        animW.target = targetWidth;
        animH.target = targetHeight;

        updateWithBounce(animW, targetWidth, true);
        updateWithBounce(animH, targetHeight, false);

        // 获取偏移值
        float[] offsets = getOffsets();
        float xOffset = offsets[0];
        float yOffset = offsets[1];
        
        // 计算基础位置（默认居中，Y为屏幕高度的5%）
        float baseX = (screenWidth - animW.value) / 2.0f;
        float baseY = screenHeight * 0.05f;
        
        // 应用偏移值
        float x = baseX + xOffset;
        float y = baseY + yOffset;

        posX.target = x;
        posY.target = y;
        posX.speed = 1f;
        posY.speed = 1f;
        posX.update(true);
        posY.update(true);

        IslandContent activeContent = getActiveContent();

        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(graphics.pose(), posX.value, posY.value, animW.value, animH.value, 5, 0xFFFFFFFF);
        StencilUtils.erase(true);

        RenderUtils.drawRoundedRect(graphics.pose(), posX.value, posY.value, animW.value, animH.value, 8, new Color(20, 20, 20, 160).getRGB());

        if (activeContent != null) {
            activeContent.render(graphics, graphics.pose(), posX.value, posY.value);
        } else {
            renderDefaultContent(graphics, graphics.pose());
        }

        StencilUtils.dispose();
    }

    public String getCurrentServerIP() {
        ServerData serverData = Minecraft.getInstance().getCurrentServer();
        if (serverData != null) {
            return serverData.ip;
        }
        return "SinglePlayer";
    }

    private void renderDefaultContent(GuiGraphics graphics, PoseStack stack) {
        String username = LiveClient.INSTANCE.liveUser != null ? LiveClient.INSTANCE.liveUser.getName() : "Player";
        // 使用 FpsConfig 获取显示的 FPS（支持 Fake FPS）
        int actualFps = Integer.parseInt(StringUtils.split(mc.fpsString, " ")[0]);
        int displayedFps = FpsConfig.getDisplayedFps(actualFps);
        String text = ChatFormatting.RED + "BlinkFix" + ChatFormatting.WHITE + " · " + username + ChatFormatting.WHITE + " · " + getCurrentServerIP() + ChatFormatting.WHITE + " · "  + displayedFps + " fps";
        Fonts.tenacity.render(stack,text, getPosX() + 6, getPosY() + 5, new Color(-1), true, 0.4f);
    }

    public float getPosX() {
        return posX.value;
    }

    public float getPosY() {
        return posY.value;
    }

    public static SmoothAnimationTimer getAnimW() {
        return animW;
    }

    public static SmoothAnimationTimer getAnimH() {
        return animH;
    }

    /**
     * 带弹性势能的回弹动画更新方法
     * 使用弹簧-阻尼振荡模型，会产生明显的弹出去再弹回来的效果
     */
    private static void updateWithBounce(SmoothAnimationTimer timer, float target, boolean isWidth) {
        float current = timer.value;
        float deltaTime = Math.max(0.001f, AnimationUtils.delta / 1000.0f); // 转换为秒，防止除零

        float velocity;
        if (isWidth) {
            velocity = animWVelocity;
        } else {
            velocity = animHVelocity;
        }

        float displacement = target - current;
        float springForce = displacement * 10;

        float acceleration = springForce;

        velocity = velocity + acceleration * deltaTime;

        velocity = velocity * 0.75f;

        float newValue = current + velocity * deltaTime * 60f;

        if (isWidth) {
            animWVelocity = velocity;
        } else {
            animHVelocity = velocity;
        }

        float positionThreshold = 0.1f;
        float velocityThreshold = 0.02f;
        if (Math.abs(target - newValue) < positionThreshold && Math.abs(velocity) < velocityThreshold) {
            newValue = target;
            if (isWidth) {
                animWVelocity = 0f;
            } else {
                animHVelocity = 0f;
            }
        }
        
        timer.value = newValue;
    }
}
