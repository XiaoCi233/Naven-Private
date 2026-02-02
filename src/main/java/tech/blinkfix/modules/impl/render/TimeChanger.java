package tech.blinkfix.modules.impl.render;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.events.impl.EventPacket;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.FloatValue;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
@ModuleInfo(
   name = "TimeChanger",
   description = "Change the time of the world",
   category = Category.RENDER
)
public class TimeChanger extends Module {
   FloatValue time = ValueBuilder.create(this, "World Time")
      .setDefaultFloatValue(8000.0F)
      .setFloatStep(1.0F)
      .setMinFloatValue(0.0F)
      .setMaxFloatValue(24000.0F)
      .build()
      .getFloatValue();

   @EventTarget
   public void onMotion(EventMotion e) {
      if (e.getType() == EventType.PRE) {
         mc.level.setDayTime((long)this.time.getCurrentValue());
      }
   }

   @EventTarget
   public void onPacket(EventPacket event) {
      if (event.getPacket() instanceof ClientboundSetTimePacket) {
         event.setCancelled(true);
      }
   }
}
