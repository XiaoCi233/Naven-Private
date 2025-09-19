package com.heypixel.heypixelmod.modules.impl.misc;

import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.impl.EventUpdate;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.utils.ChatUtils;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(
        name = "MidPearl",
        description = "Throw ender pearl with middle click",
        category = Category.MISC
)
public class MidPearl extends Module {
    private final BooleanValue spoof = ValueBuilder.create(this, "Spoof")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    private boolean middleClickPressed = false;
    private int pearlSlot = -1;
    private int originalSlot = -1;

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.screen != null || mc.player == null) return;

        boolean currentMiddleClick = GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == 1;

        if (currentMiddleClick && !middleClickPressed) {
            throwPearl();
            middleClickPressed = true;
        } else if (!currentMiddleClick) {
            middleClickPressed = false;
        }
    }

    private void throwPearl() {
        pearlSlot = findPearlSlot();
        if (pearlSlot == -1) {
            ChatUtils.addChatMessage("§cNo Pearl");
            return;
        }

        originalSlot = mc.player.getInventory().selected;

        if (spoof.getCurrentValue()) {
            mc.player.getInventory().selected = pearlSlot;
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.swing(InteractionHand.MAIN_HAND);
            mc.player.getInventory().selected = originalSlot;
        } else {
            mc.player.getInventory().selected = pearlSlot;
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private int findPearlSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && originalSlot != -1) {
            mc.player.getInventory().selected = originalSlot;
        }
        middleClickPressed = false;
        pearlSlot = -1;
        originalSlot = -1;
    }
}