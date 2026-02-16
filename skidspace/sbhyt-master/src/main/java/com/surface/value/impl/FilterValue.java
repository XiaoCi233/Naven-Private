package com.surface.value.impl;

import com.surface.value.Value;

import java.util.Objects;

public abstract class FilterValue<T> extends Value<BooleanValue[]> {
    public FilterValue(String valueName, BooleanValue... value) {
        super(valueName, value);
    }

    public final BooleanValue find(String name) {
        for (BooleanValue booleanValue : getValue()) {
            if (booleanValue.getValueName().equals(name)) return booleanValue;
        }

        return null;
    }

    public final boolean findValue(String name) {
        return Objects.requireNonNull(find(name)).getValue();
    }

    public final BooleanValue find(int index) {
        return getValue()[index];
    }

    public final boolean findValue(int index) {
        return getValue()[index].getValue();
    }

    public abstract boolean isValid(T e);
}
