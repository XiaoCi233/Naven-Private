package com.heypixel.heypixelmod.modules.impl.render;

import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;

@ModuleInfo(
   name = "NoRender",
   description = "Disables rendering",
   category = Category.RENDER
)
public class NoRender extends Module {
   public BooleanValue disableEffects = ValueBuilder.create(this, "Disable Effects").setDefaultBooleanValue(true).build().getBooleanValue();
}
