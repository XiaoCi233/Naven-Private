package tech.blinkfix.modules.impl.render;

import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMetrics;

public final class WaterMark2TextMetrics {
    private WaterMark2TextMetrics() {
    }

    public static float getFontHeight(Font font) {
        FontMetrics metrics = font.getMetrics();
        return metrics.getDescent() - metrics.getAscent();
    }

    public static float getTopY(float centerY, Font font) {
        return centerY - getFontHeight(font) / 2.0f;
    }

    public static float getBaselineY(float centerY, Font font) {
        FontMetrics metrics = font.getMetrics();
        return centerY - (metrics.getAscent() + metrics.getDescent()) / 2.0f;
    }

    public static float getBaselineFromTop(float topY, Font font) {
        FontMetrics metrics = font.getMetrics();
        return topY - metrics.getAscent();
    }
}
