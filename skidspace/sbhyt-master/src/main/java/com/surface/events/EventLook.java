package com.surface.events;

import com.cubk.event.impl.Event;
import com.surface.util.struct.Rotation;

public class EventLook implements Event {

    private Rotation rotation;

    public EventLook(Rotation rotation) {
        this.rotation = rotation;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }
}
