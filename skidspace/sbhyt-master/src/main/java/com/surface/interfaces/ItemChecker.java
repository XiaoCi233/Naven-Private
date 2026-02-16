package com.surface.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.item.*;

public interface ItemChecker {
    default boolean hasSword() {
        return Minecraft.getMinecraft().thePlayer.getHeldItem() != null && Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() != null && Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    default boolean hasBow() {
        return Minecraft.getMinecraft().thePlayer.getHeldItem() != null && Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() != null && Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() instanceof ItemBow;
    }

    default boolean hasFood() {
        return Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem() != null && (Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBow || Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem().getItem() instanceof ItemFood || Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem().getItem() instanceof ItemPotion || Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem().getItem() instanceof ItemAppleGold || Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBucketMilk);
    }
}
