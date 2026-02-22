package tech.blinkfix.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SmoothAnimationTimerTest {
    @Test
    void updateMovesTowardTarget() {
        AnimationUtils.delta = 16;
        SmoothAnimationTimer timer = new SmoothAnimationTimer(100.0F, 0.0F, 0.4F);
        timer.update(true);
        assertTrue(timer.value > 0.0F);
        for (int i = 0; i < 20; i++) {
            timer.update(true);
        }
        assertTrue(timer.value <= 100.0F);
    }

    @Test
    void updateMovesTowardZeroWhenDisabled() {
        AnimationUtils.delta = 16;
        SmoothAnimationTimer timer = new SmoothAnimationTimer(100.0F, 100.0F, 0.4F);
        timer.update(false);
        assertTrue(timer.value < 100.0F);
    }
}
