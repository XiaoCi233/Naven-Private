package tech.blinkfix.modules.impl.move;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;

@ModuleInfo(
   name = "Sprint",
   description = "Automatically sprints",
   category = Category.MOVEMENT
)
public class Sprint extends Module {

    @EventTarget
    public void onEnable() {
        super.onEnable();
    }
    @EventTarget(0)
   public void onMotion(EventMotion e) {
      if (e.getType() == EventType.PRE) {
         mc.options.keySprint.setDown(true);
         mc.options.toggleSprint().set(false);
      }
   }

   @Override
   public void onDisable() {
      mc.options.keySprint.setDown(false);
   }
}
