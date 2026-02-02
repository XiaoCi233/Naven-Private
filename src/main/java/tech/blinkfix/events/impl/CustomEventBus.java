package tech.blinkfix.events.impl;

public class CustomEventBus {
    public static final CustomEventBus INSTANCE = new CustomEventBus();
    private final com.google.common.eventbus.EventBus bus = new com.google.common.eventbus.EventBus();

    public void post(Object event) {
        bus.post(event);
    }

    public void register(Object object) {
        bus.register(object);
    }

    public void unregister(Object object) {
        bus.unregister(object);
    }
}
