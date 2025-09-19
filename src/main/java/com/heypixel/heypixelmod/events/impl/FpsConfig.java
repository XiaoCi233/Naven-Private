package com.heypixel.heypixelmod.events.impl;

public class FpsConfig {
    private static boolean fakeFpsEnabled = true;
    private static float fakeFpsOffset = 200.0f;

    public static void setFakeFps(boolean enabled, float offset) {
        fakeFpsEnabled = enabled;
        fakeFpsOffset = offset;
    }

    public static boolean isFakeFpsEnabled() {
        return fakeFpsEnabled;
    }

    public static float getFakeFpsOffset() {
        return fakeFpsOffset;
    }

    public static int getDisplayedFps(int actualFps) {
        if (fakeFpsEnabled) {
            return actualFps + (int) fakeFpsOffset;
        }
        return actualFps;
    }
}