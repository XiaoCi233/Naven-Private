// Decompiled with: CFR 0.152
// Class Version: 17
package com.heypixel.heypixelmod.modules.impl.render;

import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;

@ModuleInfo(name="LowFire", description="Show the fire lower.", category=Category.RENDER)
public class LowFire
        extends Module {
    public static LowFire instance;

    public LowFire() {
        instance = this;
    }
}
