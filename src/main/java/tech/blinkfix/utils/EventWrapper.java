package tech.blinkfix.utils;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventClientChat;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.events.impl.EventRespawn;
import dev.yalan.live.LiveClient;
import dev.yalan.live.netty.LiveProto;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent.Post;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventWrapper {
   @SubscribeEvent
   public void onRender(Post e) {
   }

   @SubscribeEvent
   public void onClientChat(ClientChatEvent e) {
      EventClientChat event = new EventClientChat(e.getMessage());
      BlinkFix.getInstance().getEventManager().call(event);
      if (event.isCancelled()) {
         e.setCanceled(true);
      }
   }

   @EventTarget
   public void onMotion(EventMotion e) {
      if (e.getType() == EventType.PRE && Minecraft.getInstance().player.tickCount <= 1) {
         BlinkFix.getInstance().getEventManager().call(new EventRespawn());
      }
   }

    @SubscribeEvent
    public void onPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn e) {
        // ========== 已注释：权限检查 ==========
        // 原先这里限制只有 ADMINISTRATOR 或 BETA 用户才能进入服务器
        // General 用户会因此崩溃，现已禁用此检查
        // PermissionUtils.checkPermissionOrCrash("Attempted to join server without permission");
        // ====================================
        
        LiveClient.INSTANCE.sendPacket(LiveProto.createUpdateMinecraftProfile(
                e.getPlayer().getUUID(),
                e.getPlayer().getName().getString()
        ));
    }

    @SubscribeEvent
    public void onPlayerRespawn(ClientPlayerNetworkEvent.Clone e) {
        LiveClient.INSTANCE.sendPacket(LiveProto.createUpdateMinecraftProfile(
                e.getNewPlayer().getUUID(),
                e.getNewPlayer().getName().getString()
        ));
    }

    @SubscribeEvent
    public void onPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut e) {
        LiveClient.INSTANCE.sendPacket(LiveProto.createRemoveMinecraftProfile());
        LiveClient.INSTANCE.getLiveUserMap().clear();
    }
}
