package tech.blinkfix.modules.impl.render;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventRenderTabOverlay;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.files.impl.NameProtectFile;
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
        this.customName = NameProtectFile.customName;
    }

    public void setCustomName(String name) {
        this.customName = name;
        NameProtectFile.customName = name;
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