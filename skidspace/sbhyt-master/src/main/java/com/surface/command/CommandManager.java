package com.surface.command;

import com.surface.Wrapper;
import com.surface.command.commands.BindCommand;
import com.surface.command.commands.ConfigCommand;
import com.surface.command.commands.ToggleCommand;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;

public final class CommandManager {
    private final LinkedHashMap<String, Command> commandMap = new LinkedHashMap<>();

    public void registerCommands() {
        register(new ToggleCommand());
        register(new BindCommand());
        register(new ConfigCommand());
    }

    public void register(Command... commands) {
        for (Command command : commands) {
            commandMap.put(command.getName().toLowerCase(Locale.ROOT), command);
        }
    }

    public boolean onChat(String message) {
        if (message.length() > 1 && message.startsWith(".")) {
            final String[] args = message.trim().substring(1).split(" ");
            final String commandName = args[0];

            final Command command = commandMap.get(commandName.toLowerCase(Locale.ROOT));

            if (command == null) {
                Wrapper.sendMessage("Unknown command: " + commandName);
            } else {
                command.execute(Arrays.copyOfRange(args, 1, args.length));
            }

            return true;
        }

        return false;
    }

    public LinkedHashMap<String, Command> getCommandMap() {
        return commandMap;
    }

    public Collection<Command> getCommands() {
        return commandMap.values();
    }
}
