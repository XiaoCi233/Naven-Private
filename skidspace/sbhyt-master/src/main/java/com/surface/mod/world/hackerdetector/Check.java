package com.surface.mod.world.hackerdetector;

import com.surface.value.impl.BooleanValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public abstract class Check {
    public static Minecraft mc = Minecraft.getMinecraft();
    private final String name;
    private final String description;
    private final BooleanValue enable;

    public Check(String name, String desc) {
        this.name = name;
        this.description = desc;
        this.enable = new BooleanValue("Enabled " + name, true);
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public BooleanValue getEnable() {
        return enable;
    }

    public abstract boolean processCheck(EntityPlayer e);
}
