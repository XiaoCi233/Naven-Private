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

public class AutoBlockA extends Check {
    private final Set<Item> swordSet = new HashSet<>();
    private final HashMap<EntityPlayer, Integer> blockTickMap = new HashMap<>();

    public AutoBlockA() {
        super("AutoBlock (A)", "Blocking while swing");
        swordSet.add(Items.wooden_sword);
        swordSet.add(Items.stone_sword);
        swordSet.add(Items.golden_sword);
        swordSet.add(Items.iron_sword);
        swordSet.add(Items.diamond_sword);
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

        if (player.isSwingInProgress && blockTickMap.get(player) > 20) {
            final ItemStack itemStack = player.getHeldItem();
            if (itemStack != null) {
                return swordSet.contains(itemStack.getItem());
            }
        }
        return false;
    }
}
