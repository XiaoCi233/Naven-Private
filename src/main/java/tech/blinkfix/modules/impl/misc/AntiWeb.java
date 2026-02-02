//package com.heypixel.heypixelmod.modules.impl.misc;
//
//import tech.blinkfix.BlinkFix;
//import api.events.tech.blinkfix.EventTarget;
//import types.api.events.tech.blinkfix.EventType;
//import impl.events.tech.blinkfix.EventRunTicks;
//import modules.tech.blinkfix.Category;
//import modules.tech.blinkfix.Module;
//import modules.tech.blinkfix.ModuleInfo;
//import notification.ui.tech.blinkfix.Notification;
//import notification.ui.tech.blinkfix.NotificationLevel;
//import utils.tech.blinkfix.PacketUtils;
//import rotation.utils.tech.blinkfix.RotationUtils;
//import values.tech.blinkfix.ValueBuilder;
//import impl.values.tech.blinkfix.BooleanValue;
//import impl.values.tech.blinkfix.FloatValue;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.level.GameType;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.WebBlock;
//import net.minecraft.world.phys.BlockHitResult;
//import net.minecraft.world.phys.Vec3;
//
//@ModuleInfo(
//        name = "AntiWeb",
//        description = "Automatically places water to clear cobwebs",
//        category = Category.MISC
//)
//public class AntiWeb extends Module {
//    private final BooleanValue autoCollect = ValueBuilder.create(this, "Auto Collect Water")
//            .setDefaultBooleanValue(true)
//            .build()
//            .getBooleanValue();
//
//    private int originalSlot = -1;
//    private int waterBucketSlot = -1;
//    private BlockPos webPosition = null;
//    private BlockPos waterPosition = null;
//    private int state = 0;
//    private int cooldown = 0;
//    private boolean rotation = false;
//    private int timeout = 0;
//
//    @EventTarget
//    public void onTick(EventRunTicks e) {
//        if (e.getType() != EventType.PRE || mc.player == null) return;
//        if (cooldown > 0) {
//            cooldown--;
//            return;
//        }
//        if (timeout > 0) {
//            timeout--;
//            if (timeout == 0 && rotation) {
//                rotation = false;
//                Notification notification = new Notification(NotificationLevel.WARNING, "Failed to place water!", 3000L);
//                BlinkFix.getInstance().getNotificationManager().addNotification(notification);
//                reset();
//            }
//        }
//        if (state == 0 && isInWeb()) {
//            webPosition = findWebPosition();
//            if (webPosition != null) {
//                waterBucketSlot = findWaterBucket();
//                if (waterBucketSlot != -1) {
//                    state = 1;
//                    originalSlot = mc.player.getInventory().selected;
//                    cooldown = 2;
//                }
//            }
//        }
//
//        switch (state) {
//            case 1:
//                if (preparePlaceWater()) {
//                    state = 2;
//                    cooldown = 2;
//                    rotation = true;
//                    timeout = 5;
//                }
//                break;
//
//            case 2:
//                if (rotation && placeWater()) {
//                    state = 3;
//                    cooldown = 10;
//                    rotation = false;
//                }
//                break;
//
//            case 3:
//                if (!isWebStillPresent()) {
//                    state = 4;
//                    cooldown = 5;
//                } else if (cooldown <= 0) {
//                    state = 0;
//                    reset();
//                }
//                break;
//
//            case 4:
//                if (autoCollect.getCurrentValue() && collectWater()) {
//                    state = 0;
//                    reset();
//                    Notification notification = new Notification(NotificationLevel.SUCCESS, "Web cleared successfully!", 3000L);
//                    BlinkFix.getInstance().getNotificationManager().addNotification(notification);
//                } else {
//                    state = 0;
//                    reset();
//                }
//                break;
//        }
//    }
//
//    private boolean isInWeb() {
//        BlockPos playerPos = mc.player.blockPosition();
//        return mc.level.getBlockState(playerPos).getBlock() instanceof WebBlock ||
//                mc.level.getBlockState(playerPos.below()).getBlock() instanceof WebBlock;
//    }
//
//    private BlockPos findWebPosition() {
//        BlockPos playerPos = mc.player.blockPosition();
//        if (mc.level.getBlockState(playerPos).getBlock() instanceof WebBlock) {
//            return playerPos;
//        }
//        if (mc.level.getBlockState(playerPos.below()).getBlock() instanceof WebBlock) {
//            return playerPos.below();
//        }
//        for (int x = -1; x <= 1; x++) {
//            for (int y = -1; y <= 1; y++) {
//                for (int z = -1; z <= 1; z++) {
//                    BlockPos checkPos = playerPos.offset(x, y, z);
//                    if (mc.level.getBlockState(checkPos).getBlock() instanceof WebBlock) {
//                        return checkPos;
//                    }
//                }
//            }
//        }
//
//        return null;
//    }
//
//    private int findWaterBucket() {
//        for (int i = 0; i < 9; i++) {
//            ItemStack item = mc.player.getInventory().getItem(i);
//            if (!item.isEmpty() && item.getItem() == Items.WATER_BUCKET) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    private boolean preparePlaceWater() {
//        mc.player.getInventory().selected = waterBucketSlot;
//        if (webPosition != null) {
//            BlockPos belowWeb = webPosition.below();
//            if (mc.level.getBlockState(belowWeb).isSolid()) {
//                waterPosition = belowWeb;
//                return true;
//            }
//            for (Direction direction : Direction.values()) {
//                if (direction == Direction.UP) continue;
//
//                BlockPos adjacentPos = webPosition.relative(direction);
//                if (mc.level.getBlockState(adjacentPos).isSolid()) {
//                    waterPosition = adjacentPos;
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
//
//    private boolean placeWater() {
//        if (waterPosition == null) return false;
//        BlockHitResult hitResult = new BlockHitResult(
//                Vec3.atCenterOf(waterPosition),
//                Direction.UP,
//                waterPosition,
//                false
//        );
//
//        useItem(mc.player, InteractionHand.MAIN_HAND, hitResult);
//        return true;
//    }
//
//    private boolean isWebStillPresent() {
//        return webPosition != null &&
//                mc.level.getBlockState(webPosition).getBlock() instanceof WebBlock;
//    }
//
//    private boolean collectWater() {
//        if (waterPosition == null) return false;
//        if (mc.level.getBlockState(waterPosition).getBlock() != Blocks.WATER) {
//            waterPosition = webPosition;
//        }
//        BlockHitResult hitResult = new BlockHitResult(
//                Vec3.atCenterOf(waterPosition),
//                Direction.UP,
//                waterPosition,
//                false
//        );
//        useItem(mc.player, InteractionHand.MAIN_HAND, hitResult);
//        return true;
//    }
//
//    private void reset() {
//        if (originalSlot != -1) {
//            mc.player.getInventory().selected = originalSlot;
//        }
//        originalSlot = -1;
//        waterBucketSlot = -1;
//        webPosition = null;
//        waterPosition = null;
//        rotation = false;
//    }
//    public InteractionResult useItem(Player pPlayer, InteractionHand pHand, BlockHitResult hitResult) {
//        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
//            return InteractionResult.PASS;
//        }
//        PacketUtils.sendSequencedPacket(id -> new ServerboundUseItemOnPacket(pHand, hitResult, id));
//        pPlayer.swing(pHand);
//        return InteractionResult.SUCCESS;
//    }
//
//    @Override
//    public void onEnable() {
//        super.onEnable();
//        reset();
//        state = 0;
//        cooldown = 0;
//        timeout = 0;
//    }
//
//    @Override
//    public void onDisable() {
//        super.onDisable();
//        reset();
//        state = 0;
//        cooldown = 0;
//        timeout = 0;
//    }
//}