package com.surface.events;

import com.cubk.event.impl.Event;

public final class Event3D implements Event {
    private final float partialTicks;

    public Event3D(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
}
