package tech.blinkfix.ui.Island;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Island 内容接口
 * 所有要在 Island 中显示的内容都应该实现这个接口
 */
public interface IslandContent {
    /**
     * 获取优先级，数字越大优先级越高
     * @return 优先级值
     */
    int getPriority();
    
    /**
     * 判断该内容是否应该显示
     * @return true 如果应该显示，false 否则
     */
    boolean shouldDisplay();
    
    /**
     * 渲染内容
     * @param graphics GuiGraphics
     * @param stack PoseStack
     * @param x 渲染 X 坐标
     * @param y 渲染 Y 坐标
     */
    void render(GuiGraphics graphics, PoseStack stack, float x, float y);
    
    /**
     * 获取内容宽度
     * @return 宽度
     */
    float getWidth();
    
    /**
     * 获取内容高度
     * @return 高度
     */
    float getHeight();
}

