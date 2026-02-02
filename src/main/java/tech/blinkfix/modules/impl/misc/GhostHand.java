package tech.blinkfix.modules.impl.misc;

import tech.blinkfix.events.impl.EventRender;
import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.utils.ProjectionUtils;
import tech.blinkfix.utils.Vector2f;
import tech.blinkfix.utils.renderer.Fonts;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventClick;
import tech.blinkfix.BlinkFix;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.utils.BlockUtils;
import tech.blinkfix.utils.ChunkUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@ModuleInfo(
        name = "GhostHand",
        description = "Allows interacting with chest through walls",
        category = Category.MISC
)
public class GhostHand extends Module {
    private final BooleanValue render = ValueBuilder.create(this, "Render")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
    private static final Set<Block> BLOCKS = new HashSet<>();
    static {
        BLOCKS.add(Blocks.CHEST);
        BLOCKS.add(Blocks.ENDER_CHEST);
        BLOCKS.add(Blocks.TRAPPED_CHEST);
        BLOCKS.add(Blocks.SHULKER_BOX);
    }
    private final List<ContainerStealer.ChestInfo> chests = new CopyOnWriteArrayList<>();
    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public void onEnable() {
        super.onEnable();
        Notification notification = new Notification(NotificationLevel.INFO, "This module may cause a ban.", 10000L);
        BlinkFix.getInstance().getNotificationManager().addNotification(notification);
    }

    @EventTarget
    public void onClick(EventClick event) {
        if (mc.options.keyUse.isDown()) {
            ghostInteractWithChest();
        }
    }

    public boolean ghostInteractWithChest() {
        if (mc.player == null || mc.level == null) {
            return false;
        }
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 lookVec = mc.player.getViewVector(1.0f);
        Vec3 reachEnd = eyePos.add(lookVec.scale(4.0));
        ChestBlockEntity targetChest = null;
        BlockHitResult fakeHit = null;
        double closestDist = Double.MAX_VALUE;
        ArrayList<BlockEntity> blockEntities = ChunkUtils.getLoadedBlockEntities().collect(Collectors.toCollection(ArrayList::new));
        for (BlockEntity be : blockEntities) {
            double dist;
            Optional<Vec3> hit;
            ChestBlockEntity chest;
            AABB box;
            if (!(be instanceof ChestBlockEntity) || (box = this.getChestBox(chest = (ChestBlockEntity)be)) == null || !(hit = box.clip(eyePos, reachEnd)).isPresent() || !((dist = hit.get().distanceTo(eyePos)) < closestDist)) continue;
            closestDist = dist;
            targetChest = chest;
            fakeHit = new BlockHitResult(hit.get(), Direction.UP, chest.getBlockPos(), false);
        }
        if (targetChest != null && fakeHit != null) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, fakeHit);
            mc.player.swing(InteractionHand.MAIN_HAND);
            return true;
        }
        return false;
    }
    private AABB getChestBox(ChestBlockEntity chestBE) {
        BlockPos pos2;
        BlockState state = chestBE.getBlockState();
        if (!state.hasProperty(ChestBlock.TYPE)) {
            return null;
        }
        ChestType chestType = state.getValue(ChestBlock.TYPE);
        if (chestType == ChestType.LEFT) {
            return null;
        }
        BlockPos pos = chestBE.getBlockPos();
        AABB box = BlockUtils.getBoundingBox(pos);
        if (chestType != ChestType.SINGLE && BlockUtils.canBeClicked(pos2 = pos.relative(ChestBlock.getConnectedDirection(state)))) {
            AABB box2 = BlockUtils.getBoundingBox(pos2);
            box = box.minmax(box2);
        }
        return box;
    }
    private void triggerBlockInteraction(BlockPos pos, Direction direction) {
        if (mc.gameMode != null) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), direction, pos, false)
            );
        }
        if (mc.player != null) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }
    @EventTarget
    public void onUpdate(EventRender event) {
        this.chests.clear();
        double range = 6.0;

        Vec3 cameraPos = ContainerStealer.mc.gameRenderer.getMainCamera().getPosition();
        BlockPos playerPos = ContainerStealer.mc.player.blockPosition();

        // 扫描玩家周围6格内的箱子
        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-6, -6, -6),
                playerPos.offset(6, 6, 6))) {

            BlockEntity be = ContainerStealer.mc.level.getBlockEntity(pos);

            if (!(be instanceof ChestBlockEntity)) {
                continue;
            }

            // 计算箱子上方中心位置
            Vec3 topCenter = new Vec3(
                    pos.getX() + 0.5,
                    pos.getY() + 1.2,
                    pos.getZ() + 0.5
            );
            if (topCenter.distanceTo(cameraPos) > range) {
                continue;
            }

            Vector2f screenPos = ProjectionUtils.project(
                    topCenter.x, topCenter.y, topCenter.z,
                    event.getRenderPartialTicks()
            );

            this.chests.add(new ContainerStealer.ChestInfo(pos, screenPos));
        }
    }
    @EventTarget
    public void onRender(EventRender2D event) {
        if (render.getCurrentValue()) {
            for (ContainerStealer.ChestInfo chest : this.chests) {
                Vector2f pos = chest.getScreenPos();
                Fonts.harmony.render(
                        event.getStack(),
                        "[Chest]",
                        pos.x, pos.y,
                        Color.YELLOW,
                        true,
                        0.4f
                );
            }
        }
    }
}