package com.surface.mod.player;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.EventPreUpdate;
import com.surface.mod.Mod;
import com.surface.util.TimerUtils;
import com.surface.value.impl.BooleanValue;
import com.surface.value.impl.NumberValue;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

public class AutoSoupModule extends Mod {
    public final NumberValue heal = new NumberValue("Health", 3.0, 1.0, 20.0, 0.5);
    public final NumberValue delay = new NumberValue("Delay", 300.0, 100.0, 1000.0, 50.0);
    public final BooleanValue drop = new BooleanValue("Drop", false);
    private final TimerUtils timer = new TimerUtils();

    public AutoSoupModule() {
        super("Auto Soup", Category.PLAYER);
        registerValues(heal, delay, drop);
    }

    @EventTarget
    private void onEvent(EventPreUpdate event) {
        int soupSlot = getSoupFromInventory();
        if (soupSlot != -1 && mc.thePlayer.getHealth() < heal.getValue().floatValue() && timer.hasTimeElapsed(delay.getValue().longValue())) {
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, getSoupFromInventory(), 6, 2, mc.thePlayer);
            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(6));
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            Wrapper.Instance.getInventoryManager().drop(6);
        }
    }

    private int getSoupFromInventory() {
        int soup = -1;
        int counter = 0;
        for (int i = 1; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                if (Item.getIdFromItem(item) == 282) {
                    counter++;
                    soup = i;
                }
            }
        }
        return soup;
    }
}
