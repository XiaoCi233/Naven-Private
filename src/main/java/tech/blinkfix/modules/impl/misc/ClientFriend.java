package tech.blinkfix.modules.impl.misc;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.utils.TimeHelper;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import net.minecraft.world.entity.player.Player;

@ModuleInfo(
   name = "ClientFriend",
   description = "Treat BlinkFix IRC users as friends - cannot attack them",
   category = Category.MISC
)
public class ClientFriend extends Module {
}