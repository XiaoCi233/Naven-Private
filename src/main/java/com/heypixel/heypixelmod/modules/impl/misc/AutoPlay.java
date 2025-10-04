package com.heypixel.heypixelmod.modules.impl.misc;

import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.regex.Pattern;

@ModuleInfo(
        name = "AutoPlay",
        description = "AutoPlay Game",
        category = Category.MISC
)
public class AutoPlay extends Module {
    private final Minecraft mc = Minecraft.getInstance();

    BooleanValue gameover = ValueBuilder.create(this, "Game Over Check")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    BooleanValue gamestart = ValueBuilder.create(this, "Game Start Check")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    private static final Pattern PATTERN_WIN_MESSAGE = Pattern.compile("游戏结束，请对此地图");
    private static final String TEXT_GAME_START = "稍等片刻，等待其他玩家";

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!isEnabled() || mc.level == null || mc.player == null || event.phase != TickEvent.Phase.END) return;
        if (gamestart.getCurrentValue()) {
            checkTitleForGameStart();
        }
    }

    private void checkTitleForGameStart() {
        if (mc.gui == null) return;

        try {
            String title = getTitleText();
            String subtitle = getSubtitleText();
            if (title.contains(TEXT_GAME_START) || subtitle.contains(TEXT_GAME_START)) {
                if (mc.player != null && mc.player.connection != null) {
                    mc.player.connection.sendCommand("again");
                }
            }
        } catch (Exception e) {
        }
    }
    private String getTitleText() {
        try {
            Component title = (Component) mc.gui.getClass().getMethod("getTitle").invoke(mc.gui);
            return title != null ? title.getString() : "";
        } catch (Exception e) {
            return "";
        }
    }
    private String getSubtitleText() {
        try {
            Component subtitle = (Component) mc.gui.getClass().getMethod("getSubtitle").invoke(mc.gui);
            return subtitle != null ? subtitle.getString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!isEnabled() || !gameover.getCurrentValue()) return;
        Component message = event.getMessage();
        String text = message.getString();
        if (PATTERN_WIN_MESSAGE.matcher(text).find() ||
                (mc.player != null && mc.player.isSpectator())) {
            if (mc.player != null && mc.player.connection != null) {
                mc.player.connection.sendCommand("again");
            }
        }
    }
}