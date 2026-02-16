package com.surface.render.clickgui.components.impl;

import com.surface.Wrapper;
import com.surface.render.clickgui.components.Component;
import com.surface.render.clickgui.components.ModuleButton;
import com.surface.render.font.FontManager;
import com.surface.util.render.RenderUtils;
import com.surface.value.impl.BooleanValue;
import renderassist.animations.ColorAnimation;
import renderassist.animations.LinearAnimation;
import renderassist.rendering.BasicRendering;

import java.awt.*;

public class BooleanComponent extends Component<BooleanValue> {

    private final ColorAnimation colorAnimation;
    private final ColorAnimation colorAnimation2;
    private float ripple;
    private boolean animated;
    private float posAnimation;
    public int alpha;
    private final boolean sub;

    public BooleanComponent(BooleanValue booleanValue, int x, int y, int width, int height, boolean sub, ModuleButton bigFather) {
        super(booleanValue, x, y, width, height, bigFather);
        this.colorAnimation = new ColorAnimation(new Color(0, 0, 0, 100));
        this.colorAnimation2 = new ColorAnimation(new Color(0, 0, 0, 100));
        this.posAnimation = x + width - 32 + (getObject().getValue() ? 21 : 7);
        this.animated = true;
        this.sub = sub;
    }

    @Override
    public float drawScreen(int mouseX, int mouseY, float partialTicks, int offset) {
        if (!getObject().isVisible()) {
            this.height = 0;
            return 0;
        }
        this.height = 20;
        this.offset = offset;
        float y = this.y + offset;

        if (sub)
            BasicRendering.drawRect(x, y, width, height, new Color(0, 0, 0, 50).getRGB());

        FontManager.TAHOMA.drawString(getObject().getValueName(), x + 8, y + height / 2f - FontManager.TAHOMA.getHeight() / 2f, -1);

        colorAnimation.animateTo(getObject().getValue() ? new Color(RenderUtils.reAlpha(Wrapper.Instance.getClickGui().getGlobalColor().getRGB(), 100), true) : new Color(0, 0, 0, 100), 0.3f);
        colorAnimation2.animateTo(getObject().getValue() ? new Color(RenderUtils.reAlpha(Wrapper.Instance.getClickGui().getGlobalColor().getRGB(), 200), true) : new Color(255, 255, 255, 100), 0.3f);

        posAnimation = LinearAnimation.animate(posAnimation, x + width - 32 + (getObject().getValue() ? 21 : 7), getBigFather().drag ? 1 : 0.5f);

        RenderUtils.drawRoundedRect(x + width - 32, y + 4, 28, 12, 6, colorAnimation.getColor().getRGB());
        BasicRendering.drawCircle(posAnimation, y + 2 + 8, 10, colorAnimation2.getColor().getRGB());
        if (!animated) {
            ripple = LinearAnimation.animate(ripple, 30, 0.1f);
            alpha = (int) LinearAnimation.animate(alpha, 0, 0.1f);
            BasicRendering.drawCircle(posAnimation, y + 2 + 8, ripple, RenderUtils.reAlpha(Wrapper.Instance.getClickGui().getGlobalColor().getRGB(), alpha));
            if (ripple > 29) {
                animated = true;
            }
        }
        return height;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!getObject().isVisible()) return;
        if (bounding(mouseX, mouseY)) {
            if (mouseButton == 0) {
                getObject().setValue(!getObject().getValue());
                ripple = 0;
                animated = false;
                alpha = 255;
            }
        }
    }
}
