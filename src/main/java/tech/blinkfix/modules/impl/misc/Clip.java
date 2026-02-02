//package com.heypixel.heypixelmod.modules.impl.misc;
//
//import api.events.tech.blinkfix.EventTarget;
//import modules.tech.blinkfix.Category;
//import modules.tech.blinkfix.Module;
//import modules.tech.blinkfix.ModuleInfo;
//import move.impl.modules.tech.blinkfix.Fly;
//import utils.tech.blinkfix.BlockUtils;
//import net.minecraft.client.Minecraft;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.world.phys.Vec3;
//import net.minecraftforge.event.TickEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//
//
//@ModuleInfo(
//        name = "Clip",
//        category = Category.MISC,
//        description = "Clip"
//)
//public class Clip extends Module {
//
//    private final int horizontal = 0;
//    private final int vertical = 6;
//    private final boolean requiresStandOn = true;
//
//    private java.util.Set<Direction> possibleClipDirections = new java.util.HashSet<>();
//
//
//    @SubscribeEvent
//    public void onTick(TickEvent.PlayerTickEvent event) {
//        if (event.phase != TickEvent.Phase.END) return;
//        if (mc.player == null || event.player != mc.player) return;
//
//        possibleClipDirections.clear();
//
//        if (Fly.getInstance().isEnabled()) {
//            return;
//        }
//
//        for (Direction direction : new Direction[]{Direction.UP, Direction.DOWN}) {
//            tryClip(direction, vertical, blockPos -> {
//                possibleClipDirections.add(direction);
//            });
//        }
//
//        Direction movementDirection = null;
//
//        if (mc.player.horizontalCollision) {
//            if (mc.options.keyUp.isDown()) {
//                movementDirection = mc.player.getDirection();
//            } else if (mc.options.keyDown.isDown()) {
//                movementDirection = mc.player.getDirection().getOpposite();
//            } else if (mc.options.keyLeft.isDown()) {
//                movementDirection = mc.player.getDirection().getClockWise().getOpposite();
//            } else if (mc.options.keyRight.isDown()) {
//                movementDirection = mc.player.getDirection().getClockWise();
//            } else {
//                return;
//            }
//        } else if (mc.options.keyShift.isDown()) {
//            movementDirection = Direction.DOWN;
//        } else if (mc.options.keyJump.isDown()) {
//            movementDirection = Direction.UP;
//        } else {
//            return;
//        }
//
//        int clipLength = (movementDirection == Direction.DOWN || movementDirection == Direction.UP) ? vertical : horizontal;
//
//        tryClip(movementDirection, clipLength, blockPos -> {
//            mc.player.setPos(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
//        });
//    }
//
//    @EventTarget
//    public void onRender2D(Render2DEvent event) {
//        if (possibleClipDirections.isEmpty()) {
//            return;
//        }
//
//        StringBuilder directionString = new StringBuilder();
//        directionString.append("[ ");
//        if (possibleClipDirections.contains(Direction.UP)) {
//            directionString.append('▲');
//        }
//        if (possibleClipDirections.contains(Direction.DOWN)) {
//            directionString.append('▼');
//        }
//        directionString.append(" ]");
//
//        // 在准星右侧绘制方向指示器
//        int width = mc.getWindow().getGuiScaledWidth();
//        int height = mc.getWindow().getGuiScaledHeight();
//
//        mc.font.drawShadow(event.getPoseStack(), directionString.toString(),
//                width / 2 + 10, height / 2 - mc.font.lineHeight / 2 + 1, 0xFFFFFF);
//    }
//
//    private void tryClip(Direction movementDirection, int length, java.util.function.Consumer<BlockPos> clip) {
//        if (length == 0) {
//            return;
//        }
//
//        boolean wallBetween = false;
//
//        // 计算新位置
//        // 寻找最近的可以穿入的空位置
//        BlockPos.MutableBlockPos position = mc.player.blockPosition().mutable();
//        for (int i = 0; i < length; i++) {
//            position.move(movementDirection);
//
//            if (isPossibleLocation(position, requiresStandOn && movementDirection != Direction.UP)) {
//                // 确保有墙阻挡才允许穿墙
//                if (wallBetween) {
//                    clip.accept(position.immutable());
//                    return;
//                }
//            } else {
//                wallBetween = true;
//            }
//        }
//    }
//
//    private boolean isPossibleLocation(BlockPos blockPos, boolean requiresStandOn) {
//        if (mc.level == null) return false;
//
//        if (requiresStandOn && !canStandOn(blockPos.below())) {
//            return false;
//        }
//
//        return mc.level.getBlockState(blockPos).isAir() && mc.level.getBlockState(blockPos.above()).isAir();
//    }
//
//    private boolean canStandOn(BlockPos blockPos) {
//        if (mc.level == null) return false;
//
//        return !mc.level.getBlockState(blockPos).isAir() &&
//                mc.level.getBlockState(blockPos).isSolid();
//    }
//}