package tech.blinkfix.modules.impl.move;

import xyz.gay.mixin.accessors.LivingEntityAccessor;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;

@ModuleInfo(
   name = "NoJumpDelay",
   description = "Removes the delay when jumping",
   category = Category.MOVEMENT
)
public class NoJumpDelay extends Module {
    @EventTarget
    public void onEnable() {
        super.onEnable();
    }

    @EventTarget
   public void onMotion(EventMotion e) {
      if (e.getType() == EventType.PRE) {
         ((LivingEntityAccessor)mc.player).setNoJumpDelay(0);
      }
   }
}
