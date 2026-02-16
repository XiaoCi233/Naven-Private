package com.surface.mod.world;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.*;
import com.surface.mod.Mod;
import com.surface.render.font.FontManager;
import com.surface.util.player.PlayerUtils;
import com.surface.util.player.RotationUtils;
import com.surface.util.render.RenderUtils;
import com.surface.util.render.shader.ShaderElement;
import com.surface.util.struct.OffsetFacing;
import com.surface.util.struct.Rotation;
import com.surface.value.impl.BooleanValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.util.vector.Vector2f;

import java.security.SecureRandom;
import java.util.*;

public class ScaffoldModule extends Mod {

    public static final List<Block> invalidBlocks = Arrays.asList(Blocks.enchanting_table, Blocks.furnace,
            Blocks.carpet, Blocks.crafting_table, Blocks.trapped_chest, Blocks.chest, Blocks.dispenser, Blocks.air,
            Blocks.water, Blocks.lava, Blocks.flowing_water, Blocks.flowing_lava, Blocks.sand, Blocks.snow_layer,
            Blocks.torch, Blocks.anvil, Blocks.jukebox, Blocks.stone_button, Blocks.wooden_button, Blocks.lever,
            Blocks.noteblock, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate,
            Blocks.wooden_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_slab, Blocks.wooden_slab,
            Blocks.stone_slab2, Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.yellow_flower, Blocks.red_flower,
            Blocks.anvil, Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.cactus, Blocks.ladder,
            Blocks.web);

    public static double keepYCoord;

    public final BooleanValue swing = new BooleanValue("Swing", true);

    private static final BooleanValue keepYValue = new BooleanValue("Keep Y", false);

    public final BooleanValue eagle = new BooleanValue("Eagle", true);
    public final BooleanValue safeValue = new BooleanValue("Safe walk", true);
    public final BooleanValue lockview = new BooleanValue("LockView", true);
    public final BooleanValue telly = new BooleanValue("Telly", true);

    public ScaffoldModule() {
        super("Scaffold", Category.WORLD);
     registerValues(swing,keepYValue,eagle,safeValue,lockview,telly);
    }


    private int slot;
    private BlockData data;
    private Vec3 vector;
    protected Random rand = new Random();
    private boolean canTellyPlace;



    @Override
    public void onEnable() {
        if (mc.thePlayer == null) return;
        canTellyPlace = false;
        this.data = null;
        this.slot = -1;
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer == null) return;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
    }


    @EventTarget
    public void onUpdate(final EventPreUpdate event) {
        if (eagle.getValue()) {
            if (getBlockUnderPlayer(mc.thePlayer) instanceof BlockAir) {
                if (mc.thePlayer.onGround) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                }
            } else if (mc.thePlayer.onGround) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }
        }

    }

    @EventTarget
    public void onStrafe(EventStrafe event) {
        //event.setYaw(RotationUtils.wrapAngleToDirection(mc.thePlayer.rotationYaw,4) * 90);
        if (keepYValue.getValue() && mc.thePlayer.onGround && isMoving() && !mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.thePlayer.jump();
        }
    }

    @EventTarget
    private void onTick(EventTick event) {
        if (mc.thePlayer == null) return;
        if (this.slot < 0) return;
        if (!telly.getValue()) {
            canTellyPlace = true;
        }
    }

    @EventTarget
    private void onMove(EventMove event) {
        if (mc.thePlayer.onGround && safeValue.getValue()) mc.thePlayer.safeWalk = true;
    }

    @EventTarget
    private void onR2D(Event2D event2D){
        int slot = getBlockSlot();
        ItemStack heldItem = slot == -1 ? null : mc.thePlayer.inventory.mainInventory[slot];
        int count = slot == -1 ? 0 : getBlockCount();
        String countStr = String.valueOf(count);
        float blockWH = heldItem != null ? 15 : -2;
        int spacing = 3;
        float x, y;
        ScaledResolution sr = new ScaledResolution(mc);
        String text = "§l" + countStr + "§r block" + (count != 1 ? "s" : "");
        FontManager.WQY.setFontSize(18);
        float textWidth = FontManager.WQY.getStringWidth(text);

        float totalWidth = ((textWidth + blockWH + spacing) + 6);
        x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
        y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 20);
        float height = 20;
        RenderUtils.scissorStart(x - 1.5, y - 1.5, totalWidth + 3, height + 3);
        ShaderElement.addBlurTask(() -> RenderUtils.drawRound(x, y, totalWidth, height, 5,true,RenderUtils.tripleColor(20, .45f),true,true,true,true));
        RenderUtils.drawRound(x, y, totalWidth, height, 5,true,RenderUtils.tripleColor(20, .45f),true,true,true,true);

        FontManager.WQY.drawString(text, x + 10 + blockWH + spacing, y + FontManager.WQY.getMiddleOfBox(height) - .5f, -1);

        if (heldItem != null) {
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, (int) x + 7, (int) (y + 10 - (blockWH / 2)));
            RenderHelper.disableStandardItemLighting();
        }
        RenderUtils.scissorEnd();
    }

    @EventTarget
    private void onPlace(EventPreUpdate event) {
        this.slot = getBlockSlot();
        if (this.slot < 0) return;
        //event.setCancelled(true);
        if (mc.thePlayer == null) return;
            place();
            mc.sendClickBlockToController(mc.currentScreen == null && mc.gameSettings.keyBindAttack.isKeyDown() && mc.inGameHasFocus);
    }

    public static double getYLevel() {
        if (!keepYValue.getValue()) {
            return Minecraft.getMinecraft().thePlayer.posY - 1.0;
        }

        return !isMoving() ? Minecraft.getMinecraft().thePlayer.posY - 1.0 : keepYCoord;
    }


    @EventTarget
    private void onUpdate(EventUpdate event) {

        if (getBlockCount() < 1) {
            return;
        }
        if (this.getBlockCount() <= 0) {
            int spoofSlot = this.getBestSpoofSlot();
            this.getBlock(spoofSlot);
        }
        this.data = this.getBlockData(new double[]{0.4, 0.4});
        this.slot = getBlockSlot();
        if (this.slot < 0) return;
        mc.thePlayer.inventoryContainer.getSlot(slot + 36).getStack();
         if(data == null)return;
        if (mc.thePlayer.onGround) {
            keepYCoord = Math.floor(mc.thePlayer.posY - 1.0);
        }
        if(telly.getValue()){

            if (mc.thePlayer.fallDistance < 0.001 && !mc.thePlayer.onGround && isMoving()) {
                mc.thePlayer.setSprinting(false);
            }
            if(mc.thePlayer.offGroundTicks >= 3.5){
                canTellyPlace = true;
            }else{
                canTellyPlace = false;
            }
        }
        if (!canTellyPlace) return;
        if (getBlockCount() < 1) {
            return;
        }
        final boolean line = PlayerUtils.movementInput() && (Math.abs(mc.thePlayer.motionX) < .03 || Math.abs(mc.thePlayer.motionZ) < .03);
        vector = getVec3(data.getBlockPos(), data.getEnumFacing());
        float yaw = line ? mc.thePlayer.rotationYaw - 180 : RotationUtils.calculate(vector).getX();
        float pitch = (!PlayerUtils.movementInput() && mc.gameSettings.keyBindJump.pressed) ? 90 : RotationUtils.calculate(vector).getY();
        Wrapper.Instance.getRotationManager().setRotation(new Rotation(RotationUtils.wrapAngleToDirection(yaw, 4 * 90), pitch), 180f, true);
    }

    @EventTarget
    private void onMotion(EventPreUpdate event) {
    }

    @EventTarget
    private void onMotion2(EventPreUpdate event) {
        this.slot = getBlockSlot();
        if (this.slot < 0) return;
    }

    private void place() {
        if (!canTellyPlace) return;
        this.slot = getBlockSlot();
        if (this.slot < 0) return;

        int last = mc.thePlayer.inventory.currentItem;
        mc.thePlayer.inventory.currentItem = this.slot;
        if (block(mc.thePlayer.posX, getYLevel(), mc.thePlayer.posZ) instanceof BlockAir) {
            if (getBlockCount() < 1) {
                return;
            }
            if (data != null) {/*
                mc.objectMouseOver.typeOfHit = MovingObjectPosition.MovingObjectType.BLOCK;
                mc.objectMouseOver.sideHit = data.getEnumFacing();
                mc.objectMouseOver.hitVec = getVec3(data.getBlockPos(),data.getEnumFacing());*/
                boolean normalPlace = true;
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), this.data.getBlockPos(), normalPlace ? data.getEnumFacing() : mc.objectMouseOver.sideHit, normalPlace ? vector : mc.objectMouseOver.hitVec)) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionX *= 1;
                        mc.thePlayer.motionZ *= 1;
                    }
                    if (swing.getValue()) {
                        mc.thePlayer.swingItem();
                    } else {
                        mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                    }
                }

                mc.thePlayer.inventory.currentItem = last;
            }
        }

    }

    public Block block(final double x, final double y, final double z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static double getRandomInRange(double min, double max) {
        SecureRandom random = new SecureRandom();
        return min == max ? min : random.nextDouble() * (max - min) + min;
    }

    public static float getMoveYaw(float yaw) {
        Vector2f from = new Vector2f((float) Minecraft.getMinecraft().thePlayer.lastTickPosX, (float) Minecraft.getMinecraft().thePlayer.lastTickPosZ),
                to = new Vector2f((float) Minecraft.getMinecraft().thePlayer.posX, (float) Minecraft.getMinecraft().thePlayer.posZ),
                diff = new Vector2f(to.x - from.x, to.y - from.y);

        double x = diff.x, z = diff.y;
        if (x != 0 && z != 0) {
            yaw = (float) Math.toDegrees((Math.atan2(-x, z) + MathHelper.PI2) % MathHelper.PI2);
        }
        return yaw;
    }

    private Vec3 getVec3(BlockPos pos, EnumFacing face) {
        double x = (double) pos.getX() + 0.5;
        double y = (double) pos.getY() + 0.5;
        double z = (double) pos.getZ() + 0.5;
        if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
            x += getRandomInRange(0.3, -0.3);
            z += getRandomInRange(0.3, -0.3);
        } else {
            y += getRandomInRange(0.3, -0.3);
        }
        if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
            z += getRandomInRange(0.3, -0.3);
        }
        if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
            x += getRandomInRange(0.3, -0.3);
        }
        return new Vec3(x, y, z);
    }

    public static Block getBlock(BlockPos pos) {
        return Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
    }

    public static Block getBlockUnderPlayer(final EntityPlayer player) {
        return getBlock(new BlockPos(player.posX, player.posY - 1.0, player.posZ));
    }

    public static boolean isMoving() {
        return Minecraft.getMinecraft().thePlayer != null && (Minecraft.getMinecraft().thePlayer.movementInput.moveForward != 0F || Minecraft.getMinecraft().thePlayer.movementInput.moveStrafe != 0F);
    }


    public int getBlockSlot() {
        for (int i = 0; i < 9; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i + 36).getHasStack()
                    || !(mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack()
                    .getItem() instanceof ItemBlock))
                continue;
            if (mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack().stackSize == 1) continue;
            return i;
        }
        return -1;
    }

    private BlockData getBlockData(double[] expanded) {
        final Vec3 targetBlock = getPlacePossibility(expanded[0], expanded[1]);
        if (targetBlock == null) return null;

        final OffsetFacing offsetFacing = getEnumFacingOffset(targetBlock);
        if (offsetFacing == null) return null;

        final BlockPos position = new BlockPos(targetBlock.xCoord, targetBlock.yCoord, targetBlock.zCoord);
        final BlockPos blockFace = position.add(offsetFacing.getOffset().xCoord, offsetFacing.getOffset().yCoord, offsetFacing.getOffset().zCoord);
        if (blockFace == null) return null;

        return new BlockData(blockFace, offsetFacing.getEnumFacing());
    }

    public OffsetFacing getEnumFacingOffset(final Vec3 position) {
        for (int x2 = -1; x2 <= 1; x2 += 2) {
            if (!(getBlock(position.xCoord + x2, position.yCoord, position.zCoord) instanceof BlockAir)) {
                if (x2 > 0) {
                    return new OffsetFacing(EnumFacing.WEST, new Vec3(x2, 0, 0));
                } else {
                    return new OffsetFacing(EnumFacing.EAST, new Vec3(x2, 0, 0));
                }
            }
        }

        for (int y2 = -1; y2 <= 1; y2 += 2) {
            if (!(getBlock(position.xCoord, position.yCoord + y2, position.zCoord) instanceof BlockAir)) {
                if (y2 < 0) {
                    return new OffsetFacing(EnumFacing.UP, new Vec3(0, y2, 0));
                }
            }
        }

        for (int z2 = -1; z2 <= 1; z2 += 2) {
            if (!(getBlock(position.xCoord, position.yCoord, position.zCoord + z2) instanceof BlockAir)) {
                if (z2 < 0) {
                    return new OffsetFacing(EnumFacing.SOUTH, new Vec3(0, 0, z2));
                } else {
                    return new OffsetFacing(EnumFacing.NORTH, new Vec3(0, 0, z2));
                }
            }
        }

        return null;
    }

    public Vec3 getPlacePossibility(double offsetX, double offsetZ) {
        final List<Vec3> possibilities = new ArrayList<>();
        final int range = (int) (5 + (Math.abs(offsetX) + Math.abs(offsetZ)));

        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = blockRelativeToPlayer(x, y, z);

                    if (!(block instanceof BlockAir)) {
                        for (int x2 = -1; x2 <= 1; x2 += 2)
                            possibilities.add(new Vec3(mc.thePlayer.posX + x + x2, mc.thePlayer.posY + y, mc.thePlayer.posZ + z));

                        for (int y2 = -1; y2 <= 1; y2 += 2)
                            possibilities.add(new Vec3(mc.thePlayer.posX + x, mc.thePlayer.posY + y + y2, mc.thePlayer.posZ + z));

                        for (int z2 = -1; z2 <= 1; z2 += 2)
                            possibilities.add(new Vec3(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z + z2));
                    }
                }
            }
        }

        if (offsetX > .5 || offsetZ > .5) {
            possibilities.removeIf(vec3 -> mc.thePlayer.getDistance(vec3.xCoord, vec3.yCoord, vec3.zCoord) > 5 || !(getBlock(vec3.xCoord, vec3.yCoord, vec3.zCoord) instanceof BlockAir) || (vec3.yCoord > mc.thePlayer.posY));
        }

        if (possibilities.isEmpty()) return null;

        possibilities.sort(Comparator.comparingDouble(vec3 -> {

            final double d0 = (mc.thePlayer.posX + offsetX) - vec3.xCoord;
            final double d1 = (mc.thePlayer.posY - 1) - vec3.yCoord;
            final double d2 = (mc.thePlayer.posZ + offsetZ) - vec3.zCoord;
            return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);

        }));

        return possibilities.get(0);
    }

    public Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).add(offsetX, offsetY, offsetZ)).getBlock();
    }


    public Block getBlock(final double x, final double y, final double z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public int getBlockCount() {
        int n = 0;
        int i = 36;
        while (i < 45) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                final Item item = stack.getItem();
                if (stack.getItem() instanceof ItemBlock && this.isValid(item)) {
                    n += stack.stackSize;
                }
            }
            ++i;
        }
        return n;
    }

    public static boolean isValid(final Item item) {
        return item instanceof ItemBlock && !invalidBlocks.contains(((ItemBlock) (item)).getBlock());
    }

    private void getBlock(int switchSlot) {
        for (int i = 9; i < 45; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()
                    && (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory)) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (is.getItem() instanceof ItemBlock) {
                    ItemBlock block = (ItemBlock) is.getItem();
                    if (isValid(block)) {
                        if (36 + switchSlot != i) {
                            swap(i, switchSlot);
                        }
                        break;
                    }
                }
            }
        }

    }

    public static void swap(int slot, int switchSlot) {
        Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().thePlayer.inventoryContainer.windowId, slot, switchSlot, 2, Minecraft.getMinecraft().thePlayer);
    }


    int getBestSpoofSlot() {
        int spoofSlot = 5;

        for (int i = 36; i < 45; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                spoofSlot = i - 36;
                break;
            }
        }

        return spoofSlot;
    }

    @AllArgsConstructor
    @Getter
    private static class BlockData {
        private final BlockPos blockPos;
        private final EnumFacing enumFacing;
    }
}
