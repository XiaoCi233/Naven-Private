package com.surface.command;

import com.surface.Wrapper;

public abstract class Command {
    private final String name;
    private final String[] usage;

    private String help = "";
    private boolean hide = false;

    public Command(String name, String usage) {
        this.name = name;
        this.usage = new String[]{usage};
    }

    public Command(String name, String[] usage) {
        this.name = name;
        this.usage = usage;
    }

    public abstract void execute(String[] args);

    public final String getName() {
        return name;
    }

    public final String[] getUsage() {
        return usage;
    }

    public final String getHelp() {
        return help;
    }

    public final void setHelp(String help) {
        this.help = help;
    }

    public final boolean isHide() {
        return hide;
    }

    public final void setHide(boolean hide) {
        this.hide = hide;
    }

    protected void sendUsage() {
        for (String s : usage) {
            Wrapper.sendMessageWith(name, "Usage: " + s);
        }
    }
}
