package com.surface.util.render;

import com.surface.util.render.shader.ShaderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import renderassist.rendering.BasicRendering;

import java.awt.*;

import static net.minecraft.client.renderer.GlStateManager.enableTexture2D;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;

public class RenderUtils {
    private static final Tessellator tessellator;
    private static final WorldRenderer worldrenderer;
    public static final ShaderUtils roundedShader = new ShaderUtils("roundedRect");
    public static final ShaderUtils roundedTextureShader = new ShaderUtils("roundedRectTexture");

    public static void bindTexture(int texture) {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * .01));
    }

    public static Color tripleColor(int rgbValue) {
        return tripleColor(rgbValue, 1);
    }

    public static void scaleStart(float x, float y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.translate(-x, -y, 0);
    }

    public static void scaleEnd() {
        GlStateManager.popMatrix();
    }

    public static Color tripleColor(int rgbValue, float alpha) {
        alpha = Math.min(1, Math.max(0, alpha));
        return new Color(rgbValue, rgbValue, rgbValue, (int) (255 * alpha));
    }

    public static void color(int color) {
        float f = (color >> 24 & 255) / 255.0f;
        float f1 = (color >> 16 & 255) / 255.0f;
        float f2 = (color >> 8 & 255) / 255.0f;
        float f3 = (color & 255) / 255.0f;
        GL11.glColor4f(f1, f2, f3, f);
    }

    public static void pre3D() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    public static void post3D() {
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    /**
     * @author 子龙 赵
     */
    public static void drawRoundTextured(float x, float y, float width, float height, float radius, float alpha, boolean topLeftCorner, boolean bottomLeftCorner, boolean topRightCorner, boolean bottomRightCorner) {
        GlStateManager.resetColor();
        GlStateManager.enableBlend();

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (0 * .01));

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
//        boolean flag = GL11.glIsEnabled(GL11.GL_ALPHA_TEST) && disableAlphaTest;
//        if (flag) {
//            GL11.glDisable(GL11.GL_ALPHA_TEST);
//        }

        roundedTextureShader.init();

        roundedTextureShader.setUniformi("textureIn", 0);
        setupRoundedRectUniforms(x, y, width, height, radius, roundedTextureShader);
        roundedTextureShader.setUniformi("blur", 0);
        roundedTextureShader.setUniformf("alpha", alpha);
        roundedTextureShader.setUniformf("corner", topLeftCorner ? 1 : 0, bottomLeftCorner ? 1 : 0, topRightCorner ? 1 : 0, bottomRightCorner ? 1 : 0);

        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedTextureShader.unload();
//        if (flag)
//            GL11.glEnable(GL11.GL_ALPHA_TEST);
        GlStateManager.disableBlend();
    }

    public static void scissorStart(double x, double y, double width, double height) {
        glEnable(GL_SCISSOR_TEST);
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        final double scale = sr.getScaleFactor();
        double finalHeight = height * scale;
        double finalY = (sr.getScaledHeight() - y) * scale;
        double finalX = x * scale;
        double finalWidth = width * scale;
        glScissor((int) finalX, (int) (finalY - finalHeight), (int) finalWidth, (int) finalHeight);
    }

    public static void scissorEnd() {
        glDisable(GL_SCISSOR_TEST);
    }

    public static void startGlScissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        int scaleFactor = 1;
        int k = mc.gameSettings.guiScale;
        if (k == 0) {
            k = 1000;
        }
        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        GL11.glPushMatrix();
        GL11.glEnable(3089);
        GL11.glScissor((int)(x * scaleFactor), (int)(mc.displayHeight - (y + height) * scaleFactor), (int)(width * scaleFactor), (int)(height * scaleFactor));
    }
    public static void stopGlScissor(){
        GL11.glDisable(3089);
        GL11.glPopMatrix();
    }
    public static void drawBoundingBox(double x, double y, double z, double width, double height, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        BasicRendering.glColor(color);
        drawBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GlStateManager.resetColor();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawBoundingBox(AxisAlignedBB bb) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        tessellator.draw();
    }


    static {
        tessellator = Tessellator.getInstance();
        worldrenderer = tessellator.getWorldRenderer();
    }


    public static void drawArrow(double x, double y, int lineWidth, int color, double length) {
        glEnable(3042);
        glDisable(3553);
        glBlendFunc(770, 771);
        glEnable(2848);
        GL11.glPushMatrix();
        GL11.glLineWidth(lineWidth);
        BasicRendering.glColor(color);
        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + 3, y + length);
        GL11.glVertex2d(x + 3 * 2, y);
        GL11.glEnd();
        GL11.glPopMatrix();
        glEnable(3553);
        glDisable(3042);
        glDisable(2848);
        enableTexture2D();
        GlStateManager.disableBlend();
        glColor4f(1, 1, 1, 1);
    }

    public static Color blendColors(final float[] fractions, final Color[] colors, final float progress) {
        if (fractions == null) {
            throw new IllegalArgumentException("Fractions can't be null");
        }
        if (colors == null) {
            throw new IllegalArgumentException("Colours can't be null");
        }
        if (fractions.length == colors.length) {
            final int[] getFractionBlack = getFraction(fractions, progress);
            final float[] range = new float[]{fractions[getFractionBlack[0]], fractions[getFractionBlack[1]]};
            final Color[] colorRange = new Color[]{colors[getFractionBlack[0]], colors[getFractionBlack[1]]};
            final float max = range[1] - range[0];
            final float value = progress - range[0];
            final float weight = value / max;
            return blend(colorRange[0], colorRange[1], 1.0f - weight);
        }
        throw new IllegalArgumentException("Fractions and colours must have equal number of elements");
    }

    public static Color blend(final Color color1, final Color color2, final double ratio) {
        final float r = (float) ratio;
        final float ir = 1.0f - r;
        final float[] rgb1 = new float[3];
        final float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        float red = rgb1[0] * r + rgb2[0] * ir;
        float green = rgb1[1] * r + rgb2[1] * ir;
        float blue = rgb1[2] * r + rgb2[2] * ir;
        if (red < 0.0f) {
            red = 0.0f;
        } else if (red > 255.0f) {
            red = 255.0f;
        }
        if (green < 0.0f) {
            green = 0.0f;
        } else if (green > 255.0f) {
            green = 255.0f;
        }
        if (blue < 0.0f) {
            blue = 0.0f;
        } else if (blue > 255.0f) {
            blue = 255.0f;
        }
        Color color3 = null;
        try {
            color3 = new Color(red, green, blue);
        } catch (final IllegalArgumentException exp) {
            exp.printStackTrace();
        }
        return color3;
    }


    public static int[] getFraction(final float[] fractions, final float progress) {
        int startPoint = 0;
        final int[] range = new int[2];
        while (startPoint < fractions.length && fractions[startPoint] <= progress) {
            ++startPoint;
        }
        if (startPoint >= fractions.length) {
            startPoint = fractions.length - 1;
        }
        range[0] = startPoint - 1;
        range[1] = startPoint;
        return range;
    }


    public static boolean isHovering(double x, double y, double width, double height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static void drawVGradientRect(double x, double y, double width, double height, int startColor, int endColor) {
        final float f = (float) (startColor >> 24 & 255) / 255.0F;
        final float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        final float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        final float f3 = (float) (startColor & 255) / 255.0F;
        final float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        final float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        final float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        final float f7 = (float) (endColor & 255) / 255.0F;
        OpenGLUtils.setup2DRendering(() -> {
            glShadeModel(GL_SMOOTH);
            worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(x + width, y, 0.0D).color(f1, f2, f3, f).endVertex();
            worldrenderer.pos(x, y, 0.0D).color(f1, f2, f3, f).endVertex();
            worldrenderer.pos(x, y + height, 0.0D).color(f5, f6, f7, f4).endVertex();
            worldrenderer.pos(x + width, y + height, 0.0D).color(f5, f6, f7, f4).endVertex();
            tessellator.draw();
            GlStateManager.resetColor();
            glShadeModel(GL_FLAT);
        });
    }

    public static int reAlpha(int color, int alpha) {
        Color c = new Color(color);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha).getRGB();
    }

    public static void drawHGradientRect(double x, double y, double width, double height, int startColor, int endColor) {
        final float f = (float) (startColor >> 24 & 255) / 255.0F;
        final float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        final float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        final float f3 = (float) (startColor & 255) / 255.0F;
        final float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        final float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        final float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        final float f7 = (float) (endColor & 255) / 255.0F;
        OpenGLUtils.setup2DRendering(() -> {
            glShadeModel(GL_SMOOTH);
            worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(x, y, 0.0D).color(f1, f2, f3, f).endVertex();
            worldrenderer.pos(x, y + height, 0.0D).color(f1, f2, f3, f).endVertex();
            worldrenderer.pos(x + width, y + height, 0.0D).color(f5, f6, f7, f4).endVertex();
            worldrenderer.pos(x + width, y, 0.0D).color(f5, f6, f7, f4).endVertex();
            tessellator.draw();
            GlStateManager.resetColor();
            glShadeModel(GL_FLAT);
        });
    }



    public static void drawRoundedRect(final double x,
                                       final double y,
                                       final double width,
                                       final double height,
                                       final RoundingMode roundingMode,
                                       final int roundingDef,
                                       final double roundingLevel,
                                       final int colour) {
        boolean bLeft = false;
        boolean tLeft = false;
        boolean bRight = false;
        boolean tRight = false;

        switch (roundingMode) {
            case TOP:
                tLeft = true;
                tRight = true;
                break;
            case BOTTOM:
                bLeft = true;
                bRight = true;
                break;
            case FULL:
                tLeft = true;
                tRight = true;
                bLeft = true;
                bRight = true;
                break;
            case LEFT:
                bLeft = true;
                tLeft = true;
                break;
            case RIGHT:
                bRight = true;
                tRight = true;
                break;
            case TOP_LEFT:
                tLeft = true;
                break;
            case TOP_RIGHT:
                tRight = true;
                break;
            case BOTTOM_LEFT:
                bLeft = true;
                break;
            case BOTTOM_RIGHT:
                bRight = true;
                break;
        }

        // Translate matrix to top-left of rect
        glTranslated(x, y, 0);
        // Enable triangle anti-aliasing
        glEnable(GL_POLYGON_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        // Enable blending
        final boolean restore = enableBlend();

        if (tLeft) {
            // Top left
            glDrawFilledEllipse(roundingLevel, roundingLevel, roundingLevel,
                    (int) (roundingDef * 0.5), (int) (roundingDef * 0.75),
                    roundingDef, false, colour);
        }

        if (tRight) {
            // Top right
            glDrawFilledEllipse(width - roundingLevel, roundingLevel, roundingLevel,
                    (int) (roundingDef * 0.75), roundingDef,
                    roundingDef, false, colour);
        }

        if (bLeft) {
            // Bottom left
            glDrawFilledEllipse(roundingLevel, height - roundingLevel, roundingLevel,
                    (int) (roundingDef * 0.25), (int) (roundingDef * 0.5),
                    roundingDef, false, colour);
        }

        if (bRight) {
            // Bottom right
            glDrawFilledEllipse(width - roundingLevel, height - roundingLevel, roundingLevel,
                    0, (int) (roundingDef * 0.25),
                    roundingDef, false, colour);
        }

        // Enable triangle anti-aliasing (to save performance on next poly draw)
        glDisable(GL_POLYGON_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);

        // Disable texture drawing
        glDisable(GL_TEXTURE_2D);
        // Set colour
        BasicRendering.glColor(colour);

        // Begin polygon
        glBegin(GL_POLYGON);
        {
            if (tLeft) {
                glVertex2d(roundingLevel, roundingLevel);
                glVertex2d(0, roundingLevel);
            } else {
                glVertex2d(0, 0);
            }

            if (bLeft) {
                glVertex2d(0, height - roundingLevel);
                glVertex2d(roundingLevel, height - roundingLevel);
                glVertex2d(roundingLevel, height);
            } else {
                glVertex2d(0, height);
            }

            if (bRight) {
                glVertex2d(width - roundingLevel, height);
                glVertex2d(width - roundingLevel, height - roundingLevel);
                glVertex2d(width, height - roundingLevel);
            } else {
                glVertex2d(width, height);
            }

            if (tRight) {
                glVertex2d(width, roundingLevel);
                glVertex2d(width - roundingLevel, roundingLevel);
                glVertex2d(width - roundingLevel, 0);
            } else {
                glVertex2d(width, 0);
            }

            if (tLeft) {
                glVertex2d(roundingLevel, 0);
            }
        }
        // Draw polygon
        glEnd();

        // Disable blending
        disableBlend(restore);
        // Translate matrix back (instead of creating a new matrix with glPush/glPop)
        glTranslated(-x, -y, 0);
        // Re-enable texture drawing
        glEnable(GL_TEXTURE_2D);
    }

    public static void glDrawFilledEllipse(final double x,
                                           final double y,
                                           final double radius,
                                           final int startIndex,
                                           final int endIndex,
                                           final int polygons,
                                           final boolean smooth,
                                           final int colour) {
        // Enable blending
        final boolean restore = enableBlend();

        if (smooth) {
            // Enable anti-aliasing
            glEnable(GL_POLYGON_SMOOTH);
            glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
        }
        // Disable texture drawing
        glDisable(GL_TEXTURE_2D);
        // Set color
        BasicRendering.glColor(colour);
        // Required because of minecraft optimizations
        glDisable(GL_CULL_FACE);

        // Begin triangle fan
        glBegin(GL_POLYGON);
        {
            // Specify center vertex
            glVertex2d(x, y);

            for (double i = startIndex; i <= endIndex; i++) {
                final double theta = 2.0 * Math.PI * i / polygons;
                // Specify triangle fan vertices in a circle (size=radius) around x & y
                glVertex2d(x + radius * Math.cos(theta), y + radius * Math.sin(theta));
            }
        }
        // Draw the triangle fan
        glEnd();

        // Disable blending
        disableBlend(restore);

        if (smooth) {
            // Disable anti-aliasing
            glDisable(GL_POLYGON_SMOOTH);
            glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE);
        }
        // See above
        glEnable(GL_CULL_FACE);
        // Re-enable texture drawing
        glEnable(GL_TEXTURE_2D);
    }

    public static void maskRound(final float x, final float y, final float width, final float height, final float radius) {
        GlStateManager.enableTexture2D();
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        doArc(x + width - radius, y + height - radius, radius, 0, 90);
        doArc(x + width - radius, y + radius, radius, 90, 180);
        doArc(x + radius, y + radius, radius, 180, 270);
        doArc(x + radius, y + height - radius, radius, 270, 360);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_FASTEST);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        doArc(x + width - radius, y + height - radius, radius, 0, 90);
        doArc(x + width - radius, y + radius, radius, 90, 180);
        doArc(x + radius, y + radius, radius, 180, 270);
        doArc(x + radius, y + height - radius, radius, 270, 360);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.resetColor();
        GlStateManager.disableBlend();
    }

    public static void doArc(float x, float y, float radius, float startAngle, float endAngle) {
        for (float angle = startAngle; angle <= endAngle; angle += (endAngle - startAngle) / 10) {
            GL11.glVertex2f(x + (float) Math.sin(Math.toRadians(angle)) * radius, y + (float) Math.cos(Math.toRadians(angle)) * radius);
        }
    }

    public static void drawRoundedRect(final double x, final double y, final float width, final float height, final float radius, final int colour) {
        drawRound((float) x, (float) y, width, height, radius, false, new Color(colour, true), true, true, true, true);
    }

    /**
     * @author 子龙 赵
     */
    public static void drawRound(float x, float y, float width, float height, float radius, boolean blur, Color color, boolean topLeftCorner, boolean bottomLeftCorner, boolean topRightCorner, boolean bottomRightCorner) {
        GlStateManager.resetColor();
        GlStateManager.enableBlend();

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (0 * .01));

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        boolean flag = GL11.glIsEnabled(GL11.GL_ALPHA_TEST) && disableAlphaTest;
//        if (flag) {
//            GL11.glDisable(GL11.GL_ALPHA_TEST);
//        }

        roundedShader.init();

        float alpha = color.getAlpha() / 255f;
//        if (alpha < 0.101 && !disableAlphaTest) {
//            alpha = 0.101f;
//        }

        setupRoundedRectUniforms(x, y, width, height, radius, roundedShader);
        roundedShader.setUniformi("blur", blur ? 1 : 0);
        roundedShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
        roundedShader.setUniformf("corner", topLeftCorner ? 1 : 0, bottomLeftCorner ? 1 : 0, topRightCorner ? 1 : 0, bottomRightCorner ? 1 : 0);

        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedShader.unload();
//        if (flag)
//            GL11.glEnable(GL11.GL_ALPHA_TEST);
        GlStateManager.disableBlend();
    }

    private static void setupRoundedRectUniforms(float x, float y, float width, float height, float radius, ShaderUtils roundedTexturedShader) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        roundedTexturedShader.setUniformf("location", x * sr.getScaleFactor(),
                (Minecraft.getMinecraft().displayHeight - (height * sr.getScaleFactor())) - (y * sr.getScaleFactor()));
        roundedTexturedShader.setUniformf("rectSize", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        roundedTexturedShader.setUniformf("radius", radius * sr.getScaleFactor());
    }

    public static void drawItemStack(ItemStack stack, float x, float y) {
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableAlpha();
        GlStateManager.clear(256);
        Minecraft.getMinecraft().getRenderItem().zLevel = -150.0F;
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stack, x, y);
        Minecraft.getMinecraft().getRenderItem().renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRendererObj, stack, x, y, null);
        Minecraft.getMinecraft().getRenderItem().zLevel = 0.0F;
        GlStateManager.enableAlpha();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    public static void drawFramebuffer(final int framebufferTexture, final int width, final int height) {
        glBindTexture(GL_TEXTURE_2D, framebufferTexture);
        glDisable(GL_ALPHA_TEST);
        final boolean restore = enableBlend();
        glBegin(GL_QUADS);
        {
            glTexCoord2f(0, 1);
            glVertex2f(0, 0);

            glTexCoord2f(0, 0);
            glVertex2f(0, height);

            glTexCoord2f(1, 0);
            glVertex2f(width, height);

            glTexCoord2f(1, 1);
            glVertex2f(width, 0);
        }
        glEnd();
        disableBlend(restore);
        glEnable(GL_ALPHA_TEST);
    }

    public static boolean enableBlend() {
        final boolean wasEnabled = glIsEnabled(GL_BLEND);

        if (!wasEnabled) {
            glEnable(GL_BLEND);
            glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        }

        return wasEnabled;
    }

    public static void disableBlend(final boolean wasEnabled) {
        if (!wasEnabled) {
            glDisable(GL_BLEND);
        }
    }

    public static void drawImage(ResourceLocation imageLocation, double x, double y, double width, double height, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();
        Minecraft.getMinecraft().getTextureManager().bindTexture(imageLocation);
        BasicRendering.glColor(color);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        GlStateManager.resetColor();
        GlStateManager.bindTexture(0);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawModalRectWithCustomSizedTexture(double x, double y, float u, float v, double width, double height, double textureWidth, double textureHeight) {
        double f = 1.0F / textureWidth;
        double f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + (float) height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + (float) width) * f, (v + (float) height) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + (float) width) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public enum RoundingMode {
        TOP_LEFT,
        BOTTOM_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,

        LEFT,
        RIGHT,

        TOP,
        BOTTOM,

        FULL
    }
}
