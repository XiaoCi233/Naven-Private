package org.msgpack.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(
            method = "createTitle",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCreateTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("BlinkFix-NextGeneration-250919 | ife has dreams, and each is wonderful in its own way.");
    }
}