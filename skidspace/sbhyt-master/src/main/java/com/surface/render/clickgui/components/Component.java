package com.surface.render.clickgui.components;

import com.surface.util.MouseUtils;
import net.minecraft.client.Minecraft;

public class Component<T> {
    private final T object;
    public float x, y, width, height;
    public int offset;
    public Minecraft mc = Minecraft.getMinecraft();
    private final ModuleButton bigFather;

    public Component(T object, int x, int y, int width, int height, ModuleButton bigFather) {
        this.object = object;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bigFather = bigFather;
    }

    public ModuleButton getBigFather() {
        return bigFather;
    }

    public float drawScreen(int mouseX, int mouseY, float partialTicks, int offset) {
        return 0;
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
    }

    public void keyTyped(char typedChar, int keyCode) {
    }

    public boolean bounding(int mouseX, int mouseY) {
        return MouseUtils.bounding(mouseX, mouseY, this.x, this.y + this.offset, this.width, this.height);
    }

    public boolean bounding(int mouseX, int mouseY, int x, int y, int width, int height) {
        return MouseUtils.bounding(mouseX, mouseY, x, y, width, height);
    }

    public T getObject() {
        return object;
    }
}
