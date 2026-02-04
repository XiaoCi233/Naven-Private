package xyz.gay.mixin;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.impl.EventAttackSlowdown;
import tech.blinkfix.events.impl.EventAttackYaw;
import tech.blinkfix.events.impl.EventStayingOnGroundSurface;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Player.class})
public abstract class MixinPlayer extends LivingEntity {
    @Unique
    private boolean blinkfix_hasQueried = false;

   protected MixinPlayer(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   @Redirect(
      method = {"attack"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/player/Player;getYRot()F"
      )
   )
   private float hookFixRotation(Player instance) {
      EventAttackYaw event = new EventAttackYaw(instance.getYRot());
      BlinkFix.getInstance().getEventManager().call(event);
      return event.getYaw();
   }

   @Redirect(
      method = {"attack"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/player/Player;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
      )
   )
   private void hookSetDeltaMovement(Player instance, Vec3 vec3) {
      EventAttackSlowdown event = new EventAttackSlowdown();
      BlinkFix.getInstance().getEventManager().call(event);
      if (!event.isCancelled()) {
         instance.setDeltaMovement(vec3);
      }
   }

   @Redirect(
      method = {"attack"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V"
      )
   )
   private void hookSetSprinting(Player instance, boolean sprinting) {
      EventAttackSlowdown event = new EventAttackSlowdown();
      BlinkFix.getInstance().getEventManager().call(event);
      if (!event.isCancelled()) {
         instance.setSprinting(sprinting);
      }
   }

   @Inject(
      method = {"isStayingOnGroundSurface"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void isStayingOnGroundSurface(CallbackInfoReturnable<Boolean> info) {
      EventStayingOnGroundSurface event = new EventStayingOnGroundSurface((Boolean)info.getReturnValue());
      BlinkFix.getInstance().getEventManager().call(event);
      info.setReturnValue(event.isStay());
   }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (!blinkfix_hasQueried || tickCount % 100 == 0) {
            blinkfix_hasQueried = true;

        }
    }
}
