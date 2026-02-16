package com.surface.mod.visual;

import com.surface.mod.Mod;
import com.surface.value.impl.ModeValue;

public class BlockAnimationsModule extends Mod {

    public BlockAnimationsModule() {
        super("Block Animation", Category.VISUAL);
        registerValues(mode);
    }

    public final ModeValue mode = new ModeValue("Mode", "1.7", new String[]{"1.7", "Swank", "Sigma"});

    @Override
    public String getModTag() {
        return mode.getValue();
    }

}
