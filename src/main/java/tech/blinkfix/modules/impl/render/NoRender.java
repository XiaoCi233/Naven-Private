package tech.blinkfix.modules.impl.render;

import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;

@ModuleInfo(
   name = "NoRender",
   description = "Disables rendering",
   category = Category.RENDER
)
public class NoRender extends Module {
   public BooleanValue disableEffects = ValueBuilder.create(this, "Disable Effects").setDefaultBooleanValue(true).build().getBooleanValue();
}
