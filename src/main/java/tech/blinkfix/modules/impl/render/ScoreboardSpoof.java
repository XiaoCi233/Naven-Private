package tech.blinkfix.modules.impl.render;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventRenderScoreboard;
import tech.blinkfix.events.impl.EventRenderTabOverlay;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@ModuleInfo(
   name = "ScoreboardSpoof",
   description = "Spoof the server name",
   category = Category.RENDER
)
public class ScoreboardSpoof extends Module {
   @EventTarget
   public void onRenderScoreboard(EventRenderScoreboard e) {
      String string = e.getComponent().getString();
      if (string.contains("布吉岛")) {
         MutableComponent textComponent = Component.literal("§d§lBlinkFix Island");
         textComponent.setStyle(e.getComponent().getStyle());
         e.setComponent(textComponent);
      }
   }

   @EventTarget
   public void onRenderTab(EventRenderTabOverlay e) {
      String string = e.getComponent().getString();
      if (string.contains("布吉岛")) {
         if (e.getType() == EventType.HEADER) {
            e.setComponent(Component.literal("§d§lBlinkFix Island"));
         } else if (e.getType() == EventType.FOOTER) {
            e.setComponent(Component.literal(""));
         }
      }
   }
}
