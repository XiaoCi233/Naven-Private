package org.msgpack.mixin;

import com.heypixel.heypixelmod.utils.localization.GUITranslationManager;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWidget.class)
public class AbstractWidgetMixin {

    @Inject(method = "setMessage", at = @At("HEAD"), cancellable = true)
    private void onSetMessage(Component message, CallbackInfo ci) {
        Component translated = GUITranslationManager.translateComponent(message);
        if (!translated.equals(message)) {
            AbstractWidget widget = (AbstractWidget) (Object) this;
            widget.setMessage(translated);
            ci.cancel();
        }
    }
}