package com.surface.render.clickgui.components.impl;

import com.surface.render.clickgui.components.Component;
import com.surface.render.clickgui.components.ModuleButton;
import com.surface.render.font.FontManager;

public class LabelComponent extends Component<String> {

    public LabelComponent(String string, int x, int y, int width, int height, ModuleButton bigFather) {
        super(string, x, y, width, height, bigFather);
    }

    @Override
    public float drawScreen(int mouseX, int mouseY, float partialTicks, int offset) {
        this.offset = offset;
        float y = this.y + offset;

        FontManager.TAHOMA.drawString(getObject(), x + 8, y + height / 2f - FontManager.TAHOMA.getHeight() / 2f, -1);

        return height;
    }

    public boolean isVisible() {
        return true;
    }
}
