package com.surface.interfaces;

import com.surface.Wrapper;
import com.surface.mod.fight.AntiBotModule;
import com.surface.value.impl.BooleanValue;
import com.surface.value.impl.FilterValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;

public interface TargetFilter {
    default FilterValue<Entity> getNewFilter() {
        final BooleanValue players = new BooleanValue("Players", true);
        final BooleanValue animals = new BooleanValue("Animals", true);
        final BooleanValue mobs = new BooleanValue("Mobs", true);
        final BooleanValue villager = new BooleanValue("Villager", false);
        final BooleanValue invisible = new BooleanValue("Invisible", false);
        return new FilterValue<Entity>("Target", players, animals, mobs, villager ,invisible) {
            @Override
            public boolean isValid(Entity entity) {
                AntiBotModule antibot = (AntiBotModule) Wrapper.Instance.getModManager().getModFromName("Anti Bot");

                if (!(entity instanceof EntityLivingBase))
                    return false;
                if (entity instanceof EntityPlayer && !players.getValue())
                    return false;
                if (entity instanceof EntityMob && !mobs.getValue())
                    return false;
                if (entity instanceof EntityVillager && !villager.getValue())
                    return false;
                if (entity instanceof EntityAnimal && !animals.getValue())
                    return false;
                if (entity.isInvisible() && !invisible.getValue())
                    return false;
                if (((EntityLivingBase) entity).getHealth() <= 0)
                    return false;
                if (antibot.isServerBot(entity))
                    return false;
                if (entity.isDead) {
                    return false;
                }
                return true;
            }
        };
    }
}
