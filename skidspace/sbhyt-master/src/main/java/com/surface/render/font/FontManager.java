package com.surface.render.font;

import com.surface.render.font.truetype.FontRecorder;
import com.surface.render.font.truetype.TrueTypeFontDrawer;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FontManager {
    public static TrueTypeFontDrawer MUSEO900;
    public static TrueTypeFontDrawer MUSEO700;
    public static TrueTypeFontDrawer TAHOMA;
    public static TrueTypeFontDrawer WQY;

    public static void init() throws FontFormatException, IOException {
        MUSEO900 = create("/assets/minecraft/surface/fonts/museo900.ttf", true, true, true);
        MUSEO700 = create("/assets/minecraft/surface/fonts/museo700.ttf", true, true, true);
        TAHOMA = create("/assets/minecraft/surface/fonts/rubik.ttf", true, true, true);
        WQY = create("/assets/minecraft/surface/fonts/wqy_microhei.ttf", true, true, true);
    }

    private static TrueTypeFontDrawer create(String path, boolean antiAliasing, boolean fractionalMetrics, boolean textureBlurred) throws FontFormatException, IOException {
        final InputStream is = FontManager.class.getResourceAsStream(path);

        if (is == null) {
            throw new FileNotFoundException(path);
        }

        final Font font;

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, is);
        } finally {
            IOUtils.closeQuietly(is);
        }

        return new TrueTypeFontDrawer(new FontRecorder[] {
                new FontRecorder(font, antiAliasing, fractionalMetrics, textureBlurred)
        });
    }
}
