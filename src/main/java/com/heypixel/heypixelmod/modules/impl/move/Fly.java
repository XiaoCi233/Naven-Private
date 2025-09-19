package com.heypixel.heypixelmod.modules.impl.move;

import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ModuleInfo(
        name = "Fly",
        description = "Fly to Moon",
        category = Category.MOVEMENT
)
public class Fly extends Module {

    private static final Fly INSTANCE = new Fly();
    private final Minecraft mc = Minecraft.getInstance();
    private float vanillaVSpeed = 2.0f;
    private float vanillaSpeed = 2.0f;

    public static Fly getInstance() {
        return INSTANCE;
    }
    @Override
    public void onEnable() {
        if (mc.player == null)
            return;
        mc.player.getAbilities().mayfly = true;
        mc.player.getAbilities().flying = true;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            if (!mc.player.isCreative() && !mc.player.isSpectator()) {
                mc.player.getAbilities().mayfly = false;
                mc.player.getAbilities().flying = false;
            }
        }
    }
    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player != mc.player) return;
        if (mc.player == null) return;

        // 重置运动
        mc.player.setDeltaMovement(0, 0, 0);

        // 垂直移动
        if (mc.options.keyJump.isDown()) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, vanillaVSpeed, mc.player.getDeltaMovement().z);
        }
        if (mc.options.keyShift.isDown()) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, -vanillaVSpeed, mc.player.getDeltaMovement().z);
        }

        // 水平移动
        strafe(vanillaSpeed);
    }

    private void strafe(float speed) {
        if (mc.player == null) return;

        float forward = mc.player.zza;
        float strafe = mc.player.xxa;
        float yaw = mc.player.getYRot();

        if (forward == 0.0f && strafe == 0.0f) {
            mc.player.setDeltaMovement(0, mc.player.getDeltaMovement().y, 0);
            return;
        }

        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        double motionX = (strafe * cos - forward * sin) * speed;
        double motionZ = (forward * cos + strafe * sin) * speed;

        mc.player.setDeltaMovement(motionX, mc.player.getDeltaMovement().y, motionZ);
    }

    // Getter 和 Setter 方法
    public float getVanillaVSpeed() {
        return vanillaVSpeed;
    }

    public void setVanillaVSpeed(float vanillaVSpeed) {
        this.vanillaVSpeed = vanillaVSpeed;
    }

    public float getVanillaSpeed() {
        return vanillaSpeed;
    }

    public void setVanillaSpeed(float vanillaSpeed) {
        this.vanillaSpeed = vanillaSpeed;
    }


}