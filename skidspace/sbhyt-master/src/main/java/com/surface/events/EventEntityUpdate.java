package com.surface.events;

import com.cubk.event.impl.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public final class EventEntityUpdate implements Event {
    private final EntityLivingBase entity;

    public EventEntityUpdate(Entity targetEntity) {
        this.entity = (EntityLivingBase) targetEntity;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }
}
