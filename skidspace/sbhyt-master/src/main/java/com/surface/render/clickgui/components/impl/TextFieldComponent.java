package com.surface.render.clickgui.components.impl;

import com.surface.render.clickgui.components.Component;
import com.surface.render.clickgui.components.ModuleButton;
import com.surface.render.font.FontManager;
import com.surface.util.element.InputField;
import com.surface.util.render.RenderUtils;
import com.surface.value.impl.TextValue;
import renderassist.animations.ColorAnimation;

import java.awt.*;

public class TextFieldComponent extends Component<TextValue> {

    private final InputField field;
    private final ColorAnimation animation;

    public TextFieldComponent(TextValue value, int x, int y, int width, int height, ModuleButton bigFather) {
        super(value, x, y, width, height, bigFather);
        field = new InputField(FontManager.TAHOMA);
        field.setText(value.getValue());
        field.setDrawingBackground(false);
        animation = new ColorAnimation(new Color(0, 0, 0, 30));
    }

    @Override
    public float drawScreen(int mouseX, int mouseY, float partialTicks, int offset) {
        if (!getObject().isVisible()) {
            this.height = 0;
            return 0;
        }
        this.height = 30;
        this.offset = offset;
        float y = this.y + offset;

        FontManager.TAHOMA.drawString(getObject().getValueName(), x + 8, y + 2, -1);
        animation.animateTo(new Color(0, 0, 0, field.isFocused() ? 60 : 20), 0.5f);

        field.setxPosition(x + 8);
        field.setyPosition(y + 4 + FontManager.TAHOMA.getHeight());
        field.setWidth(width - 16);
        field.setHeight(18);

        RenderUtils.drawRoundedRect(x + 8, y + 4 + FontManager.TAHOMA.getHeight(), width - 16, 18, 4, animation.getColor().getRGB());

        field.drawTextBox(mouseX, mouseY);

        return height;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        field.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        field.keyTyped(typedChar, keyCode);

        getObject().setValue(field.getText());
    }
}
