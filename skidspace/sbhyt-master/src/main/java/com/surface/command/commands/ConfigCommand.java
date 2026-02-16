package com.surface.command.commands;


import com.surface.Wrapper;
import com.surface.command.Command;

import java.awt.*;
import java.io.IOException;

public final class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config", ".config <opendir/load/save/delete> [config]");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            sendUsage();
            return;
        }
        if (args[0].equals("opendir")) {
            try {
                Desktop.getDesktop().open(Wrapper.Instance.getConfigManager().getClientDirectory());
            } catch (IOException e) {
                Wrapper.sendMessage("Failed to open dir");
            }
        }
        if (args.length >= 2) {
            if (args[0].equals("load")) {
                Wrapper.Instance.getConfigManager().read(args[1]);
            }
            if (args[0].equals("save")) {
                Wrapper.Instance.getConfigManager().save(args[1]);
                Wrapper.sendMessage("Config " + args[1] + " has been saved.");
            }
            if (args[0].equals("delete")) {
                Wrapper.Instance.getConfigManager().delete(args[1]);
                Wrapper.sendMessage("Config " + args[1] + " has been deleted.");
            }
        } else {
            sendUsage();
        }
    }

    @Override
    protected void sendUsage() {
        Wrapper.sendMessage(".config load <config> - Load a config ");
        Wrapper.sendMessage(".config save <config> - Save new config");
        Wrapper.sendMessage(".config delete <config> - delete a config");
        Wrapper.sendMessage(".config opendir - open configs directory");
    }
}
