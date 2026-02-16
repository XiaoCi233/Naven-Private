package com.surface.value.impl;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.EventTick;
import com.surface.util.struct.HSBData;
import com.surface.value.Value;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class ColorValue extends Value<HSBData> {
    protected boolean alphaChangeable, rainbowChangeable;
    protected boolean enabledRainbow;

    public ColorValue(String name, Color value) {
        super(name, new HSBData(value));
        alphaChangeable = false;
        rainbowChangeable = false;
        enabledRainbow = false;
        Wrapper.Instance.getEventManager().register(this);
    }

    public ColorValue alpha(boolean changeable) {
        this.setAlphaChangeable(changeable);
        return this;
    }

    public ColorValue rainbow(boolean changeable) {
        this.setRainbowChangeable(changeable);
        return this;
    }

    @EventTarget
    public void onLoop(EventTick event) {
        if (isEnabledRainbow() && Minecraft.getMinecraft().theWorld != null) {
            getValue().setHue(getRainbowHue());
        }
    }

    public Integer getRGB() {
        return getValue().getAsColor().getRGB();
    }

    public void setRGB(Integer rgb) {
        setValue(new HSBData(new Color(rgb)));
    }

    private float getRainbowHue() {
        return (System.currentTimeMillis() % (long) ((float) 5 * 1000)) / ((float) 5 * 1000);
    }

    public boolean isAlphaChangeable() {
        return alphaChangeable;
    }

    public void setAlphaChangeable(boolean alphaChangeable) {
        this.alphaChangeable = alphaChangeable;
    }

    public boolean isRainbowChangeable() {
        return rainbowChangeable;
    }

    public void setRainbowChangeable(boolean rainbowChangeable) {
        this.rainbowChangeable = rainbowChangeable;
    }

    public boolean isEnabledRainbow() {
        return enabledRainbow;
    }

    public void setRainbowEnabled(boolean enabledRainbow) {
        if (!rainbowChangeable) this.enabledRainbow = false;
        else this.enabledRainbow = enabledRainbow;
    }

    private float getRainbowHue(float second, long index) {
        return ((System.currentTimeMillis() + index) % (long) (second * 1000)) / (second * 1000);
    }
}
