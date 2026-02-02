// Decompiled with: CFR 0.152
// Class Version: 17
package tech.blinkfix.modules.impl.render;

import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;

@ModuleInfo(name="LowFire", description="Show the fire lower.", category=Category.RENDER)
public class LowFire
        extends Module {
    public static LowFire instance;

    public LowFire() {
        instance = this;
    }
}
