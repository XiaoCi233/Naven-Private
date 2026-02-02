package xyz.gay.mixin;

import tech.blinkfix.utils.localization.GUITranslationManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void onRenderBackground(GuiGraphics graphics, CallbackInfo ci) {
        // 这里可以处理屏幕标题翻译
        Screen screen = (Screen) (Object) this;
        Component title = screen.getTitle();
        if (title != null) {
            Component translatedTitle = GUITranslationManager.translateComponent(title);
            if (!translatedTitle.equals(title)) {
                // 使用反射或其他方式设置翻译后的标题
                try {
                    java.lang.reflect.Field titleField = Screen.class.getDeclaredField("title");
                    titleField.setAccessible(true);
                    titleField.set(screen, translatedTitle);
                } catch (Exception e) {
                    // 忽略错误
                }
            }
        }
    }
}