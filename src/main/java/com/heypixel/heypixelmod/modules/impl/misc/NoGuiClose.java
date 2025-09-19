// NoGuiClose.java
package com.heypixel.heypixelmod.modules.impl.misc;

import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;

@ModuleInfo(name = "NoGuiClose", category = Category.MISC, description = "module.other.noguiclose.description")
public final class NoGuiClose extends Module {
    private static NoGuiClose instance;

    private final BooleanValue chatonly = ValueBuilder.create(this, "ChatOnly")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public NoGuiClose() {
        instance = this;
    }

    public static NoGuiClose getInstance() {
        return instance;
    }

    public BooleanValue getChatonly() {
        return chatonly;
    }
}