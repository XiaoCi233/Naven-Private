package org.msgpack.mixin;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.EventClick;
import com.heypixel.heypixelmod.events.impl.EventRunTicks;
import com.heypixel.heypixelmod.events.impl.EventShutdown;
import com.heypixel.heypixelmod.modules.impl.render.Glow;
import com.heypixel.heypixelmod.utils.AnimationUtils;
import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.client.RealmsClient;
import dev.yalan.live.LiveClient;
import dev.yalan.live.gui.LiveAuthenticationScreen;
import dev.yalan.live.netty.LiveProto;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Minecraft.class})
public abstract class MixinMinecraft {
    @Inject(
            method = "createTitle",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCreateTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("BlinkFix-NextGeneration 251007-1358 | ife has dreams, and each is wonderful in its own way.");
    }
    @Shadow
    public abstract void setScreen(@Nullable Screen p_91153_);

    @Unique
    private int skipTicks;
    @Unique
    private long blinkfix_NextGeneration$lastFrame;

    /**
     * @author Yalan
     * @reason Force to authentication
     */
    @Overwrite
    private void setInitialScreen(RealmsClient p_279285_, ReloadInstance p_279164_, GameConfig.QuickPlayData p_279146_) {
        setScreen(new LiveAuthenticationScreen());
    }

    @Inject(
            method = {"<init>"},
            at = {@At("TAIL")}
    )
    private void onInit(CallbackInfo info) {
        com.heypixel.heypixelmod.BlinkFix.modRegister();
    }

    @Inject(
            method = {"<init>"},
            at = {@At("RETURN")}
    )
    public void onInit(GameConfig pGameConfig, CallbackInfo ci) {
        System.setProperty("java.awt.headless", "false");
        ModList.get().getMods().removeIf(modInfox -> modInfox.getModId().contains("blinkfix"));
        List<IModFileInfo> fileInfoToRemove = new ArrayList<>();

        for (IModFileInfo fileInfo : ModList.get().getModFiles()) {
            for (IModInfo modInfo : fileInfo.getMods()) {
                if (modInfo.getModId().contains("blinkfix")) {
                    fileInfoToRemove.add(fileInfo);
                }
            }
        }

        ModList.get().getModFiles().removeAll(fileInfoToRemove);
    }

    @Inject(
            method = {"close"},
            at = {@At("HEAD")},
            remap = false
    )
    private void shutdown(CallbackInfo ci) {
        if (com.heypixel.heypixelmod.BlinkFix.getInstance() != null && com.heypixel.heypixelmod.BlinkFix.getInstance().getEventManager() != null) {
            com.heypixel.heypixelmod.BlinkFix.getInstance().getEventManager().call(new EventShutdown());
        }
    }

    @Inject(
            method = {"tick"},
            at = {@At("HEAD")}
    )
    private void tickPre(CallbackInfo ci) {
        if (BlinkFix.getInstance() != null && com.heypixel.heypixelmod.BlinkFix.getInstance().getEventManager() != null) {
            com.heypixel.heypixelmod.BlinkFix.getInstance().getEventManager().call(new EventRunTicks(EventType.PRE));
        }
    }

    @Inject(
            method = {"tick"},
            at = {@At("TAIL")}
    )
    private void tickPost(CallbackInfo ci) {
        if (com.heypixel.heypixelmod.BlinkFix.getInstance() != null && com.heypixel.heypixelmod.BlinkFix.getInstance().getEventManager() != null) {
            com.heypixel.heypixelmod.BlinkFix.getInstance().getEventManager().call(new EventRunTicks(EventType.POST));
        }
    }

    @Inject(
            method = {"shouldEntityAppearGlowing"},
            at = {@At("RETURN")},
            cancellable = true
    )
    private void shouldEntityAppearGlowing(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (Glow.shouldGlow(pEntity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = {"runTick"},
            at = {@At("HEAD")}
    )
    private void runTick(CallbackInfo ci) {
        long currentTime = System.nanoTime() / 1000000L;
        int deltaTime = (int)(currentTime - this.blinkfix_NextGeneration$lastFrame);
        this.blinkfix_NextGeneration$lastFrame = currentTime;
        AnimationUtils.delta = deltaTime;
    }

    @ModifyArg(
            method = {"runTick"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"
            )
    )
    private float fixSkipTicks(float g) {
        if (this.skipTicks > 0) {
            g = 0.0F;
        }

        return g;
    }

    @Inject(
            method = {"handleKeybinds"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
                    ordinal = 0,
                    shift = Shift.BEFORE
            )},
            cancellable = true
    )
    private void clickEvent(CallbackInfo ci) {
        if (com.heypixel.heypixelmod.BlinkFix.getInstance() != null && com.heypixel.heypixelmod.BlinkFix.getInstance().getEventManager() != null) {
            EventClick event = new EventClick();
            com.heypixel.heypixelmod.BlinkFix.getInstance().getEventManager().call(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    public void setBlinkfix_NextGeneration$lastFrame(long blinkfix_NextGeneration$lastFrame) {
        this.blinkfix_NextGeneration$lastFrame = blinkfix_NextGeneration$lastFrame;
    }
}
