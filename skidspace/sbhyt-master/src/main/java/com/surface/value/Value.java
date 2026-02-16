package com.surface.value;

import com.surface.interfaces.Callback;

public abstract class Value<V> {
    private final String valueName;
    private V value;
    private Callback<V> callback;
    public Value(String valueName) {
        this.valueName = valueName;
    }

    public Value(String valueName, V value) {
        this.valueName = valueName;
        this.value = value;
    }

    public final String getValueName() {
        return valueName;
    }

    public V getValue() {
        return value;
    }

    public void setCallback(Callback<V> callback) {
        this.callback = callback;
    }

    private Runnable runnable;

    private V future;

    public void setFuture(V future) {
        this.future = future;
    }

    public V getFuture() {
        return future;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public void setValue(V value) {
        if (onChangeValue(this.value,value)) {
            this.value = value;

            future = value;

            if (runnable != null && callback != null) {
                runnable.run();
                this.value = callback.callback;
            }
        }
    }

    public final void forceSetValue(V value) {
        this.value = value;
    }

    public boolean isVisible() {
        return true;
    }

    protected boolean onChangeValue(V pre, V post) {
        return true;
    }
}
