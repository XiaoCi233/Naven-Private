package com.heypixel.heypixelmod.utils;

import java.awt.Color;

public class ColorUtil {
    /**
     * 设置颜色透明度
     * @param color 原始颜色
     * @param alpha 透明度(0.0 ~ 1.0)
     * @return 新颜色
     */
    public static Color applyOpacity(Color color, float alpha) {
        alpha = Math.max(0.0f, Math.min(1.0f, alpha)); // 限制范围
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255));
    }
}