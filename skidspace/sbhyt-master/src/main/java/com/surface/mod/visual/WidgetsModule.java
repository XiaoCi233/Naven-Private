package com.surface.mod.visual;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.Event2D;
import com.surface.mod.Mod;
import com.surface.render.widgets.PotionHUD;
import com.surface.render.widgets.TargetHUD;
import com.surface.render.widgets.InventoryHUD;
import com.surface.render.widgets.Widget;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.List;

public class WidgetsModule extends Mod {

    private final List<Widget> widgetList = new ArrayList<>();

    public WidgetsModule() {
        super("Widgets", Category.VISUAL);
        addWidget(new TargetHUD());
        addWidget(new PotionHUD());
        addWidget(new InventoryHUD());
    }

    @EventTarget
    public void onRender2D(Event2D event) {
        final float pt = event.getPartialTicks();
        final ScaledResolution sr = event.getScaledResolution();
        final int mouseX = Mouse.getX() / sr.getScaleFactor();
        final int mouseY = (Display.getHeight() - Mouse.getY()) / sr.getScaleFactor();
        for (Widget widget : widgetList) {
            if (widget.getValue())
                widget.render(mouseX, mouseY, pt);
        }
    }

    private void addWidget(Widget widget) {
        this.registerValues(widget);
        this.widgetList.add(widget);
    }

}
