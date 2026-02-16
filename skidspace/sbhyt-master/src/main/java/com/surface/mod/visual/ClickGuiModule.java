package com.surface.mod.visual;

import com.surface.Wrapper;
import com.surface.mod.Mod;
import org.lwjgl.input.Keyboard;

public class ClickGuiModule extends Mod {

    public ClickGuiModule() {
        super("Click Gui", Category.VISUAL);
        setKeyCode(Keyboard.KEY_RSHIFT);
    }

    @Override
    protected void onEnable() {
        mc.displayGuiScreen(Wrapper.Instance.getClickGui());
        setState(false);
    }
}
