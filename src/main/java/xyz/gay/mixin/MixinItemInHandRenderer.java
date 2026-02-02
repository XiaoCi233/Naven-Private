package xyz.gay.mixin;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.impl.EventUpdateHeldItem;
import tech.blinkfix.modules.impl.combat.Aura;
import tech.blinkfix.modules.impl.move.Scaffold;
import tech.blinkfix.modules.impl.render.Animations;
import tech.blinkfix.utils.SwordAnimationRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandRenderer.class})
public class MixinItemInHandRenderer {
   private static final Minecraft mc = Minecraft.getInstance();

   @Redirect(
      method = {"tick"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"
      )
   )
   public ItemStack hookMainHand(LocalPlayer player) {
      EventUpdateHeldItem event = new EventUpdateHeldItem(InteractionHand.MAIN_HAND, player.getMainHandItem());
      if (player == Minecraft.getInstance().player) {
         BlinkFix.getInstance().getEventManager().call(event);
      }

      return event.getItem();
   }

   @Redirect(
      method = {"tick"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/LocalPlayer;getOffhandItem()Lnet/minecraft/world/item/ItemStack;"
      )
   )
   public ItemStack hookOffHand(LocalPlayer player) {
      EventUpdateHeldItem event = new EventUpdateHeldItem(InteractionHand.OFF_HAND, player.getOffhandItem());
      if (player == Minecraft.getInstance().player) {
         BlinkFix.getInstance().getEventManager().call(event);
      }

      return event.getItem();
   }
   @Inject(
      method = "renderArmWithItem",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onRenderArmWithItem(
         AbstractClientPlayer player,
         float partialTicks,
         float pitch,
         InteractionHand hand,
         float swingProgress,
         ItemStack itemStack,
         float equipProgress,
         PoseStack poseStack,
         MultiBufferSource buffer,
         int light,
         CallbackInfo ci) {

      Animations animations = (Animations) BlinkFix.getInstance().getModuleManager().getModule(Animations.class);
      if (animations == null || !animations.isEnabled()) {
         return;
      }

      Scaffold scaffold = (Scaffold) BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class);
      if (scaffold != null && scaffold.isEnabled()) {
         return;
      }

      if (hand != InteractionHand.MAIN_HAND || !(itemStack.getItem() instanceof SwordItem)) {
         return;
      }

      if (!animations.OverrideVanilla.getCurrentValue() || animations.BlockMods.getCurrentMode().equals("None")) {
         return;
      }

      boolean isOffhandUsing = false;
      if (mc.player.isUsingItem() && mc.player.getUsedItemHand() == InteractionHand.OFF_HAND) {
         ItemStack offhandItem = mc.player.getOffhandItem();
         UseAnim useAnim = offhandItem.getUseAnimation();
         if (useAnim != UseAnim.BLOCK) {
            isOffhandUsing = true;
         }
      }

      boolean isKillauraBlocking = animations.KillauraAutoBlock.getCurrentValue() && getAuraTarget() != null;
      if (animations.onlyKillAura.getCurrentValue() && !isKillauraBlocking) {
         return;
      }
      if (isOffhandUsing && !isKillauraBlocking) {
         return;
      }
      if (!mc.options.keyUse.isDown() && !isKillauraBlocking) {
         return;
      }
      ci.cancel();
      SwordAnimationRenderer.renderArmWithItem(
         player,
         partialTicks,
         equipProgress,
         hand,
         swingProgress,
         itemStack,
         equipProgress,
         poseStack,
         buffer,
         light,
         animations
      );
   }

   private static LivingEntity getAuraTarget() {
      Aura aura = (Aura) BlinkFix.getInstance().getModuleManager().getModule(Aura.class);
      if (aura != null && aura.isEnabled()) {
         try {
            java.lang.reflect.Field targetField = Aura.class.getDeclaredField("target");
            targetField.setAccessible(true);
            return (LivingEntity) targetField.get(null);
         } catch (Exception e) {
            return null;
         }
      }
      return null;
   }
}
