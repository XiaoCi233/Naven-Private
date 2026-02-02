package tech.blinkfix.utils;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.modules.impl.combat.Aura;
import tech.blinkfix.modules.impl.render.Animations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

public class SwordAnimationRenderer {

    public static void renderArmWithItem(
            AbstractClientPlayer player,
            float partialTicks,
            float equipProgress,
            InteractionHand interactionHand,
            float swingProgress,
            ItemStack itemStack,
            float equippedProg,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int light,
            Animations animations) {
        
        if (player.isScoping()) {
            return;
        }
        
        boolean flag = interactionHand == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidarm = flag ? player.getMainArm() : player.getMainArm().getOpposite();
        
        poseStack.pushPose();

        boolean skipOffhandShield = !flag &&
                player.getOffhandItem().getItem() instanceof ShieldItem &&
                !animations.RenderOffhandShield.getCurrentValue();

        if (!skipOffhandShield) {
            if (itemStack.isEmpty()) {
                if (flag && !player.isInvisible()) {
                    renderPlayerArm(poseStack, multiBufferSource, light, equippedProg, swingProgress, humanoidarm, animations);
                }
            } else if (itemStack.is(Items.CROSSBOW)) {
                renderCrossbow(player, partialTicks, interactionHand, itemStack, equippedProg, swingProgress, 
                             humanoidarm, poseStack, multiBufferSource, light);
            } else {
                boolean flag2 = humanoidarm == HumanoidArm.RIGHT;
                if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0
                        && player.getUsedItemHand() == interactionHand) {
                    renderUsingItem(player, partialTicks, itemStack, equippedProg, swingProgress, 
                                  humanoidarm, poseStack, multiBufferSource, light);
                } else if (shouldRenderBlockingAnimation(player, itemStack, animations)) {
                    renderBlockingAnimation(player, itemStack, equippedProg, swingProgress, 
                                          humanoidarm, poseStack, multiBufferSource, light, animations);
                } else if (player.isAutoSpinAttack()) {
                    renderAutoSpinAttack(equippedProg, humanoidarm, poseStack);
                } else {
                    renderNormalItem(player, itemStack, equippedProg, swingProgress, 
                                   humanoidarm, poseStack, multiBufferSource, light, animations);
                }

                int i = flag2 ? 1 : -1;
                renderItem(
                        player,
                        itemStack,
                        i == 1 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                        i != 1,
                        poseStack,
                        multiBufferSource,
                        light);
            }
        }

        poseStack.popPose();
    }

    private static boolean shouldRenderBlockingAnimation(AbstractClientPlayer player, ItemStack itemStack, Animations animations) {
        Minecraft mc = Minecraft.getInstance();
        
        if (!(itemStack.getItem() instanceof SwordItem)) {
            return false;
        }
        
        boolean isKillauraBlocking = animations.KillauraAutoBlock.getCurrentValue() && 
                                    getAuraTarget() != null;
        
        return (mc.options.keyUse.isDown() || isKillauraBlocking) && 
               !animations.BlockMods.getCurrentMode().equals("None");
    }

    private static void renderBlockingAnimation(AbstractClientPlayer player, ItemStack itemStack, 
                                               float equippedProg, float swingProgress, 
                                               HumanoidArm humanoidarm, PoseStack poseStack, 
                                               MultiBufferSource multiBufferSource, int light, 
                                               Animations animations) {
        int i = humanoidarm == HumanoidArm.RIGHT ? 1 : -1;
        String blockMode = animations.BlockMods.getCurrentMode().toLowerCase();
        
        switch (blockMode) {
            case "1.7":
                render17BlockAnimation(i, swingProgress, poseStack, animations);
                break;
            case "push":
                renderPushBlockAnimation(i, swingProgress, poseStack, animations);
                break;
        }
    }

    private static void render17BlockAnimation(int handSide, float swingProgress, PoseStack poseStack, Animations animations) {
        poseStack.translate((double) ((float) handSide * animations.BlockingX.getCurrentValue()),
                          (double) (animations.BlockingY.getCurrentValue()), -0.72F);
        
        float f17 = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float f22 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
        
        poseStack.mulPose(Axis.YP.rotation((float) handSide * (45.0F + f17 * -20.0F) * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.ZP.rotation((float) handSide * f22 * -20.0F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.XP.rotation(f22 * -80.0F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.YP.rotation((float) handSide * -45.0F * (float) Math.PI / 180.0F));
        
        poseStack.scale(0.9F, 0.9F, 0.9F);
        poseStack.translate(-0.2F, 0.126F, 0.2F);
        
        poseStack.mulPose(Axis.XP.rotation(-102.25F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.YP.rotation((float) handSide * 15.0F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.ZP.rotation((float) handSide * 80.0F * (float) Math.PI / 180.0F));
    }

    private static void renderPushBlockAnimation(int handSide, float swingProgress, PoseStack poseStack, Animations animations) {
        poseStack.translate((double) ((float) handSide * animations.BlockingX.getCurrentValue()),
                          (double) (animations.BlockingY.getCurrentValue()), -0.72F);
        poseStack.translate((double) ((float) handSide * -0.1414214F), 0.08F, 0.1414214F);
        
        poseStack.mulPose(Axis.XP.rotation(-102.25F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.YP.rotation((float) handSide * 13.365F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.ZP.rotation((float) handSide * 78.05F * (float) Math.PI / 180.0F));
        
        float f15 = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float f3 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
        
        poseStack.mulPose(Axis.XP.rotation(f15 * -10.0F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.YP.rotation(f15 * -10.0F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.ZP.rotation(f15 * -10.0F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.XP.rotation(f3 * -10.0F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.YP.rotation(f3 * -10.0F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.ZP.rotation(f3 * -10.0F * (float) Math.PI / 180.0F));
    }

    private static void renderCrossbow(AbstractClientPlayer player, float partialTicks, InteractionHand hand,
                                      ItemStack itemStack, float equippedProg, float swingProgress,
                                      HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int light) {
        boolean flag1 = CrossbowItem.isCharged(itemStack);
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        
        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
            applyItemArmTransform(poseStack, arm, equippedProg);
            poseStack.translate((double) ((float) i * -0.4785682F), -0.094387F, 0.0573153F);
            poseStack.mulPose(Axis.XP.rotation(-11.935F * (float) Math.PI / 180.0F));
            poseStack.mulPose(Axis.YP.rotation((float) i * 65.3F * (float) Math.PI / 180.0F));
            poseStack.mulPose(Axis.ZP.rotation((float) i * -9.785F * (float) Math.PI / 180.0F));
            
            float f6 = (float) itemStack.getUseDuration() - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
            float f10 = f6 / (float) CrossbowItem.getChargeDuration(itemStack);
            f10 = Math.min(f10, 1.0F);
            
            if (f10 > 0.1F) {
                float f14 = Mth.sin((f6 - 0.1F) * 1.3F);
                float f20 = f10 - 0.1F;
                float f25 = f14 * f20;
                poseStack.translate((double) (f25 * 0.0F), (double) (f25 * 0.004F), (double) (f25 * 0.0F));
            }
            
            poseStack.translate((double) (f10 * 0.0F), (double) (f10 * 0.0F), (double) (f10 * 0.04F));
            poseStack.scale(1.0F, 1.0F, 1.0F + f10 * 0.2F);
            poseStack.mulPose(Axis.YP.rotation((float) i * -45.0F * (float) Math.PI / 180.0F));
        } else {
            float f5 = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
            float f9 = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * (float) (Math.PI * 2));
            float f13 = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
            poseStack.translate((double) ((float) i * f5), (double) f9, (double) f13);
            applyItemArmTransform(poseStack, arm, equippedProg);
            applyItemArmAttackTransform(poseStack, arm, swingProgress);
            
            if (flag1 && swingProgress < 0.001F && arm == HumanoidArm.RIGHT) {
                poseStack.translate((double) ((float) i * -0.641864F), 0.0, 0.0);
                poseStack.mulPose(Axis.YP.rotation((float) i * 10.0F * (float) Math.PI / 180.0F));
            }
        }
        
        renderItem(player, itemStack,
                 i == 1 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                 i != 1, poseStack, buffer, light);
    }
    

    private static void renderUsingItem(AbstractClientPlayer player, float partialTicks, ItemStack itemStack,
                                       float equippedProg, float swingProgress, HumanoidArm arm,
                                       PoseStack poseStack, MultiBufferSource buffer, int light) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        
        switch (itemStack.getUseAnimation()) {
            case NONE:
            case BLOCK:
                applyItemArmTransform(poseStack, arm, equippedProg);
                break;
            case EAT:
            case DRINK:
                applyEatTransform(poseStack, partialTicks, arm, itemStack);
                applyItemArmTransform(poseStack, arm, equippedProg);
                break;
            case BOW:
                renderBowAnimation(player, partialTicks, itemStack, equippedProg, arm, poseStack, i);
                break;
            case SPEAR:
                renderSpearAnimation(player, partialTicks, itemStack, equippedProg, arm, poseStack, i);
                break;
            case CROSSBOW:
            case SPYGLASS:
            case TOOT_HORN:
            case BRUSH:
            case CUSTOM:
            default:
                applyItemArmTransform(poseStack, arm, equippedProg);
                break;
        }
    }

    private static void renderBowAnimation(AbstractClientPlayer player, float partialTicks, ItemStack itemStack,
                                          float equippedProg, HumanoidArm arm, PoseStack poseStack, int handSide) {
        applyItemArmTransform(poseStack, arm, equippedProg);
        poseStack.translate((double) ((float) handSide * -0.2785682F), 0.183444F, 0.1573153F);
        poseStack.mulPose(Axis.XP.rotation(-13.935F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.YP.rotation((float) handSide * 35.3F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.ZP.rotation((float) handSide * -9.785F * (float) Math.PI / 180.0F));
        
        float f8 = (float) itemStack.getUseDuration() - ((float) player.getUseItemRemainingTicks() - Minecraft.getInstance().getFrameTime() + 1.0F);
        float f12 = f8 / 20.0F;
        f12 = (f12 * f12 + f12 * 2.0F) / 3.0F;
        f12 = Math.min(f12, 1.0F);
        
        if (f12 > 0.1F) {
            float f19 = Mth.sin((f8 - 0.1F) * 1.3F);
            float f24 = f12 - 0.1F;
            float f26 = f19 * f24;
            poseStack.translate((double) (f26 * 0.0F), (double) (f26 * 0.004F), (double) (f26 * 0.0F));
        }
        
        poseStack.translate((double) (f12 * 0.0F), (double) (f12 * 0.0F), (double) (f12 * 0.04F));
        poseStack.scale(1.0F, 1.0F, 1.0F + f12 * 0.2F);
        poseStack.mulPose(Axis.YP.rotation((float) handSide * -45.0F * (float) Math.PI / 180.0F));
    }

    private static void renderSpearAnimation(AbstractClientPlayer player, float partialTicks, ItemStack itemStack,
                                            float equippedProg, HumanoidArm arm, PoseStack poseStack, int handSide) {
        applyItemArmTransform(poseStack, arm, equippedProg);
        poseStack.translate((double) ((float) handSide * -0.5F), 0.7F, 0.1F);
        poseStack.mulPose(Axis.XP.rotation(-55.0F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.YP.rotation((float) handSide * 35.3F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.ZP.rotation((float) handSide * -9.785F * (float) Math.PI / 180.0F));
        
        float f7 = (float) itemStack.getUseDuration() - ((float) player.getUseItemRemainingTicks() - Minecraft.getInstance().getFrameTime() + 1.0F);
        float f11 = f7 / 10.0F;
        f11 = Math.min(f11, 1.0F);
        
        if (f11 > 0.1F) {
            float f18 = Mth.sin((f7 - 0.1F) * 1.3F);
            float f23 = f11 - 0.1F;
            float f4 = f18 * f23;
            poseStack.translate((double) (f4 * 0.0F), (double) (f4 * 0.004F), (double) (f4 * 0.0F));
        }
        
        poseStack.translate(0.0, 0.0, (double) (f11 * 0.2F));
        poseStack.scale(1.0F, 1.0F, 1.0F + f11 * 0.2F);
        poseStack.mulPose(Axis.YP.rotation((float) handSide * -45.0F * (float) Math.PI / 180.0F));
    }
    

    private static void renderNormalItem(AbstractClientPlayer player, ItemStack itemStack, float equippedProg,
                                        float swingProgress, HumanoidArm arm, PoseStack poseStack,
                                        MultiBufferSource buffer, int light, Animations animations) {
        applyItemArmTransform(poseStack, arm, equippedProg);
        applyItemArmAttackTransform(poseStack, arm, swingProgress);
    }

    private static void renderAutoSpinAttack(float equippedProg, HumanoidArm arm, PoseStack poseStack) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        applyItemArmTransform(poseStack, arm, equippedProg);
        poseStack.translate((double) ((float) i * -0.4F), 0.8F, 0.3F);
        poseStack.mulPose(Axis.YP.rotation((float) i * 65.0F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.ZP.rotation((float) i * -85.0F * (float) Math.PI / 180.0F));
    }
    private static void renderPlayerArm(PoseStack poseStack, MultiBufferSource buffer, int light,
                                       float equippedProg, float swingProgress, HumanoidArm arm,
                                       Animations animations) {

    }

    private static void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equippedProg) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((double) ((float) i * 0.56F), (double) (-0.52F), -0.72F);
    }

    private static void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float swingProgress) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
        
        poseStack.translate((double) ((float) i * 0.56F), (double) (-0.52F), -0.72F);
        poseStack.mulPose(Axis.XP.rotation(-102.25F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.YP.rotation((float) i * 13.365F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.ZP.rotation((float) i * 78.05F * (float) Math.PI / 180.0F));
        
        float swingFactor = Mth.clamp(swingProgress, 0.0F, 1.0F);
        poseStack.mulPose(Axis.XP.rotation(f * -15.0F * swingFactor * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.YP.rotation(f1 * -15.0F * swingFactor * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.ZP.rotation(f1 * -70.0F * swingFactor * (float) Math.PI / 180.0F));
    }
    private static void applyEatTransform(PoseStack poseStack, float partialTicks, HumanoidArm arm, ItemStack item) {
        Minecraft mc = Minecraft.getInstance();
        float f = (float) item.getUseDuration() - ((float) mc.player.getUseItemRemainingTicks() - partialTicks + 1.0F);
        float f1 = f / (float) item.getUseDuration();
        
        if (f1 < 0.8F) {
            float f2 = Mth.abs(Mth.cos(f / 4.0F * (float) Math.PI) * 0.1F);
            poseStack.translate(0.0D, (double) f2, 0.0D);
        }
        
        float f3 = 1.0F - (float) Math.pow((double) (1.0F - f1), 27.0D);
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((double) (f3 * 0.6F * (float) i), (double) (f3 * -0.5F), (double) (f3 * 0.0F));
        poseStack.mulPose(Axis.YP.rotation((float) i * f3 * 90.0F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.XP.rotation(f3 * 10.0F * (float) Math.PI / 180.0F));
        poseStack.mulPose(Axis.ZP.rotation((float) i * f3 * 30.0F * (float) Math.PI / 180.0F));
    }

    private static void renderItem(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType,
                                   boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (stack.isEmpty()) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        itemRenderer.renderStatic(entity, stack, transformType, leftHand, poseStack, buffer, entity.level(), light, 0, 0);
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

