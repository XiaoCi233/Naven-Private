package com.surface.mod.player;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventPreUpdate;
import com.surface.events.EventTick;
import com.surface.mod.Mod;
import com.surface.util.HYTUtils;
import com.surface.util.ItemUtils;
import com.surface.util.TimerUtils;
import com.surface.value.impl.NumberValue;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;

public class ChestStealerModule extends Mod {

    public static final TimerUtils timer = new TimerUtils();
    public static boolean isChest = false;
    public static TimerUtils openChestTimer = new TimerUtils();
    private final NumberValue delay = new NumberValue("StealDelay", 100, 0, 1000, 10);

    private int nextDelay = 0;

    public ChestStealerModule() {
        super("ChestStealer", Category.PLAYER);
        registerValues(delay);
    }

    @Override
    public String getModTag() {
        return delay.getValue().toString();
    }

    @EventTarget
    public void onMotion(EventTick event) {
        if (HYTUtils.isInLobby()) return;

            if (mc.thePlayer.openContainer == null)
                return;

            if (mc.thePlayer.openContainer instanceof ContainerFurnace) {
                ContainerFurnace container = (ContainerFurnace) mc.thePlayer.openContainer;

                if (isFurnaceEmpty(container) && openChestTimer.hasTimeElapsed(100) && timer.hasTimeElapsed(100)) {
                    mc.thePlayer.closeScreen();
                    return;
                }

                for (int i = 0; i < container.tileFurnace.getSizeInventory(); ++i) {
                    if (container.tileFurnace.getStackInSlot(i) != null) {
                        if (timer.hasTimeElapsed(nextDelay)) {

                            for (int j = 0; j < 21; ++j) {
                                mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                            }
                            nextDelay = (int) (delay.getValue() * MathHelper.getRandomDoubleInRange(0.75, 1.25));
                            timer.reset();
                        }
                    }
                }
            }

            if (mc.thePlayer.openContainer instanceof ContainerBrewingStand) {
                ContainerBrewingStand container = (ContainerBrewingStand) mc.thePlayer.openContainer;

                if (isBrewingStandEmpty(container) && openChestTimer.hasTimeElapsed(100) && timer.hasTimeElapsed(100)) {
                    mc.thePlayer.closeScreen();
                    return;
                }

                for (int i = 0; i < container.tileBrewingStand.getSizeInventory(); ++i) {
                    if (container.tileBrewingStand.getStackInSlot(i) != null) {
                        if (timer.hasTimeElapsed(nextDelay)) {

                            for (int j = 0; j < 21; ++j) {
                                mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                            }
                            nextDelay = (int) (delay.getValue() * MathHelper.getRandomDoubleInRange(0.75, 1.25));
                            timer.reset();
                        }
                    }
                }
            }

            if (mc.thePlayer.openContainer instanceof ContainerChest && isChest) {
                ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;

                if (isChestEmpty(container) && openChestTimer.hasTimeElapsed(100) && timer.hasTimeElapsed(100)) {
                    mc.thePlayer.closeScreen();
                    return;
                }

                for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); ++i) {
                    if (container.getLowerChestInventory().getStackInSlot(i) != null) {
                        if (timer.hasTimeElapsed(nextDelay) && (isItemUseful(container, i))) {
                            for (int j = 0; j < 21; ++j) {
                                mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                            }
                            nextDelay = (int) (delay.getValue() * MathHelper.getRandomDoubleInRange(0.75, 1.25));
                            timer.reset();
                        }
                    }
                }
            }
        }

    private boolean isChestEmpty(ContainerChest c) {
        for (int i = 0; i < c.getLowerChestInventory().getSizeInventory(); ++i) {
            if (c.getLowerChestInventory().getStackInSlot(i) != null) {
                if (isItemUseful(c, i)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isFurnaceEmpty(ContainerFurnace c) {
        for (int i = 0; i < c.tileFurnace.getSizeInventory(); ++i) {
            if (c.tileFurnace.getStackInSlot(i) != null) {
                return false;
            }
        }

        return true;
    }

    private boolean isBrewingStandEmpty(ContainerBrewingStand c) {
        for (int i = 0; i < c.tileBrewingStand.getSizeInventory(); ++i) {
            if (c.tileBrewingStand.getStackInSlot(i) != null) {
                return false;
            }
        }

        return true;
    }

    private boolean isItemUseful(ContainerChest c, int i) {
        ItemStack itemStack = c.getLowerChestInventory().getStackInSlot(i);
        Item item = itemStack.getItem();

        if (item instanceof ItemAxe || item instanceof ItemPickaxe) {
            return true;
        }

        if (item instanceof ItemFood)
            return true;
        if (item instanceof ItemBow || item == Items.arrow)
            return true;

        if (item instanceof ItemPotion && !ItemUtils.isPotionNegative(itemStack))
            return true;
        if (item instanceof ItemSword && ItemUtils.isBestSword(c, itemStack))
            return true;
        if (item instanceof ItemArmor && ItemUtils.isBestArmor(c, itemStack))
            return true;
        if (item instanceof ItemBlock)
            return true;

        return item instanceof ItemEnderPearl;
    }
}
