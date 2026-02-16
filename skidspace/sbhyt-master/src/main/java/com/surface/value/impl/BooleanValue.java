package com.surface.value.impl;

import com.surface.value.Value;

public class BooleanValue extends Value<Boolean> {
    public BooleanValue(String valueName, Boolean defaultValue) {
        super(valueName, defaultValue);
    }

    public final boolean getReversedValue() {
        return !getValue();
    }
}
