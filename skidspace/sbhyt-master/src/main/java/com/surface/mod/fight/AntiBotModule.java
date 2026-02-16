package com.surface.mod.fight;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.EventPacket;
import com.surface.events.EventWorldLoad;
import com.surface.mod.Mod;
import com.surface.render.notification.Notification;
import com.surface.render.notification.NotificationType;
import com.surface.value.impl.BooleanValue;
import com.surface.value.impl.FilterValue;
import com.surface.value.impl.ModeValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S14PacketEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiBotModule extends Mod {
    private final BooleanValue entityID = new BooleanValue("Entity ID", false);
    private final BooleanValue sleep = new BooleanValue("Sleep", false);
    private final BooleanValue noArmor = new BooleanValue("NoArmor", false);
    private final BooleanValue height = new BooleanValue("Height", false);
    private final BooleanValue ground = new BooleanValue("Ground", false);
    private final BooleanValue dead = new BooleanValue("Dead", false);
    private final BooleanValue health = new BooleanValue("Health", false);
    private final BooleanValue hytGetNames = new BooleanValue("Hyt Username", false);
    private final BooleanValue tips = new BooleanValue("Alert", true);
    private final ModeValue hytBW = new ModeValue("Hyt Bed Wars", "4v4", new String[]{"4v4", "1v1", "32", "16"});
    private final List<Integer> groundBotList = new ArrayList<>();
    private final List<String> playerName = new ArrayList<>();

    public AntiBotModule() {
        super("Anti Bot", Category.FIGHT);
        FilterValue<BooleanValue> filterValue = new FilterValue<BooleanValue>("Checks", entityID, sleep, noArmor, height, ground, dead, health, hytGetNames) {
            @Override
            public boolean isValid(BooleanValue e) {
                return false;
            }
        };
        registerValues(hytBW, filterValue, tips);
    }

    @EventTarget
    public void onWorld(EventWorldLoad event) {
        clearAll();
    }

    private void clearAll() {
        playerName.clear();
    }

    @EventTarget
    public void onPacketReceive(EventPacket event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        Packet<?> packet = event.getPacket();
        if (event.getPacket() instanceof S14PacketEntity && ground.getValue()) {
            Entity entity = ((S14PacketEntity) event.getPacket()).getEntity(mc.theWorld);

            if (entity instanceof EntityPlayer) {
                if (((S14PacketEntity) event.getPacket()).onGround && !groundBotList.contains(entity.getEntityId())) {
                    groundBotList.add(entity.getEntityId());
                }
            }
        }
        if (hytGetNames.getValue() && packet instanceof S02PacketChat) {
            S02PacketChat s02PacketChat = (S02PacketChat) packet;
            if (s02PacketChat.getChatComponent().getUnformattedText().contains("获得胜利!") || s02PacketChat.getChatComponent().getUnformattedText().contains("游戏开始 ...")) {
                clearAll();
            }
            switch (hytBW.getValue()) {
                case "4v4":
                case "1v1":
                case "32": {
                    Matcher matcher = Pattern.compile("杀死了 (.*?)\\(").matcher(s02PacketChat.getChatComponent().getUnformattedText());
                    Matcher matcher2 = Pattern.compile("起床战争>> (.*?) (\\((((.*?) 死了!)))").matcher(s02PacketChat.chatComponent.getUnformattedText());
                    if (matcher.find() && !s02PacketChat.chatComponent.getUnformattedText().contains(": 起床战争>>") || !s02PacketChat.chatComponent.getUnformattedText().contains(": 杀死了")) {
                        String name = matcher.group(1).trim();
                        if (!name.isEmpty()) {
                            playerName.add(name);
                            if (tips.getValue())
                                Wrapper.Instance.getNotificationManager().pop(new Notification("Anti Bot", "Detected suspected bot entity: " + name, NotificationType.ALERT, 3000));
                            new Thread(() -> {
                                try {
                                    Thread.sleep(6000);
                                    playerName.remove(name);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }).start();
                        }
                    }
                    if (matcher2.find() && !s02PacketChat.chatComponent.getUnformattedText().contains(": 起床战争>>") || !s02PacketChat.chatComponent.getUnformattedText().contains(": 杀死了")) {
                        String name = matcher2.group(1).trim();
                        if (!name.isEmpty()) {
                            playerName.add(name);
                            if (tips.getValue())
                                Wrapper.Instance.getNotificationManager().pop(new Notification("Anti Bot", "Detected suspected bot entity: " + name, NotificationType.ALERT, 3000));

                            new Thread(() -> {
                                try {
                                    Thread.sleep(6000);
                                    playerName.remove(name);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }).start();
                        }
                    }
                    break;
                }
                case "16": {
                    Matcher matcher = Pattern.compile("击败了 (.*?)!").matcher(s02PacketChat.chatComponent.getUnformattedText());
                    Matcher matcher2 = Pattern.compile("玩家 (.*?)死了！").matcher(s02PacketChat.chatComponent.getUnformattedText());
                    if (matcher.find() && !s02PacketChat.chatComponent.getUnformattedText().contains(": 击败了") || !s02PacketChat.chatComponent.getUnformattedText().contains(": 玩家 ")) {
                        String name = matcher.group(1).trim();
                        if (!name.isEmpty()) {
                            playerName.add(name);
                            if (tips.getValue())
                                Wrapper.Instance.getNotificationManager().pop(new Notification("Anti Bot", "Detected suspected bot entity: " + name, NotificationType.ALERT, 3000));

                            new Thread(() -> {
                                try {
                                    Thread.sleep(10000);
                                    playerName.remove(name);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }).start();
                        }
                    }
                    if (matcher2.find() && !s02PacketChat.chatComponent.getUnformattedText().contains(": 击败了") || !s02PacketChat.chatComponent.getUnformattedText().contains(": 玩家 ")) {
                        String name = matcher2.group(1).trim();
                        if (!name.isEmpty()) {
                            playerName.add(name);
                            Wrapper.Instance.getNotificationManager().pop(new Notification("Anti Bot", "Detected suspected bot entity: " + name, NotificationType.ALERT, 3000));

                            new Thread(() -> {
                                try {
                                    Thread.sleep(10000);
                                    playerName.remove(name);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }).start();
                        }
                    }
                    break;

                }
            }
        }
    }

    public boolean isServerBot(Entity entity) {
        if (getState()) {
            if (entity instanceof EntityPlayer) {
                if (hytGetNames.getValue() && playerName.contains(entity.getName())) {
                    return true;
                }
                if (height.getValue() && (entity.height <= 0.5 || ((EntityPlayer) entity).isPlayerSleeping() || entity.ticksExisted < 80)) {
                    return true;
                }
                if (dead.getValue() && entity.isDead) {
                    return true;
                }
                if (health.getValue() && ((EntityPlayer) entity).getHealth() == 0.0F) {
                    return true;
                }
                if (sleep.getValue() && ((EntityPlayer) entity).isPlayerSleeping()) {
                    return true;
                }
                if (entityID.getValue() && (entity.getEntityId() >= 1000000000 || entity.getEntityId() <= -1)) {
                    return true;
                }
                if (ground.getValue() && !groundBotList.contains(entity.getEntityId())) {
                    return true;
                }
                return noArmor.getValue() && (((EntityPlayer) entity).inventory.armorInventory[0] == null
                        && ((EntityPlayer) entity).inventory.armorInventory[1] == null
                        && ((EntityPlayer) entity).inventory.armorInventory[2] == null
                        && ((EntityPlayer) entity).inventory.armorInventory[3] == null);
            }
        }
        return false;
    }
}