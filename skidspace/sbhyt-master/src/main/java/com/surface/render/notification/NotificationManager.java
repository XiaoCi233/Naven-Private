package com.surface.render.notification;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.Event2D;
import com.surface.render.font.FontManager;
import com.surface.util.render.RenderUtils;
import com.surface.util.render.shader.ShaderElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import renderassist.animations.LinearAnimation;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {

    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public NotificationManager() {
        Wrapper.Instance.getEventManager().register(this);
    }

    @EventTarget
    public void onScreenRender(Event2D event) {
        notifications.removeIf(Notification::isShouldRemove);

        int tarY = 20;
        for (Notification notification : notifications) {

            if (notification.isShouldRemove()) {
                notifications.remove(notification);
                return;
            }

            final float strWidth = Math.max(Math.max(FontManager.TAHOMA.getStringWidth2(notification.getTitle()), FontManager.TAHOMA.getStringWidth2(notification.getMessage())) + 30, 134);
            final float recWidth = strWidth + 26;

            if (notification.getX() == -1) notification.setX(recWidth);

            final float right = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth() - 2;
            final float bottom = new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight() - 3;

            final Color c = notification.getType() == NotificationType.INFO ? Color.WHITE : notification.getType() == NotificationType.SUCCESSFULLY ? Color.GREEN : new Color(226, 87, 76);

            RenderUtils.drawRoundedRect(right - recWidth + notification.getX(), bottom - 25 - notification.getY(), recWidth, 30, 4, new Color(0, 0, 0, 100).getRGB());

            ShaderElement.addBlurTask(() -> RenderUtils.drawRoundedRect(right - recWidth + notification.getX(), bottom - 25 - notification.getY(), recWidth, 30, 4, new Color(0, 0, 0, 100).getRGB()));
            ShaderElement.addBloomTask(() -> RenderUtils.drawRoundedRect(right - recWidth + notification.getX(), bottom - 25 - notification.getY(), recWidth, 30, 4, -1));

            notification.radiusAnimation = (float) notification.getTimer().getTime() / notification.getShownTime() * recWidth;

            RenderUtils.drawRoundedRect(right - recWidth + notification.getX(), bottom - 25 - notification.getY(), notification.radiusAnimation, 30, RenderUtils.RoundingMode.LEFT, 12, 2, RenderUtils.reAlpha(c.getRGB(), 30));

            RenderUtils.drawImage(new ResourceLocation("surface/images/" + notification.getType().name().toLowerCase() + ".png"), right - recWidth + notification.getX() + 4, bottom - 25 - notification.getY() + 4, 16, 16, c.getRGB());
            FontManager.WQY.setFontSize(18);
            FontManager.WQY.drawString(notification.getTitle(), right - recWidth + notification.getX() + 24, bottom - 25 - notification.getY() + 4, -1);
            FontManager.WQY.setFontSize(16);
            FontManager.WQY.drawString(notification.getMessage(), right - recWidth + notification.getX() + 24, bottom - 25 - notification.getY() + 16, -1);

            notification.setX(LinearAnimation.animate(notification.getX(), notification.getTimer().hasTimeElapsed(notification.getShownTime()) ? recWidth + 5 : 0, .3F));
            notification.setY(LinearAnimation.animate(notification.getY(), (tarY += 33) - 33, .3F));

            notification.setShouldRemove(notification.getX() == recWidth + 5 && notification.getY() > -10);
        }
    }

    public void pop(Notification notification) {
        notifications.add(notification);
    }

    public List<Notification> getNotifications() {
        return notifications;
    }
}
