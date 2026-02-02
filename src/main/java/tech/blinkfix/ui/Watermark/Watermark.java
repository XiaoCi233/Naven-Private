package tech.blinkfix.ui.Watermark;

import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.events.impl.EventShader;
import tech.blinkfix.utils.RenderUtils;
import tech.blinkfix.utils.SmoothAnimationTimer;
import tech.blinkfix.utils.StencilUtils;
import tech.blinkfix.utils.renderer.Fonts;
import tech.blinkfix.utils.renderer.text.CustomTextRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.yalan.live.LiveUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import tech.blinkfix.events.api.types.EventType;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Watermark {
    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    public static final int headerColor = new Color(150, 45, 45, 255).getRGB();
    public static final int bodyColor = new Color(0, 0, 0, 120).getRGB();
    public static final int backgroundColor = new Color(25, 25, 25, 130).getRGB();
    private static float width;
    private static float watermarkHeight;
    
    // Client 模式动画相关字段
    private static boolean paused = false;
    private static long pauseStart = 0L;
    private static long pauseDuration = 0L;
    private static boolean phaseW = false;
    private static boolean phaseH = false;
    private static SmoothAnimationTimer animW = new SmoothAnimationTimer(0);
    private static SmoothAnimationTimer animH = new SmoothAnimationTimer(0);
    
    /**
     * 获取显示的 FPS 值（支持 Fake FPS）
     */
    private static int getDisplayedFps(Minecraft mc, boolean fakeFps, float fakeFpsSize) {
        int actualFps = Integer.parseInt(StringUtils.split(mc.fpsString, " ")[0]);
        if (fakeFps) {
            return actualFps + (int) fakeFpsSize;
        }
        return actualFps;
    }
    
    /**
     * Client 模式动画更新
     */
    private static void updateClientAnimation(float maxW, float maxH) {
        animW.speed = 0.16f;
        animH.speed = 0.16f;
        
        if (paused) {
            if (Util.getMillis() - pauseStart >= pauseDuration) {
                paused = false;
            }
            return;
        }

        if (!phaseW && !phaseH) {
            animW.target = maxW;
            animW.update(true);
        } else if (phaseW && !phaseH) {
            animH.target = 1f;
            animH.update(false);
        } else if (phaseW && phaseH) {
            animW.target = 1f;
            animW.update(false);
        } else {
            animH.target = maxH;
            animH.update(true);
        }

        if (!phaseW && !phaseH && animW.value >= maxW - 0.1f) {
            phaseW = true;
            startPause(100);
        } else if (phaseW && !phaseH && animH.value <= 1.1f) {
            phaseH = true;
            long longPause = 800L;
            startPause(longPause);
        } else if (phaseW && phaseH && animW.value <= 1.1f) {
            phaseW = false;
            startPause(100);
        } else if (!phaseW && phaseH && animH.value >= maxH - 0.1f) {
            phaseH = false;
            startPause(400);
        }
    }
    
    /**
     * 开始暂停动画
     */
    private static void startPause(long duration) {
        paused = true;
        pauseStart = Util.getMillis();
        pauseDuration = duration;
    }
    
    public static void onShader(EventShader e, String style, float cornerRadius, float watermarkSize, float vPadding, boolean renderBlackBackground, boolean blackFont, boolean fakeFps, float fakeFpsSize) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if ("Capsule".equals(style) && e.getType() == EventType.BLUR) {
            CustomTextRenderer font = Fonts.opensans;
            Minecraft mc = Minecraft.getInstance();
            String clientName = "BlinkFix-NextGen";
            String username = LiveUtils.getCurrentUsername();
            int displayedFps = getDisplayedFps(mc, fakeFps, fakeFpsSize);
            String otherInfo = username + " | " + displayedFps + " FPS | " + format.format(new Date());

            float clientNameWidth = font.getWidth(clientName, (double)watermarkSize);
            float otherInfoWidth = font.getWidth(otherInfo, (double)watermarkSize);
            float height = (float)font.getHeight(true, (double)watermarkSize);

            float x = 5.0f, y = 5.0f;
            float hPadding = 7.0f;
            float spacing = 5.0f;
            float capsule_height = height + vPadding * 2;

            float capsule1_width = clientNameWidth + hPadding * 2;
            float capsule2_x = x + capsule1_width + spacing;
            float capsule2_width = otherInfoWidth + hPadding * 2;

            RenderUtils.drawRoundedRect(e.getStack(), x, y, capsule1_width, capsule_height, cornerRadius, Integer.MIN_VALUE);
            RenderUtils.drawRoundedRect(e.getStack(), capsule2_x, y, capsule2_width, capsule_height, cornerRadius, Integer.MIN_VALUE);
        }
    }

    public static void onRender(EventRender2D e, float watermarkSize, String style, boolean rainbow, float rainbowSpeed, float rainbowOffset, float cornerRadius, float vPadding, boolean renderBlackBackground, boolean blackFont, boolean fakeFps, float fakeFpsSize) {
        if ("Classic".equals(style)) {
            renderClassic(e, watermarkSize, cornerRadius, vPadding, fakeFps, fakeFpsSize);
        } else if ("Beta".equals(style)) {
            renderWanFan(e, watermarkSize, cornerRadius, vPadding);
        } else if ("Capsule".equals(style)) {
            renderCapsule(e, watermarkSize, cornerRadius, vPadding, renderBlackBackground, blackFont, fakeFps, fakeFpsSize);
        } else if ("exhibition".equals(style)) {
            renderExhibition(e, watermarkSize, rainbow, rainbowSpeed, rainbowOffset, fakeFps, fakeFpsSize);
        } else if ("skeet".equals(style)) {
            renderSkeet(e, watermarkSize, rainbow, rainbowSpeed, rainbowOffset, vPadding, fakeFps, fakeFpsSize);
        } else if ("Client".equals(style)) {
            renderClient(e, watermarkSize, fakeFps, fakeFpsSize);
        } else if ("Symmetry".equals(style)) {
            renderSymmetry(e, watermarkSize, fakeFps, fakeFpsSize);
        } else if ("BlinkFix".equals(style)) {
            renderBlinkFix(e, watermarkSize);
        } else if ("Jello".equals(style)) {
            renderJello(e, watermarkSize);
        } else {
            renderRainbow(e, watermarkSize, rainbow, rainbowSpeed, rainbowOffset, cornerRadius, vPadding, fakeFps, fakeFpsSize);
        }
    }

    /**
     * 绘制静态的彩虹条
     */
    private static void drawRainbowBar(PoseStack stack, float x, float y, float width, float height) {
        for (float i = 0; i < width; i++) {
            float hue = i / width;
            int color = Color.HSBtoRGB(hue, 0.8f, 1.0f);
            RenderUtils.fill(stack, x + i, y, x + i + 1, y + height, color);
        }
    }

    /**
     * 绘制动态的、与ArrayList同步的彩虹条
     */
    private static void drawAnimatedRainbowBar(PoseStack stack, float x, float y, float width, float height, float rainbowSpeed, float rainbowOffset) {
        for (float i = 0; i < width; i++) {
            int color = RenderUtils.getRainbowOpaque(
                    (int)(i * -rainbowOffset), 1.0F, 1.0F, (21.0F - rainbowSpeed) * 1000.0F
            );
            RenderUtils.fill(stack, x + i, y, x + i + 1, y + height, color);
        }
    }

    /**
     * 渲染 "Rainbow" 样式的Watermark
     */
    private static void renderRainbow(EventRender2D e, float watermarkSize, boolean rainbow, float rainbowSpeed, float rainbowOffset, float cornerRadius, float vPadding, boolean fakeFps, float fakeFpsSize) {
        CustomTextRenderer font = Fonts.opensans;
        Minecraft mc = Minecraft.getInstance();
        e.getStack().pushPose();

        String clientName = "BlinkFix-NextGen";
        String separator = " | ";
        String username = LiveUtils.getCurrentUsername();
        int displayedFps = getDisplayedFps(mc, fakeFps, fakeFpsSize);
        String otherInfo = username + " | " + displayedFps + " FPS | " + format.format(new Date());
        String fullText = clientName + separator + otherInfo;

        width = font.getWidth(fullText, (double)watermarkSize) + 14.0F;
        watermarkHeight = (float)font.getHeight(true, (double)watermarkSize);
        float x = 5.0f, y = 5.0f;
        float textX = x + 7.0f;
        float textY = y + vPadding;
        float totalHeight = watermarkHeight + vPadding * 2;
        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(e.getStack(), x, y, width, totalHeight, cornerRadius, Integer.MIN_VALUE);
        StencilUtils.erase(true);
        RenderUtils.drawRoundedRect(e.getStack(), x, y, width, totalHeight, cornerRadius, backgroundColor);
        if (rainbow) {
            drawAnimatedRainbowBar(e.getStack(), x, y, width, 2.0F, rainbowSpeed, rainbowOffset);
        } else {
            drawRainbowBar(e.getStack(), x, y, width, 2.0F);
        }
        if (rainbow) {
            float clientNameWidth = font.getWidth(clientName, (double)watermarkSize);
            float currentX = textX;
            for (char c : clientName.toCharArray()) {
                String character = String.valueOf(c);
                int color = RenderUtils.getRainbowOpaque(
                        (int)(currentX * -rainbowOffset / 5), 1.0F, 1.0F, (21.0F - rainbowSpeed) * 1000.0F
                );
                font.render(e.getStack(), character, currentX, textY, new Color(color), true, (double)watermarkSize);
                currentX += font.getWidth(character, (double)watermarkSize);
            }
            font.render(e.getStack(), separator + otherInfo, textX + clientNameWidth, textY, Color.WHITE, true, (double)watermarkSize);
        } else {
            float clientNameWidth = font.getWidth(clientName, (double)watermarkSize);
            int clientNameColor = new Color(110, 255, 110).getRGB();
            font.render(e.getStack(), clientName, textX, textY, new Color(clientNameColor), true, (double)watermarkSize);
            font.render(e.getStack(), separator + otherInfo, textX + clientNameWidth, textY, Color.WHITE, true, (double)watermarkSize);
        }

        StencilUtils.dispose();
        e.getStack().popPose();
    }

    /**
     * 渲染 "exhibition" 样式的Watermark
     */
    private static void renderExhibition(EventRender2D e, float watermarkSize, boolean rainbow, float rainbowSpeed, float rainbowOffset, boolean fakeFps, float fakeFpsSize) {
        CustomTextRenderer font = Fonts.opensans;
        Minecraft mc = Minecraft.getInstance();
        e.getStack().pushPose();

        String clientName = "BlinkFix-NextGen";
        String separator = " [";
        String username = LiveUtils.getCurrentUsername();
        int displayedFps = getDisplayedFps(mc, fakeFps, fakeFpsSize);
        String otherInfo = username + "] [" + displayedFps + " FPS] [" + format.format(new Date()) + "]";

        float x = 5.0f;
        float y = 5.0f;

        float currentX = x;

        String firstChar = String.valueOf(clientName.charAt(0));
        String restOfClientName = clientName.substring(1);

        // 渲染 'N'
        if (rainbow) {
            // 动态彩虹 'N'
            int color = RenderUtils.getRainbowOpaque(
                    (int)(currentX * -rainbowOffset / 5), 1.0F, 1.0F, (21.0F - rainbowSpeed) * 1000.0F
            );
            font.render(e.getStack(), firstChar, currentX, y, new Color(color), true, (double)watermarkSize);
        } else {
            // 静态彩虹 'N' (使用色相环的第一个颜色，红色)
            font.render(e.getStack(), firstChar, currentX, y, new Color(Color.HSBtoRGB(0f, 0.8f, 1f)), true, (double)watermarkSize);
        }

        currentX += font.getWidth(firstChar, (double)watermarkSize);

        // 渲染其余白色文本
        String restOfText = restOfClientName + separator + otherInfo;
        font.render(e.getStack(), restOfText, currentX, y, Color.WHITE, true, (double)watermarkSize);

        e.getStack().popPose();
    }

    /**
     * 渲染 "skeet" 样式的Watermark
     */
    private static void renderSkeet(EventRender2D e, float watermarkSize, boolean rainbow, float rainbowSpeed, float rainbowOffset, float vPadding, boolean fakeFps, float fakeFpsSize) {
        CustomTextRenderer font = Fonts.opensans;
        Minecraft mc = Minecraft.getInstance();
        e.getStack().pushPose();
        String username = LiveUtils.getCurrentUsername();
        int displayedFps = getDisplayedFps(mc, fakeFps, fakeFpsSize);
        String text = "BlinkFix | " + username + " | " + displayedFps + " FPS | " + format.format(new Date());

        float textWidth = font.getWidth(text, (double)watermarkSize);
        width = textWidth + 14.0F; // 7px padding on each side
        watermarkHeight = (float)font.getHeight(true, (double)watermarkSize);
        float borderWidth = 2.0f;
        float rainbowHeight = 1.0f;
        float topSectionHeight = borderWidth + rainbowHeight; // 顶部边框 + 彩虹条
        float totalHeight = topSectionHeight + watermarkHeight + vPadding * 2 + borderWidth; // 顶部区域 + 内容 + 底部边框

        float x = 5.0f;
        float y = 5.0f;

        int skeetBorderColor = new Color(45, 45, 45).getRGB();
        int skeetBackgroundColor = new Color(35, 35, 35).getRGB();

        // 绘制背景
        RenderUtils.fill(e.getStack(), x + borderWidth, y + topSectionHeight, x + width - borderWidth, y + totalHeight - borderWidth, skeetBackgroundColor);

        // 绘制边框
        // 顶部边框
        RenderUtils.fill(e.getStack(), x, y, x + width, y + borderWidth, skeetBorderColor);
        // 彩虹条
        if (rainbow) {
            drawAnimatedRainbowBar(e.getStack(), x, y + borderWidth, width, rainbowHeight, rainbowSpeed, rainbowOffset);
        } else {
            drawRainbowBar(e.getStack(), x, y + borderWidth, width, rainbowHeight);
        }
        // 左边框
        RenderUtils.fill(e.getStack(), x, y + borderWidth, x + borderWidth, y + totalHeight, skeetBorderColor);
        // 右边框
        RenderUtils.fill(e.getStack(), x + width - borderWidth, y + borderWidth, x + width, y + totalHeight, skeetBorderColor);
        // 底边框
        RenderUtils.fill(e.getStack(), x, y + totalHeight - borderWidth, x + width, y + totalHeight, skeetBorderColor);


        // 渲染文本
        float textX = x + 7.0f;
        float textY = y + topSectionHeight + vPadding;
        font.render(e.getStack(), text, textX, textY, Color.WHITE, true, (double)watermarkSize);

        e.getStack().popPose();
    }

    /**
     * 渲染 "Classic" 样式的Watermark
     */
    private static void renderClassic(EventRender2D e, float watermarkSize, float cornerRadius, float vPadding, boolean fakeFps, float fakeFpsSize) {
        CustomTextRenderer font = Fonts.opensans;
        Minecraft mc = Minecraft.getInstance();
        e.getStack().pushPose();
        String username = LiveUtils.getCurrentUsername();
        int displayedFps = getDisplayedFps(mc, fakeFps, fakeFpsSize);
        String text = "BlinkFix | " + username + " | " + displayedFps + " FPS | " + format.format(new Date());

        width = font.getWidth(text, (double)watermarkSize) + 14.0F;
        watermarkHeight = (float)font.getHeight(true, (double)watermarkSize);
        float totalHeight = 3.0f + watermarkHeight + vPadding * 2; // 3px 顶部栏 + 文本高度 + 上下边距

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(e.getStack(), 5.0F, 5.0F, width, totalHeight, cornerRadius, Integer.MIN_VALUE);
        StencilUtils.erase(true);
        RenderUtils.fill(e.getStack(), 5.0F, 5.0F, 5.0F + width, 8.0F, headerColor);
        RenderUtils.fill(e.getStack(), 5.0F, 8.0F, 5.0F + width, 5.0F + totalHeight, bodyColor);
        font.render(e.getStack(), text, 12.0, 8.0F + vPadding, Color.WHITE, true, (double)watermarkSize);
        StencilUtils.dispose();
        e.getStack().popPose();
    }

    /**
     * 渲染 "Capsule" 样式的Watermark
     */
    private static void renderCapsule(EventRender2D e, float watermarkSize, float cornerRadius, float vPadding, boolean renderBlackBackground, boolean blackFont, boolean fakeFps, float fakeFpsSize) {
        CustomTextRenderer font = Fonts.opensans;
        Minecraft mc = Minecraft.getInstance();
        e.getStack().pushPose();
        String username = LiveUtils.getCurrentUsername();
        String clientName = "BlinkFix-NextGen";
        int displayedFps = getDisplayedFps(mc, fakeFps, fakeFpsSize);
        String otherInfo = username + " | " + displayedFps + " FPS | " + format.format(new Date());

        float clientNameWidth = font.getWidth(clientName, (double)watermarkSize);
        float otherInfoWidth = font.getWidth(otherInfo, (double)watermarkSize);
        float height = (float)font.getHeight(true, (double)watermarkSize);

        float x = 5.0f, y = 5.0f;
        float hPadding = 7.0f;
        float spacing = 5.0f;
        float capsule_height = height + vPadding * 2;

        float capsule1_width = clientNameWidth + hPadding * 2;
        float capsule2_x = x + capsule1_width + spacing;
        float capsule2_width = otherInfoWidth + hPadding * 2;
        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(e.getStack(), x, y, capsule1_width, capsule_height, cornerRadius, Integer.MIN_VALUE);
        RenderUtils.drawRoundedRect(e.getStack(), capsule2_x, y, capsule2_width, capsule_height, cornerRadius, Integer.MIN_VALUE);
        StencilUtils.erase(true);
        if (renderBlackBackground) {
            RenderUtils.drawRoundedRect(e.getStack(), x, y, capsule1_width, capsule_height, cornerRadius, backgroundColor);
            RenderUtils.drawRoundedRect(e.getStack(), capsule2_x, y, capsule2_width, capsule_height, cornerRadius, backgroundColor);
        }
        Color textColor = blackFont ? Color.BLACK : Color.WHITE;
        font.render(e.getStack(), clientName, x + hPadding, y + vPadding, textColor, true, (double)watermarkSize);
        font.render(e.getStack(), otherInfo, capsule2_x + hPadding, y + vPadding, textColor, true, (double)watermarkSize);

        StencilUtils.dispose();
        e.getStack().popPose();
    }
    
    /**
     * 渲染 "Client" 样式的Watermark（使用原始的渲染和动画逻辑）
     */
    private static void renderClient(EventRender2D e, float watermarkSize, boolean fakeFps, float fakeFpsSize) {
        CustomTextRenderer font = Fonts.opensans;
        Minecraft mc = Minecraft.getInstance();
        
        int displayedFps = getDisplayedFps(mc, fakeFps, fakeFpsSize);
        String fpsText = "FPS:" + displayedFps;
        String clientName = "BlinkFix-NextGen";
        
        // 第一步：先渲染客户端名称（作为背景层，会被动画覆盖）
        font.render(e.getStack(), clientName, 4, 3, Color.WHITE, true, (double)watermarkSize);
        
        // 计算最大尺寸（使用客户端名称来计算宽度）
        float maxW = font.getWidth(clientName, (double)watermarkSize) + 10;
        float maxH = (float)(font.getHeight(true, (double)watermarkSize) + 2);
        
        // 第二步：更新动画
        updateClientAnimation(maxW, maxH);
        
        // 第三步：绘制动画背景（会覆盖客户端名称）
        RenderUtils.drawRoundedRect(e.getStack(), 1, 1, animW.value, animH.value, 2, new Color(28, 228, 228, 255).getRGB());
        
        // 第四步：使用裁剪在动画区域内渲染FPS文本
        RenderUtils.scissorStart(1, 1, animW.value, animH.value);
        font.render(e.getStack(), fpsText, 4, 3, Color.WHITE, true, (double)(watermarkSize * 0.95));
        RenderUtils.scissorEnd();
    }
    
    /**
     * 渲染 "Symmetry" 样式的Watermark（原 "Styles Alpha" 模式）
     */
    private static void renderSymmetry(EventRender2D e, float watermarkSize, boolean fakeFps, float fakeFpsSize) {
        CustomTextRenderer font = Fonts.harmony;
        String clientName = "BlinkFix-NextGen";
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        String username = LiveUtils.getCurrentUsername();
        String rightInfo = username + " | " + time;
        
        // 计算宽度
        float clientNameWidth = font.getWidth(clientName, 0.38);
        float rightInfoWidth = font.getWidth(rightInfo, 0.69);
        float width = clientNameWidth + 10 + rightInfoWidth;
        float height = (float) font.getHeight(false, 0.5);
        
        // 绘制背景
        RenderUtils.drawRoundedRect(e.getStack(), 4, 5, width, height, 2, new Color(0, 0, 0, 100).getRGB());
        
        // 绘制左侧高亮区域
        float highlightWidth = clientNameWidth + 19;
        RenderUtils.scissorStart(4, 5, highlightWidth, height + 1);
        RenderUtils.drawRoundedRect(e.getStack(), 4, 5, width, height, 2, new Color(255, 255, 255, 50).getRGB());
        RenderUtils.scissorEnd();
        
        // 渲染文本
        float xPos = 12;
        float yPos = 8;
        
        // 渲染 "SYMMETRY" 文本，分成两部分："SYMMET"（蓝色）和 "RY"（灰色）
        String firstPart = "BlinkFix-NextGen";
        String secondPart = "";
        font.render(e.getStack(), firstPart, xPos, yPos, new Color(150, 45, 45, 255), true, 0.36);
        float firstPartWidth = font.getWidth(firstPart + " ", 0.36);
        font.render(e.getStack(), secondPart, xPos + firstPartWidth, yPos, Color.gray.brighter(), true, 0.36);
        
        // 渲染右侧信息
        float rightX = 4 + clientNameWidth + 40;
        font.render(e.getStack(), rightInfo, rightX, yPos + 0.5f, Color.WHITE, true, 0.34);
    }
    
    /**
     * 渲染 "BlinkFix" 样式的Watermark（简单在左上角显示客户端名字）
     */
    private static void renderBlinkFix(EventRender2D e, float watermarkSize) {
        CustomTextRenderer font = Fonts.test;
        String clientName = "BlinkFix-NextGen";
        
        // 在左上角渲染客户端名字
        font.render(e.getStack(), clientName, 4, 3, Color.WHITE, true, (double)watermarkSize);
    }
    
    /**
     * 渲染 "Jello" 样式的Watermark（两行文本，主标题和副标题）
     */
    private static void renderJello(EventRender2D e, float watermarkSize) {
        CustomTextRenderer tenacity = Fonts.tenacity;
        e.getStack().pushPose();
        
        String mainText = "BlinkFix";
        String subText = "NextGen";
        
        // 渲染主文本
        tenacity.render(e.getStack(), mainText, 10.0f, 10.0f, Color.white, true, 0.75f);
        
        // 计算主文本的宽度和高度
        double mainWidth = tenacity.getWidth(mainText, true, 0.75f);
        double mainHeight = tenacity.getHeight(true, 0.75f);
        double subHeight = tenacity.getHeight(true, 0.5f);
        
        // 渲染副文本，位置在主文本右侧，垂直对齐
        tenacity.render(e.getStack(), subText, 11.0f + (float)mainWidth, 10.0f + (float)mainHeight - (float)subHeight - 1.0f, Color.white, true, 0.5f);
        
        e.getStack().popPose();
    }
    private static void renderWanFan(EventRender2D e, float watermarkSize, float cornerRadius, float vPadding) {
        CustomTextRenderer font = Fonts.opensans;
        Minecraft mc = Minecraft.getInstance();
        e.getStack().pushPose();

        String textLeft = "WanFan-XD";
        String textRight = "ZKM25deobf";

        float x = 5.0f, y = 5.0f;
        float hPadding = 8.0f;
        float spacing = 6.0f;

        float leftW = font.getWidth(textLeft, (double)watermarkSize);
        float rightW = font.getWidth(textRight, (double)watermarkSize);
        float textH = (float) font.getHeight(true, (double) watermarkSize);
        float totalH = textH + vPadding * 2.0f;
        int purple = new Color(180, 0, 255, 140).getRGB();
        int purpleSolid = new Color(180, 0, 255, 255).getRGB();

        float totalW = leftW + rightW + hPadding * 2.0f + spacing + 16.0f;

        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(e.getStack(), x, y, totalW, totalH, cornerRadius, Integer.MIN_VALUE);
        StencilUtils.erase(true);
        RenderUtils.drawRoundedRect(e.getStack(), x, y, totalW, totalH, cornerRadius, purple);

        float stripeW = 3.0f;
        float stripeInset = 10.0f;
        float stripeX1 = x + stripeInset;
        float stripeX2 = stripeX1 + stripeW;
        float stripeTop = y + 3.0f;
        float stripeBottom = y + totalH - 3.0f;
        float skew = 4.0f;
        RenderUtils.drawTriangle(stripeX1, stripeTop, stripeX2, stripeTop, stripeX2 + skew, stripeBottom, purpleSolid);
        RenderUtils.drawTriangle(stripeX1, stripeTop, stripeX2 + skew, stripeBottom, stripeX1 + skew, stripeBottom, purpleSolid);

        float gap = 6.0f;
        float paraH = totalH - 2 * 3.0f;
        float paraY = y + 3.0f;
        float paraX = stripeX2 + gap;
        float paraW = rightW + 14.0f;
        RenderUtils.drawTriangle(paraX + 8.0f, paraY, paraX + paraW, paraY, paraX + paraW - 8.0f, paraY + paraH, purpleSolid);
        RenderUtils.drawTriangle(paraX, paraY, paraX + 8.0f, paraY, paraX, paraY + paraH, purpleSolid);
        RenderUtils.drawRectBound(e.getStack(), paraX, paraY, paraW - 8.0f, paraH, purpleSolid);

        float textLeftX = x + hPadding;
        float textLeftY = y + vPadding;
        float textRightX = paraX + 6.0f;
        float textRightY = y + vPadding;
        font.drawString(e.getStack(), textLeft, textLeftX, textLeftY, Color.WHITE, true, (double)watermarkSize);
        font.drawString(e.getStack(), textRight, textRightX, textRightY, Color.WHITE, true, (double)watermarkSize);

        StencilUtils.dispose();
        e.getStack().popPose();
    }
}