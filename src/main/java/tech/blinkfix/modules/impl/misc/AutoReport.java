package tech.blinkfix.modules.impl.misc;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventRunTicks;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.Module;
import tech.blinkfix.utils.TimeHelper;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.FloatValue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@ModuleInfo(
        name = "AutoReport",
        description = "Auto Report Hacker $ GreenPlayer^_^",
        category = Category.MISC
)
public class AutoReport extends Module {
    public FloatValue delay = ValueBuilder.create(this, "Delay")
            .setDefaultFloatValue(6000.0F)
            .setFloatStep(100.0F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(15000.0F)
            .build()
            .getFloatValue();

    private final TimeHelper timer = new TimeHelper();
    private final Random random = new Random();
    private final Minecraft mc = Minecraft.getInstance();

    private final Set<String> reportedPlayers = new HashSet<>();

    @EventTarget
    public void onMotion(EventRunTicks e) {

        if (!mc.isSingleplayer()) {
            if (e.getType() == EventType.POST && timer.delay((double) delay.getCurrentValue())) {
                List<String> playerList = getUnreportedPlayers();

                if (!playerList.isEmpty()) {
                    String targetName = playerList.get(random.nextInt(playerList.size()));
                    reportPlayer(targetName);
                    timer.reset();
                }
            }
        }
    }

    private void reportPlayer(String playerName) {
        if (playerName == null || playerName.isEmpty()) return;

        mc.player.connection.sendChat("/report " + playerName);
        reportedPlayers.add(playerName);
    }


    private List<String> getUnreportedPlayers() {
        List<String> players = new ArrayList<>();

        if (mc.player != null && mc.player.connection != null) {
            for (PlayerInfo info : mc.player.connection.getOnlinePlayers()) {
                String name = info.getProfile().getName();

                if (!name.equals(mc.player.getGameProfile().getName()) && !reportedPlayers.contains(name)) {
                    players.add(name);
                }
            }
        }
        return players;
    }

    @Override
    public void onDisable() {
        reportedPlayers.clear();
        super.onDisable();
    }
}