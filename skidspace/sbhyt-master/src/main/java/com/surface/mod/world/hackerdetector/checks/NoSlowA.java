package com.surface.mod.world.hackerdetector.checks;

import com.cubk.event.annotations.EventTarget;
import com.surface.events.EventTick;
import com.surface.mod.world.hackerdetector.Check;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NoSlowA extends Check {
    private final Set<Item> checks = new HashSet<>();
    private final HashMap<EntityPlayer, Integer> blockTickMap = new HashMap<>();

    public NoSlowA() {
        super("NoSlow (A)", "Sprinting while using items");
        checks.add(Items.apple);
        checks.add(Items.golden_apple);
    }

    @EventTarget
    public void onTick(EventTick e) {
        for (Map.Entry<EntityPlayer, Integer> player : blockTickMap.entrySet()) {
            player.setValue(player.getKey().isBlocking() ? player.getValue() + 1 : 0);
        }
    }

    @Override
    public boolean processCheck(EntityPlayer player) {
        if (!blockTickMap.containsKey(player))
            blockTickMap.put(player, 0);

        if (player.onGround && !((player.moveForward != 0.0F || player.moveStrafing != 0.0F))) {
            return false;
        }

        if (blockTickMap.get(player) > 20) {
            final ItemStack itemStack = player.getHeldItem();
            if (itemStack != null) {
                return checks.contains(itemStack.getItem());
            }
        }
        return false;
    }
}
