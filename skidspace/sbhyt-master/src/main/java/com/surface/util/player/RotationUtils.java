package com.surface.util.player;

import com.surface.util.struct.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.util.vector.Vector2f;

import javax.vecmath.Vector3d;

public class RotationUtils {

    protected static Minecraft mc = Minecraft.getMinecraft();

    public static Rotation getRotation(Vector3d from, Vector3d to) {

        final double x = to.getX() - from.getX();
        final double y = to.getY() - from.getY();
        final double z = to.getZ() - from.getZ();

        final double sqrt = Math.sqrt(x * x + z * z);

        final float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90F;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(y, sqrt)));

        return new Rotation(yaw, Math.min(Math.max(pitch, -90), 90));
    }

    public static Vec3 getNearestPointBB(Vec3 eye, AxisAlignedBB box) {
        double[] origin = {eye.xCoord, eye.yCoord, eye.zCoord};
        double[] destMins = {box.minX, box.minY, box.minZ};
        double[] destMaxs = {box.maxX, box.maxY, box.maxZ};

        for (int i = 0; i < 3; i++) {
            if (origin[i] > destMaxs[i]) {
                origin[i] = destMaxs[i];
            } else if (origin[i] < destMins[i]) {
                origin[i] = destMins[i];
            }
        }

        return new Vec3(origin[0], origin[1], origin[2]);
    }

    public static int wrapAngleToDirection(float yaw, int zones) {
        int angle = (int) ((double) (yaw + (float) (360 / (2 * zones))) + 0.5) % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle / (360 / zones);
    }

    public static Vector2f calculate(final com.surface.util.Vector3d from, final com.surface.util.Vector3d to) {

        final com.surface.util.Vector3d diff = to.subtract(from);
        final double distance = Math.hypot(diff.getX(), diff.getZ());
        final float yaw = (float) (MathHelper.atan2(diff.getZ(), diff.getX()) * MathHelper.TO_DEGREES) - 90.0F;
        final float pitch = (float) (-(MathHelper.atan2(diff.getY(), distance) * MathHelper.TO_DEGREES));
        return new Vector2f(yaw, pitch);
    }

    public static Vector2f calculate(final Vec3 to) {
        return calculate(mc.thePlayer.getCustomPositionVector().add(0, mc.thePlayer.getEyeHeight(), 0), new com.surface.util.Vector3d(to.xCoord, to.yCoord, to.zCoord));
    }

    public static Rotation getRotationFromEyeToPoint(Vector3d point3d) {
        return getRotation(new Vector3d(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), point3d);
    }

    public static Rotation getRotationFromEyePrevToPoint(Vector3d point3d) {
        return getRotation(new Vector3d(mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX), mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) + mc.thePlayer.getEyeHeight(), mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ)), point3d);
    }

    public static Rotation getRotationFromEyePrevToEntity(EntityLivingBase entity) {
        return getRotation(new Vector3d(mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX), mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) + mc.thePlayer.getEyeHeight(), mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ)), new Vector3d(entity.posX, entity.getEntityBoundingBox().minY + entity.getEyeHeight(), entity.posZ));
    }

    public static Rotation getRotationFromEyeToEntity(EntityLivingBase entity) {
        return getRotation(new Vector3d(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), new Vector3d(entity.posX, entity.getEntityBoundingBox().minY + entity.getEyeHeight(), entity.posZ));
    }

    public static float getRotationDifference(float current, float target) {
        return MathHelper.wrapAngleTo180_float(target - current);
    }
}
