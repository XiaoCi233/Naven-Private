package com.heypixel.heypixelmod.modules.impl.misc;

import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.EventMotion;
import com.heypixel.heypixelmod.events.impl.EventRender;
import com.heypixel.heypixelmod.events.impl.EventRender2D;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.modules.impl.move.Scaffold;
import com.heypixel.heypixelmod.utils.BlockUtils;
import com.heypixel.heypixelmod.utils.ChunkUtils;
import com.heypixel.heypixelmod.utils.InventoryUtils;
import com.heypixel.heypixelmod.utils.ProjectionUtils;
import com.heypixel.heypixelmod.utils.TickTimeHelper;
import com.heypixel.heypixelmod.utils.Vector2f;
import com.heypixel.heypixelmod.utils.renderer.Fonts;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import com.heypixel.heypixelmod.values.impl.FloatValue;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@ModuleInfo(name = "ZhagnTieNanChestStealer", description = "Automatically steals items from chests", category = Category.MISC)
public class ZhagnTieNanChestStealer extends Module {
    private static final TickTimeHelper timer = new TickTimeHelper();
    private static final TickTimeHelper timer2 = new TickTimeHelper();
    private final BooleanValue swap = ValueBuilder.create(this, "Instant").setDefaultBooleanValue(false).build().getBooleanValue();
    private final FloatValue delay = ValueBuilder.create(this, "Delay (Ticks)").setDefaultFloatValue(3.0f).setFloatStep(1.0f).setMinFloatValue(1.0f).setMaxFloatValue(10.0f).build().getFloatValue();
    private final FloatValue delay1 = ValueBuilder.create(this, "Multi Stack Delay (Ticks)").setDefaultFloatValue(3.0f).setFloatStep(1.0f).setMinFloatValue(1.0f).setMaxFloatValue(10.0f).build().getFloatValue();
    private final BooleanValue pickEnderChest = ValueBuilder.create(this, "Ender Chest").setDefaultBooleanValue(false).build().getBooleanValue();
    private Screen lastTickScreen;
    private final List<ChestInfo> chests = new CopyOnWriteArrayList<ChestInfo>();

    public static boolean isWorking() {
        return !timer.delay(3);
    }

    @EventTarget(value = 1)
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof ContainerScreen) {
                ContainerScreen container = (ContainerScreen) currentScreen;
                ChestMenu menu = (ChestMenu) container.getMenu();
                if (currentScreen != this.lastTickScreen) {
                    timer.reset();
                } else {
                    String chestTitle = container.getTitle().getString();
                    String chest = Component.translatable("container.chest").getString();
                    String largeChest = Component.translatable("container.chestDouble").getString();
                    String enderChest = Component.translatable("container.enderchest").getString();
                    if (chestTitle.equals(chest) || chestTitle.equals(largeChest) || chestTitle.equals("Chest") || this.pickEnderChest.getCurrentValue() && chestTitle.equals(enderChest)) {
                        if (this.isChestEmpty(menu) && timer.delay(this.delay.getCurrentValue())) {
                            Minecraft.getInstance().player.closeContainer();
                        } else {
                            List<Integer> slots = IntStream.range(0, menu.getRowCount() * 9).boxed().collect(Collectors.toList());
                            Collections.shuffle(slots);
                            for (Integer pSlotId : slots) {
                                ItemStack stack = menu.getSlot(pSlotId).getItem();
                                if (!ZhagnTieNanChestStealer.isItemUseful(stack) || !this.isBestItemInChest(menu, stack) || !timer.delay(this.delay.getCurrentValue())) continue;
                                if (this.swap.getCurrentValue()) {
                                    int slot = ZhagnTieNanChestStealer.getFirstEmptySlot();
                                    if (slot != -1 && slot + 18 < 54) {
                                        if (stack.getCount() <= 1) {
                                            if (slot < 9) {
                                                Minecraft.getInstance().gameMode.handleInventoryMouseClick(menu.containerId, pSlotId, slot, ClickType.SWAP, Minecraft.getInstance().player);
                                            } else {
                                                Minecraft.getInstance().gameMode.handleInventoryMouseClick(menu.containerId, slot + 18, 8, ClickType.SWAP, Minecraft.getInstance().player);
                                                Minecraft.getInstance().gameMode.handleInventoryMouseClick(menu.containerId, pSlotId, 8, ClickType.SWAP, Minecraft.getInstance().player);
                                            }
                                        } else if (timer2.delay(this.delay1.getCurrentValue())) {
                                            Minecraft.getInstance().gameMode.handleInventoryMouseClick(menu.containerId, pSlotId, 0, ClickType.QUICK_MOVE, Minecraft.getInstance().player);
                                            timer2.reset();
                                        }
                                    } else {
                                        Minecraft.getInstance().player.closeContainer();
                                    }
                                } else {
                                    Minecraft.getInstance().gameMode.handleInventoryMouseClick(menu.containerId, pSlotId, 0, ClickType.QUICK_MOVE, Minecraft.getInstance().player);
                                }
                                timer.reset();
                                break;
                            }
                        }
                    }
                }
            }
            this.lastTickScreen = currentScreen;
        }
    }

    @EventTarget
    public void onUpdate(EventRender event) {
        this.chests.clear();
        double range = 6.0;
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BlockPos playerPos = Minecraft.getInstance().player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-6, -6, -6), playerPos.offset(6, 6, 6))) {
            Vec3 topCenter;
            BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
            if (!(be instanceof ChestBlockEntity) || (topCenter = new Vec3((double)pos.getX() + 0.5, (double)pos.getY() + 1.2, (double)pos.getZ() + 0.5)).distanceTo(cameraPos) > range) continue;
            Vector2f screenPos = ProjectionUtils.project(topCenter.x, topCenter.y, topCenter.z, event.getRenderPartialTicks());
            this.chests.add(new ChestInfo(pos, screenPos));
        }
    }

    @EventTarget
    public void onRender(EventRender2D event) {
        for (ChestInfo chest : this.chests) {
            Vector2f pos = chest.getScreenPos();
            Fonts.harmony.render(event.getStack(), "[Chest]", pos.x, pos.y, Color.YELLOW, true, 0.4f);
        }
    }

    @EventTarget
    public void onTick(EventMotion eventMotion) {
        if (Minecraft.getInstance().options.keyUse.isDown()) {
            if (eventMotion.getType() == EventType.POST) {
                return;
            }
            this.ghostInteractWithChest();
        }
    }

    public boolean ghostInteractWithChest() {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) {
            return false;
        }
        Vec3 eyePos = Minecraft.getInstance().player.getEyePosition(1.0f);
        Vec3 lookVec = Minecraft.getInstance().player.getLookAngle();
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
            Minecraft.getInstance().gameMode.useItemOn(Minecraft.getInstance().player, InteractionHand.MAIN_HAND, fakeHit);
            Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
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

    private boolean isBestItemInChest(ChestMenu menu, ItemStack stack) {
        if (InventoryUtils.isGodItem(stack) || InventoryUtils.isSharpnessAxe(stack)) {
            return true;
        }
        for (int i = 0; i < menu.getRowCount() * 9; ++i) {
            ItemStack checkStack = menu.getSlot(i).getItem();
            if (stack.getItem() instanceof ArmorItem && checkStack.getItem() instanceof ArmorItem) {
                ArmorItem item = (ArmorItem) stack.getItem();
                ArmorItem checkItem = (ArmorItem) checkStack.getItem();
                if (item.getEquipmentSlot() != checkItem.getEquipmentSlot() || !(InventoryUtils.getProtection(checkStack) > InventoryUtils.getProtection(stack))) continue;
                return false;
            }
            if (!(stack.getItem() instanceof SwordItem && checkStack.getItem() instanceof SwordItem ? InventoryUtils.getSwordDamage(checkStack) > InventoryUtils.getSwordDamage(stack) : (stack.getItem() instanceof PickaxeItem && checkStack.getItem() instanceof PickaxeItem ? InventoryUtils.getToolScore(checkStack) > InventoryUtils.getToolScore(stack) : (stack.getItem() instanceof AxeItem && checkStack.getItem() instanceof AxeItem ? InventoryUtils.getToolScore(checkStack) > InventoryUtils.getToolScore(stack) : stack.getItem() instanceof ShovelItem && checkStack.getItem() instanceof ShovelItem && InventoryUtils.getToolScore(checkStack) > InventoryUtils.getToolScore(stack))))) continue;
            return false;
        }
        return true;
    }

    private boolean isChestEmpty(ChestMenu menu) {
        for (int i = 0; i < menu.getRowCount() * 9; ++i) {
            ItemStack item = menu.getSlot(i).getItem();
            if (item.isEmpty() || !ZhagnTieNanChestStealer.isItemUseful(item) || !this.isBestItemInChest(menu, item)) continue;
            return false;
        }
        return true;
    }

    public static int getFirstEmptySlot() {
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        for (int i = 0; i < inventory.items.size(); ++i) {
            if (i == 8 || !inventory.getItem(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    public static boolean isItemUseful(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (InventoryUtils.isGodItem(stack) || InventoryUtils.isSharpnessAxe(stack)) {
            return true;
        }
        if (stack.getItem() instanceof ArmorItem) {
            float bestArmor;
            ArmorItem item = (ArmorItem) stack.getItem();
            float protection = InventoryUtils.getProtection(stack);
            return !(protection <= (bestArmor = InventoryUtils.getBestArmorScore(item.getEquipmentSlot())));
        }
        if (stack.getItem() instanceof SwordItem) {
            float bestDamage;
            float damage = InventoryUtils.getSwordDamage(stack);
            return !(damage <= (bestDamage = InventoryUtils.getBestSwordDamage()));
        }
        if (stack.getItem() instanceof PickaxeItem) {
            float bestScore;
            float score = InventoryUtils.getToolScore(stack);
            return !(score <= (bestScore = InventoryUtils.getBestPickaxeScore()));
        }
        if (stack.getItem() instanceof AxeItem) {
            float bestScore;
            float score = InventoryUtils.getToolScore(stack);
            return !(score <= (bestScore = InventoryUtils.getBestAxeScore()));
        }
        if (stack.getItem() instanceof ShovelItem) {
            float bestScore;
            float score = InventoryUtils.getToolScore(stack);
            return !(score <= (bestScore = InventoryUtils.getBestShovelScore()));
        }
        if (stack.getItem() instanceof CrossbowItem) {
            float bestScore;
            float score = InventoryUtils.getCrossbowScore(stack);
            return !(score <= (bestScore = InventoryUtils.getBestCrossbowScore()));
        }
        if (stack.getItem() instanceof BowItem && InventoryUtils.isPunchBow(stack)) {
            float bestScore;
            float score = InventoryUtils.getPunchBowScore(stack);
            return !(score <= (bestScore = InventoryUtils.getBestPunchBowScore()));
        }
        if (stack.getItem() instanceof BowItem && InventoryUtils.isPowerBow(stack)) {
            float bestScore;
            float score = InventoryUtils.getPowerBowScore(stack);
            return !(score <= (bestScore = InventoryUtils.getBestPowerBowScore()));
        }
        if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
            return !InventoryUtils.hasItem(stack.getItem());
        }
        if (stack.getItem() == Items.WATER_BUCKET && InventoryUtils.getItemCount(Items.WATER_BUCKET) >= InventoryCleaner.getWaterBucketCount()) {
            return false;
        }
        if (stack.getItem() == Items.LAVA_BUCKET && InventoryUtils.getItemCount(Items.LAVA_BUCKET) >= InventoryCleaner.getLavaBucketCount()) {
            return false;
        }
        if (stack.getItem() instanceof BlockItem && Scaffold.isValidStack(stack) && InventoryUtils.getBlockCountInInventory() + stack.getCount() >= InventoryCleaner.getMaxBlockSize()) {
            return false;
        }
        if (stack.getItem() == Items.ARROW && InventoryUtils.getItemCount(Items.ARROW) + stack.getCount() >= InventoryCleaner.getMaxArrowSize()) {
            return false;
        }
        if (stack.getItem() instanceof FishingRodItem && InventoryUtils.getItemCount(Items.FISHING_ROD) >= 1) {
            return false;
        }
        if (!(stack.getItem() != Items.SNOWBALL && stack.getItem() != Items.EGG || InventoryUtils.getItemCount(Items.SNOWBALL) + InventoryUtils.getItemCount(Items.EGG) + stack.getCount() < InventoryCleaner.getMaxProjectileSize() && InventoryCleaner.shouldKeepProjectile())) {
            return false;
        }
        if (stack.getItem() instanceof ItemNameBlockItem) {
            return false;
        }
        return InventoryUtils.isCommonItemUseful(stack);
    }

    private static class ChestInfo {
        BlockPos blockPos;
        Vector2f screenPos;

        public BlockPos getBlockPos() {
            return this.blockPos;
        }

        public Vector2f getScreenPos() {
            return this.screenPos;
        }

        public void setBlockPos(BlockPos blockPos) {
            this.blockPos = blockPos;
        }

        public void setScreenPos(Vector2f screenPos) {
            this.screenPos = screenPos;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ChestInfo)) {
                return false;
            }
            ChestInfo other = (ChestInfo) o;
            if (!other.canEqual(this)) {
                return false;
            }
            BlockPos this$blockPos = this.getBlockPos();
            BlockPos other$blockPos = other.getBlockPos();
            if (this$blockPos == null ? other$blockPos != null : !this$blockPos.equals(other$blockPos)) {
                return false;
            }
            Vector2f this$screenPos = this.getScreenPos();
            Vector2f other$screenPos = other.getScreenPos();
            return !(this$screenPos == null ? other$screenPos != null : !this$screenPos.equals(other$screenPos));
        }

        protected boolean canEqual(Object other) {
            return other instanceof ChestInfo;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            BlockPos $blockPos = this.getBlockPos();
            result = result * 59 + ($blockPos == null ? 43 : $blockPos.hashCode());
            Vector2f $screenPos = this.getScreenPos();
            result = result * 59 + ($screenPos == null ? 43 : $screenPos.hashCode());
            return result;
        }

        public String toString() {
            return "ZhagnTieNanChestStealer.ChestInfo(blockPos=" + this.getBlockPos() + ", screenPos=" + this.getScreenPos() + ")";
        }

        public ChestInfo(BlockPos blockPos, Vector2f screenPos) {
            this.blockPos = blockPos;
            this.screenPos = screenPos;
        }
    }
}