package com.surface.render.clickgui.components.impl;

import com.surface.render.clickgui.components.Component;
import com.surface.render.clickgui.components.ModuleButton;
import com.surface.render.font.FontManager;
import com.surface.value.impl.ModeValue;
import renderassist.animations.RippleAnimation;
import renderassist.rendering.BasicRendering;

import java.awt.*;
import java.util.Arrays;

public class ModeComponent extends Component<ModeValue> {

    private final RippleAnimation animation;

    public ModeComponent(ModeValue modeValue, int x, int y, int width, int height, ModuleButton bigFather) {
        super(modeValue, x, y, width, height, bigFather);
        this.animation = new RippleAnimation();

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

        FontManager.TAHOMA.drawString(getObject().getValueName(), x + 8, y + height / 2f - FontManager.TAHOMA.getHeight() / 2f, -1);

        float w = FontManager.TAHOMA.getStringWidth(getObject().getValue());

        animation.draw(() -> BasicRendering.drawRect(x + width - w - 2 - 9, y + 2, w + 6, 16, 6));

        FontManager.TAHOMA.drawString(getObject().getValue(), x + width - w - 8, y + height / 2f - FontManager.TAHOMA.getHeight() / 2f, new Color(255, 255, 255, 200).getRGB());

        return height;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!getObject().isVisible()) return;
        if (bounding(mouseX, mouseY)) {
            float y = this.y + offset;
            float w = FontManager.TAHOMA.getStringWidth(getObject().getValue());
            animation.addRipple(x + width - w - 2 - 9 + (w + 6) / 2f, y + 2 + 8, 100, 1);
            if (mouseButton == 0) {
                java.util.List<String> values = Arrays.asList(getObject().getModes());
                getObject().setValue(values.indexOf(getObject().getValue()) + 1 == values.size() ? values.get(0) : values.get(values.indexOf(getObject().getValue()) + 1));
            }
            if (mouseButton == 1) {
                java.util.List<String> values = Arrays.asList(getObject().getModes());
                getObject().setValue(values.indexOf(getObject().getValue()) - 1 < 0 ? values.get(values.size() - 1) : values.get(values.indexOf(getObject().getValue()) - 1));
            }
        }
    }
}
