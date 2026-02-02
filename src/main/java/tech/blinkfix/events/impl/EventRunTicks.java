package tech.blinkfix.events.impl;

import tech.blinkfix.events.api.events.Event;
import tech.blinkfix.events.api.types.EventType;

public class EventRunTicks implements Event {
   private final EventType type;

   public EventType getType() {
      return this.type;
   }

   public EventRunTicks(EventType type) {
      this.type = type;
   }
}
