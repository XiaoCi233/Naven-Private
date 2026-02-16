package com.surface.util;

public class MouseUtils {
    public static boolean bounding(float mouseX, float mouseY, float x, float y, float width, float height) {
        return mouseX > x && mouseX <= x + width && mouseY > y && mouseY <= y + height;
    }
}
