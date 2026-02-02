//package com.heypixel.heypixelmod.modules.impl.misc;
//
//import tech.blinkfix.BlinkFix;
//import modules.tech.blinkfix.Category;
//import modules.tech.blinkfix.Module;
//import modules.tech.blinkfix.ModuleInfo;
//import utils.tech.blinkfix.MathUtils;
//import utils.tech.blinkfix.Vector2f;
//import rotation.utils.tech.blinkfix.RotationUtils;
//import values.tech.blinkfix.ValueBuilder;
//import impl.values.tech.blinkfix.BooleanValue;
//import net.minecraft.world.level.block.*;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.world.phys.BlockHitResult;
//import net.minecraft.world.phys.Vec3;
//import net.minecraft.world.phys.HitResult;
//
//import java.util.ArrayList;
//
//import static combat.impl.modules.tech.blinkfix.Aura.target;
//
//
//@ModuleInfo(
//        name = "ContainerAura",
//        description = "Automatically places water to clear cobwebs",
//        category = Category.MISC)
//
//@SuppressWarnings("unused")
//public class ContainerAura extends Module {
//    private final BooleanValue thoughtWall = ValueBuilder.create(this, "ThoughtWall").setDefaultBooleanValue(false).build().getBooleanValue();
//    private final BooleanValue chestOnly = ValueBuilder.create(this, "ChestOnly").setDefaultBooleanValue(false).build().getBooleanValue();
//
//    private Rotation needRot;
//    private Direction needFacing;
//    private MathUtils MathUtils = new MathUtils();
//
//    public static ArrayList<BlockPos> openedContainer = new ArrayList<>();
//
//    @Override
//    public void onEnable() {
//        openedContainer.clear();
//    }
//
//    private Handler<WorldEvent> worldEventHandler = event -> {
//        openedContainer.clear();
//    };
//
//    private Handler<UpdateEvent> scannerHandler = event -> {
//        try {
//            if (BlinkFix.moduleManager.getModule(KillAura.class).getState() && KillAura.target != null) return;
//            if (BlinkFix.moduleManager.getModule(eScaffold.class).getState()) return;
//            BlockPos nearestContainer = null;
//            double nearestDistance = Double.MAX_VALUE;
//            if (mc.screen instanceof AbstractContainerScreen) return;
//
//            for (int x = -5; x < 6; x++) {
//                for (int y = -5; y < 6; y++) {
//                    for (int z = -5; z < 6; z++) {
//                        BlockPos fixedBP = new BlockPos((int) mc.player.getX() + x, (int) mc.player.getY() + y, (int) mc.player.getZ() + z);
//                        if (checkContainerOpenable(fixedBP)) {
//                            Vec3 startVec = new Vec3(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(), mc.player.getZ());
//                            Vec3 endVec = new Vec3(fixedBP.getX() + 0.5, fixedBP.getY() + 0.5, fixedBP.getZ() + 0.5);
//
//                            BlockHitResult hitResult = mc.level.clip(new net.minecraft.world.level.ClipContext(
//                                    startVec, endVec,
//                                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
//                                    net.minecraft.world.level.ClipContext.Fluid.NONE,
//                                    mc.player
//                            ));
//
//                            if (mc.player.distanceToSqr(fixedBP.getX(), fixedBP.getY(), fixedBP.getZ()) < 20.25 && // 4.5^2 = 20.25
//                                    !openedContainer.contains(fixedBP) &&
//                                    mc.player.distanceToSqr(fixedBP.getX(), fixedBP.getY(), fixedBP.getZ()) <= nearestDistance &&
//                                    isContainer(mc.level.getBlockState(fixedBP).getBlock()) &&
//                                    hitResult != null &&
//                                    hitResult.getType() == HitResult.Type.BLOCK &&
//                                    (hitResult.getBlockPos().equals(fixedBP) || thoughtWall.getValue())) {
//
//                                nearestDistance = mc.player.distanceToSqr(fixedBP.getX(), fixedBP.getY(), fixedBP.getZ());
//                                nearestContainer = fixedBP;
//                                Vector2f r = RotationUtils.getRotations(target);
//                                needRot = new Rotation(r[0], r[1]);
//                                needFacing = hitResult.getDirection();
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (nearestContainer == null) return;
//            if (!MathUtils.check(500L)) return;
//            MathUtils.reset();
//            MathUtils.INSTANCE.getRotationManager().setRotation(needRot, 180f, true);
//
//            // 使用 InteractionHand.MAIN_HAND 作为默认手
//            mc.gameMode.useItemOn(
//                    mc.player,
//                    mc.level,
//                    mc.player.getMainHandItem(),
//                    net.minecraft.world.InteractionHand.MAIN_HAND,
//                    new BlockHitResult(
//                            getVec3(nearestContainer, needFacing),
//                            needFacing,
//                            nearestContainer,
//                            false
//                    )
//            );
//
//            openedContainer.add(nearestContainer);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    };
//
//    private boolean isContainer(Block block) {
//        return block instanceof ChestBlock ||
//                ((block instanceof FurnaceBlock || block instanceof BrewingStandBlock) && !chestOnly.getCurrentValue());
//    }
//
//    private boolean checkContainerOpenable(BlockPos blockPos) {
//        BlockState blockState = mc.level.getBlockState(blockPos);
//        if (!(blockState.getBlock() instanceof ChestBlock)) return true;
//
//        BlockState upBlockState = mc.level.getBlockState(blockPos.above());
//        if (upBlockState.isSolid() && !(upBlockState.getBlock() instanceof GlassBlock)) {
//            return false;
//        }
//        return true;
//    }
//
//    // 添加 getVec3 方法（如果不存在）
//    private static Vec3 getVec3(BlockPos pos, Direction facing) {
//        return new Vec3(
//                pos.getX() + 0.5 + facing.getStepX() * 0.5,
//                pos.getY() + 0.5 + facing.getStepY() * 0.5,
//                pos.getZ() + 0.5 + facing.getStepZ() * 0.5
//        );
//    }
//}