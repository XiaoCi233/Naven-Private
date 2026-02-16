package com.surface.command.commands;


import com.surface.Wrapper;
import com.surface.command.Command;
import com.surface.mod.Mod;
import org.lwjgl.input.Keyboard;

public final class BindCommand extends Command {
    public BindCommand() {
        super("bind", ".bind <ModName> <KeyName>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 2) {
            sendUsage();
            return;
        }

        Mod mod = Wrapper.Instance.getModManager().findModule(args[0]);

        if (mod != null) {
            mod.setKeyCode(Keyboard.getKeyIndex(args[1].toUpperCase()));
            Wrapper.sendMessage("Module " + mod.getName() + " key bind has been set to " + Keyboard.getKeyIndex(args[1].toUpperCase()));
        } else {
            Wrapper.sendMessage("Module not found: " + args[0]);
        }
    }
}
