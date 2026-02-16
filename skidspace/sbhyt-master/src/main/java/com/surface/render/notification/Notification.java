package com.surface.render.notification;

import com.surface.util.TimerUtils;

public class Notification {

    private String title, message;
    private final NotificationType notificationType;
    private final long shownTime;
    private boolean shouldRemove;

    private float x = -1, y = -30;
    public float radiusAnimation = 1;

    private final TimerUtils timer = new TimerUtils();

    public Notification(String title, String message, NotificationType notificationType) {
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.shownTime = 3000L;

        timer.reset();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Notification(String title, String message, NotificationType notificationType, long shownTime) {
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.shownTime = shownTime;

        timer.reset();
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return notificationType;
    }

    public long getShownTime() {
        return shownTime;
    }

    public TimerUtils getTimer() {
        return timer;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setShouldRemove(boolean shouldRemove) {
        this.shouldRemove = shouldRemove;
    }

    public boolean isShouldRemove() {
        return shouldRemove;
    }
}
