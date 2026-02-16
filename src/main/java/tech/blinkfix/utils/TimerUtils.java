package tech.blinkfix.utils;

public class TimerUtils {
    private long lastTime;
    private long lastMs = 0L;

    public TimerUtils() {
        this.reset();
    }
    public boolean delay(long nextDelay) {
        return System.currentTimeMillis() - lastMs >= nextDelay;
    }
    public void reset() {
        this.lastTime = System.currentTimeMillis();
    }

    public long getTimePassed() {
        return System.currentTimeMillis() - this.lastTime;
    }

    public boolean hasTimePassed(long time) {
        if (time <= 0) {
            return true;
        }
        return System.currentTimeMillis() - lastTime >= time;
    }

    public long getTime() {
        return System.currentTimeMillis() - this.lastTime;
    }

    public void setTime(final long time) {
        this.lastTime = time;
    }
}