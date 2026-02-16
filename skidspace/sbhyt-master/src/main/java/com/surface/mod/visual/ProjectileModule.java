package com.surface.mod.visual;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.Event2D;
import com.surface.events.Event3D;
import com.surface.events.EventPostUpdate;
import com.surface.events.EventPreUpdate;
import com.surface.mod.Mod;
import com.surface.util.render.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectileModule extends Mod {
    public ProjectileModule() {
        super("Projectile", Category.VISUAL);
    }

    float yaw, pitch;

    @EventTarget
    public void onPre(EventPreUpdate e) {
            yaw = e.getYaw();
            pitch = e.getPitch();
    }

    @EventTarget
    public void onUpdate(Event3D event) {
        boolean isBow = false;
        float pitchDifference = 0.0F;
        float motionFactor = 1.5F;
        float motionSlowdown = 0.99F;
        if (mc.thePlayer.getCurrentEquippedItem() != null) {
            Item heldItem = mc.thePlayer.getCurrentEquippedItem().getItem();
            float gravity;
            float size;
            if (heldItem instanceof ItemBow) {
                isBow = true;
                gravity = 0.05F;
                size = 0.3F;
                float power = (float) mc.thePlayer.getItemInUseDuration() / 20.0F;
                power = (power * power + power * 2.0F) / 3.0F;

                if ((double) power < 0.1D) {
                    return;
                }

                if (power > 1.0F) {
                    power = 1.0F;
                }

                motionFactor = power * 3.0F;
            } else if (heldItem instanceof ItemFishingRod) {
                gravity = 0.04F;
                size = 0.25F;
                motionSlowdown = 0.92F;
            } else if (ItemPotion.isSplash(mc.thePlayer.getCurrentEquippedItem().getMetadata())) {
                gravity = 0.05F;
                size = 0.25F;
                pitchDifference = -20.0F;
                motionFactor = 0.5F;
            } else {
                if (!(heldItem instanceof ItemSnowball) && !(heldItem instanceof ItemEnderPearl)
                        && !(heldItem instanceof ItemEgg)) {
                    return;
                }

                gravity = 0.03F;
                size = 0.25F;
            }

            double posX = mc.getRenderManager().renderPosX
                    - (double) (MathHelper.cos(yaw / 180.0F * 3.1415927F) * 0.16F);
            double posY = mc.getRenderManager().renderPosY + (double) mc.thePlayer.getEyeHeight()
                    - 0.10000000149011612D;
            double posZ = mc.getRenderManager().renderPosZ
                    - (double) (MathHelper.sin(yaw / 180.0F * 3.1415927F) * 0.16F);
            double motionX = (double) (-MathHelper.sin(yaw / 180.0F * 3.1415927F)
                    * MathHelper.cos(pitch / 180.0F * 3.1415927F)) * (isBow ? 1.0D : 0.4D);
            double motionY = (double) (-MathHelper
                    .sin((pitch + pitchDifference) / 180.0F * 3.1415927F))
                    * (isBow ? 1.0D : 0.4D);
            double motionZ = (double) (MathHelper.cos(yaw / 180.0F * 3.1415927F)
                    * MathHelper.cos(pitch / 180.0F * 3.1415927F)) * (isBow ? 1.0D : 0.4D);
            float distance = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
            motionX /= distance;
            motionY /= distance;
            motionZ /= distance;
            motionX *= motionFactor;
            motionY *= motionFactor;
            motionZ *= motionFactor;
            MovingObjectPosition landingPosition = null;
            boolean hasLanded = false;
            boolean hitEntity = false;
            RenderUtils.pre3D();
            RenderUtils.color(new Color(206, 89, 255, 255).getRGB());
            GL11.glLineWidth(2.0F);
            GL11.glBegin(3);

            while (!hasLanded && posY > 0.0D) {
                Vec3 posBefore = new Vec3(posX, posY, posZ);
                Vec3 posAfter = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                landingPosition = mc.theWorld.rayTraceBlocks(posBefore, posAfter, false, true, false);
                posBefore = new Vec3(posX, posY, posZ);
                posAfter = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                if (landingPosition != null) {
                    hasLanded = true;
                    posAfter = new Vec3(landingPosition.hitVec.xCoord, landingPosition.hitVec.yCoord,
                            landingPosition.hitVec.zCoord);
                }

                AxisAlignedBB arrowBox = new AxisAlignedBB(posX - (double) size, posY - (double) size,
                        posZ - (double) size, posX + (double) size, posY + (double) size, posZ + (double) size);
                List entityList = this
                        .getEntitiesWithinAABB(arrowBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));

                for (int i = 0; i < entityList.size(); ++i) {
                    Entity var18 = (Entity) entityList.get(i);
                    if (var18.canBeCollidedWith() && var18 != mc.thePlayer) {
                        AxisAlignedBB var2 = var18.getEntityBoundingBox().expand(size, size,
                                size);
                        MovingObjectPosition possibleEntityLanding = var2.calculateIntercept(posBefore, posAfter);
                        if (possibleEntityLanding != null) {
                            hitEntity = true;
                            hasLanded = true;
                            landingPosition = possibleEntityLanding;
                        }
                    }
                }

                posX += motionX;
                posY += motionY;
                posZ += motionZ;
                BlockPos var35 = new BlockPos(posX, posY, posZ);
                Block var36 = mc.theWorld.getBlockState(var35).getBlock();
                if (var36.getMaterial() == Material.water) {
                    motionX *= 0.6D;
                    motionY *= 0.6D;
                    motionZ *= 0.6D;
                } else {
                    motionX *= motionSlowdown;
                    motionY *= motionSlowdown;
                    motionZ *= motionSlowdown;
                }

                motionY -= gravity;
                GL11.glVertex3d(posX - mc.getRenderManager().renderPosX,
                        posY - mc.getRenderManager().renderPosY,
                        posZ - mc.getRenderManager().renderPosZ);
            }

            GL11.glEnd();
            GL11.glPushMatrix();
            GL11.glTranslated(posX - mc.getRenderManager().renderPosX,
                    posY - mc.getRenderManager().renderPosY,
                    posZ - mc.getRenderManager().renderPosZ);

            if (landingPosition != null) {

                int side = landingPosition.sideHit.getIndex();

                if (side == 1 && heldItem instanceof ItemEnderPearl) {
                    RenderUtils.color(new Color(255, 248, 0, 255).getRGB());
                } else if (side == 2) {
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                } else if (side == 3) {
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                } else if (side == 4) {
                    GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                } else if (side == 5) {
                    GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                }

                if (hitEntity) {
                    RenderUtils.color(new Color(255, 248, 0, 255).getRGB());
                }

            }

            this.renderPoint();
            GL11.glPopMatrix();
            RenderUtils.post3D();
        }
    }

    private void renderPoint() {
        GL11.glBegin(1);
        GL11.glVertex3d(-0.5D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, 0.0D, -0.5D);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glVertex3d(0.5D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glVertex3d(0.0D, 0.0D, 0.5D);
        GL11.glVertex3d(0.0D, 0.0D, 0.0D);
        GL11.glEnd();
        Cylinder c = new Cylinder();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);

        c.setDrawStyle(GLU.GLU_LINE);
        c.draw(0.5F, 0.5F, 0F, 256, 27);
    }

    private List<Entity> getEntitiesWithinAABB(AxisAlignedBB axisalignedBB) {
        ArrayList<Entity> list = new ArrayList<>();
        int chunkMinX = MathHelper.floor_double((axisalignedBB.minX - 2.0D) / 16.0D);
        int chunkMaxX = MathHelper.floor_double((axisalignedBB.maxX + 2.0D) / 16.0D);
        int chunkMinZ = MathHelper.floor_double((axisalignedBB.minZ - 2.0D) / 16.0D);
        int chunkMaxZ = MathHelper.floor_double((axisalignedBB.maxZ + 2.0D) / 16.0D);

        for (int x = chunkMinX; x <= chunkMaxX; ++x) {
            for (int z = chunkMinZ; z <= chunkMaxZ; ++z) {
                if (mc.theWorld.getChunkProvider().chunkExists(x, z)) {
                    mc.theWorld.getChunkFromChunkCoords(x, z).getEntitiesWithinAABBForEntity(mc.thePlayer, axisalignedBB, list, EntitySelectors.selectAnything);
                }
            }
        }

        return list;
    }
}
