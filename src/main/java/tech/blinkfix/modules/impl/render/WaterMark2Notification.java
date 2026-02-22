package tech.blinkfix.modules.impl.render;

import java.util.UUID;

public abstract class WaterMark2Notification {
    public final String id;
    public String title;
    public String message;
    public long createTime;
    public long duration;
    public boolean isMarkedForDelete;

    protected WaterMark2Notification(String title, String message, long duration) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.createTime = System.currentTimeMillis();
        this.duration = duration;
    }

    public void update(long nowMs) {
        if (nowMs > createTime + duration) {
            isMarkedForDelete = true;
        }
    }

    public void updateState(String newMsg, boolean newEnable, long newDuration) {
        this.message = newMsg;
        this.createTime = System.currentTimeMillis();
        this.duration = newDuration;
    }

    public abstract void draw(float x, float y);
}
