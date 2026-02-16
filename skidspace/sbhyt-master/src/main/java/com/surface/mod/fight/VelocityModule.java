package com.surface.mod.fight;

import com.surface.mod.Mod;
import com.surface.mod.fight.velocity.NormalVelocity;
import com.surface.mod.fight.velocity.VerticalVelocity;

public class VelocityModule extends Mod {
    public VelocityModule() {
        super("Velocity", Category.FIGHT);
        regitserSubModules(new NormalVelocity(this), new VerticalVelocity(this));
    }
}
