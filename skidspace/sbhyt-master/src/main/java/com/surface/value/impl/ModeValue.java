package com.surface.value.impl;

import com.surface.value.Value;

public class ModeValue extends Value<String> {
    private final String[] modes;

    public ModeValue(String valueName, String defaultValue, String[] modes) {
        super(valueName, defaultValue);

        this.modes = modes;
    }

    public final String[] getModes() {
        return modes;
    }

    public boolean isCurrentMode(String value) {
        return getValue().equals(value);
    }

    public boolean isNotCurrentMode(String value) {
        return !getValue().equals(value);
    }
}
