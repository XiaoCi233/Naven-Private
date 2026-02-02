package tech.blinkfix.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import dev.yalan.live.LiveClient;
import dev.yalan.live.LiveUser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class FriendManager {
   private static final List<String> friends = new CopyOnWriteArrayList<>();

   public static boolean isFriend(Entity player) {
      if (!(player instanceof Player)) {
         return false;
      }

      // Original friend-list based check
      if (friends.contains(player.getName().getString())) {
         return true;
      }

      // Treat BlinkFix IRC users as friendly based on LiveUser mapping
      try {
         LiveClient live = LiveClient.INSTANCE;
         if (live != null && live.getLiveUserMap() != null) {
            LiveUser liveUser = live.getLiveUserMap().get(((Player)player).getUUID());
            if (liveUser != null && liveUser.isBlinkFixUser()) {
               return true;
            }
         }
      } catch (Throwable ignored) {
      }

      return false;
   }

   public static boolean isFriend(String player) {
      return friends.contains(player);
   }

   public static void addFriend(Player player) {
      friends.add(player.getName().getString());
   }

   public static void addFriend(String name) {
      friends.add(name);
   }

   public static void removeFriend(Player player) {
      friends.remove(player.getName().getString());
   }

   public static List<String> getFriends() {
      return friends;
   }
}
