package tech.blinkfix.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnimationUtilsTest {
    @Test
    void getAnimationStateMovesTowardTarget() {
        AnimationUtils.delta = 16;
        float value = AnimationUtils.getAnimationState(0.0F, 10.0F, 100.0F);
        assertTrue(value > 0.0F);
    }
}
