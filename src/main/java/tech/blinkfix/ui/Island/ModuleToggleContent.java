package tech.blinkfix.ui.Island;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import tech.blinkfix.modules.Module;
import tech.blinkfix.utils.RenderUtils;
import tech.blinkfix.utils.SmoothAnimationTimer;
import tech.blinkfix.utils.renderer.Fonts;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 模块开关内容 - 显示最近切换的模块状态（列表形式）
 * 优先级: 50
 */
public class ModuleToggleContent implements IslandContent {
    private static class ModuleToggleEntry {
        final Module module;
        long toggleTime;
        boolean isEnabled;
        final SmoothAnimationTimer toggleAnimation;
        
        ModuleToggleEntry(Module module, long toggleTime, boolean isEnabled) {
            this.module = module;
            this.toggleTime = toggleTime;
            this.isEnabled = isEnabled;
            this.toggleAnimation = new SmoothAnimationTimer(isEnabled ? 1.0f : 0.0f, isEnabled ? 1.0f : 0.0f, 0.3f);
        }
        
        void updateToggleState(boolean newState, long newTime) {
            if (this.isEnabled != newState) {
                this.isEnabled = newState;
                this.toggleTime = newTime;
                this.toggleAnimation.target = newState ? 1.0f : 0.0f;
            } else {
                this.toggleTime = newTime;
            }
        }
    }
    
    private final List<ModuleToggleEntry> toggleEntries = new ArrayList<>();
    private static final long DISPLAY_DURATION = 1500;
    private static final float ENTRY_SPACING = 4f;
    
    @Override
    public int getPriority() {
        return 50;
    }
    
    @Override
    public boolean shouldDisplay() {
        cleanupExpiredEntries();
        return !toggleEntries.isEmpty();
    }
    
    public void onModuleToggled(Module module) {
        long currentTime = System.currentTimeMillis();
        boolean newState = module.isEnabled();

        ModuleToggleEntry existingEntry = null;
        for (ModuleToggleEntry entry : toggleEntries) {
            if (entry.module == module) {
                existingEntry = entry;
                break;
            }
        }
        
        if (existingEntry != null) {
            existingEntry.updateToggleState(newState, currentTime);
        } else {
            toggleEntries.add(0, new ModuleToggleEntry(module, currentTime, newState));
        }
    }
    
    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        toggleEntries.removeIf(entry -> (currentTime - entry.toggleTime) >= DISPLAY_DURATION);
    }
    
    @Override
    public void render(GuiGraphics graphics, PoseStack stack, float x, float y) {
        cleanupExpiredEntries();
        
        if (toggleEntries.isEmpty()) {
            return;
        }
        
        float padding = 6f;
        float toggleWidth = 28f;
        float toggleHeight = 14f;
        float entryHeight = 28f;
        float currentY = y + padding + 1;

        for (ModuleToggleEntry entry : toggleEntries) {
            entry.toggleAnimation.update(true);

            float toggleX = x + padding;
            float toggleY = currentY;

            float animValue = entry.toggleAnimation.value;

            Color enabledColor = new Color(100, 150, 255, 255);
            Color disabledColor = new Color(50, 50, 50, 255);
            int bgR = (int) (disabledColor.getRed() + (enabledColor.getRed() - disabledColor.getRed()) * animValue);
            int bgG = (int) (disabledColor.getGreen() + (enabledColor.getGreen() - disabledColor.getGreen()) * animValue);
            int bgB = (int) (disabledColor.getBlue() + (enabledColor.getBlue() - disabledColor.getBlue()) * animValue);
            Color bgColor = new Color(bgR, bgG, bgB, 255);

            RenderUtils.drawRoundedRect(stack, toggleX, toggleY, toggleWidth, toggleHeight, toggleHeight / 2f, enabledColor.getRGB());

            Color borderColor = new Color(
                Math.min(255, bgR + 30),
                Math.min(255, bgG + 30),
                Math.min(255, bgB + 30),
                180
            );
            RenderUtils.drawRoundedRect(stack, toggleX + 1f, toggleY + 1f, toggleWidth - 2f, toggleHeight - 2f, toggleHeight / 2f - 0.5f, borderColor.getRGB());

            float circleSize = 10f;
            float circleY = toggleY + (toggleHeight - circleSize) / 2f;
            float leftPos = toggleX + 2f;
            float rightPos = toggleX + toggleWidth - circleSize - 2f;
            float circleX = leftPos + (rightPos - leftPos) * animValue;

            // 阴影
            RenderUtils.drawRoundedRect(stack, circleX + 0.5f, circleY + 0.5f + 0.5f, circleSize, circleSize, circleSize / 2f, new Color(0, 0, 0, 60).getRGB());

            // 主圆角矩形（白色，带渐变效果）
            Color circleColor = new Color((int) (255 - 30 * (1 - animValue)), (int) (255 - 30 * (1 - animValue)), (int) (255 - 30 * (1 - animValue)), 255);
            RenderUtils.drawRoundedRect(stack, circleX, circleY, circleSize, circleSize, circleSize / 2f, circleColor.getRGB());

            float textX = toggleX + toggleWidth + 5f;
            float textY = currentY - 4;

            Color textColor = new Color(255,255,255, 255);
            Fonts.tenacity.render(stack, "Module Toggled", textX, textY, textColor, true, 0.35f);

            String statusText = entry.isEnabled ? ChatFormatting.DARK_AQUA + entry.module.getName() + ChatFormatting.WHITE + " has bean" + ChatFormatting.GREEN + " Enabled" + ChatFormatting.WHITE + "!" : ChatFormatting.DARK_AQUA + entry.module.getName() + ChatFormatting.WHITE + " has bean" + ChatFormatting.RED + " Disabled" + ChatFormatting.WHITE + "!";
            Fonts.tenacity.render(stack, statusText, textX, textY + 13, new Color(-1), true, 0.3f);

            currentY += entryHeight + ENTRY_SPACING;
        }
    }

    @Override
    public float getWidth() {
        cleanupExpiredEntries();

        if (toggleEntries.isEmpty()) {
            return 200;
        }

        float padding = 6f;
        float toggleWidth = 28f;
        float spacing = 5f;
        float maxWidth = 0;

        for (ModuleToggleEntry entry : toggleEntries) {
            float moduleNameWidth = Fonts.tenacity.getWidth("Module Toggled", 0.35f) + 8;
            String statusText = entry.isEnabled ? entry.module.getName() + " has bean Enabled" : entry.module.getName() + " has bean Disabled";
            float statusWidth = Fonts.tenacity.getWidth(statusText, 0.3f) + 8;
            float entryWidth = padding + toggleWidth + spacing + Math.max(moduleNameWidth, statusWidth) + padding;
            maxWidth = Math.max(maxWidth, entryWidth);
        }
        
        return maxWidth;
    }
    
    @Override
    public float getHeight() {
        cleanupExpiredEntries();
        
        if (toggleEntries.isEmpty()) {
            return 40;
        }

        float entryHeight = 28f;

        return toggleEntries.size() * entryHeight + (toggleEntries.size() - 1) * ENTRY_SPACING;
    }
}
