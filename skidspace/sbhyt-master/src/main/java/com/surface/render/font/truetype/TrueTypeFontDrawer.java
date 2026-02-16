package com.surface.render.font.truetype;

import com.surface.render.font.FontDrawer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class TrueTypeFontDrawer implements FontDrawer {
    private static final String RANDOM_STRING = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
    private static final String COLOR_CODE = "0123456789abcdef";
    private static final int[] COLORS = new int[32];

    private static final int MASK_BOLD = 0x00000001;
    private static final int MASK_ITALIC = 0x00000002;
    private static final int MASK_STRIKETHROUGH = 0x00000004;
    private static final int MASK_UNDERLINE = 0x00000008;
    private static final int MASK_RANDOM = 0x00000010;

    private static boolean STATE_GL_BLEND = false;
    private static boolean STATE_GL_TEXTURE_2D = false;

    static {
        for (int i = 0; i < COLORS.length; i++) {
            final int offset = (i >> 3 & 1) * 85;

            int red = (i >> 2 & 1) * 170 + offset;
            int green = (i >> 1 & 1) * 170 + offset;
            int blue = (i & 1) * 170 + offset;

            if (i == 6) {
                red += 85;
            }

            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            COLORS[i] = (red & 255) << 16 | (green & 255) << 8 | blue & 255;
        }
    }

    private final HashMap<Integer, SizeInfo> sizeInfoMap;
    private final FontRecorder[] fontRecorders;

    private BufferedImage buffer;
    private Graphics2D graphics;

    private int curSize;

    public TrueTypeFontDrawer(FontRecorder[] fontRecorders) {
        this.fontRecorders = fontRecorders;
        this.curSize = -1;

        this.sizeInfoMap = new HashMap<>();
    }

    public String trimStringToWidth(CharSequence text, float width) {
        return trimStringToWidth(text, width, false);
    }

    public String trimStringToWidth(CharSequence text, float initialValue, float width) {
        return trimStringToWidth(text, width, initialValue, false);
    }

    public String trimStringToWidth(CharSequence text, float width, boolean reverse) {
        return trimStringToWidth(text, width, 0.0f, reverse);
    }

    public float getMiddleOfBox(float height) {
        return height / 2f - getHeight() / 2f;
    }


    public String trimStringToWidth(CharSequence text, float width, float initialValue, boolean reverse) {
        StringBuilder builder = new StringBuilder();

        float f = initialValue;
        int i = reverse ? text.length() - 1 : 0;
        int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int k = i; k >= 0 && k < text.length() && f < width; k += j) {
            char c0 = text.charAt(k);
            float f1 = getStringWidth2(String.valueOf(c0));

            if (flag) {
                flag = false;

                if (c0 != 'l' && c0 != 'L') {
                    if (c0 == 'r' || c0 == 'R') {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (f1 < 0.0F) {
                flag = true;
            } else {
                f += f1;
                if (flag1) ++f;
            }

            if (f > width) break;

            if (reverse) {
                builder.insert(0, c0);
            } else {
                builder.append(c0);
            }
        }

        return builder.toString();
    }


    @Override
    public void drawString(String s, double x, double y, int color) {
        drawString(s, x, y, color, false);
    }

    @Override
    public void drawStringWithShadow(String s, double x, double y, int color) {
        drawString(s, x + 0.5, y + 0.5, color, true);
        drawString(s, x, y, color, false);
    }

    @Override
    public void drawChar(char c, double x, double y, int color) {
        final Glyph glyph = getGlyph(c);

        checkTexture(c, glyph);

        preDraw();
        glyph.draw(x, y, color, false);
        postDraw();
    }

    @Override
    public void drawCharWithShadow(char c, double x, double y, int color) {
        drawChar(c, x + 0.5, y + 0.5, getShadowColor(color));
        drawChar(c, x, y, color);
    }

    public int getStringWidth2(String s) {
        return MathHelper.floor_double(getStringWidthD2(s));
    }

    public double getStringWidthD2(String s) {
        if (s == null || s.isEmpty()) {
            return 0.0;
        }

        final int len = s.length();

        double width = 0.0;

        for (int i = 0; i < len; i++) {
            if (s.charAt(i) == '§') {
                width -= getGlyph(s.charAt(i + 1)).width;
                continue;
            }
            width += getGlyph(s.charAt(i)).width;
        }

        return width;
    }


    @Override
    public int getStringWidth(String s) {
        return MathHelper.floor_double(getStringWidthD(s));
    }

    @Override
    public double getStringWidthD(String s) {
        if (s == null || s.isEmpty()) {
            return 0.0;
        }

        final int len = s.length();

        double width = 0.0;

        for (int i = 0; i < len; i++) {
            width += getGlyph(s.charAt(i)).width;
        }

        return width;
    }

    @Override
    public int getHeight() {
        return MathHelper.floor_double(getHeightD());
    }

    @Override
    public double getHeightD() {
        return curSize / 2.0;
    }

    private void drawString(String s, double x, double y, int color, boolean shadow) {
        if (s == null || s.isEmpty()) {
            return;
        }

        if ((color & -67108864) == 0) {
            color |= -16777216;
        }

        final int initialColor = color;
        final int len = s.length();

        int flags = 0;

        preDraw();

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            if (c == '§' && i < len - 1) {
                c = s.charAt(++i);

                switch (c) {
                    case 'k': {
                        flags |= MASK_RANDOM;
                        break;
                    }
                    case 'l': {
                        flags |= MASK_BOLD;
                        break;
                    }
                    case 'o': {
                        flags |= MASK_ITALIC;
                        break;
                    }
                    case 'n': {
                        flags |= MASK_UNDERLINE;
                        break;
                    }
                    case 'm': {
                        flags |= MASK_STRIKETHROUGH;
                        break;
                    }
                    case 'r': {
                        flags = 0;
                        color = initialColor;
                        break;
                    }
                    default: {
                        int colorIndex = COLOR_CODE.indexOf(c);

                        flags = 0;

                        if (colorIndex == -1) {
                            colorIndex = 15;
                        }

                        color = COLORS[colorIndex] | (((initialColor >> 24) & 0xFF) << 24);

                        break;
                    }
                }
            } else {
                final Glyph glyph = getGlyph(c);

                if ((flags & MASK_RANDOM) != 0 && RANDOM_STRING.indexOf(c) != -1) {
                    final float width = getGlyph(c).width;

                    do {
                        c = RANDOM_STRING.charAt(ThreadLocalRandom.current().nextInt(RANDOM_STRING.length()));
                    } while (width != getGlyph(c).width);
                }

                checkTexture(c, glyph);

                drawGlyph(glyph, x, y, flags, shadow, color);

                if ((flags & MASK_BOLD) != 0) {
                    x += 0.5;
                }

                x += glyph.width;
            }
        }

        postDraw();
    }

    private void drawGlyph(Glyph glyph, double x, double y, int flags, boolean shadow, int color) {
        if (shadow) {
            color = getShadowColor(color);
        }

        final boolean italic = (flags & MASK_ITALIC) != 0;

        if ((flags & MASK_BOLD) != 0) {
            glyph.draw(x + 0.5, y, color, italic);
        }

        glyph.draw(x, y, color, italic);

        if (!shadow) {
            if ((flags & MASK_STRIKETHROUGH) != 0) {
                drawStrikethrough(glyph, x, y, color);
            }

            if ((flags & MASK_UNDERLINE) != 0) {
                drawUnderLine(glyph, x, y, color);
            }
        }
    }

    private Glyph getGlyph(char c) {
        final SizeInfo sizeInfo = getCurSizeInfo();

        return sizeInfo.glyphMap.computeIfAbsent(c, this::createGlyph);
    }

    private Glyph createGlyph(char c) {
        final SizeInfo sizeInfo = getCurSizeInfo();

        FontRecorder fontRecorder = null;

        for (FontRecorder fr : fontRecorders) {
            if (fr.canDisplay(c)) {
                fontRecorder = fr;
                break;
            }
        }

        if (fontRecorder == null) {
            return sizeInfo.unsupportedGlyph;
        }

        graphics.setFont(fontRecorder.font(sizeInfo.fontSize));

        final FontMetrics fontMetrics = graphics.getFontMetrics();

        return new Glyph(sizeInfo, fontRecorder, fontMetrics.getStringBounds(String.valueOf(c), graphics).getBounds().width);
    }

    private void checkBuffer(int size) {
        if (buffer == null || size > buffer.getWidth()) {
            growBuffer(size);
        }
    }

    private void growBuffer(int size) {
        this.buffer = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        this.graphics = this.buffer.createGraphics();
        this.graphics.setBackground(new Color(0, 0, 0, 0));
        this.graphics.setColor(Color.WHITE);
    }

    private void checkTexture(char c, Glyph glyph) {
        if (glyph.textureID == -1) {
            final FontRecorder fontRecorder = glyph.fontRecorder;
            final int textureSize = glyph.sizeInfo.textureSize;
            final int fontSize = glyph.sizeInfo.fontSize;
            final Graphics2D g = this.graphics;

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, fontRecorder.isAntiAliasing() ? RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fontRecorder.isFractionalMetrics() ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g.setFont(fontRecorder.font(fontSize));
            g.clearRect(0, 0, textureSize, textureSize);
            g.drawString(String.valueOf(c), 0, fontSize - fontSize / 8);

            glyph.uploadTexture(fontRecorder.isTextureBlurred());
        }
    }

    public void setFontSize(int fontSize) {
        this.curSize = fontSize;

        checkBuffer(computeTextureSize(fontSize));
        this.sizeInfoMap.computeIfAbsent(fontSize, SizeInfo::new);
    }

    private SizeInfo getCurSizeInfo() {
        return sizeInfoMap.get(curSize);
    }

    private void drawStrikethrough(Glyph glyph, double x, double y, int color) {
        final double v = y + curSize / 4.0;

        drawRect(x, v, x + glyph.width, v + 1.0, color);
    }

    private void drawUnderLine(Glyph glyph, double x, double y, int color) {
        final double v = y + curSize / 2.0;

        drawRect(x, v, x + glyph.width, v + 1.0, color);
    }

    private static int computeTextureSize(int fontSize) {
        return fontSize + MathHelper.floor_double(fontSize / 8.0) + 2;
    }

    private static int getShadowColor(int color) {
        return (color & 16579836) >> 2 | color & -16777216;
    }

    private static void preDraw() {
        STATE_GL_BLEND = GL11.glIsEnabled(GL11.GL_BLEND);
        STATE_GL_TEXTURE_2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);

        if (!STATE_GL_BLEND) {
            GlStateManager.enableBlend();
        }

        if (!STATE_GL_TEXTURE_2D) {
            GlStateManager.enableTexture2D();
        }

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void postDraw() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        if (!STATE_GL_BLEND) {
            GlStateManager.disableBlend();
        }

        if (!STATE_GL_TEXTURE_2D) {
            GlStateManager.disableTexture2D();
        }
    }

    private static void drawRect(double left, double top, double right, double bottom, int color) {
        if (left < right) {
            final double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            final double j = top;
            top = bottom;
            bottom = j;
        }

        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        final float r = (float) (color >> 16 & 255) / 255F;
        final float g = (float) (color >> 8 & 255) / 255F;
        final float b = (float) (color & 255) / 255F;
        final float a = (float) (color >> 24 & 255) / 255F;

        GlStateManager.disableTexture2D();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0).color(r, g, b, a).endVertex();
        worldrenderer.pos(right, bottom, 0.0).color(r, g, b, a).endVertex();
        worldrenderer.pos(right, top, 0.0).color(r, g, b, a).endVertex();
        worldrenderer.pos(left, top, 0.0).color(r, g, b, a).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    public void drawCenteredString(String text, double x, double y, int color) {
        this.drawString(text, x - (float) (this.getStringWidth(text) / 2), y, color);
    }

    private final class SizeInfo {
        private final HashMap<Character, Glyph> glyphMap;
        private final Glyph unsupportedGlyph;
        private final int textureSize;
        private final int fontSize;

        public SizeInfo(int fontSize) {
            this.fontSize = fontSize;
            this.glyphMap = new HashMap<>();
            this.textureSize = computeTextureSize(fontSize);
            this.unsupportedGlyph = createUnsupportedGlyph();
        }

        private Glyph createUnsupportedGlyph() {
            final Glyph glyph = new Glyph(this, null, this.textureSize);

            graphics.clearRect(0, 0, textureSize, textureSize);
            graphics.drawRect(0, 0, textureSize, textureSize);
            glyph.uploadTexture(false);

            return glyph;
        }
    }

    private class Glyph {
        final SizeInfo sizeInfo;
        final FontRecorder fontRecorder;
        final float width;

        int textureID;

        Glyph(SizeInfo sizeInfo, FontRecorder fontRecorder, int width) {
            this.sizeInfo = sizeInfo;
            this.fontRecorder = fontRecorder;
            this.width = width / 2.0F;
            this.textureID = -1;
        }

        void uploadTexture(boolean textureBlurred) {
            final int textureSize = this.sizeInfo.textureSize;

            this.textureID = TextureUtil.glGenTextures();

            TextureUtil.allocateTexture(textureID, textureSize, textureSize);
            TTFDrawerUtil.uploadTexture(textureID, buffer, textureSize, textureSize, textureBlurred);
        }

        void draw(double x, double y, int color, boolean italic) {
            GlStateManager.bindTexture(textureID);

            final double offset = italic ? 2.0 : 0.0;

            final Tessellator tessellator = Tessellator.getInstance();
            final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            final float r = (float) (color >> 16 & 255) / 255F;
            final float g = (float) (color >> 8 & 255) / 255F;
            final float b = (float) (color & 255) / 255F;
            final float a = (float) (color >> 24 & 255) / 255F;
            final double textureSize = sizeInfo.textureSize / 2.0;

            worldRenderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldRenderer.pos(x + offset, y, 0.0).tex(0, 0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x - offset, y + textureSize, 0.0).tex(0, 1).color(r, g, b, a).endVertex();
            worldRenderer.pos(x + offset + textureSize, y, 0.0).tex(1, 0).color(r, g, b, a).endVertex();
            worldRenderer.pos(x - offset + textureSize, y + textureSize, 0.0).tex(1, 1).color(r, g, b, a).endVertex();
            tessellator.draw();
        }
    }
}
