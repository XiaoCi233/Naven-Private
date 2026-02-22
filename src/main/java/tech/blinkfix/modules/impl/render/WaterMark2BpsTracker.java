package tech.blinkfix.modules.impl.render;

public final class WaterMark2BpsTracker {
    private double prevX;
    private double prevZ;
    private double displayedBps;
    private long lastUpdateTime;
    private long intervalMs;

    public WaterMark2BpsTracker(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public void setInterval(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public double update(double x, double z, long nowMs) {
        double distanceX = x - prevX;
        double distanceZ = z - prevZ;
        double currentCalculatedBps = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ) * 20.0;
        if (nowMs - lastUpdateTime >= intervalMs) {
            displayedBps = currentCalculatedBps;
            lastUpdateTime = nowMs;
        }
        prevX = x;
        prevZ = z;
        return displayedBps;
    }

    public double getDisplayedBps() {
        return displayedBps;
    }
}
