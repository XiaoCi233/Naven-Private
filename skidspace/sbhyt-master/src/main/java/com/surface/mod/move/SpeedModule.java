package com.surface.mod.move;

import com.surface.mod.Mod;
import com.surface.mod.move.speed.GroundStrafeSpeed;

public class SpeedModule extends Mod {
    public SpeedModule() {
        super("Speed", Category.MOVE);
        regitserSubModules(new GroundStrafeSpeed(this));
    }
}
