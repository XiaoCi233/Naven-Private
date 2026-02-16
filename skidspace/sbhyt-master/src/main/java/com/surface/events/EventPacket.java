package com.surface.events;

import com.cubk.event.impl.CancellableEvent;
import net.minecraft.network.Packet;

public final class EventPacket extends CancellableEvent {
    public static final int MODE_SEND = 0;
    public static final int MODE_RECEIVE = 1;

    private final int mode;
    private final Packet<?> packet;

    public EventPacket(Packet<?> packet, int mode) {
        this.packet = packet;
        this.mode = mode;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public boolean isSendMode() {
        return mode == MODE_SEND;
    }

    public boolean isReceiveMode() {
        return mode == MODE_RECEIVE;
    }
}
