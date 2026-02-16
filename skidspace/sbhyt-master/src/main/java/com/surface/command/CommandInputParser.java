package com.surface.command;

import com.surface.Wrapper;

public class CommandInputParser {
    private final String[] args;

    private int index = 0;

    public CommandInputParser(String[] args) {
        this.args = args;
    }

    public boolean isOutbound(int length) {
        return length > args.length;
    }

    public String readString() {
        return args[index++];
    }

    public Integer readInt() {
        final String s = readString();

        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Wrapper.sendMessage(s + " is not a Integer");
        }

        return null;
    }

    public Double readDouble() {
        final String s = readString();

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            Wrapper.sendMessage(s + " is not a number");
        }

        return null;
    }
}
