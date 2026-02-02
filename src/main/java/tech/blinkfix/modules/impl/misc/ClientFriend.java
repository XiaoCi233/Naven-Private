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
import dev.yalan.live.LiveClient;
import dev.yalan.live.LiveUser;
import net.minecraft.world.entity.player.Player;

/**
 * IRC友好模块
 * 当启用时，检测IRC用户是否为BlinkFix用户，如果是则无法攻击
 * 禁用后强制15秒冷却，防止立即被刺
 */
@ModuleInfo(
   name = "ClientFriend",
   description = "Treat BlinkFix IRC users as friends - cannot attack them",
   category = Category.MISC
)
public class ClientFriend extends Module {
   
   // 防刺计时器 - 禁用模块后的保护时间
   public static TimeHelper attackTimer = new TimeHelper();
   
   // 强制冷却时间（毫秒）- 15秒
   private static final long COOLDOWN_TIME = 15000L;
   
   private final BooleanValue showNotification = ValueBuilder.create(this, "Show Notification")
      .setDefaultBooleanValue(true)
      .build()
      .getBooleanValue();
   
   private final BooleanValue debugMode = ValueBuilder.create(this, "Debug Mode")
      .setDefaultBooleanValue(false)
      .build()
      .getBooleanValue();

   @Override
   public void onEnable() {
      super.onEnable();
      if (showNotification.getCurrentValue()) {
         Notification notification = new Notification(
            NotificationLevel.INFO, 
            "IRC Friend Mode Enabled - BlinkFix users are now protected", 
            3000L
         );
         BlinkFix.getInstance().getNotificationManager().addNotification(notification);
      }
      
      if (debugMode.getCurrentValue()) {
         System.out.println("[ClientFriend] Module enabled - IRC user protection active");
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      
      // 重置防刺计时器 - 开始15秒强制冷却
      attackTimer.reset();
      
      if (showNotification.getCurrentValue()) {
         Notification notification = new Notification(
            NotificationLevel.INFO, 
            "You can attack other players after 15 seconds.", 
            COOLDOWN_TIME
         );
         BlinkFix.getInstance().getNotificationManager().addNotification(notification);
      }
      
      if (debugMode.getCurrentValue()) {
         System.out.println("[ClientFriend] Module disabled - 15s cooldown started (Anti-backstab protection)");
      }
   }
   
   /**
    * 检查冷却时间是否已过（强制15秒）
    * 
    * @return true 如果冷却时间已过，可以攻击
    */
   public static boolean isCooldownExpired() {
      // 检查是否已经过了15秒冷却时间
      return attackTimer.delay(COOLDOWN_TIME);
   }
   
   /**
    * 检查是否可以攻击目标玩家（基于IRC BlinkFix用户检测和冷却时间）
    * 
    * @param target 目标玩家
    * @return true 如果可以攻击，false 如果是IRC好友不能攻击或在冷却期内
    */
   public static boolean canAttack(Player target) {
      // 获取 ClientFriend 模块实例
      ClientFriend clientFriend = (ClientFriend) BlinkFix.getInstance()
                                                   .getModuleManager()
                                                   .getModule(ClientFriend.class);
      
      // 如果模块未启用，检查冷却时间
      if (clientFriend == null || !clientFriend.isEnabled()) {
         // 模块禁用后，在15秒冷却期内不能攻击（防刺保护）
         if (!isCooldownExpired()) {
            if (clientFriend != null && clientFriend.debugMode.getCurrentValue()) {
               System.out.println("[ClientFriend] Attack blocked - 15s cooldown active (Anti-backstab protection)");
            }
            return false;
         }
         return true;
      }
      
      try {
         LiveClient live = LiveClient.INSTANCE;
         
         // 如果 LiveClient 未初始化，允许攻击
         if (live == null || live.getLiveUserMap() == null) {
            return true;
         }
         
         // 查询目标玩家的IRC信息
         LiveUser targetUser = live.getLiveUserMap().get(target.getUUID());
         
         // 如果目标不是IRC用户，允许攻击
         if (targetUser == null) {
            return true;
         }
         
         // 如果目标是BlinkFix用户，禁止攻击
         if (targetUser.isBlinkFixUser()) {
            if (clientFriend.debugMode.getCurrentValue()) {
               System.out.println("[ClientFriend] Blocked attack on BlinkFix IRC user: " + 
                                target.getName().getString() + " (Client: " + targetUser.getClientId() + ")");
            }
            return false;
         }
         
         return true;
         
      } catch (Throwable e) {
         // 出错时默认允许攻击
         if (clientFriend != null && clientFriend.debugMode.getCurrentValue()) {
            System.err.println("[ClientFriend] Error checking IRC user: " + e.getMessage());
            e.printStackTrace();
         }
         return true;
      }
   }
   
   /**
    * 检查目标玩家是否为BlinkFix IRC用户
    * 
    * @param target 目标玩家
    * @return true 如果是BlinkFix用户
    */
   public static boolean isBlinkFixUser(Player target) {
      try {
         LiveClient live = LiveClient.INSTANCE;
         if (live != null && live.getLiveUserMap() != null) {
            LiveUser targetUser = live.getLiveUserMap().get(target.getUUID());
            return targetUser != null && targetUser.isBlinkFixUser();
         }
      } catch (Throwable ignored) {
      }
      return false;
   }
   
   /**
    * 获取目标玩家的IRC客户端ID
    * 
    * @param target 目标玩家
    * @return 客户端ID，如果不是IRC用户则返回null
    */
   public static String getClientId(Player target) {
      try {
         LiveClient live = LiveClient.INSTANCE;
         if (live != null && live.getLiveUserMap() != null) {
            LiveUser targetUser = live.getLiveUserMap().get(target.getUUID());
            if (targetUser != null) {
               return targetUser.getClientId();
            }
         }
      } catch (Throwable ignored) {
      }
      return null;
   }
}
