package xyz.gay.mixin;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventRenderTabOverlay;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.blinkfix.modules.impl.render.Island;

@Mixin({PlayerTabOverlay.class})
public abstract class MixinPlayerTabOverlay {
   @Shadow
   public abstract Component getNameForDisplay(PlayerInfo var1);
   
   /**
    * 取消 Tab 列表渲染（当 Island 模块启用时）
    * 使用简化的方法匹配，避免签名不兼容问题
    */
   @Inject(
      method = "render",
      at = @At("HEAD"),
      cancellable = true,
      remap = true
   )
   private void hookRenderHead(CallbackInfo ci) {
      try {
         // 检查 Island 模块是否启用
         Island islandModule =
             (Island) BlinkFix.getInstance()
                 .getModuleManager().getModule(Island.class);
         
         // 如果 Island 启用，取消原版 Tab 列表渲染
         if (islandModule != null && islandModule.isEnabled()) {
            ci.cancel();
         }
      } catch (Exception e) {
         // 如果出错，不取消渲染，避免影响游戏运行
      }
   }

   /**
    * Hook Header 渲染
    */
   @Redirect(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/Font;split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;",
         ordinal = 0
      ),
      remap = true
   )
   private List<FormattedCharSequence> hookHeader(Font instance, FormattedText pText, int pMaxWidth) {
      try {
         Component component = (Component)pText;
         EventRenderTabOverlay event = new EventRenderTabOverlay(EventType.HEADER, component, null);
         BlinkFix.getInstance().getEventManager().call(event);
         return instance.split(event.getComponent(), pMaxWidth);
      } catch (Exception e) {
         return instance.split(pText, pMaxWidth);
      }
   }

   /**
    * Hook Footer 渲染
    */
   @Redirect(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/Font;split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;",
         ordinal = 1
      ),
      remap = true
   )
   private List<FormattedCharSequence> hookFooter(Font instance, FormattedText pText, int pMaxWidth) {
      try {
         Component component = (Component)pText;
         EventRenderTabOverlay event = new EventRenderTabOverlay(EventType.FOOTER, component, null);
         BlinkFix.getInstance().getEventManager().call(event);
         return instance.split(event.getComponent(), pMaxWidth);
      } catch (Exception e) {
         return instance.split(pText, pMaxWidth);
      }
   }

   /**
    * Hook 玩家名称显示
    */
   @Redirect(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;getNameForDisplay(Lnet/minecraft/client/multiplayer/PlayerInfo;)Lnet/minecraft/network/chat/Component;"
      ),
      remap = true
   )
   private Component hookName(PlayerTabOverlay instance, PlayerInfo pPlayerInfo) {
      try {
         Component nameForDisplay = this.getNameForDisplay(pPlayerInfo);
         EventRenderTabOverlay event = new EventRenderTabOverlay(EventType.NAME, nameForDisplay, pPlayerInfo);
         BlinkFix.getInstance().getEventManager().call(event);
         return event.getComponent();
      } catch (Exception e) {
         return this.getNameForDisplay(pPlayerInfo);
      }
   }
}
