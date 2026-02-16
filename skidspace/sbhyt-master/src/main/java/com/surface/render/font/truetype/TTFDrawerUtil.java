package com.surface.render.font.truetype;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.IntBuffer;

class TTFDrawerUtil {
    private static final IntBuffer dataBuffer;

    static {
        dataBuffer = getDataBuffer();
    }

    static void uploadTexture(int textureID, BufferedImage image, int w, int h, boolean textureBlurred) {
        GlStateManager.bindTexture(textureID);

        int s = w * h;
        int k = 4194304 / w;
        int[] aint = new int[k * w];

        if (textureBlurred) {
            GL11.glTexParameteri(3553, 10241, 9729);
            GL11.glTexParameteri(3553, 10240, 9729);
        } else {
            GL11.glTexParameteri(3553, 10241, 9728);
            GL11.glTexParameteri(3553, 10240, 9728);
        }

        GL11.glTexParameteri(3553, 10242, 10496);
        GL11.glTexParameteri(3553, 10243, 10496);

        for(int l = 0; l < s; l += w * k) {
            int i1 = l / w;
            int j1 = Math.min(k, h - i1);
            int k1 = w * j1;
            image.getRGB(0, i1, w, j1, aint, 0, w);
            copyToBuffer(aint, k1);
            GL11.glTexSubImage2D(3553, 0, 0, i1, w, j1, 32993, 33639, getDataBuffer());
        }
    }

    private static void copyToBuffer(int[] p_copyToBuffer_0_, int p_copyToBuffer_1_) {
        copyToBufferPos(p_copyToBuffer_0_, 0, p_copyToBuffer_1_);
    }

    private static void copyToBufferPos(int[] p_copyToBufferPos_0_, int p_copyToBufferPos_1_, int p_copyToBufferPos_2_) {
        int[] aint = p_copyToBufferPos_0_;

        if (Minecraft.getMinecraft().gameSettings.anaglyph) {
            aint = TextureUtil.updateAnaglyph(p_copyToBufferPos_0_);
        }

        dataBuffer.clear();
        dataBuffer.put(aint, p_copyToBufferPos_1_, p_copyToBufferPos_2_);
        dataBuffer.position(0).limit(p_copyToBufferPos_2_);
    }

    private static IntBuffer getDataBuffer() {
        try {
            final Class<TextureUtil> c = TextureUtil.class;

            for (Field field : c.getDeclaredFields()) {
                if (field.getType() == IntBuffer.class) {
                    field.setAccessible(true);

                    return (IntBuffer) field.get(null);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to get data buffer.", e);
        }

        throw new RuntimeException("DataBuffer");
    }
}
