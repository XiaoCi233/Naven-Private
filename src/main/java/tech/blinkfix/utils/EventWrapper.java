package tech.blinkfix.utils;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventClientChat;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.events.impl.EventRespawn;
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
}