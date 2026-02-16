package com.surface.interfaces;

import com.surface.mod.Mod;
import net.minecraft.util.EnumChatFormatting;

public interface ModuleFormatter {
    default String formatModule(Mod module) {
        return module.getName() + (module.getModTag() == null ? "" : " " + EnumChatFormatting.GRAY + module.getModTag());
    }

}
