package com.surface.command.commands;


import com.surface.Wrapper;
import com.surface.command.Command;
import com.surface.mod.Mod;

public final class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", ".toggle <ModName>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            sendUsage();
            return;
        }

        Mod mod = Wrapper.Instance.getModManager().findModule(args[0]);

        if (mod != null) {
            mod.toggle();
            Wrapper.sendMessage("Toggle mod " + mod.getName());
        } else {
            Wrapper.sendMessage("Module not found: " + args[0]);
        }
    }
}
