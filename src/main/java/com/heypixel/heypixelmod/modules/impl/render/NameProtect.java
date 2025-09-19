package com.heypixel.heypixelmod.modules.impl.render;

import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.impl.EventRenderTabOverlay;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.network.chat.Component;

@ModuleInfo(
        name = "NameProtect",
        description = "You can .setname Protect your name",
        category = Category.RENDER
)
public class NameProtect extends Module {
    public static NameProtect instance;
    private String customName = "§d塞西莉亚宝宝§7";
    public NameProtect() {
        instance = this;
    }
    public void setCustomName(String name) {
        this.customName = name;
    }
    public static String getName(String string) {
        if (!instance.isEnabled() || mc.player == null) {
            return string;
        } else {
            return string.contains(mc.player.getName().getString()) ?
                    StringUtils.replace(string, mc.player.getName().getString(), "§d"+ instance.customName +"§7") : string;
        }
    }

    @EventTarget
    public void onRenderTab(EventRenderTabOverlay e) {
        e.setComponent(Component.literal(getName(e.getComponent().getString())));
    }
}