package com.surface.mod.world;

import com.surface.Wrapper;
import com.surface.mod.Mod;

public class DisablerModule extends Mod {

    public DisablerModule() {
        super("Disabler", Category.WORLD);
    }

    public static boolean getGrimPost() {
        return Wrapper.Instance.getModManager().getModFromName("Disabler").isEnable();
    }

    @Override
    public String getModTag() {
        return "Post";
    }

}