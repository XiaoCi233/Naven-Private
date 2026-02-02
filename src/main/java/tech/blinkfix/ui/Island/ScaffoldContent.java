package tech.blinkfix.ui.Island;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import tech.blinkfix.BlinkFix;
import tech.blinkfix.modules.impl.move.Scaffold;
import tech.blinkfix.utils.RenderUtils;
import tech.blinkfix.utils.SmoothAnimationTimer;
import tech.blinkfix.utils.renderer.Fonts;

import java.awt.*;

/**
 * Scaffold 状态内容 - 显示 Scaffold 模块的状态和方块信息
 * 优先级: 30
 */
public class ScaffoldContent implements IslandContent {
    private static final Minecraft mc = Minecraft.getInstance();
    private double bps = 0.0;
    private final SmoothAnimationTimer progressAnimation = new SmoothAnimationTimer(0.0f, 0.3f); // 进度条平滑动画
    
    @Override
    public int getPriority() {
        return 100;
    }
    
    @Override
    public boolean shouldDisplay() {
        Scaffold scaffold = getScaffoldModule();
        return scaffold != null && scaffold.isEnabled();
    }
    
    private Scaffold getScaffoldModule() {
        try {
            return (Scaffold) BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    private int getBlockCount() {
        Scaffold scaffold = getScaffoldModule();
        if (scaffold != null) {
            return scaffold.getBlockCount();
        }
        return 0;
    }
    
    /**
     * 计算玩家移动速度（Blocks Per Second）
     * 适配 1.20.1 版本
     */
    private double speedCalculator() {
        if (mc.player != null) {
            Player player = mc.player;
            
            // 获取当前位置和上一帧位置
            double deltaX = player.getX() - player.xo;
            double deltaZ = player.getZ() - player.zo;
            
            // 获取 timer 速度（使用反射或直接使用默认值1.0）
            double timerSpeed = 1.0;
            try {
                java.lang.reflect.Field timerField = Minecraft.class.getDeclaredField("timer");
                timerField.setAccessible(true);
                Object timer = timerField.get(mc);
                java.lang.reflect.Field speedField = timer.getClass().getDeclaredField("timerSpeed");
                speedField.setAccessible(true);
                timerSpeed = speedField.getFloat(timer);
            } catch (Exception ignored) {
            }
            
            // 计算每秒方块数（1 tick = 1/20 秒）
            double bps = Math.hypot(deltaX, deltaZ) * timerSpeed * 20.0;
            
            return Math.round(bps * 100.0) / 100.0;
        } else {
            return 0.00;
        }
    }
    
    private void updateBps() {
        bps = speedCalculator();
    }
    
    @Override
    public void render(GuiGraphics graphics, PoseStack stack, float x, float y) {
        if (!shouldDisplay()) {
            return;
        }
        
        updateBps();
        
        float padding = 6f;
        float titleScale = 0.4f;
        float subtitleScale = 0.35f;
        
        // 图标背景和图标尺寸
        float iconSize = 35f;
        float iconPadding = 5f;
        float iconBgX = x + padding;
        float iconBgY = y + padding;

        // 绘制图标背景（圆角正方形）
        RenderUtils.drawRoundedRect(stack, iconBgX, iconBgY, iconSize, iconSize, 5f, new Color(40, 40, 40, 200).getRGB());
        
        // 绘制空心正方体图标（等角投影 - 真正的3D透视效果）
        float iconCenterX = iconBgX + iconSize / 2f;
        float iconCenterY = iconBgY + iconSize / 2f;
        float cubeSize = 13f;
        
        // 等角投影系数（isometric projection）
        double cos30 = Math.cos(Math.toRadians(30));
        double sin30 = Math.sin(Math.toRadians(30));
        float scale = cubeSize / 2.5f;
        
        // 立方体的8个顶点（归一化坐标 -1 到 1）
        // 前面（z = -0.8）
        float[] frontLeftBottom = projectPoint(-1, -1, -0.8f, iconCenterX, iconCenterY, scale, cos30, sin30);
        float[] frontRightBottom = projectPoint(1, -1, -0.8f, iconCenterX, iconCenterY, scale, cos30, sin30);
        float[] frontLeftTop = projectPoint(-1, 1, -0.8f, iconCenterX, iconCenterY, scale, cos30, sin30);
        float[] frontRightTop = projectPoint(1, 1, -0.8f, iconCenterX, iconCenterY, scale, cos30, sin30);
        
        // 后面（z = 0.8）
        float[] backLeftBottom = projectPoint(-1, -1, 0.8f, iconCenterX, iconCenterY, scale, cos30, sin30);
        float[] backRightBottom = projectPoint(1, -1, 0.8f, iconCenterX, iconCenterY, scale, cos30, sin30);
        float[] backLeftTop = projectPoint(-1, 1, 0.8f, iconCenterX, iconCenterY, scale, cos30, sin30);
        float[] backRightTop = projectPoint(1, 1, 0.8f, iconCenterX, iconCenterY, scale, cos30, sin30);
        
        int cubeColor = new Color(100, 150, 255, 255).getRGB();
        float lineWidth = 1.6f;
        
        // 前面正方形的4条边
        drawLine(stack, frontLeftTop[0], frontLeftTop[1], frontRightTop[0], frontRightTop[1], lineWidth, cubeColor);
        drawLine(stack, frontLeftBottom[0], frontLeftBottom[1], frontRightBottom[0], frontRightBottom[1], lineWidth, cubeColor);
        drawLine(stack, frontLeftBottom[0], frontLeftBottom[1], frontLeftTop[0], frontLeftTop[1], lineWidth, cubeColor);
        drawLine(stack, frontRightBottom[0], frontRightBottom[1], frontRightTop[0], frontRightTop[1], lineWidth, cubeColor);
        
        // 后面正方形的4条边
        drawLine(stack, backLeftTop[0], backLeftTop[1], backRightTop[0], backRightTop[1], lineWidth, cubeColor);
        drawLine(stack, backLeftBottom[0], backLeftBottom[1], backRightBottom[0], backRightBottom[1], lineWidth, cubeColor);
        drawLine(stack, backLeftBottom[0], backLeftBottom[1], backLeftTop[0], backLeftTop[1], lineWidth, cubeColor);
        drawLine(stack, backRightBottom[0], backRightBottom[1], backRightTop[0], backRightTop[1], lineWidth, cubeColor);
        
        // 连接前后正方形的4条边（透视连接线）
        drawLine(stack, frontLeftBottom[0], frontLeftBottom[1], backLeftBottom[0], backLeftBottom[1], lineWidth, cubeColor);
        drawLine(stack, frontRightBottom[0], frontRightBottom[1], backRightBottom[0], backRightBottom[1], lineWidth, cubeColor);
        drawLine(stack, frontLeftTop[0], frontLeftTop[1], backLeftTop[0], backLeftTop[1], lineWidth, cubeColor);
        drawLine(stack, frontRightTop[0], frontRightTop[1], backRightTop[0], backRightTop[1], lineWidth, cubeColor);

        // 文本位置（在图标右边）
        float textX = iconBgX + iconSize + iconPadding;
        String title = "Scaffold Toggled";
        float titleY = y + padding;
        Fonts.tenacity.render(stack, title, textX, titleY, Color.WHITE, true, titleScale);

        int blockCount = getBlockCount();
        String subtitle = blockCount + " blocks left - " + String.format("%.2f", bps) + " blocks/s";
        float titleHeight = (float) Fonts.tenacity.getHeight(true, titleScale);
        float subtitleY = titleY + titleHeight + 4;
        Fonts.tenacity.render(stack, subtitle, textX, subtitleY, new Color(200, 200, 200, 255), true, subtitleScale);

        // 进度条在整体下面
        float contentHeight = titleHeight + 4 + (float) Fonts.tenacity.getHeight(true, subtitleScale);
        float progressBarY = y + padding + contentHeight + 8;
        float progressBarWidth = getWidth() - padding * 2;
        float progressBarHeight = 6f;
        float progressBarRadius = 3f;

        // 进度条背景（圆角矩形）
        RenderUtils.drawRoundedRect(stack, x + padding, progressBarY, progressBarWidth, progressBarHeight, progressBarRadius, new Color(30, 30, 30, 200).getRGB());

        // 计算目标进度值（0.0 - 1.0）
        float targetProgress = Math.min(blockCount / 100.0f, 1.0f);
        
        // 更新进度条动画
        progressAnimation.target = targetProgress;
        progressAnimation.speed = 0.05f;
        progressAnimation.update(true);
        
        // 使用动画值来计算进度条宽度（平滑过渡）
        float animatedProgress = progressAnimation.value;
        float progressFillWidth = progressBarWidth * animatedProgress;
        
        // 进度条填充（圆角矩形）
        if (progressFillWidth > 0) {
            RenderUtils.drawRoundedRect(stack, x + padding, progressBarY, progressFillWidth, progressBarHeight, progressBarRadius, new Color(100, 150, 255, 255).getRGB());
        }
    }
    
    /**
     * 将3D坐标投影到2D屏幕坐标（等角投影）
     */
    private float[] projectPoint(float x, float y, float z, float centerX, float centerY, float scale, double cos30, double sin30) {
        float screenX = centerX + (float)((x - y) * cos30 * scale);
        float screenY = centerY + (float)((x + y) * sin30 * scale - z * scale);
        return new float[]{screenX, screenY};
    }
    
    /**
     * 绘制一条线（使用多个小矩形分段近似）
     */
    private void drawLine(PoseStack stack, float x1, float y1, float x2, float y2, float width, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.1f) return;
        
        float angle = (float) Math.atan2(dy, dx);
        float halfWidth = width / 2f;
        
        // 分段绘制线条，每段2像素长
        int segments = Math.max(1, (int)(length / 2f));
        for (int i = 0; i < segments; i++) {
            float t1 = (float) i / segments;
            float t2 = (float) (i + 1) / segments;
            
            float segX1 = x1 + dx * t1;
            float segY1 = y1 + dy * t1;
            float segX2 = x1 + dx * t2;
            float segY2 = y1 + dy * t2;
            
            // 计算垂直于线条的向量
            float perpX = (float)(-Math.sin(angle) * halfWidth);
            float perpY = (float)(Math.cos(angle) * halfWidth);
            
            // 绘制一个小的矩形作为线段
            float segLength = (float) Math.sqrt((segX2 - segX1) * (segX2 - segX1) + (segY2 - segY1) * (segY2 - segY1));
            if (segLength < 0.1f) continue;
            
            // 计算矩形的四个角点
            float[] p1 = new float[]{segX1 + perpX, segY1 + perpY};
            float[] p2 = new float[]{segX2 + perpX, segY2 + perpY};
            float[] p3 = new float[]{segX2 - perpX, segY2 - perpY};
            float[] p4 = new float[]{segX1 - perpX, segY1 - perpY};
            
            // 找到边界框并绘制
            float minX = Math.min(Math.min(p1[0], p2[0]), Math.min(p3[0], p4[0]));
            float maxX = Math.max(Math.max(p1[0], p2[0]), Math.max(p3[0], p4[0]));
            float minY = Math.min(Math.min(p1[1], p2[1]), Math.min(p3[1], p4[1]));
            float maxY = Math.max(Math.max(p1[1], p2[1]), Math.max(p3[1], p4[1]));
            
            RenderUtils.drawRectBound(stack, minX, minY, maxX - minX, maxY - minY, color);
        }
    }
    
    @Override
    public float getWidth() {
        if (!shouldDisplay()) {
            return 200;
        }
        
        float padding = 6f;
        float iconSize = 35f;
        float iconPadding = 5f;
        float titleScale = 0.4f;
        float subtitleScale = 0.35f;
        
        int blockCount = getBlockCount();
        String title = "Scaffold Toggle";
        String subtitle = blockCount + " blocks left - " + String.format("%.2f", bps) + " blocks/s";
        
        float titleWidth = Fonts.tenacity.getWidth(title, titleScale);
        float subtitleWidth = Fonts.tenacity.getWidth(subtitle, subtitleScale);
        float textWidth = Math.max(titleWidth, subtitleWidth);
        
        return padding + iconSize + iconPadding + textWidth + padding;
    }
    
    @Override
    public float getHeight() {
        if (!shouldDisplay()) {
            return 40;
        }
        
        float padding = 6f;
        float titleScale = 0.4f;
        float subtitleScale = 0.35f;
        float titleHeight = (float) Fonts.tenacity.getHeight(true, titleScale);
        float subtitleHeight = (float) Fonts.tenacity.getHeight(true, subtitleScale);
        float progressBarHeight = 6f;
        float spacing = 4f + 8f; // 标题和副标题间距 + 副标题和进度条间距
        
        return padding + titleHeight + spacing + subtitleHeight + progressBarHeight + padding;
    }
}
