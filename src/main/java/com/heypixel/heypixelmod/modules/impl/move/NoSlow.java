package com.heypixel.heypixelmod.modules.impl.move;

import com.heypixel.heypixelmod.components.BlinkComponent;
import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.*;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import com.heypixel.heypixelmod.values.impl.ModeValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

@ModuleInfo(
        name = "NoSlow",
        description = "Prevents slowdown when using items",
        category = Category.MOVEMENT
)
public class NoSlow extends Module {
    public ModeValue mode = ValueBuilder.create(this, "Mode")
            .setDefaultModeIndex(0)
            .setModes("Normal", "Blink", "Hypixel", "Half")
            .build()
            .getModeValue();
    public BooleanValue sword = ValueBuilder.create(this, "Sword")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public BooleanValue food = ValueBuilder.create(this, "Food")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public BooleanValue bow = ValueBuilder.create(this, "Bow")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public BooleanValue potion = ValueBuilder.create(this, "Potion")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public BooleanValue shield = ValueBuilder.create(this, "Shield")
            .setDefaultBooleanValue(true)
            .setVisibility(() -> mode.isCurrentMode("Hypixel") && sword.getCurrentValue())
            .build()
            .getBooleanValue();

    public BooleanValue web = ValueBuilder.create(this, "Web")
            .setDefaultBooleanValue(true)
            .setVisibility(() -> mode.isCurrentMode("Half"))
            .build()
            .getBooleanValue();

    private int playerInWebTick = 0;
    private int ticksInWeb = 0;
    private boolean temporarilyDisabled = false;
    private boolean isUsingItem = false;
    private int usingItemTicks = 0;

    public void setTemporarilyDisabled(boolean disabled) {
        this.temporarilyDisabled = disabled;
    }

    @Override
    public void onEnable() {
        if (!temporarilyDisabled) {
            super.onEnable();
        }
        isUsingItem = false;
        usingItemTicks = 0;
        playerInWebTick = 0;
        ticksInWeb = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        isUsingItem = false;
        usingItemTicks = 0;
        playerInWebTick = 0;
        ticksInWeb = 0;

        if (BlinkComponent.isBlinking()) {
            BlinkComponent.stopBlink();
        }
    }
    public boolean shouldApplyNoSlow() {
        return this.isEnabled() && !temporarilyDisabled;
    }
    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.POST && this.playerInWebTick < mc.player.tickCount) {
            this.ticksInWeb = 0;
        }
    }

    @EventTarget
    public void onJump(EventMoveInput e) {
        if (mode.isCurrentMode("Half") && web.getCurrentValue() && this.ticksInWeb > 1) {
            e.setJump(false);
        }
    }

    @EventTarget
    public void onStuck(EventStuckInBlock e) {
        if (mode.isCurrentMode("Half") && web.getCurrentValue() && e.getState().getBlock() == Blocks.COBWEB) {
            this.playerInWebTick = mc.player.tickCount;
            this.ticksInWeb++;
            if (this.ticksInWeb > 5) {
                Vec3 newSpeed = new Vec3(0.88, 1.88, 0.88);
                e.setStuckSpeedMultiplier(newSpeed);
            }
        }
    }

    @EventTarget
    public void onSlow(EventSlowdown eventSlowdown) {
        if (!shouldHandleSlowdown()) {
            return;
        }

        switch (mode.getCurrentMode()) {
            case "Normal":
                handleNormalMode(eventSlowdown);
                break;
            case "Blink":
                handleBlinkMode(eventSlowdown);
                break;
            case "Hypixel":
                handleHypixelMode(eventSlowdown);
                break;
            case "Half":
                handleHalfMode(eventSlowdown);
                break;
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (isUsingItem) {
            usingItemTicks++;
            if (mode.isCurrentMode("Blink") && isUsingItem) {
                handleBlinkUpdate();
            }
        } else {
            usingItemTicks = 0;
            if (BlinkComponent.isBlinking()) {
                BlinkComponent.stopBlink();
            }
        }
        if (mode.isCurrentMode("Hypixel") && isUsingItem) {
            handleHypixelBypass();
        }
    }

    private void handleBlinkUpdate() {
        ItemStack mainHand = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (food.getCurrentValue() && mainHand.getItem().isEdible() &&
                mc.player.isUsingItem() && mc.player.getUseItemRemainingTicks() > 10) {
            if (usingItemTicks % 4 == 0) {
                ServerboundUseItemPacket packet = new ServerboundUseItemPacket(
                        InteractionHand.MAIN_HAND,
                        mc.player.getUseItemRemainingTicks()
                );
                mc.getConnection().send(packet);
            }
        }
    }

    private boolean shouldHandleSlowdown() {
        if (!mc.player.isUsingItem()) {
            isUsingItem = false;
            usingItemTicks = 0;
            return false;
        }

        ItemStack mainHand = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = mc.player.getItemInHand(InteractionHand.OFF_HAND);
        boolean shouldHandle = false;
        if (sword.getCurrentValue() && mainHand.getItem() instanceof net.minecraft.world.item.SwordItem) {
            shouldHandle = true;
        } else if (food.getCurrentValue() && mainHand.getItem().isEdible()) {
            shouldHandle = true;
        } else if (bow.getCurrentValue() && mainHand.getItem() instanceof net.minecraft.world.item.BowItem) {
            shouldHandle = true;
        } else if (potion.getCurrentValue() && mainHand.getItem() == Items.POTION) {
            shouldHandle = true;
        } else if (mode.isCurrentMode("Hypixel") && shield.getCurrentValue() &&
                offHand.getItem() == Items.SHIELD && mc.player.getUseItemRemainingTicks() > 0) {
            shouldHandle = true;
        }
        if (shouldHandle) {
            isUsingItem = true;
        } else {
            isUsingItem = false;
            usingItemTicks = 0;
        }

        return shouldHandle;
    }

    private void handleNormalMode(EventSlowdown eventSlowdown) {
        eventSlowdown.setSlowdown(false);
        mc.player.setSprinting(true);
    }

    private void handleBlinkMode(EventSlowdown eventSlowdown) {
        ItemStack mainHand = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHand.getItem() instanceof net.minecraft.world.item.SwordItem ||
                mainHand.getItem() instanceof net.minecraft.world.item.BowItem) {
            return;
        }
        if (!BlinkComponent.isBlinking()) {
            BlinkComponent.startBlink();
        }
        eventSlowdown.setSlowdown(false);
        mc.player.setSprinting(true);
    }

    private void handleHypixelMode(EventSlowdown eventSlowdown) {
        ItemStack offHand = mc.player.getItemInHand(InteractionHand.OFF_HAND);
        boolean isShieldBlocking = offHand.getItem() == Items.SHIELD &&
                mc.player.getUseItemRemainingTicks() > 0;
        if (isShieldBlocking && shield.getCurrentValue()) {
            simulate18SwordBlock();
        }

        eventSlowdown.setSlowdown(false);
        mc.player.setSprinting(true);
    }

    private void handleHalfMode(EventSlowdown eventSlowdown) {
        ItemStack mainHand = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (food.getCurrentValue() && mainHand.getItem().isEdible()) {
            handleHalfFood(eventSlowdown);
        } else if (bow.getCurrentValue() && mainHand.getItem() instanceof net.minecraft.world.item.BowItem) {
            handleHalfBow(eventSlowdown);
        } else if (potion.getCurrentValue() && mainHand.getItem() instanceof net.minecraft.world.item.PotionItem) {
            handleHalfPotion(eventSlowdown);
        } else {
            eventSlowdown.setSlowdown(false);
            mc.player.setSprinting(true);
        }
    }

    private void handleHalfFood(EventSlowdown eventSlowdown) {
        if (usingItemTicks % 2 == 0) {
            eventSlowdown.setSlowdown(false);
            mc.player.setSprinting(true);
        } else {
            eventSlowdown.setSlowdown(true);
        }
    }

    private void handleHalfBow(EventSlowdown eventSlowdown) {
        if (usingItemTicks % 2 == 0) {
            eventSlowdown.setSlowdown(false);
            mc.player.setSprinting(true);
        } else {
            eventSlowdown.setSlowdown(true);
        }
    }

    private void handleHalfPotion(EventSlowdown eventSlowdown) {
        if (usingItemTicks % 2 == 0) {
            eventSlowdown.setSlowdown(false);
            mc.player.setSprinting(true);
        } else {
            eventSlowdown.setSlowdown(true);
        }
    }

    private void simulate18SwordBlock() {
        if (usingItemTicks % 5 == 0) {
            ServerboundUseItemPacket packet = new ServerboundUseItemPacket(
                    InteractionHand.OFF_HAND,
                    mc.player.getUseItemRemainingTicks()
            );
            mc.getConnection().send(packet);
        }
    }

    private void handleHypixelBypass() {
        ItemStack mainHand = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (bow.getCurrentValue() && mainHand.getItem() instanceof net.minecraft.world.item.BowItem &&
                mc.player.isUsingItem() && mc.player.getUseItemRemainingTicks() > 10) {
            if (usingItemTicks % 3 == 0) {
                ServerboundUseItemPacket packet = new ServerboundUseItemPacket(
                        InteractionHand.MAIN_HAND,
                        mc.player.getUseItemRemainingTicks()
                );
                mc.getConnection().send(packet);
            }
        }

        if (food.getCurrentValue() && mainHand.getItem().isEdible() &&
                mc.player.isUsingItem() && mc.player.getUseItemRemainingTicks() > 10) {
            if (usingItemTicks % 4 == 0) {
                ServerboundUseItemPacket packet = new ServerboundUseItemPacket(
                        InteractionHand.MAIN_HAND,
                        mc.player.getUseItemRemainingTicks()
                );
                mc.getConnection().send(packet);
            }
        }
    }
}