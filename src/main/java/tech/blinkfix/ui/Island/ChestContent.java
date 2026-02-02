package tech.blinkfix.ui.Island;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tech.blinkfix.BlinkFix;
import tech.blinkfix.modules.impl.misc.ContainerStealer;
import tech.blinkfix.utils.renderer.Fonts;
import com.mojang.blaze3d.platform.Lighting;

import java.awt.*;

/**
 * 箱子内容 - 在Dynamic Island中显示打开的箱子界面
 * 优先级: 120 (高于普通模块但低于CommandPalette和ErrorMessage)
 */
public class ChestContent implements IslandContent {
    private static final Minecraft mc = Minecraft.getInstance();
    
    @Override
    public int getPriority() {
        return 120;
    }
    
    @Override
    public boolean shouldDisplay() {
        if (mc.player == null) {
            return false;
        }
        ContainerStealer chestStealer = (ContainerStealer) BlinkFix.getInstance().getModuleManager().getModule(ContainerStealer.class);
        if (chestStealer != null && chestStealer.isEnabled() && chestStealer.silent.getCurrentValue()) {
            return mc.player.containerMenu instanceof ChestMenu;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, PoseStack stack, float x, float y) {
        if (mc.player == null || !(mc.player.containerMenu instanceof ChestMenu menu)) {
            return;
        }

        Lighting.setupForFlatItems();
        graphics.pose().pushPose();
        graphics.pose().translate(x + 8, y + 4, 0);
        
        boolean isEmpty = true;
        int containerSlotCount = menu.getRowCount() * 9;

        for (int i = 0; i < containerSlotCount; i++) {
            Slot slot = menu.getSlot(i);
            if (slot == null) continue;

            int slotX = i % 9;
            int slotY = i / 9;
            float slotRenderX = slotX * 18.0f + 3.0f;
            float slotRenderY = slotY * 18.0f + 3.0f;
            
            ItemStack stack2 = slot.getItem();

            if (!stack2.isEmpty()) {
                isEmpty = false;
                RenderSystem.enableDepthTest();
                graphics.renderItem(stack2, (int)(slotRenderX - 2), (int)(slotRenderY - 3));
                graphics.renderItemDecorations(mc.font, stack2, (int)(slotRenderX - 2), (int)(slotRenderY - 3));
                RenderSystem.disableDepthTest();
            }
        }
        
        graphics.pose().popPose();

        if (isEmpty) {
            graphics.pose().pushPose();
            graphics.pose().translate(x + getWidth() / 2.0f, y + getHeight() / 2.0f, 0);
            String emptyText = "Empty...";
            float textWidth = Fonts.harmony.getWidth(emptyText, 0.5f);
            Fonts.harmony.render(graphics.pose(), emptyText, -textWidth / 2.0f, -Fonts.harmony.getHeight(false, 0.5f) / 2.0f, new Color(-1),true, 0.5f);
            graphics.pose().popPose();
        }

        Lighting.setupFor3DItems();
    }
    
    @Override
    public float getWidth() {
        return 180.0f;
    }
    
    @Override
    public float getHeight() {
        if (mc.player == null || !(mc.player.containerMenu instanceof ChestMenu menu)) {
            return 50.0f;
        }

        int rows = menu.getRowCount();
        return rows * 18.0f + 6.0f;
    }
}

