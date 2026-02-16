package com.surface.render.clickgui.components.impl;

import com.surface.render.clickgui.components.Component;
import com.surface.render.clickgui.components.ModuleButton;
import com.surface.render.font.FontManager;
import com.surface.util.render.RenderUtils;
import com.surface.value.impl.ColorValue;
import org.lwjgl.input.Mouse;
import renderassist.rendering.BasicRendering;

import java.awt.*;

public class ColorComponent extends Component<ColorValue> {

    private boolean slidingHue;
    private boolean slidingSB; // 不要误会，没有骂你，这个SB的意思是Saturation & Brightness，简称SB
    private boolean slidingAlpha;

    public ColorComponent(ColorValue colorValue, int x, int y, int width, int height, ModuleButton bigFather) {
        super(colorValue, x, y, width, height, bigFather);

    }

    @Override
    public float drawScreen(int mouseX, int mouseY, float partialTicks, int offset) {
        if (!getObject().isVisible()) {
            this.height = 0;
            return 0;
        }
        this.offset = offset;
        float y = this.y + offset;
        final ColorValue colorValue = this.getObject();
        final Color valColor = colorValue.getValue().getAsColor();
        FontManager.TAHOMA.drawString(getObject().getValueName(), x + 8, y + 18 / 2f - FontManager.TAHOMA.getHeight() / 2f, -1);

        float x = this.x;
        x += 8 - 3.5f;
        y += 15;

        final float[] hsba = {
                colorValue.getValue().getHue(),
                colorValue.getValue().getSaturation(),
                colorValue.getValue().getBrightness(),
                colorValue.getValue().getAlpha(),
        };
        BasicRendering.drawRect(x + 3.5, y + 3.5, width - 16 - 11, 60, Color.getHSBColor(hsba[0], 1, 1).getRGB());
        RenderUtils.drawHGradientRect(x + 3.5, y + 3.5, width - 16 - 11, 60, Color.getHSBColor(hsba[0], 0, 1).getRGB(), 0x00F);
        RenderUtils.drawVGradientRect(x + 3.5, y + 3.5, width - 16 - 11, 60, 0x00F, Color.getHSBColor(hsba[0], 1, 0).getRGB());

        double colorY = y + 3.5 + ((1 - hsba[2]) * 60) - .5;
        BasicRendering.drawCircle(x + 3.5 + hsba[1] * (width - 16 - 11) - .5, colorY, 3, -1);
        BasicRendering.drawCircle(x + 3.5 + hsba[1] * (width - 16 - 11) - .5, colorY, 2, valColor.getRGB());

        float v = Math.max((mouseY - y - 3) / 60F, 0);
        if (slidingSB && Mouse.isButtonDown(0)) {
            colorValue.getValue().setSaturation(Math.min(Math.max((mouseX - x - 3) / (width - 16 - 11), 0), 1));
            colorValue.getValue().setBrightness(1 - Math.min(v, 1));
        } else {
            slidingSB = false;
        }

        for (float f = 0F; f < 5F; f += 1F) {
            final Color lasCol = Color.getHSBColor(f / 5F, 1F, 1F);
            final Color tarCol = Color.getHSBColor(Math.min(f + 1F, 5F) / 5F, 1F, 1F);
            RenderUtils.drawVGradientRect(this.x + width - 8 - 9, y + 3.5 + f * 12, 9, 12, lasCol.getRGB(), tarCol.getRGB());
        }

        BasicRendering.drawRect(this.x + width - 8 - 9, y + 2.5 + hsba[0] * 60, 9, 1, new Color(204, 198, 255).getRGB());

        if (slidingHue && Mouse.isButtonDown(0)) {
            colorValue.getValue().setHue(Math.min(v, 1));
            colorValue.setRainbowEnabled(false);
        } else {
            slidingHue = false;
        }

        if (colorValue.isAlphaChangeable()) {

            for (int xPosition = 0; xPosition < (width - 16) / 2f; xPosition++)
                for (int yPosition = 0; yPosition < 4; yPosition++)
                    BasicRendering.drawRect(x + 3.5 + (xPosition * 2), y + 67.5 + (yPosition * 2), 2, 2, ((yPosition % 2 == 0) == (xPosition % 2 == 0)) ? new Color(255, 255, 255).getRGB() : new Color(190, 190, 190).getRGB());

            RenderUtils.drawHGradientRect(x + 3.5, y + 67.5, width - 16, 8, 0x00F, Color.getHSBColor(hsba[0], 1, 1).getRGB());
            BasicRendering.drawRect(x + 3 + hsba[3] * (width - 16), y + 67.5, 1, 8, new Color(204, 198, 255).getRGB());

            if (slidingAlpha && Mouse.isButtonDown(0)) {
                colorValue.getValue().setAlpha(Math.min(Math.max((mouseX - x - 3) / (width - 16), 0), 1));
            } else {
                slidingAlpha = false;
            }
        }
        this.height = (colorValue.isAlphaChangeable() ? 80 : 67) + 15;
        return (colorValue.isAlphaChangeable() ? 80 : 67) + 15;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float y = this.y + offset;
        float x = this.x;
        x += 8 - 3.5f;
        y += 15;

        if (mouseButton == 0) {
            if (RenderUtils.isHovering(x + 3, y + 3, width - 16 - 11, 61, mouseX, mouseY)) {
                slidingSB = true;
            }
            if (RenderUtils.isHovering(this.x + width - 8 - 9, y + 3, 10, 61, mouseX, mouseY)) {
                slidingHue = true;
            }
            if (RenderUtils.isHovering(x + 3, y + 67, width - 16, 9, mouseX, mouseY)) {
                slidingAlpha = true;
            }
        }
    }
}
