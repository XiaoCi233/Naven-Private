package com.surface.mod.world;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.EventPreUpdate;
import com.surface.mod.Mod;
import com.surface.mod.world.hackerdetector.Check;
import com.surface.mod.world.hackerdetector.checks.AutoBlockA;
import com.surface.mod.world.hackerdetector.checks.NoSlowA;
import com.surface.render.notification.Notification;
import com.surface.render.notification.NotificationType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HackerDetectorModule extends Mod {
    private final ArrayList<EntityPlayer> hackers = new ArrayList<>();
    private final ArrayList<Check> checks = new ArrayList<>();

    private final Map<EntityPlayer, Check> cheatMap = new ConcurrentHashMap<>();

    public HackerDetectorModule() {
        super("Hacker Detector", Category.WORLD);
        addCheck(new AutoBlockA());
        addCheck(new NoSlowA());
    }

    private void addCheck(Check check) {
        checks.add(check);
        Wrapper.Instance.getEventManager().register(check);
        registerValues(check.getEnable());
    }

    private void flag(EntityPlayer target, Check flag) {
        cheatMap.put(target, flag);
        System.out.println(EnumChatFormatting.GRAY + target.getName() + " " + EnumChatFormatting.WHITE + "flagged " + EnumChatFormatting.AQUA + flag.getName() + EnumChatFormatting.WHITE + " (vl:" + target.cheatingVL + ") " + EnumChatFormatting.WHITE + " | " + flag.getDescription());
    }

    public boolean isHacker(EntityPlayer p) {
        return hackers.contains(p);
    }

    public void onEnable() {
        super.onEnable();
        hackers.clear();
    }

    public void onDisable() {
        super.onDisable();
        hackers.clear();
    }

    @EventTarget
    public void onUpdate(EventPreUpdate event) {
        if (mc.thePlayer.ticksExisted <= 105) {
            hackers.clear();
        } else {
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (player == mc.thePlayer || player.ticksExisted < 105 || player.isInvisible())
                    continue;

                if (hackers.contains(player))
                    continue;

                for (Check check : checks) {
                    if (!check.getEnable().getValue()) continue;
                    if (check.processCheck(player)) {
                        player.cheatingVL++;
                        flag(player, check);

                        if (player.cheatingVL > 1) {
                            Wrapper.Instance.getNotificationManager().pop(new Notification("Hacker Detector", player.getDisplayName().getFormattedText() + " flagged " + check.getName() + " (" + check.getDescription() + ")", NotificationType.ALERT, 10000));
                            if (!hackers.contains(player)) {
                                hackers.add(player);
                            }
                        }
                    }
                }

            }
        }
    }

}