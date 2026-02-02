package xyz.gay.mixin;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.modules.impl.misc.ContainerStealer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerScreen.class)
public abstract class MixinContainerScreen extends AbstractContainerScreen<ChestMenu> {
    
    @Shadow
    @Final
    private static ResourceLocation CONTAINER_BACKGROUND;
    
    @Shadow
    private int containerRows;
    
    public MixinContainerScreen() {
        super(null, null, null);
    }
    
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRender(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
        ContainerStealer containerStealer = (ContainerStealer) BlinkFix.getInstance().getModuleManager().getModule(ContainerStealer.class);
        
        // 只有当 ContainerStealer 启用且 silent 模式开启时，才取消渲染（隐藏原版箱子界面）
        if (containerStealer != null && containerStealer.isEnabled() && 
            containerStealer.silent.getCurrentValue()) {
            ci.cancel();
            return;
        }
    }
    
    @Inject(method = "renderBg", at = @At("HEAD"), cancellable = true)
    public void onRenderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY, CallbackInfo ci) {
        ContainerStealer containerStealer = (ContainerStealer) BlinkFix.getInstance().getModuleManager().getModule(ContainerStealer.class);
        
        // 只有当 ContainerStealer 启用且 silent 模式开启时，才取消背景渲染
        if (containerStealer != null && containerStealer.isEnabled() && 
            containerStealer.silent.getCurrentValue()) {
            ci.cancel();
        }
    }
}

