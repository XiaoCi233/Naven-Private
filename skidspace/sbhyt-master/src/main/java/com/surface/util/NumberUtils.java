package com.surface.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

public final class NumberUtils {
    private static final HashMap<Integer, DecimalFormat> formatHashMap = new HashMap<>();
    public static Random random = new Random();

    public static String roundToString(int i, double d) {
        if (Double.isNaN(d)) {
            return "NaN";
        }

        if (d == Double.POSITIVE_INFINITY) {
            return "+∞";
        }

        if (d == Double.NEGATIVE_INFINITY) {
            return "-∞";
        }

        DecimalFormat decimalFormat = formatHashMap.get(i);

        if (decimalFormat == null) {
            decimalFormat = new DecimalFormat();

            decimalFormat.setMinimumFractionDigits(i);
            decimalFormat.setMaximumFractionDigits(i);

            formatHashMap.put(i, decimalFormat);
        }

        return decimalFormat.format(d);
    }

    public static String roundToString(int i, float f) {
        return roundToString(i, (double) f);
    }

    public static double round(double d, int scale) {
        return new BigDecimal(d).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    public static int getRandom(final int min, final int max) {
        if (max < min) {
            return 0;
        }
        return min + random.nextInt((max - min) + 1);
    }

    public static double getRandom(double min, double max) {
        final double range = max - min;

        double scaled = random.nextDouble() * range;
        if (scaled > max) scaled = max;

        double shifted = scaled + min;
        if (shifted > max) shifted = max;

        return shifted;
    }

    public static boolean inRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
}
