package com.heypixel.heypixelmod.events.impl;

public class SettingChangeEvent {
    private final Object setting;

    public SettingChangeEvent(Object setting) {
        this.setting = setting;
    }

    public Object getSetting() {
        return setting;
    }
}