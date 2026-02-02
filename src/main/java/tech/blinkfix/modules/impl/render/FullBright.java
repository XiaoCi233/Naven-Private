package tech.blinkfix.modules.impl.render;

import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.FloatValue;

@ModuleInfo(
   name = "FullBright",
   description = "Make your world brighter.",
   category = Category.RENDER
)
public class FullBright extends Module {
   public FloatValue brightness = ValueBuilder.create(this, "Brightness")
      .setDefaultFloatValue(1.0F)
      .setFloatStep(0.1F)
      .setMinFloatValue(0.0F)
      .setMaxFloatValue(1.0F)
      .build()
      .getFloatValue();
}
