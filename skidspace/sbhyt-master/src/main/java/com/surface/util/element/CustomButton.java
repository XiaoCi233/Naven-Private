package com.surface.util.element;

import com.surface.render.font.FontManager;
import com.surface.util.render.RenderUtils;
import com.surface.util.render.shader.ShaderElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import renderassist.animations.ColorAnimation;
import renderassist.rendering.StencilUtils;

import java.awt.*;

public class CustomButton extends GuiButton {
    private ColorAnimation animation = new ColorAnimation(new Color(255, 255, 255, 40));

    public CustomButton(int buttonId, int x, int y, String buttonText) {
        this(buttonId, x, y, 200, 20, buttonText);
    }

    public CustomButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver) {
        int i = 1;

        if (!this.enabled) {
            i = 0;
        } else if (mouseOver) {
            i = 2;
        }

        return i;
    }

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        StencilUtils.initStencilToWrite();
        RenderUtils.drawRoundedRect(xPosition, yPosition, this.width, this.height, 4, new Color(255, 255, 255, 30).getRGB());
        StencilUtils.readStencilBuffer(1);
        ShaderElement.renderBlur(12);
        StencilUtils.endStencilBuffer();
        ShaderElement.getTasks().clear();
        animation.animateTo(new Color(255, 255, 255, RenderUtils.isHovering(xPosition, yPosition, this.width, this.height, mouseX, mouseY) ? 100 : 30), 0.3f);
        RenderUtils.drawRoundedRect(xPosition, yPosition, this.width, this.height, 4, animation.getColor().getRGB());
        FontManager.TAHOMA.drawCenteredString(displayString, xPosition + width / 2f, FontManager.TAHOMA.getMiddleOfBox(height) + yPosition, -1);
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY) {
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    /**
     * Whether the mouse cursor is currently over the button.
     */
    public boolean isMouseOver() {
        return this.hovered;
    }

    public void drawButtonForegroundLayer(int mouseX, int mouseY) {
    }

    public void playPressSound(SoundHandler soundHandlerIn) {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    public int getButtonWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
