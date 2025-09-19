package com.heypixel.heypixelmod.utils;

import java.util.ArrayList;
import java.util.List;

public class TickTimeHelper {

    private static final List<TickTimeHelper> timers = new ArrayList<>();
    private long lastMS;
    public static void update() {
    }
    public TickTimeHelper() {
        // 默认记录一次当前时间
        timers.add(this);
        this.lastMS = System.currentTimeMillis();
    }
    public boolean delay(int ms) {
        return System.currentTimeMillis() - lastMS >= ms;
    }
    public boolean delay(float ms) {
        return System.currentTimeMillis() - lastMS >= ms;
    }
    public void reset() {
        this.lastMS = System.currentTimeMillis();
    }
}
