package tech.blinkfix.modules.impl.move;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventSlowdown;
import tech.blinkfix.events.impl.EventUpdate;
import tech.blinkfix.events.impl.EventMoveInput;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.ModeValue;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@ModuleInfo(
        name = "NoSlow",
        description = "NoSlowDown",
        category = Category.MOVEMENT
)
public class NoSlow extends Module {
    private boolean wasSlowdownActive = false;
    private boolean pausePhase = false;
    private int pauseTimer = 0;
    private final int PAUSE_DURATION = 0;
    private int onGroundTick = 0;

    public ModeValue mode = ValueBuilder.create(this, "Mode")
            .setDefaultModeIndex(0)
            .setModes("50%", "90%", "GrimJump", "None", "Grim50%", "Grim1/3")
            .build()
            .getModeValue();
            
    public BooleanValue food = ValueBuilder.create(this, "Food")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
            
    public BooleanValue bow = ValueBuilder.create(this, "Bow")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
            
    public BooleanValue crossbow = ValueBuilder.create(this, "Crossbow")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    @EventTarget
    public void onSlow(EventSlowdown eventSlowdown) {
        if (mc.player == null || (checkFood() && mc.player.getUseItemRemainingTicks() > 30)) return;
        
        // Check item restrictions
        if (!food.getCurrentValue() && checkFood()) return;
        if (!bow.getCurrentValue() && checkItem(Items.BOW)) return;
        if (!crossbow.getCurrentValue() && checkItem(Items.CROSSBOW)) return;
        
        // Original modes
        if (this.mode.isCurrentMode("50%")) {
            boolean shouldActivate = mc.player.getUseItemRemainingTicks() % 3 != 0;

            if (shouldActivate) {
                if (!wasSlowdownActive) {
                    pausePhase = true;
                    pauseTimer = PAUSE_DURATION;
                    wasSlowdownActive = true;
                    return;
                }

                if (pausePhase) {
                    pauseTimer--;
                    if (pauseTimer <= 0) {
                        pausePhase = false;
                        eventSlowdown.setSlowdown(false);
                        mc.player.setSprinting(true);
                    }
                    return;
                }

                eventSlowdown.setSlowdown(false);
                mc.player.setSprinting(true);
            } else {
                wasSlowdownActive = false;
                pausePhase = false;
                pauseTimer = 0;
            }
        } else if (this.mode.isCurrentMode("90%")) {
            if (mc.player.getUseItemRemainingTicks() % 3 != 0 && mc.player.getUseItemRemainingTicks() <= 28) {
                eventSlowdown.setSlowdown(false);
                mc.player.setSprinting(true);
            }
        }
        // New modes
        else if (this.mode.isCurrentMode("GrimJump")) {
            GrimJump(eventSlowdown);
        } else if (this.mode.isCurrentMode("None")) {
            none(eventSlowdown);
        } else if (this.mode.isCurrentMode("Grim50%")) {
            Grim50(eventSlowdown);
        } else if (this.mode.isCurrentMode("Grim1/3")) {
            grim1_3(eventSlowdown);
        }
    }
    
    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.player == null) return;
        this.setSuffix(mode.getCurrentMode());
        if (mc.player.onGround()) {
            onGroundTick++;
        } else {
            onGroundTick = 0;
        }
    }
    
    @EventTarget
    public void onMoveInput(EventMoveInput event) {
        if (mc.player == null) return;
        if (mc.player.onGround() && mc.player.isUsingItem() && (event.getForward() != 0 || event.getStrafe() != 0) && mode.isCurrentMode("GrimJump")) {
            event.setJump(true);
        }
    }
    
    @Override
    public void onEnable() {
        onGroundTick = 0;
    }

    @Override
    public void onDisable() {
        onGroundTick = 0;
    }
    
    private void GrimJump(EventSlowdown eventSlowdown) {
        if (mc.player == null) return;
        if (onGroundTick == 1 && mc.player.getUseItemRemainingTicks() <= 30) {
            eventSlowdown.setSlowdown(false);
            if (!mc.player.isSprinting()) mc.player.setSprinting(true);
        }
    }

    private void Grim50(EventSlowdown eventSlowdown) {
        if (mc.player == null) return;
        if (mc.player.getUseItemRemainingTicks() % 2 == 0 && mc.player.getUseItemRemainingTicks() <= 30) {
            eventSlowdown.setSlowdown(false);
            if (!mc.player.isSprinting()) mc.player.setSprinting(true);
        }
    }

    private void none(EventSlowdown eventSlowdown) {
        if (mc.player == null) return;
        eventSlowdown.setSlowdown(false);
        if (!mc.player.isSprinting()) mc.player.setSprinting(true);
    }

    private void grim1_3(EventSlowdown eventSlowdown) {
        if (mc.player == null) return;
        if (mc.player.getUseItemRemainingTicks() % 3 == 0 && (!checkFood() || mc.player.getUseItemRemainingTicks() <= 30)) {
            eventSlowdown.setSlowdown(false);
            if (!mc.player.isSprinting()) mc.player.setSprinting(true);
        }
    }
    
    private boolean checkFood() {
        if (mc.player == null) return false;
        ItemStack mainHandItem = mc.player.getMainHandItem();
        ItemStack offhandItem = mc.player.getOffhandItem();
        return mainHandItem.is(Items.GOLDEN_APPLE)
                || offhandItem.is(Items.GOLDEN_APPLE)
                || mainHandItem.is(Items.ENCHANTED_GOLDEN_APPLE)
                || offhandItem.is(Items.ENCHANTED_GOLDEN_APPLE)
                || mainHandItem.is(Items.POTION)
                || offhandItem.is(Items.POTION);
    }

    private boolean checkItem(Item item) {
        if (mc.player == null) return false;
        ItemStack mainHandItem = mc.player.getMainHandItem();
        ItemStack offhandItem = mc.player.getOffhandItem();
        return mainHandItem.is(item) || offhandItem.is(item);
    }
}