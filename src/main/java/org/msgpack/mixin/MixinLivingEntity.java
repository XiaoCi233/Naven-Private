package org.msgpack.mixin;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.events.impl.EventFallFlying;
import com.heypixel.heypixelmod.events.impl.EventJump;
import com.heypixel.heypixelmod.events.impl.EventRotationAnimation;
import com.heypixel.heypixelmod.modules.impl.render.AntiNausea;
import com.heypixel.heypixelmod.modules.impl.render.FullBright;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class MixinLivingEntity extends Entity {
   public MixinLivingEntity(EntityType<?> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   @Redirect(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F",
         opcode = 182,
         ordinal = 0
      ),
      method = {"jumpFromGround"}
   )
   private float modifyJumpYaw(LivingEntity entity) {
      EventJump event = new EventJump(entity.getYRot());
      com.heypixel.heypixelmod.BlinkFix.getInstance().getEventManager().call(event);
      return event.getYaw();
   }

   @Redirect(
      method = {"travel"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/LivingEntity;getXRot()F"
      )
   )
   private float hookModifyFallFlyingPitch(LivingEntity instance) {
      EventFallFlying event = new EventFallFlying(instance.getXRot());
      BlinkFix.getInstance().getEventManager().call(event);
      return event.getPitch();
   }

   @Inject(
      method = {"hasEffect"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hasEffect(MobEffect pEffect, CallbackInfoReturnable<Boolean> cir) {
      LivingEntity thisEntity = (LivingEntity)(Object)this;
      if (thisEntity == Minecraft.getInstance().player) {
         FullBright fullBright = (FullBright) com.heypixel.heypixelmod.BlinkFix.getInstance().getModuleManager().getModule(FullBright.class);
         if (pEffect == MobEffects.NIGHT_VISION && fullBright.isEnabled()) {
            cir.setReturnValue(true);
            cir.cancel();
         }

         AntiNausea antiNausea = (AntiNausea) com.heypixel.heypixelmod.BlinkFix.getInstance().getModuleManager().getModule(AntiNausea.class);
         if (pEffect == MobEffects.CONFUSION && antiNausea.isEnabled()) {
            cir.setReturnValue(false);
            cir.cancel();
         }
      }
   }

   @Redirect(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"
      ),
      method = {"tickHeadTurn"}
   )
   private float modifyHeadYaw(LivingEntity entity) {
      if (entity == Minecraft.getInstance().player) {
         EventRotationAnimation event = new EventRotationAnimation(entity.getYRot(), 0.0F, 0.0F, 0.0F);
         com.heypixel.heypixelmod.BlinkFix.getInstance().getEventManager().call(event);
         return event.getYaw();
      } else {
         return entity.getYRot();
      }
   }
}
