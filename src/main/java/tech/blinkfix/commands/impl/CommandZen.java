package tech.blinkfix.commands.impl;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.commands.Command;
import tech.blinkfix.commands.CommandInfo;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.utils.ChatUtils;

@CommandInfo(
        name = "CommandZen",
        description = "LZen",
        aliases = {"zen"}
)
public class CommandZen extends Command {
    @Override
    public void onCommand(String[] args) {
        ChatUtils.addChatMessage("你打不过Zen.龇牙");
        Notification notification = new Notification(NotificationLevel.INFO, "你打不过Zen", 50000L);
        BlinkFix.getInstance().getNotificationManager().addNotification(notification);
    }

    @Override
    public String[] onTab(String[] args) {
        return new String[0];
    }
}