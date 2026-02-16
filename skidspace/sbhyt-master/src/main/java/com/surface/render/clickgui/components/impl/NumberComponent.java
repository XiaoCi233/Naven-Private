package com.surface.render.clickgui.components.impl;


import com.surface.Wrapper;
import com.surface.render.clickgui.components.Component;
import com.surface.render.clickgui.components.ModuleButton;
import com.surface.render.font.FontManager;
import com.surface.util.render.RenderUtils;
import com.surface.value.impl.NumberValue;
import net.minecraft.util.MathHelper;
import renderassist.animations.LinearAnimation;
import renderassist.rendering.BasicRendering;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberComponent extends Component<NumberValue> {
    private boolean sliding;
    private float size;
    private float draw;

    public NumberComponent(NumberValue numberValue, int x, int y, int width, int height, ModuleButton bigFather) {
        super(numberValue, x, y, width, height, bigFather);
        this.size = 6;
    }

    @Override
    public float drawScreen(int mouseX, int mouseY, float partialTicks, int offset) {
        if (!getObject().isVisible()) {
            this.height = 0;
            return 0;
        }
        this.height = 20;

        this.offset = offset;
        float y = offset + this.y;
        FontManager.TAHOMA.drawString(getObject().getValueName(), x + 8, y + 3, -1);

        String display = getObject().getValue() + "";
        if (display.endsWith(".0")) display = display.substring(0, display.length() - 2);
        else if (display.startsWith("0.")) display = "." + display.substring(2);
        else if (display.startsWith("-0.")) display = "-" + display.substring(2);

        FontManager.TAHOMA.drawString(display, x + width - 8 - FontManager.TAHOMA.getStringWidth(display), y + 3, -1);

        draw = LinearAnimation.animate(draw, (float) ((getObject().getValue() - getObject().getMinValue()) / (getObject().getMaxValue() - getObject().getMinValue())), 0.5f);

        RenderUtils.drawRoundedRect(x + 8, y + 15, width - 16, 3, 1, new Color(0, 0, 0, 144).getRGB());
        RenderUtils.drawRoundedRect(x + 8, y + 15, draw * (width - 16), 3, 1, Wrapper.Instance.getClickGui().getGlobalColor().getRGB());

        size = LinearAnimation.animate(size, sliding ? 8 : 6, 0.1f);

        BasicRendering.drawCircle(x + 8 + draw * (width - 16) - 2, y + 16.5f, size, Wrapper.Instance.getClickGui().getGlobalColor().brighter().getRGB());

        if (this.sliding) {
            double value = ((double) mouseX - x) / (width - 8);
            value = MathHelper.clamp_double(value, 0.0d, 1.0d);
            double rounded = round(Math.round((value * (getObject().getMaxValue() - getObject().getMinValue()) + getObject().getMinValue()) / getObject().getIncrease()) * getObject().getIncrease(), getObject().getIncrease());
            this.getObject().setValue(rounded);
        }

        return height;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!getObject().isVisible()) return;
        if (bounding(mouseX, mouseY)) {
            sliding = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        this.sliding = false;
    }

    public static double round(final double value, final double inc) {
        if (inc == 0.0) return value;
        else if (inc == 1.0) return Math.round(value);
        else {
            BigDecimal bdValue = BigDecimal.valueOf(value);
            BigDecimal bdInc = BigDecimal.valueOf(inc);

            BigDecimal floored = bdValue.divide(bdInc, 0, RoundingMode.FLOOR).multiply(bdInc);
            BigDecimal halfOfInc = bdInc.divide(BigDecimal.valueOf(2.0));

            if (bdValue.compareTo(floored.add(halfOfInc)) >= 0) {
                return floored.add(bdInc).doubleValue();
            } else {
                return floored.doubleValue();
            }
        }
    }

}
