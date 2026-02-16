package com.surface.render.widgets;

import com.surface.render.font.FontManager;
import com.surface.util.render.RenderUtils;
import com.surface.util.render.shader.ShaderElement;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;

import java.awt.*;

import static com.surface.Wrapper.mc;

public class InventoryHUD extends Widget {
    public InventoryHUD() {
        super(150, 100, "InventoryHUD");
    }

    @Override
    public void render(int mouseX, int mouseY, float renderPartialTicks) {
        setX(60);
        setY(60);
        FontManager.WQY.setFontSize(16);

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        ShaderElement.addBlurTask(() -> RenderUtils.drawRoundedRect(x + 1, y + 13, width + 166, height + 65, 4f, new Color(0, 0, 0, 144).getRGB()));
        RenderUtils.drawRoundedRect(x + 1, y + 13, width + 166, height + 65, 4f, new Color(0, 0, 0, 144).getRGB());

        boolean hasStacks = false;

        FontManager.WQY.drawString("Inventory",
                x + 4,
                y + 14,
                Color.WHITE.getRGB());

        for (int i1 = 9; i1 < mc.thePlayer.inventoryContainer.inventorySlots.size() - 9; ++i1) {
            Slot slot = mc.thePlayer.inventoryContainer.inventorySlots.get(i1);
            if (slot.getHasStack()) hasStacks = true;
            int i = slot.xDisplayPosition;
            int j = slot.yDisplayPosition;
            mc.getRenderItem().renderItemAndEffectIntoGUI(slot.getStack(), (int) (x + i - 4), (int) (y + j - 60));
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, slot.getStack(), x + i - 4, y + j - 60, null);
        }

        if (!hasStacks) {
           FontManager.WQY.drawString("Empty",
                    x + 167 / 2 - mc.fontRendererObj.getStringWidth("Empty") / 2,
                    y + 72 / 2 + 8,
                    Color.WHITE.getRGB());
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
    }

}
