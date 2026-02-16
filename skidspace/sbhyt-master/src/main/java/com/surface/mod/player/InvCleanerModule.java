package com.surface.mod.player;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.EventPreUpdate;
import com.surface.mod.Mod;
import com.surface.mod.world.ScaffoldModule;
import com.surface.util.HYTUtils;
import com.surface.util.TimerUtils;
import com.surface.util.player.PlayerUtils;
import com.surface.value.impl.BooleanValue;
import com.surface.value.impl.FilterValue;
import com.surface.value.impl.NumberValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.Arrays;

public class InvCleanerModule extends Mod {
    public final NumberValue delay = new NumberValue("Delay", 120D, 0D, 300D, 10D);
    public final BooleanValue onlyWhileNotMoving = new BooleanValue("No Moving", false);
    public final BooleanValue fakeinv = new BooleanValue("FakeINV", true);
    public final BooleanValue inventoryOnly = new BooleanValue("Open Inventory", false);
    public final BooleanValue dropArchery = new BooleanValue("Drop archery", false);
    public final BooleanValue dropFood = new BooleanValue("Drop food", false);
    public final BooleanValue dropTNTandFlint = new BooleanValue("Drop TNT and Flint", false);
    public final BooleanValue dropSpawnEgg = new BooleanValue("Drop Spawn egg", false);
    public final BooleanValue moveArrows = new BooleanValue("Move arrows", true) {
        @Override
        public boolean isVisible() {
            return !dropArchery.getValue();
        }
    };
    private final String[] shitItems = {"stick", "egg", "string","cake", "mushroom", "compass", "dyePowder", "feather", "bucket", "chest", "snow", "fish", "enchant", "exp", "shears", "anvil", "torch", "seeds", "leather", "reeds", "skull", "record", "snowball", "piston"};
    private final String[] serverItems = {"selector", "tracking compass", "(right click)", "tienda ", "perfil", "salir", "shop", "collectibles", "game", "profil", "lobby", "show all", "hub", "friends only", "cofre", "(click", "teleport", "play", "exit", "hide all", "jeux", "gadget", " (activ", "emote", "amis", "bountique", "choisir", "choose "};
    private final TimerUtils timer = new TimerUtils();
    public InvCleanerModule() {
        super("Inventory Cleaner", Category.PLAYER);
        FilterValue<Item> filterValue = new FilterValue<Item>("Drop", dropArchery, dropFood, dropTNTandFlint, dropSpawnEgg) {
            @Override
            public boolean isValid(Item e) {
                return false;
            }
        };
        registerValues(delay, onlyWhileNotMoving,fakeinv, inventoryOnly, filterValue, moveArrows);
    }

    @Override
    public String getModTag() {
        return delay.getValue().toString();
    }

    public float getDamageScore(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return 0;

        float damage = 0;
        Item item = stack.getItem();

        if (item instanceof ItemSword) {
            damage += ((ItemSword) item).getDamageVsEntity();
        } else if (item instanceof ItemTool) {
            damage += item.getMaxDamage();
        }

        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25F +
                EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 0.1F;

        return damage;
    }

    public float getProtScore(ItemStack stack) {
        float prot = 0;
        if (stack.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor) stack.getItem();
            prot += armor.damageReduceAmount
                    + ((100 - armor.damageReduceAmount) * EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack)) * 0.0075F;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack) / 100F;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack) / 100F;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack) / 100F;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) / 25.F;
            prot += EnchantmentHelper.getEnchantmentLevel(Enchantment.featherFalling.effectId, stack) / 100F;
        }
        return prot;
    }

    @EventTarget
    @SuppressWarnings("unused")
    public void onTick(EventPreUpdate e) {
        if (mc.playerController.isSpectator()) return;
        if(AutoArmorModule.isWorking())return;
        if(Wrapper.Instance.getModManager().getModFromName("Scaffold").isEnable())return;
        if (HYTUtils.isInLobby()) return;
        if (stop()) return;
        if (!mc.thePlayer.isUsingItem() && (mc.currentScreen == null || mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiIngameMenu)) {

            if (fakeinv.getValue()) {
                C16PacketClientStatus p = new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT);
                mc.thePlayer.sendQueue.addToSendQueue(p);
            }

            long delay = this.delay.getValue().longValue();
            if (timer.hasTimeElapsed(delay)) {
                Slot slot = mc.thePlayer.inventoryContainer.getSlot(getDesiredSlot(ItemType.WEAPON));
                if (!slot.getHasStack() || !isBestWeapon(slot.getStack())) {
                    getBestWeapon();
                }
            }
            if (timer.hasTimeElapsed(delay)) getBestPickaxe();
            if (timer.hasTimeElapsed(delay)) getBestAxe();
            if (timer.hasTimeElapsed(delay)) getBestShovel();
            if (timer.hasTimeElapsed(delay)) {
                for (int i = 9; i < 45; i++) {
                    if (stop()) return;
                    Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
                    ItemStack is = slot.getStack();
                    if (is != null && shouldDrop(is, i)) {
                        Wrapper.Instance.getInventoryManager().drop(i);
                        timer.reset();
                        break;
                    }
                }
            }
            if (timer.hasTimeElapsed(delay)) swapBlocks();
            if (timer.hasTimeElapsed(delay)) getBestBow();
            if (timer.hasTimeElapsed(delay)) moveArrows();
        }
    }

    private int getDesiredSlot(ItemType tool) {
        int slot = 36;
        switch (tool) {
            case WEAPON:
                slot = (1);
                break;
            case PICKAXE:
                slot = (2);
                break;
            case AXE:
                slot = (3);
                break;
            case SHOVEL:
                slot = (4);
                break;
            case BLOCK:
                slot = (5);
                break;
            case BOW:
                slot = (6);
                break;
        }
        return slot + 35;
    }

    private boolean isBestWeapon(ItemStack is) {
        float damage = getDamageScore(is);
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack()) {
                ItemStack is2 = slot.getStack();
                if (getDamageScore(is2) > damage && is2.getItem() instanceof ItemSword) {
                    return false;
                }
            }
        }
        return is.getItem() instanceof ItemSword;
    }

    private void getBestWeapon() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (isBestWeapon(is) && getDamageScore(is) > 0 && is.getItem() instanceof ItemSword) {
                    swap(i, getDesiredSlot(ItemType.WEAPON) - 36);
                    break;
                }
            }
        }
    }

    private boolean shouldDrop(ItemStack stack, int slot) {
        String stackName = stack.getDisplayName().toLowerCase();
        Item item = stack.getItem();
        String ulName = item.getUnlocalizedName();
        if (Arrays.stream(serverItems).anyMatch(stackName::contains)) return false;

        if (item instanceof ItemBlock) {
            return !ScaffoldModule.isValid(Item.getItemFromBlock(((ItemBlock) item).getBlock()));
        }

        int weaponSlot = getDesiredSlot(ItemType.WEAPON);
        int pickaxeSlot = getDesiredSlot(ItemType.PICKAXE);
        int axeSlot = getDesiredSlot(ItemType.AXE);
        int shovelSlot = getDesiredSlot(ItemType.SHOVEL);

        if ((slot != weaponSlot || !isBestWeapon(mc.thePlayer.inventoryContainer.getSlot(weaponSlot).getStack()))
                && (slot != pickaxeSlot || !isBestPickaxe(mc.thePlayer.inventoryContainer.getSlot(pickaxeSlot).getStack()))
                && (slot != axeSlot || !isBestAxe(mc.thePlayer.inventoryContainer.getSlot(axeSlot).getStack()))
                && (slot != shovelSlot || !isBestShovel(mc.thePlayer.inventoryContainer.getSlot(shovelSlot).getStack()))) {
            if (item instanceof ItemArmor) {
                for (int type = 1; type < 5; type++) {
                    if (mc.thePlayer.inventoryContainer.getSlot(4 + type).getHasStack()) {
                        ItemStack is = mc.thePlayer.inventoryContainer.getSlot(4 + type).getStack();
                        if (isBestArmor(is, type) && !ulName.toLowerCase().contains("chain")) {
                            continue;
                        }
                    }
                    if (isBestArmor(stack, type) && !ulName.toLowerCase().contains("chain")) {
                        return false;
                    }
                }
            }
            
            if ((item == Items.wheat)
                    || (item == Items.spawn_egg && dropSpawnEgg.getValue())
                    || (item instanceof ItemFood && dropFood.getValue() && !(item instanceof ItemAppleGold))
                    || (item instanceof ItemPotion && isShitPotion(stack))) {
                return true;
            } else if (!(item instanceof ItemSword) && !(item instanceof ItemTool) && !(item instanceof ItemHoe) && !(item instanceof ItemArmor)) {
                if (dropArchery.getValue() && (item instanceof ItemBow || item == Items.arrow)) {
                    return true;
                } else {
                    if (dropTNTandFlint.getValue() && (ulName.contains("tnt") || ulName.contains("flint"))) return true;
                    return item instanceof ItemGlassBottle || Arrays.stream(shitItems).anyMatch(ulName::contains);
                }
            }
            return true;
        }

        return false;
    }

    private void getBestPickaxe() {
        for (int i = 9; i < 45; ++i) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack()) {
                ItemStack is = slot.getStack();
                if (isBestPickaxe(is) && !isBestWeapon(is)) {
                    int desiredSlot = getDesiredSlot(ItemType.PICKAXE);
                    if (i == desiredSlot) return;
                    Slot slot2 = mc.thePlayer.inventoryContainer.getSlot(desiredSlot);
                    if (!slot2.getHasStack() || !isBestPickaxe(slot2.getStack())) {
                        swap(i, desiredSlot - 36);
                    }
                }
            }
        }
    }

    private void getBestAxe() {
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack()) {
                ItemStack is = slot.getStack();
                if (isBestAxe(is) && !isBestWeapon(is)) {
                    int desiredSlot = getDesiredSlot(ItemType.AXE);
                    if (i == desiredSlot) return;
                    Slot slot2 = mc.thePlayer.inventoryContainer.getSlot(desiredSlot);
                    if (!slot2.getHasStack() || !isBestAxe(slot2.getStack())) {
                        swap(i, desiredSlot - 36);
                        timer.reset();
                    }
                }
            }
        }
    }

    private void getBestShovel() {
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack()) {
                ItemStack is = slot.getStack();
                if (isBestShovel(is) && !isBestWeapon(is)) {
                    int desiredSlot = getDesiredSlot(ItemType.SHOVEL);
                    if (i == desiredSlot) return;
                    Slot slot2 = mc.thePlayer.inventoryContainer.getSlot(desiredSlot);
                    if (!slot2.getHasStack() || !isBestShovel(slot2.getStack())) {
                        swap(i, desiredSlot - 36);
                        timer.reset();
                    }
                }
            }
        }
    }

    private void getBestBow() {
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack()) {
                ItemStack is = slot.getStack();
                String stackName = is.getDisplayName().toLowerCase();
                if (Arrays.stream(serverItems).anyMatch(stackName::contains) || !(is.getItem() instanceof ItemBow))
                    continue;
                if (isBestBow(is) && !isBestWeapon(is)) {
                    int desiredSlot = getDesiredSlot(ItemType.BOW);
                    if (i == desiredSlot) return;
                    Slot slot2 = mc.thePlayer.inventoryContainer.getSlot(desiredSlot);
                    if (!slot2.getHasStack() || !isBestBow(slot2.getStack())) {
                        swap(i, desiredSlot - 36);
                    }
                }
            }
        }
    }

    private void moveArrows() {
        if (dropArchery.getValue() || !moveArrows.getValue()) return;
        for (int i = 36; i < 45; i++) {
            ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (is != null && is.getItem() == Items.arrow) {
                for (int j = 0; j < 36; j++) {
                    if (mc.thePlayer.inventoryContainer.getSlot(j).getStack() == null) {
                        Wrapper.Instance.getInventoryManager().click(i, 0, true);
                        timer.reset();
                        break;
                    }
                }
            }
        }
    }

    private int getMostBlocks() {
        int stack = 0;
        int biggestSlot = -1;
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            ItemStack is = slot.getStack();
            if (is != null && is.getItem() instanceof ItemBlock && is.stackSize > stack && Arrays.stream(serverItems).noneMatch(is.getDisplayName().toLowerCase()::contains)) {
                stack = is.stackSize;
                biggestSlot = i;
            }
        }
        return biggestSlot;
    }

    private void swapBlocks() {
        int mostBlocksSlot = getMostBlocks();
        int desiredSlot = getDesiredSlot(ItemType.BLOCK);
        if (mostBlocksSlot != -1 && mostBlocksSlot != desiredSlot) {
            // only switch if the hotbar slot doesn't already have blocks of the same quantity to prevent an inf loop
            Slot dss = mc.thePlayer.inventoryContainer.getSlot(desiredSlot);
            ItemStack dsis = dss.getStack();
            if (!(dsis != null && dsis.getItem() instanceof ItemBlock && dsis.stackSize >= mc.thePlayer.inventoryContainer.getSlot(mostBlocksSlot).getStack().stackSize && Arrays.stream(serverItems).noneMatch(dsis.getDisplayName().toLowerCase()::contains))) {
                swap(mostBlocksSlot, desiredSlot - 36);
            }
        }
    }

    private boolean isBestPickaxe(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemPickaxe)) {
            return false;
        } else {
            float value = getToolScore(stack);
            for (int i = 9; i < 45; i++) {
                Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
                if (slot.getHasStack()) {
                    ItemStack is = slot.getStack();
                    if (is.getItem() instanceof ItemPickaxe && getToolScore(is) > value) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private boolean isBestShovel(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemSpade)) {
            return false;
        } else {
            float score = getToolScore(stack);
            for (int i = 9; i < 45; i++) {
                Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
                if (slot.getHasStack()) {
                    ItemStack is = slot.getStack();
                    if (is.getItem() instanceof ItemSpade && getToolScore(is) > score) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private boolean isBestAxe(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemAxe)) {
            return false;
        } else {
            float value = getToolScore(stack);
            for (int i = 9; i < 45; i++) {
                Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
                if (slot.getHasStack()) {
                    ItemStack is = slot.getStack();
                    if (getToolScore(is) > value && is.getItem() instanceof ItemAxe && !isBestWeapon(stack)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private boolean isBestBow(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBow)) {
            return false;
        } else {
            float value = getPowerLevel(stack);
            for (int i = 9; i < 45; i++) {
                Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
                if (slot.getHasStack()) {
                    ItemStack is = slot.getStack();
                    if (getPowerLevel(is) > value && is.getItem() instanceof ItemBow && !isBestWeapon(stack)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private float getPowerLevel(ItemStack stack) {
        float score = 0;
        Item item = stack.getItem();
        if (item instanceof ItemBow) {
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) * 0.5F;
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1F;
        }
        return score;
    }

    private float getToolScore(ItemStack stack) {
        float score = 0;
        Item item = stack.getItem();
        if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool) item;
            String name = item.getUnlocalizedName().toLowerCase();
            if (item instanceof ItemPickaxe) {
                score = tool.getStrVsBlock(stack, Blocks.stone) - (name.contains("gold") ? 5 : 0);
            } else if (item instanceof ItemSpade) {
                score = tool.getStrVsBlock(stack, Blocks.dirt) - (name.contains("gold") ? 5 : 0);
            } else {
                if (!(item instanceof ItemAxe)) return 1;
                score = tool.getStrVsBlock(stack, Blocks.log) - (name.contains("gold") ? 5 : 0);
            }
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) * 0.0075F;
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) / 100F;
        }
        return score;
    }

    private boolean isShitPotion(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemPotion) {
            ItemPotion pot = (ItemPotion) stack.getItem();
            if (pot.getEffects(stack) == null) return true;
            for (PotionEffect effect : pot.getEffects(stack)) {
                if (effect.getPotionID() == Potion.moveSlowdown.getId()
                        || effect.getPotionID() == Potion.weakness.getId()
                        || effect.getPotionID() == Potion.poison.getId()
                        || effect.getPotionID() == Potion.harm.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isBestArmor(ItemStack stack, int type) {
        String typeStr = "";
        switch (type) {
            case 1:
                typeStr = "helmet";
                break;
            case 2:
                typeStr = "chestplate";
                break;
            case 3:
                typeStr = "leggings";
                break;
            case 4:
                typeStr = "boots";
                break;
        }
        if (stack.getUnlocalizedName().contains(typeStr)) {
            float prot = getProtScore(stack);
            for (int i = 5; i < 45; i++) {
                Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
                if (slot.getHasStack()) {
                    ItemStack is = slot.getStack();
                    if (is.getUnlocalizedName().contains(typeStr) && getProtScore(is) > prot) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void swap(int slot, int hSlot) {
        Wrapper.Instance.getInventoryManager().swap(slot, hSlot);
        timer.reset();
    }

    private boolean stop() {
        return (inventoryOnly.getValue() && !(mc.currentScreen instanceof GuiInventory)) || (onlyWhileNotMoving.getValue() && PlayerUtils.movementInput());
    }

    private enum ItemType {
        WEAPON, PICKAXE, AXE, SHOVEL, BLOCK, BOW
    }
}
