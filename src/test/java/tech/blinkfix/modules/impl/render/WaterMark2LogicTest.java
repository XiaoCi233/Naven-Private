package tech.blinkfix.modules.impl.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WaterMark2LogicTest {

    @Test
    public void springMovesTowardsTarget() {
        float pos = 0.0f;
        float vel = 0.0f;
        for (int i = 0; i < 120; i++) {
            WaterMark2Spring.Result result = WaterMark2Spring.step(pos, 100.0f, vel, 0.05f, 0.3f);
            pos = result.position;
            vel = result.velocity;
        }
        assertTrue(pos > 90.0f);
        assertTrue(Math.abs(vel) < 5.0f);
    }

    @Test
    public void bpsTrackerRespectsInterval() {
        WaterMark2BpsTracker tracker = new WaterMark2BpsTracker(100L);
        assertEquals(0.0, tracker.update(0.0, 0.0, 0L), 0.0001);
        assertEquals(20.0, tracker.update(1.0, 0.0, 120L), 0.0001);
        assertEquals(20.0, tracker.update(2.0, 0.0, 150L), 0.0001);
    }
}
