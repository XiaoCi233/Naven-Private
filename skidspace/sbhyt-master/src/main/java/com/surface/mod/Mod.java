package com.surface.mod;

import com.surface.Wrapper;
import com.surface.interfaces.Callback;
import com.surface.render.font.FontManager;
import com.surface.render.font.truetype.TrueTypeFontDrawer;
import com.surface.render.notification.Notification;
import com.surface.render.notification.NotificationType;
import com.surface.value.Value;
import com.surface.value.impl.ModeValue;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Mod {
    protected final Minecraft mc = Minecraft.getMinecraft();

    private final ArrayList<Value<?>> values = new ArrayList<>();
    private final String name;
    private final Category category;

    private boolean state = false;
    private boolean hideState = false;
    private int keyCode = Keyboard.KEY_NONE;

    private float arrayX, arrayY;

    private final List<SubMod<?>> subMods = new ArrayList<>();
    private SubMod<?> subMod;
    public ModeValue subMode;

    public Mod(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public boolean isSub(SubMod<? extends Mod> subMod) {
        return subMod.getName().equals(this.subMod.getName());
    }

    public final ArrayList<Value<?>> getValues() {
        return values;
    }

    public final void registerValues(Value<?>... values) {
        this.values.addAll(Arrays.asList(values));
    }

    public final String getName() {
        return name;
    }

    public final Category getCategory() {
        return category;
    }

    public final boolean getState() {
        return state;
    }

    public final boolean isEnable() {
        return state;
    }

    public final boolean isDisable() {
        return !state;
    }

    public final void toggle() {
        setState(!state);
    }

    public final void setStateNoNotification(boolean state) {
        setState(state, false);
    }
    public final void setState(boolean state) {
        setState(state, true);
    }

    public final void setState(boolean state, boolean notification) {
        this.state = state;

        if (subMod != null) {
            subMod.setEnabled(state);
        }

        if (state) {
            setArrayY(getHeight(this) - 1);
            Wrapper.Instance.getEventManager().register(this);
            onEnable();
        } else {
            Wrapper.Instance.getEventManager().unregister(this);
            onDisable();
        }

        if (mc.currentScreen == null && notification) {
            Wrapper.Instance.getNotificationManager().pop(new Notification("Module", getName() + " has been " + (state ? "enabled" : "disabled") + ".", NotificationType.INFO, 3000));
        }
    }

    protected float getHeight(Mod module) {
        final TrueTypeFontDrawer font = FontManager.TAHOMA;

        font.setFontSize(16);

        float y = 2;
        for (Mod abstractModule : Wrapper.Instance.getModManager().getModsSorted()) {
            if (abstractModule == module) {
                return y;
            }
            y += font.getHeight() + 1;
        }
        return 2;
    }

    public final boolean isHide() {
        return hideState;
    }

    public final void setHide(boolean hideState) {
        this.hideState = hideState;
    }

    public final int getKeyCode() {
        return keyCode;
    }

    public final void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public String getModTag() {

        if (subMod != null)
            return subMod.getName();

        return null;
    }

    protected void onEnable() {}

    protected void onDisable() {}

    public void onPostInit() {
    }

    public Value<?> getValue(String key) {
        for (Value<?> value : getValues())
            if (value.getValueName().equals(key))
                return value;
        return null;
    }

    public enum Category {
        FIGHT("Fight"),
        VISUAL("Visual"),
        MOVE("Move"),
        PLAYER("Player"),
        WORLD("World");

        private final String name;

        Category(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public float getArrayY() {
        return arrayY;
    }

    public float getArrayX() {
        return arrayX;
    }

    public void setArrayX(float arrayX) {
        this.arrayX = arrayX;
    }

    public void setArrayY(float arrayY) {
        this.arrayY = arrayY;
    }

    public void regitserSubModules(SubMod<?>... subs) {
        subMods.addAll(Arrays.asList(subs));
        if (subMode == null) {
            final List<String> modes = new ArrayList<>();
            for (SubMod<?> sub : subs) {
                modes.add(sub.getName());
            }
            subMode = new ModeValue("Mode", subs[0].getName(), modes.toArray(new String[0]));
            final Callback<String> callback = new Callback<>();
            subMode.setRunnable(() -> {
                if (isEnable()) {
                    subMod.toggle();
                    callback.callback = subMode.getFuture();
                    updateSub(subMode.getFuture());
                    subMod.toggle();
                } else {
                    callback.callback = subMode.getFuture();
                    updateSub(subMode.getFuture());
                }
            });
            subMode.setCallback(callback);
            values.add(0, subMode);
            updateSub();
        }
    }

    private void updateSub() {
        subMods.forEach(sub -> {
            if (sub.getName().equals(subMode.getValue())) subMod = sub;
        });
    }

    private void updateSub(String name) {
        subMods.forEach(sub -> {
            if (sub.getName().equals(name)) subMod = sub;
        });
    }

}
