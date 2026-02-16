package com.surface.mod;

import com.surface.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.security.SecureRandom;

public abstract class SubMod<T extends Mod> {

    protected static final Minecraft mc = Minecraft.getMinecraft();

    private final T parent;
    private boolean enabled;

    public SubMod(T parent) {
        this.parent = parent;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) subscribe();
            else remove();
        }
    }

    public abstract String getName();

    public void toggle() {
        setEnabled(!isEnabled());
    }

    public void onEnable() {
    }

    public void onDisable() {
    }


    protected float getRandomInRange(float min, float max) {
        SecureRandom random = new SecureRandom();
        return random.nextFloat() * (max - min) + min;
    }


    protected void updateTool(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        float strength = 1.0F;
        int slot = -1;

        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack != null && itemStack.getStrVsBlock(block) > strength) {
                slot = i;
                strength = itemStack.getStrVsBlock(block);
            }
        }

        if (slot != -1 && mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem) != mc.thePlayer.inventory.getStackInSlot(slot)) {
            mc.thePlayer.inventory.currentItem = slot;
        }
    }

    protected float[] getRotations(double posX, double posY, double posZ) {
        EntityPlayerSP player = mc.thePlayer;
        double x = posX - player.posX;
        double y = posY - (player.posY + (double) player.getEyeHeight());
        double z = posZ - player.posZ;
        double dist = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(y, dist) * 180.0D / Math.PI);
        return new float[]{yaw, pitch};
    }


    protected float[] getRotationsToEntity(Entity ent) {
        final double differenceX = ent.posX - mc.thePlayer.posX;
        final double differenceY = (ent.posY + ent.height) - (mc.thePlayer.posY + mc.thePlayer.height) - 0.5;
        final double differenceZ = ent.posZ - mc.thePlayer.posZ;
        final float rotationYaw = (float) (Math.atan2(differenceZ, differenceX) * 180.0D / Math.PI) - 90.0f;
        final float rotationPitch = (float) (Math.atan2(differenceY, mc.thePlayer.getDistanceToEntity(ent)) * 180.0D
                / Math.PI);
        final float finishedYaw = mc.thePlayer.rotationYaw
                + MathHelper.wrapAngleTo180_float(rotationYaw - mc.thePlayer.rotationYaw);
        final float finishedPitch = mc.thePlayer.rotationPitch
                + MathHelper.wrapAngleTo180_float(rotationPitch - mc.thePlayer.rotationPitch);
        return new float[]{finishedYaw, -MathHelper.clamp_float(finishedPitch, -90, 90)};
    }


    protected boolean isMoving() {
        return mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0;
    }

    private void subscribe() {
        try {
            Wrapper.Instance.getEventManager().register(this);
            onEnable();
        } catch (Exception ex) {
            remove();
            enabled = false;
            if (mc.thePlayer != null || mc.theWorld != null) {
                ex.printStackTrace();
            }
        }
    }

    private void remove() {
        try {
            Wrapper.Instance.getEventManager().unregister(this);
            onDisable();
        } catch (Exception ex) {
            if (mc.thePlayer != null || mc.theWorld != null) {
                ex.printStackTrace();
            }
        }
    }

    public T getParent() {
        return parent;
    }
}
