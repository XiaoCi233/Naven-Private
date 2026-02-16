package com.surface.events;

import com.cubk.event.impl.Event;
import net.minecraft.client.gui.ScaledResolution;

public final class Event2D implements Event {
    private final ScaledResolution scaledResolution;
    private final float partialTicks;

    public Event2D(ScaledResolution scaledResolution, float partialTicks) {
        this.scaledResolution = scaledResolution;
        this.partialTicks = partialTicks;
    }

    public ScaledResolution getScaledResolution() {
        return scaledResolution;
    }

    public int getWidth() {
        return scaledResolution.getScaledWidth();
    }

    public int getHeight() {
        return scaledResolution.getScaledHeight();
    }

    public double getDWidth() {
        return scaledResolution.getScaledWidth_double();
    }

    public double getDHeight() {
        return scaledResolution.getScaledHeight_double();
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
