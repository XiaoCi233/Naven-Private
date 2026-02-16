package com.surface.render.clickgui;

import com.surface.Wrapper;
import com.surface.mod.Mod;
import com.surface.mod.visual.InterfaceModule;
import com.surface.render.clickgui.components.Panel;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import renderassist.animations.ColorAnimation;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGui extends GuiScreen {
    public List<Panel> panels = new ArrayList<>();

    private final ColorAnimation animation = new ColorAnimation(Color.WHITE);

    @Override
    public void initGui() {
        if (this.panels.isEmpty()) {
            int x = 30;
            for (Mod.Category category : Mod.Category.values()) {
                panels.add(new Panel(category, x, 30, 150, 20));
                x += 160;
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Wrapper.Instance.getConfigManager().save("modules");
    }

    @Override
    public void handleMouseInput() throws IOException {
        float dw = Mouse.getEventDWheel();
        this.panels.forEach(panel -> panel.handleMouseInput(dw));
        super.handleMouseInput();
    }

    public Color getGlobalColor() {
        InterfaceModule interfaceModule = (InterfaceModule) Wrapper.Instance.getModManager().getModFromName("Interface");
        animation.animateTo(interfaceModule.colorValue.getValue().getAsColor(), 0.3f);
        return animation.getColor();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.panels.forEach(panel -> panel.drawScreen(mouseX, mouseY, partialTicks));
        this.panels.forEach(panel -> panel.doDrag(mouseX, mouseY));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        this.panels.forEach(panel -> panel.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        this.panels.forEach(panel -> panel.mouseReleased(mouseX, mouseY, state));
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        this.panels.forEach(panel -> panel.keyTyped(typedChar, keyCode));
        try {
            super.keyTyped(typedChar, keyCode);
        } catch (IOException ignored) {
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
