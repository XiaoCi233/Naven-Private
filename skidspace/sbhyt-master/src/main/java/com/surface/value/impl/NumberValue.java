package com.surface.value.impl;

import com.surface.value.Value;

public class NumberValue extends Value<Double> {
    private final double maxValue;
    private final double minValue;
    private final double increase;

    public NumberValue(String valueName, double defaultValue, double min, double max, double increase) {
        super(valueName, defaultValue);

        this.minValue = min;
        this.maxValue = max;
        this.increase = increase;
    }

    public final int getIntValue() {
        return getValue().intValue();
    }

    public final float getFloatValue() {
        return getValue().floatValue();
    }

    public final boolean isZero() {
        return getValue() == 0.0;
    }

    public final boolean isNotZero() {
        return getValue() != 0.0;
    }

    public final boolean isMax() {
        return getValue() == maxValue;
    }

    public final boolean isMin() {
        return getValue() == minValue;
    }

    public final double getMaxValue() {
        return maxValue;
    }

    public final double getMinValue() {
        return minValue;
    }

    public final double getIncrease() {
        return increase;
    }
}
