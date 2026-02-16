
package com.surface.mod.player;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventTick;
import com.surface.mod.Mod;
import com.surface.util.TimerUtils;
import com.surface.value.impl.BooleanValue;
import com.surface.value.impl.ModeValue;
import com.surface.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState;


public class AutoArmorModule extends Mod {

    public static NumberValue DELAY = new NumberValue("Delay", 1.0D, 0.0D, 10.0D, 1.0D);
    private final ModeValue MODE = new ModeValue("Mode", "Basic", new String[]{"Basic", "Fake Inventory", "Open Inventory"});
    private final BooleanValue drop = new BooleanValue("Drop", true);
    private static final TimerUtils timer = new TimerUtils();


    public AutoArmorModule() {
        super("AutoArmor", Category.PLAYER);
        registerValues(DELAY,MODE,drop);
    }

    @Override
    public String getModTag() {
        return MODE.getValue();
    }

    @EventTarget
    public void onEvent(EventTick event) {
        long delay = DELAY.getValue().longValue() * 50;
        if (MODE.isCurrentMode("OpenINV") && !(mc.currentScreen instanceof GuiInventory)) {
            return;
        }

        if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat) {
            if (timer.hasTimeElapsed(delay)) {
                getBestArmor();
            }
        }
    }

    public static boolean isWorking() {
        return !timer.hasTimeElapsed(DELAY.getValue().longValue() * 50);
    }

    public void getBestArmor() {
        for (int type = 1; type < 5; type++) {
            if (mc.thePlayer.inventoryContainer.getSlot(4 + type).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(4 + type).getStack();
                if (isBestArmor(is, type)) {
                    continue;
                } else {
                    if (MODE.isCurrentMode("FakeINV")) {
                        C16PacketClientStatus p = new C16PacketClientStatus(EnumState.OPEN_INVENTORY_ACHIEVEMENT);
                        mc.thePlayer.sendQueue.addToSendQueue(p);
                    }
                    if (drop.getValue()) {
                        drop(4 + type);
                    }
                }
            }
            for (int i = 9; i < 45; i++) {
                if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                    ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                    if (isBestArmor(is, type) && getProtection(is) > 0) {
                        shiftClick(i);
                        timer.reset();
                        if (DELAY.getValue().longValue() > 0)
                            return;
                    }
                }
            }
        }
    }

    public static boolean isBestArmor(ItemStack stack, int type) {
        String strType = "";

        switch (type) {
            case 1:
                strType = "helmet";
                break;
            case 2:
                strType = "chestplate";
                break;
            case 3:
                strType = "leggings";
                break;
            case 4:
                strType = "boots";
                break;
        }

        if (!stack.getUnlocalizedName().contains(strType)) {
            return false;
        }

        float protection = getProtection(stack);
        if (((ItemArmor) (stack.getItem())).getArmorMaterial() == ItemArmor.ArmorMaterial.CHAIN) return false;

        for (int i = 5; i < 45; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = Minecraft.getMinecraft().thePlayer.inventoryContainer.getSlot(i).getStack();
                if (getProtection(is) > protection && is.getUnlocalizedName().contains(strType)) return false;
            }
        }

        return true;
    }

    public void shiftClick(int slot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 0, 1, mc.thePlayer);
    }

    public void drop(int slot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 1, 4, mc.thePlayer);
    }

    public static float getProtection(ItemStack stack) {
        float prot = 0.0f;
        if (stack.getItem() instanceof ItemArmor) {
            final ItemArmor armor = (ItemArmor) stack.getItem();
            prot += armor.damageReduceAmount + (100 - armor.damageReduceAmount)
                    * EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.0075;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack) / 100.0;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack) / 100.0;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack) / 100.0;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) / 50.0;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.featherFalling.effectId, stack) / 100.0;
        }
        return prot;
    }



}
