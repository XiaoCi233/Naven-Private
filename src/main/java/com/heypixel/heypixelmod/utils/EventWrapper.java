package com.heypixel.heypixelmod.utils;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.EventClientChat;
import com.heypixel.heypixelmod.events.impl.EventMotion;
import com.heypixel.heypixelmod.events.impl.EventRespawn;
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
      com.heypixel.heypixelmod.BlinkFix.getInstance().getEventManager().call(event);
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
