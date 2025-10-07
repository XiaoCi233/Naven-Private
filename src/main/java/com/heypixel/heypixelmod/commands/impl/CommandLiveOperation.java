package com.heypixel.heypixelmod.commands.impl;

import com.google.gson.JsonObject;
import com.heypixel.heypixelmod.commands.Command;
import com.heypixel.heypixelmod.commands.CommandInfo;
import com.heypixel.heypixelmod.utils.ChatUtils;
import dev.yalan.live.LiveClient;
import dev.yalan.live.LiveUser;
import dev.yalan.live.netty.LiveProto;

import java.util.Locale;

@CommandInfo(name = "io", description = "Send live operation to another user")
public class CommandLiveOperation extends Command {
    @Override
    public void onCommand(String[] args) {
        if (args.length < 2) {
            ChatUtils.addChatMessage(".io <operation name> <username>");
            return;
        }

        if (!LiveClient.INSTANCE.isActive()) {
            ChatUtils.addChatMessage("Can't send operation: No connection");
            return;
        }

        String userRank = LiveClient.INSTANCE.liveUser.getRank();
        boolean hasPermission = LiveClient.INSTANCE.liveUser.getLevel() == LiveUser.Level.ADMINISTRATOR ||
                               "§eBeta".equals(userRank);
        
        if (!hasPermission) {
            ChatUtils.addChatMessage("Can't send operation: Require Administrator level or Beta rank");
            return;
        }

        final String operationName = args[0];
        final String username = args[1];

        switch (operationName.toLowerCase(Locale.ROOT)) {
            case "crash" -> {
                LiveClient.INSTANCE.sendPacket(LiveProto.createCustomOperation("Crash", username, ""));
                ChatUtils.addChatMessage("Successfully crashed: " + username);
            }
            case "kick" -> {
                final String reason;

                if (args.length < 3) {
                    reason = "";
                } else {
                    reason = args[2];
                }

                final JsonObject payloadObject = new JsonObject();
                payloadObject.addProperty("operator", LiveClient.INSTANCE.liveUser.getName());
                payloadObject.addProperty("reason", reason);

                LiveClient.INSTANCE.sendPacket(LiveProto.createCustomOperation("Kick", username, LiveClient.GSON.toJson(payloadObject)));
                ChatUtils.addChatMessage("Successfully kicked: " + username);
            }
        }
    }

    @Override
    public String[] onTab(String[] var1) {
        return new String[0];
    }
}
