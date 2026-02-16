package com.surface.render.clickgui.components;

import com.surface.Wrapper;
import com.surface.mod.Mod;
import com.surface.render.clickgui.components.impl.*;
import com.surface.render.font.FontManager;
import com.surface.util.MouseUtils;
import com.surface.util.render.RenderUtils;
import com.surface.value.Value;
import com.surface.value.impl.*;
import net.minecraft.client.Minecraft;
import renderassist.animations.ColorAnimation;
import renderassist.animations.LinearAnimation;
import renderassist.animations.RippleAnimation;
import renderassist.rendering.BasicRendering;
import renderassist.rendering.StencilUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleButton {
    private final Mod module;
    public float x;
    public float y;
    private final int width;
    private final int height;
    private int offset;
    private boolean open;
    private final List<Component<?>> components = new ArrayList<>();
    private final RippleAnimation animation;
    private final ColorAnimation colorAnimation;
    private float curHeight;
    private double lengthAnimation = 3;
    private double openAnimation = 5;
    public boolean drag = false;

    public ModuleButton(Mod module, int x, int y, int width, int height) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.animation = new RippleAnimation();
        this.colorAnimation = new ColorAnimation(new Color(0, 0, 0, 0));

        for (Value<?> value : module.getValues()) {
            if (value instanceof BooleanValue)
                components.add(new BooleanComponent((BooleanValue) value, x, y, width, height, false, this));
            if (value instanceof NumberValue)
                components.add(new NumberComponent((NumberValue) value, x, y, width, height, this));
            if (value instanceof ModeValue)
                components.add(new ModeComponent((ModeValue) value, x, y, width, height, this));
            if (value instanceof ColorValue)
                components.add(new ColorComponent((ColorValue) value, x, y, width, height, this));
            if (value instanceof FilterValue) {
                components.add(new LabelComponent(value.getValueName(), x, y, width, height, this));
                for (BooleanValue booleanValue : ((FilterValue<?>) value).getValue()) {
                    components.add(new BooleanComponent(booleanValue, x, y, width, height, true, this));
                }
            }
            if (value instanceof TextValue)
                components.add(new TextFieldComponent((TextValue) value, x, y, width, height, this));
        }
    }

    public int drawScreen(int mouseX, int mouseY, float partialTicks, int offset) {
        this.offset = offset;
        float y = this.y + offset;
        animation.draw(x, y, width, height);

        this.colorAnimation.animateTo(module.isEnable() ? new Color(RenderUtils.reAlpha(Wrapper.Instance.getClickGui().getGlobalColor().getRGB(), 144)) : new Color(0, 0, 0, 0), 0.1f);

        BasicRendering.drawRect(x, y, width, height, colorAnimation.getColor().getRGB());
        FontManager.TAHOMA.drawString(module.getName(), x + 8, y + (height / 2f - FontManager.TAHOMA.getHeight() / 2f), -1);

        if (!module.getValues().isEmpty()) {
            double frameRate = Minecraft.getDebugFPS() / 8.3;
            if (open && lengthAnimation > -3) {
                lengthAnimation -= 3 / frameRate;
            } else if (!open && lengthAnimation < 3) {
                lengthAnimation += 3 / frameRate;
            }
            if (open && openAnimation < 8) {
                openAnimation += 3 / frameRate;
            } else if (!open && openAnimation > 5) {
                openAnimation -= 3 / frameRate;
            }
            RenderUtils.drawArrow(x + width - 14, y + 3.5f + openAnimation, 2, -1, lengthAnimation);
        }

        float finalHeight = height;
        if (open) {
            BasicRendering.drawRect(x, y + height, width, curHeight - height, new Color(0, 0, 0, 40).getRGB());
            StencilUtils.initStencilToWrite();
            BasicRendering.drawRect(x, y, width, curHeight, -1);
            StencilUtils.readStencilBuffer(528);
            for (Component<?> component : components) {
                finalHeight += component.drawScreen(mouseX, mouseY, partialTicks, (int) (finalHeight + offset));
            }
            StencilUtils.endStencilBuffer();
        }

        curHeight = LinearAnimation.animate(curHeight, finalHeight, 0.5f);

        return (int) curHeight;
    }

    public int getHeight() {
        return height;
    }

    public boolean isOpen() {
        return open;
    }

    public List<Component<?>> getComponents() {
        return components;
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (bounding(mouseX, mouseY)) {

            if (mouseButton == 0) {
                animation.mouseClicked(mouseX, mouseY);
                this.module.toggle();
            } else if (mouseButton == 1) {
                this.open = !this.open;
            }
        }

        if (open)
            components.forEach(component -> component.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (open)
            components.forEach(component -> component.keyTyped(typedChar, keyCode));
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (open)
            components.forEach(component -> component.mouseReleased(mouseX, mouseY, state));
    }

    public boolean bounding(int mouseX, int mouseY) {
        return MouseUtils.bounding(mouseX, mouseY, this.x, this.y + this.offset, this.width, this.height);
    }
}
