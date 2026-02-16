package com.surface.mod.player;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventPreUpdate;
import com.surface.mod.Mod;
import com.surface.util.TimerUtils;
import com.surface.value.impl.BooleanValue;
import com.surface.value.impl.NumberValue;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;

public class RefillModule extends Mod {

    public NumberValue delay = new NumberValue("Delay", 100f, 50f, 1000f, 50f);
    public BooleanValue Soup = new BooleanValue("Soup", false);
    public BooleanValue Pot = new BooleanValue("Pot", false);
    public BooleanValue onInv = new BooleanValue("Only Inventor", false);
    TimerUtils time = new TimerUtils();
    Item value;

    public RefillModule() {
        super("Auto Refill", Category.PLAYER);
        registerValues(delay, Soup, Pot, onInv);
    }


    @EventTarget
    public void onUpdate(EventPreUpdate event) {
        if (Soup.getValue()) {
            this.value = Items.mushroom_stew;
        } else if (Pot.getValue()) {
            ItemPotion itempotion = Items.potionitem;
            this.value = ItemPotion.getItemById(373);
        }

        this.refill();
    }

    private void refill() {
        if (!onInv.getValue() || mc.currentScreen instanceof GuiInventory) {
            if (!isHotbarFull() && this.time.hasTimeElapsed(delay.getValue().longValue())) {
                refill(this.value);
                this.time.reset();
            }
        }
    }


    public boolean isHotbarFull() {
        for (int i = 0; i <= 36; ++i) {
            ItemStack itemstack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemstack == null) {
                return false;
            }
        }

        return true;
    }


    public void refill(Item value) {
        for (int i = 9; i < 37; ++i) {
            ItemStack itemstack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (itemstack != null && itemstack.getItem() == value) {
                mc.playerController.windowClick(0, i, 0, 1, mc.thePlayer);
                break;
            }
        }
    }
}
