package com.surface.render.font.truetype;

import java.awt.*;
import java.util.HashMap;

public class FontRecorder {
    private final HashMap<Integer, Font> derivedFonts;

    private final Font font;
    private final boolean antiAliasing;
    private final boolean fractionalMetrics;
    private final boolean textureBlurred;

    public FontRecorder(Font font, boolean antiAliasing, boolean fractionalMetrics, boolean textureBlurred) {
        this.derivedFonts = new HashMap<>();
        this.font = font;
        this.antiAliasing = antiAliasing;
        this.fractionalMetrics = fractionalMetrics;
        this.textureBlurred = textureBlurred;
    }

    public boolean canDisplay(char c) {
        return font.canDisplay(c);
    }

    public Font font(int size) {
        return derivedFonts.computeIfAbsent(size, (k) -> font.deriveFont(Font.PLAIN, k));
    }

    public boolean isAntiAliasing() {
        return antiAliasing;
    }

    public boolean isFractionalMetrics() {
        return fractionalMetrics;
    }

    public boolean isTextureBlurred() {
        return textureBlurred;
    }
}
