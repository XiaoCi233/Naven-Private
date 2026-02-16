package com.surface.render.clickgui.components;

import com.surface.Wrapper;
import com.surface.mod.Mod;
import com.surface.render.font.FontManager;
import com.surface.util.MouseUtils;
import com.surface.util.render.MaskUtils;
import com.surface.util.render.RenderUtils;
import com.surface.util.render.shader.ShaderElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import renderassist.animations.LinearAnimation;
import renderassist.rendering.BasicRendering;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Panel {
    private final Mod.Category category;
    private float x;
    private float y;
    private final int width;
    private final int height;
    private final List<ModuleButton> moduleButtons = new ArrayList<>();
    private boolean open = true;
    private float dragX, dragY;
    private boolean drag = false;
    private float scrollY;
    private float scrollAni;
    public Panel(Mod.Category category, int x, int y, int width, int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        ArrayList<Mod> mods = Wrapper.Instance.getModManager().getModsInCategory(category);

        for (Mod module : mods) {
            moduleButtons.add(new ModuleButton(module, x, y, width, height));
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float finalRealHeight = getFinalRealHeight();
        ShaderElement.addBlurTask(() -> RenderUtils.drawRoundedRect(x, y, width, finalRealHeight, 10, new Color(0, 0, 0, 144).getRGB()));
        ShaderElement.addBloomTask(() -> RenderUtils.drawRoundedRect(x, y, width, finalRealHeight, 10, -1));
        RenderUtils.drawRoundedRect(x, y, width, finalRealHeight, 10, new Color(0, 0, 0, 60).getRGB());
        FontManager.TAHOMA.drawString(category.name(), x + 24, y + height / 2f - FontManager.TAHOMA.getHeight() / 2f, -1);
        RenderUtils.drawImage(new ResourceLocation("surface/images/" + category.name().toLowerCase() + ".png"), x + 6, y + 1.5f, 16, 16, -1);
        int offset = height;
        if (this.open) {
            MaskUtils.defineMask();
            BasicRendering.drawRect(x, y + height, width, Minecraft.getMinecraft().getResolution().getScaledHeight() - 100, -1);
            MaskUtils.finishDefineMask();
            MaskUtils.drawOnMask();
            for (ModuleButton button : this.moduleButtons) {
                offset += button.drawScreen(mouseX, mouseY, partialTicks, (int) (offset + scrollAni));
            }
            MaskUtils.resetMask();
            if (offset > new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight() - 100) {
                scrollY = Math.max(scrollY, -offset + (new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight() - 100) + height);
                if (RenderUtils.isHovering(x, y, width, height + offset, mouseX, mouseY)) {
                    scrollAni = LinearAnimation.animate(scrollAni, scrollY, 0.4f);
                } else {
                    scrollY = scrollAni;
                }
            }
        }
    }

    private float getFinalRealHeight() {
        float realHeight = height;

        if (open) {
            for (ModuleButton button : moduleButtons) {
                realHeight += button.getHeight();
                if (button.isOpen()) {
                    for (Component<?> component : button.getComponents()) {
                        realHeight += component.height;
                    }
                }
            }
            realHeight += 6;
        }

        return Math.min(realHeight, Minecraft.getMinecraft().getResolution().getScaledHeight() - 100 + 6 + 20);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.open) moduleButtons.forEach(moduleButton -> moduleButton.mouseClicked(mouseX, mouseY, mouseButton));
        if (bounding(mouseX, mouseY)) {
            if (mouseButton == 0) {
                this.drag = true;
                this.dragX = mouseX - x;
                this.dragY = mouseY - y;
            } else {
                this.open = !this.open;
            }
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (this.open) moduleButtons.forEach(moduleButton -> moduleButton.keyTyped(typedChar, keyCode));
    }


    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.open)
            moduleButtons.forEach(moduleButton -> moduleButton.mouseReleased(mouseX, mouseY, state));
    }

    public boolean bounding(int mouseX, int mouseY) {
        return MouseUtils.bounding(mouseX, mouseY, this.x, this.y, this.width, this.height);
    }

    public void doDrag(int mouseX, int mouseY) {
        if (this.drag) {
            if (!Mouse.isButtonDown(0)) {
                this.drag = false;
                for (ModuleButton button : moduleButtons) {
                    button.drag = false;
                }
                return;
            }
            this.x = mouseX - this.dragX;
            this.y = mouseY - this.dragY;
            for (ModuleButton button : moduleButtons) {
                button.x = mouseX - this.dragX;
                button.y = mouseY - this.dragY;
                button.drag = true;
                for (Component<?> component : button.getComponents()) {
                    component.x = mouseX - this.dragX;
                    component.y = mouseY - this.dragY;
                }
            }
        }
    }

    public void handleMouseInput(float DWheel) {
        this.scrollY += DWheel;
        if (this.scrollY >= 0.0f) {
            this.scrollY = 0.0f;
        }
    }
}
