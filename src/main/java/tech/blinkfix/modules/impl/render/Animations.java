package tech.blinkfix.modules.impl.render;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.events.impl.EventPacket;
import tech.blinkfix.events.impl.EventRender;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.impl.combat.Aura;
import tech.blinkfix.modules.impl.move.Scaffold;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.values.impl.ModeValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.client.ForgeHooksClient;

@ModuleInfo(name = "Animations", description = "Customizes item animations and block animations", category = Category.RENDER)
public class Animations extends Module {
    public BooleanValue onlyKillAura = ValueBuilder.create(this, "Only KillAura").setDefaultBooleanValue(false).build().getBooleanValue();
    public final ModeValue BlockMods = ValueBuilder.create(this, "Block Mods")
            .setModes("None", "1.7", "Push")
            .setDefaultModeIndex(1)
            .build()
            .getModeValue();

    public final BooleanValue BlockOnlySword = ValueBuilder.create(this, "Block Only Sword")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final BooleanValue KillauraAutoBlock = ValueBuilder.create(this, "Killaura Auto Block")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final BooleanValue OverrideVanilla = ValueBuilder.create(this, "Override Vanilla")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final BooleanValue ShowHUDItem = ValueBuilder.create(this, "Show HUD Item")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final BooleanValue RenderOffhandShield = ValueBuilder.create(this, "Render Offhand Shield")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public final FloatValue BlockingX = ValueBuilder.create(this, "Blocking-X")
            .setDefaultFloatValue(0.56F)
            .setMinFloatValue(-2.0F)
            .setMaxFloatValue(2.0F)
            .setFloatStep(0.01F)
            .build()
            .getFloatValue();

    public final FloatValue BlockingY = ValueBuilder.create(this, "Blocking-Y")
            .setDefaultFloatValue(-0.52F)
            .setMinFloatValue(-2.0F)
            .setMaxFloatValue(2.0F)
            .setFloatStep(0.01F)
            .build()
            .getFloatValue();

    private boolean flip;
    public static boolean isBlocking = false;
    private final Minecraft mc = Minecraft.getInstance();
    private float mainHandHeight = 0.0F;
    private float offHandHeight = 0.0F;
    private float oMainHandHeight = 0.0F;
    private float oOffHandHeight = 0.0F;
    private ItemStack mainHandItem = ItemStack.EMPTY;
    private ItemStack offHandItem = ItemStack.EMPTY;
    private boolean isScaffoldEnabled() {
        Scaffold scaffold = (Scaffold) BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class);
        return scaffold != null && scaffold.isEnabled();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        // Mixin 现在处理渲染，不再需要注册 Forge 事件
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getType() == EventType.SEND && event.getPacket() instanceof ServerboundSwingPacket) {
            flip = !flip;
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (event.getType() != EventType.PRE || mc.player == null)
            return;

        updateHandStates();
    }

    private void updateHandStates() {
        oMainHandHeight = mainHandHeight;
        oOffHandHeight = offHandHeight;

        LocalPlayer localplayer = mc.player;
        ItemStack itemstack = localplayer.getMainHandItem();
        ItemStack itemstack1 = localplayer.getOffhandItem();
        boolean isBlocking = isBlocking();

        if (isBlocking) {
            mainHandHeight = 1.0F;
            if (ItemStack.matches(mainHandItem, itemstack)) {
                mainHandItem = itemstack;
            }
            if (ItemStack.matches(offHandItem, itemstack1)) {
                offHandItem = itemstack1;
            }
            return;
        }

        if (localplayer.isHandsBusy()) {
            mainHandHeight = Mth.clamp(mainHandHeight - 0.4F, 0.0F, 1.0F);
            offHandHeight = Mth.clamp(offHandHeight - 0.4F, 0.0F, 1.0F);
        } else {
            float f = localplayer.getAttackStrengthScale(1.0F);
            boolean flag = ForgeHooksClient.shouldCauseReequipAnimation(mainHandItem, itemstack,
                    localplayer.getInventory().selected);
            boolean flag1 = ForgeHooksClient.shouldCauseReequipAnimation(offHandItem, itemstack1, -1);

            if (!flag && mainHandItem != itemstack) {
                mainHandItem = itemstack;
            }

            if (!flag1 && offHandItem != itemstack1) {
                offHandItem = itemstack1;
            }
            float targetMainHeight = !flag ? f * f * f : 0.0F;
            float targetOffHeight = !flag1 ? 1.0F : 0.0F;

            mainHandHeight += Mth.clamp(targetMainHeight - mainHandHeight, -0.2F, 0.2F);
            offHandHeight += Mth.clamp(targetOffHeight - offHandHeight, -0.2F, 0.2F);
        }

        if (mainHandHeight < 0.1F) {
            mainHandItem = itemstack;
        }

        if (offHandHeight < 0.1F) {
            offHandItem = itemstack1;
        }
    }
    private boolean isBlocking() {
        if (isScaffoldEnabled()) {
            return false;
        }

        if (!this.isEnabled() || BlockMods.getCurrentMode().equals("None"))
            return false;

        LocalPlayer player = mc.player;
        if (player == null)
            return false;

        ItemStack mainHandItem = player.getMainHandItem();
        if (BlockOnlySword.getCurrentValue() && !(mainHandItem.getItem() instanceof SwordItem))
            return false;

        boolean isOffhandUsing = false;
        if (player.isUsingItem() && player.getUsedItemHand() == InteractionHand.OFF_HAND) {
            ItemStack offhandItem = player.getOffhandItem();
            UseAnim useAnim = offhandItem.getUseAnimation();
            if (useAnim != UseAnim.BLOCK) {
                isOffhandUsing = true;
            }
        }

        boolean isKillauraBlocking = KillauraAutoBlock.getCurrentValue() && getAuraTarget() != null;

        if (onlyKillAura.getCurrentValue()) {
            return isKillauraBlocking;
        }

        if (isKillauraBlocking) {
            return true;
        }

        if (isOffhandUsing) {
            return false;
        }

        return mc.options.keyUse.isDown();
    }

    @EventTarget
    public void onRender(EventRender event) {
        if (isScaffoldEnabled()) {
            return;
        }

        if (mc.player == null || mc.level == null)
            return;

        if (ShowHUDItem.getCurrentValue()) {
            renderHUDItem(event);
        }
    }

    private void renderHUDItem(EventRender event) {
        ItemStack mainHandItem = mc.player.getMainHandItem();
        if (mainHandItem.isEmpty())
            return;

        PoseStack poseStack = new PoseStack();
        MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
        float partialTicks = mc.getFrameTime();
        int packedLight = 15728880;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float itemX = screenWidth - 100;
        float itemY = screenHeight - 100;

        poseStack.translate(itemX, itemY, 0);

        float swingProgress = mc.player.getAttackAnim(partialTicks);
        if (swingProgress > 0) {
            float swingAngle = Mth.sin(swingProgress * swingProgress * (float) Math.PI) * 10.0F;
            poseStack.mulPose(Axis.ZP.rotationDegrees(swingAngle));
        }

        float scale = 1.5F;
        poseStack.scale(scale, scale, scale);

        renderItem(mc.player, mainHandItem, ItemDisplayContext.GUI, false, poseStack, bufferSource, packedLight);
    }

    // 渲染逻辑已移至 SwordAnimationRenderer 和 MixinItemInHandRenderer

    private LivingEntity getAuraTarget() {
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

    private boolean getTargetHudEnabled() {
        Aura aura = (Aura) BlinkFix.getInstance().getModuleManager().getModule(Aura.class);
        if (aura != null && aura.isEnabled()) {
            try {
                java.lang.reflect.Field targetHudField = Aura.class.getDeclaredField("targetHud");
                targetHudField.setAccessible(true);
                Object targetHudValue = targetHudField.get(aura);
                if (targetHudValue != null) {
                    java.lang.reflect.Method getCurrentValueMethod = targetHudValue.getClass().getMethod("getCurrentValue");
                    return (Boolean) getCurrentValueMethod.invoke(targetHudValue);
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    // 所有渲染辅助方法已移至 SwordAnimationRenderer

    private void renderItem(LivingEntity entity, ItemStack stack,
                            ItemDisplayContext transformType, boolean leftHand,
                            PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (stack.isEmpty())
            return;
        ItemRenderer itemRenderer = mc.getItemRenderer();
        itemRenderer.renderStatic(entity, stack, transformType, leftHand, poseStack, buffer, entity.level(), light, 0,
                0);
    }
}