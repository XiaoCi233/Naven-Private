package com.surface.render.font;

public interface FontDrawer {
    void drawString(String s, double x, double y, int color);

    void drawStringWithShadow(String s, double x, double y, int color);

    void drawChar(char c, double x, double y, int color);

    void drawCharWithShadow(char c, double x, double y, int color);

    int getStringWidth(String s);

    double getStringWidthD(String s);

    int getHeight();

    double getHeightD();

    int getStringWidth2(String s);
}
