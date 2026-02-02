package tech.blinkfix.ui.Island;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import tech.blinkfix.utils.renderer.Fonts;

import java.awt.*;

/**
 * 错误消息内容 - 显示命令执行错误提示
 * 优先级: 150 (高于模块切换和Scaffold，低于命令面板)
 */
public class ErrorMessageContent implements IslandContent {
    private static final Minecraft mc = Minecraft.getInstance();
    
    // 静态实例，确保可以全局访问
    private static ErrorMessageContent instance;
    
    private String errorMessage = "";
    private long displayStartTime = 0;
    private boolean isSuccess = false; // 是否为成功消息
    private static final long DISPLAY_DURATION = 1500; // 1500ms后自动关闭
    
    public ErrorMessageContent() {
        instance = this;
    }
    
    public static ErrorMessageContent getInstance() {
        return instance;
    }
    
    /**
     * 显示错误消息
     */
    public void showError(String message) {
        this.errorMessage = message;
        this.isSuccess = false;
        this.displayStartTime = System.currentTimeMillis();
    }
    
    /**
     * 显示成功消息
     */
    public void showSuccess(String message) {
        this.errorMessage = message;
        this.isSuccess = true;
        this.displayStartTime = System.currentTimeMillis();
    }
    
    @Override
    public int getPriority() {
        return 150; // 优先级高于ModuleToggleContent(50)和ScaffoldContent(100)，低于CommandPaletteContent(200)
    }
    
    @Override
    public boolean shouldDisplay() {
        if (errorMessage.isEmpty()) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        // 如果超过显示时长，清除消息
        if (currentTime - displayStartTime >= DISPLAY_DURATION) {
            errorMessage = "";
            displayStartTime = 0;
            return false;
        }
        return true;
    }
    
    @Override
    public void render(GuiGraphics graphics, PoseStack stack, float x, float y) {
        if (errorMessage.isEmpty()) {
            return;
        }
        
        float padding = 10f;
        float scale = 0.4f;
        
        // 根据消息类型选择颜色
        Color textColor = isSuccess ? new Color(100, 255, 100, 255) : new Color(255, 100, 100, 255);
        
        // 渲染消息文本
        Fonts.harmony.render(stack, errorMessage, x + padding, y + padding, textColor, true, scale);
    }
    
    @Override
    public float getWidth() {
        if (errorMessage.isEmpty()) {
            return 200;
        }
        
        float padding = 10f * 2;
        float scale = 0.4f;
        float textWidth = Fonts.harmony.getWidth(errorMessage, scale);
        
        return textWidth + padding;
    }
    
    @Override
    public float getHeight() {
        if (errorMessage.isEmpty()) {
            return 40;
        }
        
        float padding = 10f * 2;
        float scale = 0.4f;
        float textHeight = (float) Fonts.harmony.getHeight(true, scale);
        
        return textHeight + padding;
    }
}

