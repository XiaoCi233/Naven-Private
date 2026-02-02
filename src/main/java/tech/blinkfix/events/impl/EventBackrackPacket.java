package tech.blinkfix.events.impl;

import tech.blinkfix.events.api.events.callables.EventCancellable;
import net.minecraft.network.protocol.Packet;

public class EventBackrackPacket extends EventCancellable {
    private Packet<?> packet;

    public EventBackrackPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }
}

