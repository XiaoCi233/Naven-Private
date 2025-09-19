package com.heypixel.heypixelmod.commands.impl;

import com.heypixel.heypixelmod.commands.Command;
import com.heypixel.heypixelmod.commands.CommandInfo;
import com.heypixel.heypixelmod.utils.ChatUtils;
import com.heypixel.heypixelmod.modules.impl.render.NameProtect;

@CommandInfo(
        name = "setname",
        description = "Set a custom name for yourself in-game",
        aliases = {"nameprotect", "changename"}
)
public class CommandSetName extends Command {
    @Override
    public void onCommand(String[] args) {
        System.out.println("Command received: .setname");

        if (args.length == 1) {
            String newName = args[0];
            NameProtect.instance.setCustomName(newName);
            ChatUtils.addChatMessage("Your name has been changed to: " + newName);
        } else {
            ChatUtils.addChatMessage("Usage: .setname <newName>");
        }
    }

    @Override
    public String[] onTab(String[] args) {
        return new String[0];
    }
}
