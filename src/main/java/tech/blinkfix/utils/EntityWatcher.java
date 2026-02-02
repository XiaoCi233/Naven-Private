package tech.blinkfix.utils;

import tech.blinkfix.BlinkFix;
import xyz.gay.mixin.accessors.LivingEntityAccessor;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.events.impl.EventRespawn;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import org.antlr.v4.runtime.misc.OrderedHashSet;

public class EntityWatcher {
   private static final Minecraft mc = Minecraft.getInstance();
   private static final Map<Entity, Set<String>> tags = new ConcurrentHashMap<>();
   private static final Map<String, SharedESPData> sharedESPData = new ConcurrentHashMap<>();

   public static Set<String> getEntityTags(AbstractClientPlayer player) {
      List<MobEffect> effects = PotionResolver.resolve((Integer)player.getEntityData().get(LivingEntityAccessor.getEffectColorId()));
      effects.remove(MobEffects.ABSORPTION);
      Set<String> currentPlayerTags = new OrderedHashSet();
      if (tags.containsKey(player)) {
         currentPlayerTags.addAll(tags.get(player));
      }

      Set<String> collect = effects.stream()
         .map(effect -> "effect.minecraft." + BuiltInRegistries.MOB_EFFECT.getKey(effect).getPath())
         .collect(Collectors.toSet());
      currentPlayerTags.addAll(collect);
      return currentPlayerTags;
   }

   @EventTarget
   public void onRespawn(EventRespawn e) {
      tags.clear();
      sharedESPData.clear();
   }

   @EventTarget
   public void onMotion(EventMotion e) {
      if (e.getType() == EventType.PRE && mc.level != null) {
         getSharedESPData().forEach((ign, data) -> {
            if (System.currentTimeMillis() - data.getUpdateTime() > 500L) {
               getSharedESPData().remove(ign);
            }
         });

          for (AbstractClientPlayer player : new ArrayList<>(mc.level.players())) {
              if (player != mc.player) {
                  if (!tags.containsKey(player)) {
                      tags.put(player, new HashSet<>());
                  }

                  Set<String> playerTags = tags.get(player);
                  String playerName = player.getName().getString();

                  if ((InventoryUtils.isGodAxe(player.getMainHandItem()) || InventoryUtils.isGodAxe(player.getOffhandItem())) && !playerTags.contains("God Axe")) {
                      String message = "§6[WARNING] §c" + playerName + " 有秒入虎!";
                      Notification notification = new Notification(NotificationLevel.WARNING, playerName + " is holding god axe!", 3000L);
                      BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                      ChatUtils.addChatMessage(message);
                      playerTags.add("God Axe");
                  }

                  if ((InventoryUtils.isEnchantedGApple(player.getMainHandItem()) || InventoryUtils.isEnchantedGApple(player.getOffhandItem()))
                          && !playerTags.contains("Enchanted Golden Apple")) {
                      String message = "§6[WARNING] §e" + playerName + " 手持附魔金苹果!";
                      Notification notification = new Notification(
                              NotificationLevel.WARNING, playerName + " is holding enchanted golden apple!", 3000L
                      );
                      BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                      ChatUtils.addChatMessage(message);
                      playerTags.add("Enchanted Golden Apple");
                  }

                  if ((InventoryUtils.isEndCrystal(player.getMainHandItem()) || InventoryUtils.isEndCrystal(player.getOffhandItem()))
                          && !playerTags.contains("End Crystal")) {
                      String message = "§6[WARNING] §5" + playerName + " 手持末影水晶!";
                      Notification notification = new Notification(NotificationLevel.WARNING, playerName + " is holding end crystal!", 3000L);
                      BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                      ChatUtils.addChatMessage(message);
                      playerTags.add("End Crystal");
                  }

                  if ((InventoryUtils.isKBBall(player.getMainHandItem()) || InventoryUtils.isKBBall(player.getOffhandItem())) && !playerTags.contains("KB Ball")) {
                      String message = "§6[WARNING] §9" + playerName + " 手持击退球!";
                      Notification notification = new Notification(NotificationLevel.WARNING, playerName + " is holding KB Ball!", 3000L);
                      BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                      ChatUtils.addChatMessage(message);
                      playerTags.add("KB Ball");
                  }

                  if ((InventoryUtils.isKBStick(player.getMainHandItem()) || InventoryUtils.isKBStick(player.getOffhandItem()))
                          && !playerTags.contains("KB Stick")) {
                      String message = "§6[WARNING] §9" + playerName + " 手持击退棒!";
                      Notification notification = new Notification(NotificationLevel.WARNING, playerName + " is holding KB Stick!", 3000L);
                      BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                      ChatUtils.addChatMessage(message);
                      playerTags.add("KB Stick");
                  }

                  if ((InventoryUtils.getPunchLevel(player.getMainHandItem()) > 2 || InventoryUtils.getPunchLevel(player.getOffhandItem()) > 2)
                          && !playerTags.contains("Punch Bow")) {
                      String message = "§6[警告] §a" + playerName + " 手持击退弓(Punch " +
                              Math.max(InventoryUtils.getPunchLevel(player.getMainHandItem()), InventoryUtils.getPunchLevel(player.getOffhandItem())) + ")!";
                      Notification notification = new Notification(NotificationLevel.WARNING, playerName + " is holding Punch Bow!", 3000L);
                      BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                      ChatUtils.addChatMessage(message);
                      playerTags.add("Punch Bow");
                  }

                  if ((InventoryUtils.getPowerLevel(player.getMainHandItem()) > 3 || InventoryUtils.getPowerLevel(player.getOffhandItem()) > 3)
                          && !playerTags.contains("Power Bow")) {
                      int powerLevel = Math.max(InventoryUtils.getPowerLevel(player.getMainHandItem()), InventoryUtils.getPowerLevel(player.getOffhandItem()));
                      String message = "§6[警告] §a" + playerName + " 手持力量弓(Power " + powerLevel + ")!";
                      Notification notification = new Notification(NotificationLevel.WARNING, playerName + " is holding Power Bow!", 3000L);
                      BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                      ChatUtils.addChatMessage(message);
                      playerTags.add("Power Bow");
                  }
              }
         }
      }
   }

   public static Map<String, SharedESPData> getSharedESPData() {
      return sharedESPData;
   }
}
