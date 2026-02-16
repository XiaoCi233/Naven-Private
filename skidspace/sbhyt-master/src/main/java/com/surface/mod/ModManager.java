package com.surface.mod;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.EventKey;
import com.surface.interfaces.ModuleFormatter;
import com.surface.mod.visual.InterfaceModule;
import com.surface.render.font.FontManager;
import org.lwjgl.input.Keyboard;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.compare;

public final class ModManager implements ModuleFormatter {
    private final Map<String, Mod> mods = new LinkedHashMap<>();

    public ModManager() {
        Wrapper.Instance.getEventManager().register(this);
    }

    public List<Mod> getModsSorted() {
        Stream<Mod> stream = Wrapper.Instance.getModManager().getMods().stream();

        FontManager.TAHOMA.setFontSize(16);

        stream = stream.sorted((mod1, mod2) -> compare(InterfaceModule.fontDrawer.getStringWidth2(formatModule(mod2)),
                InterfaceModule.fontDrawer.getStringWidth2(formatModule(mod1))));
        return stream.filter(m -> !m.isHide()).collect(Collectors.toList());
    }

    public void register(Mod... modules) {
        for (Mod mod : modules)
            mods.put(mod.getName(), mod);
    }

    public Mod getModFromName(String name) {
        return mods.get(name);
    }

    public Mod findModule(String name) {
        for (final Mod m : mods.values()) {
            if (m.getName().replace(" ", "").equalsIgnoreCase(name.replace(" ", ""))) {
                return m;
            }
        }
        return null;
    }


    public Collection<Mod> getMods() {
        return mods.values();
    }

    public ArrayList<Mod> getModsInCategory(Mod.Category category) {
        ArrayList<Mod> modsList = new ArrayList<>();
        for (Mod module : getMods()) {
            if (module.getCategory() == category) {
                modsList.add(module);
            }
        }
        return modsList;
    }

    @EventTarget
    public void onKey(EventKey event) {
        if (event.getKey() != Keyboard.KEY_NONE) {
            for (final Mod mod : mods.values()) {
                if (mod.getKeyCode() == event.getKey()) mod.toggle();
            }
        }
    }
}
