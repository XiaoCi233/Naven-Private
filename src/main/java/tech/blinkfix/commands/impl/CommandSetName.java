package tech.blinkfix.commands.impl;

import tech.blinkfix.commands.Command;
import tech.blinkfix.commands.CommandInfo;
import tech.blinkfix.utils.ChatUtils;
import tech.blinkfix.modules.impl.render.NameProtect;
import tech.blinkfix.files.FileManager;

@CommandInfo(
        name = "setname",
        description = "Set a custom name for yourself in-game",
        aliases = {"nameprotect", "changename"}
)
public class CommandSetName extends Command {
    @Override
    public void onCommand(String[] args) {
        if (args.length == 1) {
            String newName = args[0];
            NameProtect.instance.setCustomName(newName);
            FileManager fileManager = new FileManager();
            fileManager.save();

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