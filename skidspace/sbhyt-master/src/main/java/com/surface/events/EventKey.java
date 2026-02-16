package com.surface.events;

import com.cubk.event.impl.Event;

public final class EventKey implements Event {
    private final int key;

    public EventKey(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
