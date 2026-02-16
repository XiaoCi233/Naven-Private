package com.surface.mod.player;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventAttack;
import com.surface.events.EventPacket;
import com.surface.events.EventPreUpdate;
import com.surface.events.EventUpdate;
import com.surface.mod.Mod;
import com.surface.value.impl.ModeValue;
import com.surface.value.impl.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.apache.commons.lang3.RandomUtils;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsultsModule extends Mod {
    public InsultsModule() {
        super("Insults", Category.PLAYER);
        registerValues(delay,prefix,style);
    }

    public final Map<String, List<String>> map = new HashMap<>();
    private final NumberValue delay = new NumberValue("Delay", 0, 0, 50, 1);
    private final ModeValue prefix = new ModeValue("Prefix", "None", new String[]{"None", "@"});
    private final ModeValue style = new ModeValue("Styles", "L,get surface client now", new String[]{"L,get surface client now", "BiliBili subs to Anoxia2333"});

    private EntityPlayer target;
    private int ticks;

    @EventTarget
    private void onMotion(EventPreUpdate event) {

        if (target != null && !mc.theWorld.playerEntities.contains(target) && target.isDead) {
            if (ticks >= delay.getValue().intValue() + Math.random() * 5) {

                if(prefix.isCurrentMode("None")) {
                    mc.thePlayer.sendChatMessage(target.getName() + " " + style.getValue());
                }

                if(prefix.isCurrentMode("@")) {
                    mc.thePlayer.sendChatMessage("@" + target.getName() + " " + style.getValue());
                }

                target = null;
            }
            ticks++;
        }
    }

    @EventTarget
    private void onPacket(EventPacket eventPacket){
        if(eventPacket.isSendMode()){
            if (mc.thePlayer == null || mc.theWorld == null)
                return;

            Packet<?> packet = eventPacket.getPacket();

            if (packet instanceof C01PacketChatMessage) {
                String message = ((C01PacketChatMessage) packet).getMessage();
                if (message.startsWith("/"))
                    return;

                StringBuilder stringBuilder = new StringBuilder();

                for (char c : message.toCharArray()) {
                    if (c >= 33 && c <= 128)
                        stringBuilder.append(Character.toChars(c + 65248));
                    else
                        stringBuilder.append(c);
                }
                ((C01PacketChatMessage) packet).setMessage(stringBuilder.toString());
            }
        }
    }

    @EventTarget
    private void onMotion(EventAttack event) {
        if (event.isPost()) return;

        final Entity target = event.getTarget();

        if (target instanceof EntityPlayer) {
            this.target = (EntityPlayer) target;
            ticks = 0;
        }
    }



}
