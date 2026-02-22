package tech.blinkfix.modules.impl.render;

public final class WaterMark2ToggleNotification extends WaterMark2Notification {
    public boolean enabled;
    public final String moduleName;
    public final WaterMark2Animation.SwitchAnimationState anim = new WaterMark2Animation.SwitchAnimationState();
    private final WaterMark2NotificationRenderer renderer;

    public WaterMark2ToggleNotification(String title, String message, long duration, boolean enabled, String moduleName,
                                        WaterMark2NotificationRenderer renderer) {
        super(title, message, duration);
        this.enabled = enabled;
        this.moduleName = moduleName;
        this.renderer = renderer;
        anim.updateState(enabled);
    }

    @Override
    public void updateState(String newMsg, boolean newEnable, long newDuration) {
        super.updateState(newMsg, newEnable, newDuration);
        this.enabled = newEnable;
        anim.updateState(newEnable);
    }

    @Override
    public void draw(float x, float y) {
        renderer.draw(x, y, enabled, anim, title, message);
    }
}
