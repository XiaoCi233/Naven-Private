package com.surface.mod.visual;

import com.cubk.event.annotations.EventPriority;
import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.EventPreUpdate;
import com.surface.events.EventRotationUpdate;
import com.surface.events.EventTick;
import com.surface.mod.Mod;
import com.surface.mod.world.ScaffoldModule;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class RotationAnimationModule extends Mod {

	private float yaw, pitch, prevYaw, prevPitch;
	private boolean isRotating;

	public RotationAnimationModule() {
		super("Rotation Animation", Category.VISUAL);
	}

	@EventTarget
	public void onTick(EventTick event) {
		prevYaw = yaw;
		prevPitch = pitch;
	}

	@EventTarget
	@EventPriority(8965)
	public void onUpdate(EventPreUpdate event) {
		yaw = event.getYaw();
		pitch = event.getPitch();

		isRotating = event.isRotate();
	}

	@EventTarget
	public void onSelfRotation(EventRotationUpdate event) {
		final Entity entity = event.getEntity();
		float partialTicks = event.getPartialTicks();

		if (entity instanceof EntityPlayerSP && entity.ridingEntity == null && partialTicks != 1 && isRotating) {
			event.setRenderYawOffset(interpolateAngle(partialTicks, prevYaw, yaw));
			event.setRenderHeadYaw(interpolateAngle(partialTicks, prevYaw, yaw) - event.getRenderYawOffset());
			event.setRenderHeadPitch(lerp(partialTicks, prevPitch, pitch));
		}
	}

	// Form Minecraft
	public float interpolateAngle(float p_219805_0_, float p_219805_1_, float p_219805_2_) {
		return p_219805_1_ + p_219805_0_ * MathHelper.wrapAngleTo180_float(p_219805_2_ - p_219805_1_);
	}

	public float lerp(float pct, float start, float end) {
		return start + pct * (end - start);
	}
}
