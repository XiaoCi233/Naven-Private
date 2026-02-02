package tech.blinkfix.modules.impl.misc;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventRunTicks;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

@ModuleInfo(
        name = "AutoPlay",
        description = "AutoPlay Game",
        category = Category.MISC
)
public class AutoPlay extends Module  {
    private final Minecraft mc = Minecraft.getInstance();
    private boolean waitingForResponse = false;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    BooleanValue gameover = ValueBuilder.create(this, "Game Over Check")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    FloatValue delay = ValueBuilder.create(this, "Delay (ms)")
            .setDefaultFloatValue(1000.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(10000.0f)
            .setFloatStep(100.0f)
            .build()
            .getFloatValue();
    
    private float lastDelayValue = 0.0f;
    private boolean hasShownLowDelayWarning = false;

    @Override
    public void onEnable() {
        super.onEnable();
        lastDelayValue = delay.getCurrentValue();
        hasShownLowDelayWarning = false;
        
        // Check if delay is less than 500 when enabling AutoPlay
        if (delay.getCurrentValue() < 500.0f) {
            Notification notification = new Notification(NotificationLevel.INFO, "TA delay that is too short may lead to misjudgment.", 10000L);
            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            hasShownLowDelayWarning = true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        // 重置状态
        waitingForResponse = false;
        retryCount = 0;
    }

//    BooleanValue gamestart = ValueBuilder.create(this, "Game Start Check")
//            .setDefaultBooleanValue(true)
//            .build()
//            .getBooleanValue();
//    public void onTitlePacket(ClientboundSetTitleTextPacket packet) {
//        if (!isEnabled() || !gamestart.getCurrentValue()) return;
//        if (!hasPermission()) {
//            Notification notification = new Notification(NotificationLevel.INFO, "You not Admin or Beta.", 3000L);
//            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
//            this.setEnabled(false);
//            return;
//        }
//
//        Component titleComponent = packet.getText();
//        if (titleComponent != null) {
//            String title = titleComponent.getString();
//            if (title.contains("稍等片刻，等待其他玩家")) {
//                sendAgainCommand();
//            }
//        }
//    }
//    public void onSubtitlePacket(ClientboundSetSubtitleTextPacket packet) {
//        if (!isEnabled() || !gamestart.getCurrentValue()) return;
//        if (!hasPermission()) {
//            Notification notification = new Notification(NotificationLevel.INFO, "You not Admin or Beta.", 3000L);
//            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
//            this.setEnabled(false);
//            return;
//        }
//
//        Component subtitleComponent = packet.getText();
//        if (subtitleComponent != null) {
//            String subtitle = subtitleComponent.getString();
//            if (subtitle.contains("稍等片刻，等待其他玩家")) {
//                sendAgainCommand();
//            }
//        }
//    }
    @EventTarget
    public void onTick(EventRunTicks event) {
        if (!isEnabled() || event.getType() != EventType.PRE) return;
        
        // Check if delay value has changed and warn if it's less than 500
        float currentDelay = delay.getCurrentValue();
        if (currentDelay != lastDelayValue) {
            if (currentDelay < 500.0f && (!hasShownLowDelayWarning || lastDelayValue >= 500.0f)) {
                Notification notification = new Notification(NotificationLevel.INFO, "TA delay that is too short may lead to misjudgment.", 10000L);
                BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                hasShownLowDelayWarning = true;
            } else if (currentDelay >= 500.0f) {
                hasShownLowDelayWarning = false;
            }
            lastDelayValue = currentDelay;
        }
    }
    
    public void onSystemChatPacket(ClientboundSystemChatPacket packet) {
        if (!isEnabled()) return;
        
        Component message = packet.content();
        if (message == null) return;
        
        String text = message.getString();
        
        // 检测是否已经连接到当前玩法服务器
        if (waitingForResponse && text.contains("您已经连接到当前玩法服务器了")) {
            if (retryCount < MAX_RETRIES) {
                retryCount++;
                sendAgainCommand();
            } else {
                // 超过最大重试次数，重置状态
                waitingForResponse = false;
                retryCount = 0;
            }
            return;
        }
        
        // 检测游戏结束
        if (gameover.getCurrentValue()) {
            if (text.contains("游戏结束，请对此地图") ||
                    (mc.player != null && mc.player.isSpectator())) {
                retryCount = 0;  // 重置重试计数
                sendAgainCommand();
            }
        }
    }
    private void sendAgainCommand() {
        if (mc.player == null || mc.player.connection == null) return;
        
        int delayMs = (int) delay.getCurrentValue();
        if (delayMs <= 0) {
            // 无延迟，立即发送
            mc.player.connection.sendCommand("play swrsolo");
            waitingForResponse = true;
            // 5秒后如果还没收到响应，重置状态
            scheduleResetResponse(5000);
        } else {
            // 使用延迟发送
            new Thread(() -> {
                try {
                    Thread.sleep(delayMs);
                    mc.execute(() -> {
                        if (mc.player != null && mc.player.connection != null) {
                            mc.player.connection.sendCommand("play swrsolo");
                            waitingForResponse = true;
                            // 5秒后如果还没收到响应，重置状态
                            scheduleResetResponse(5000);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    private void scheduleResetResponse(int delayMs) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                mc.execute(() -> {
                    if (waitingForResponse) {
                        waitingForResponse = false;
                        retryCount = 0;
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

//    @Override
//    public boolean hasPermission() {
//        try {
//            LiveClient client = LiveClient.INSTANCE;
//            if (client == null || client.liveUser == null) {
//                return false;
//            }
//            LiveUser user = client.liveUser;
//            return user.getLevel() == LiveUser.Level.ADMINISTRATOR ||
//                    user.getLevel() == LiveUser.Level.BETA;
//        } catch (Throwable ignored) {
//            return false;
//        }
//    }
}