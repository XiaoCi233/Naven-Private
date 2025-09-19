// NoGuiCloseMixin.java
package org.msgpack.mixin;

import com.heypixel.heypixelmod.modules.impl.misc.NoGuiClose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class NoGuiCloseMixin {

    @Inject(method = "handleContainerClose", at = @At("HEAD"), cancellable = true)
    private void onHandleContainerClose(ClientboundContainerClosePacket packet, CallbackInfo ci) {
        NoGuiClose module = NoGuiClose.getInstance();

        if (module != null && module.isEnabled() &&
                (Minecraft.getInstance().screen instanceof ChatScreen || !module.getChatonly().getCurrentValue())) {
            ci.cancel();
        }
    }
}