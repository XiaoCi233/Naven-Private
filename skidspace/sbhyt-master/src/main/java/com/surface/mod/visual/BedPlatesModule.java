package com.surface.mod.visual;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.Event2D;
import com.surface.mod.Mod;
import com.surface.render.font.FontManager;
import com.surface.util.TimerUtils;
import com.surface.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BedPlatesModule extends Mod {

    private final NumberValue layers = new NumberValue("Layers", 3D, 1D, 10D, 1D);
    private final TimerUtils timer = new TimerUtils();
    private final List<BlockPos> beds = new ArrayList<>();
    private final List<List<Block>> bedBlocks = new ArrayList<>();
    private final List<BlockPos> retardedList = new ArrayList<>();


    private static final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
    private static final FloatBuffer modelView = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer vector = GLAllocation.createDirectFloatBuffer(4);


    public BedPlatesModule() {
        super("BedPlates", Category.VISUAL);
        registerValues(layers);
    }


    public void onEnable() {
        for (int i = 0; i < 8; i++) {
            this.beds.add(null);
            this.bedBlocks.add(new ArrayList<>());
        }
    }

    public void onDisable() {
        this.beds.clear();
        this.bedBlocks.clear();
    }

    @EventTarget
    public void renderBlockList(Event2D event) {
        int index = 0;
        if (timer.hasTimeElapsed(3000)) {
            beds.clear();
            bedBlocks.clear();
            for (int i = 0; i < 8; i++) {
                beds.add(null);
                bedBlocks.add(new ArrayList<>());
            }
            int radius = 30;
            int ind = 0;
            for (int y = radius; y >= -radius; --y) {
                for (int x = -radius; x <= radius; ++x) {
                    for (int z = -radius; z <= radius; ++z) {
                        if (mc.thePlayer != null && mc.theWorld != null) {
                            BlockPos pos = new BlockPos(mc.thePlayer.posX + (double) x, mc.thePlayer.posY + (double) y, mc.thePlayer.posZ + (double) z);
                            Block bl = mc.theWorld.getBlockState(pos).getBlock();
                            if (retardedList.contains(pos))
                                continue;
                            if (ind < 8) {
                                if (bl.equals(Blocks.bed)) {
                                    boolean found = find(pos.getX(), pos.getY(), pos.getZ(), ind);
                                    if (found) {
                                        retardedList.add(pos.north());
                                        retardedList.add(pos.south());
                                        retardedList.add(pos.east());
                                        retardedList.add(pos.west());
                                        ind++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            timer.reset();
        }
        if (!this.beds.isEmpty()) {

            for (BlockPos blockPos : this.beds) {
                if (blockPos == null)
                    continue;
                if (beds.get(index) != null) {
                    final double x = blockPos.getX();
                    final double y = blockPos.getY();
                    final double z = blockPos.getZ();
                    final AxisAlignedBB aabb = new AxisAlignedBB(x, y - 1, z, x, y + 1, z);

                    final List<Vector3d> vectors = Arrays.asList(new Vector3d(aabb.minX, aabb.minY, aabb.minZ),
                            new Vector3d(aabb.minX, aabb.maxY, aabb.minZ), new Vector3d(aabb.maxX, aabb.minY, aabb.minZ),
                            new Vector3d(aabb.maxX, aabb.maxY, aabb.minZ), new Vector3d(aabb.minX, aabb.minY, aabb.maxZ),
                            new Vector3d(aabb.minX, aabb.maxY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.minY, aabb.maxZ),
                            new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ));


                    mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);

                    Vector4d position = null;

                    for (Vector3d vector : vectors) {

                        vector = worldToScreen(new ScaledResolution(mc), vector.x - mc.getRenderManager().viewerPosX, vector.y - mc.getRenderManager().viewerPosY, vector.z - mc.getRenderManager().viewerPosZ);

                        if (vector != null && vector.z >= .0 && vector.z < 1) {

                            if (position == null)
                                position = new Vector4d(vector.x, vector.y, vector.z, .0);

                            position.x = Math.min(vector.x, position.x);
                            position.y = Math.min(vector.y, position.y);
                            position.z = Math.max(vector.x, position.z);
                            position.w = Math.max(vector.y, position.w);
                        }
                    }

                    mc.entityRenderer.setupOverlayRendering();
                    if (position != null) {
                        float width = bedBlocks.get(index).size() * 20 + 4;
                        final float posX = (float) position.x - width / 2f;
                        final float posY = (float) position.y;
                        FontManager.WQY.setFontSize(18);
                        FontManager.WQY.drawCenteredString(((int) mc.thePlayer.getDistance(blockPos)) + "m", posX + width / 2, posY + 4, -1);

                        float curX = posX + 4;
                        for (Block block : bedBlocks.get(index)) {
                            ItemStack stack = new ItemStack(block);
                            GlStateManager.pushMatrix();
                            RenderHelper.enableGUIStandardItemLighting();
                            GlStateManager.disableAlpha();
                            GlStateManager.clear(256);
                            mc.getRenderItem().zLevel = -150.0F;
                            GlStateManager.disableLighting();
                            GlStateManager.disableDepth();
                            GlStateManager.disableBlend();
                            GlStateManager.enableLighting();
                            GlStateManager.enableDepth();
                            GlStateManager.disableLighting();
                            GlStateManager.disableDepth();
                            GlStateManager.disableTexture2D();
                            GlStateManager.disableAlpha();
                            GlStateManager.disableBlend();
                            GlStateManager.enableBlend();
                            GlStateManager.enableAlpha();
                            GlStateManager.enableTexture2D();
                            GlStateManager.enableLighting();
                            GlStateManager.enableDepth();
                            mc.getRenderItem().renderItemIntoGUI(stack, curX, posY + FontManager.WQY.getHeight() + 8);
                            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, stack, (int) curX, (posY + FontManager.WQY.getHeight() + 8), null);
                            mc.getRenderItem().zLevel = 0.0F;
                            GlStateManager.enableAlpha();
                            RenderHelper.disableStandardItemLighting();
                            GlStateManager.popMatrix();
                            curX += 20;
                        }
                    }
                    mc.entityRenderer.setupOverlayRendering();
                    index++;
                }
            }
        }
    }

    public static Vector3d worldToScreen(ScaledResolution sr, double x, double y, double z) {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

        if (GLU.gluProject((float) x, (float) y, (float) z, modelView, projection, viewport, vector))
            return new Vector3d(vector.get(0) / sr.getScaleFactor(), (Minecraft.getMinecraft().displayHeight - vector.get(1)) / sr.getScaleFactor(), vector.get(2));

        return null;
    }


    private boolean find(double x, double y, double z, int index) {
        BlockPos bedPos = new BlockPos(x, y, z);
        Block bed = mc.theWorld.getBlockState(bedPos).getBlock();
        bedBlocks.get(index).clear();
        beds.set(index, null);

        if (beds.contains(bedPos)) {
            return false;
        }

        Block[] targetBlocks = {
                Blocks.wool, Blocks.stained_hardened_clay, Blocks.stained_glass, Blocks.planks, Blocks.log, Blocks.log2, Blocks.end_stone, Blocks.obsidian,
                Blocks.bedrock
        }; // BW可以见到的守家方块类型

        for (int yOffset = 0; yOffset <= layers.getValue(); ++yOffset) {
            for (int xOffset = (int) -layers.getValue(); xOffset <= layers.getValue(); ++xOffset) {
                for (int zOffset = (int) -layers.getValue(); zOffset <= layers.getValue(); ++zOffset) {
                    Block blockAtOffset = mc.theWorld.getBlockState(new BlockPos(bedPos.getX() + xOffset, bedPos.getY() + yOffset, bedPos.getZ() + zOffset)).getBlock();

                    for (Block targetBlock : targetBlocks) {
                        if (blockAtOffset.equals(targetBlock) && !bedBlocks.get(index).contains(targetBlock)) {
                            bedBlocks.get(index).add(targetBlock);
                        }
                    }
                }
            }
        }

        if (bed.equals(Blocks.bed)) {
            beds.set(index, bedPos);
            return true;
        }

        return false;
    }

}