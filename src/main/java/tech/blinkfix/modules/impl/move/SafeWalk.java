package tech.blinkfix.modules.impl.move;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import com.mojang.blaze3d.platform.InputConstants;

@ModuleInfo(
   name = "SafeWalk",
   description = "Prevents you from falling off blocks",
   category = Category.MOVEMENT
)
public class SafeWalk extends Module {
   public static boolean isOnBlockEdge(float sensitivity) {
      return !mc.level
         .getCollisions(mc.player, mc.player.getBoundingBox().move(0.0, -0.5, 0.0).inflate((double)(-sensitivity), 0.0, (double)(-sensitivity)))
         .iterator()
         .hasNext();
   }

    @EventTarget
    public void onEnable() {
        super.onEnable();
    }

   @EventTarget
   public void onMotion(EventMotion e) {
      if (e.getType() == EventType.PRE) {
         mc.options.keyShift.setDown(mc.player.onGround() && isOnBlockEdge(0.3F));
      }
   }

   @Override
   public void onDisable() {
      boolean isHoldingShift = InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyShift.getKey().getValue());
      mc.options.keyShift.setDown(isHoldingShift);
   }
}
