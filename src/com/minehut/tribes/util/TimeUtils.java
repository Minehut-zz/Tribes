package com.minehut.tribes.util;

import java.text.DecimalFormat;

/**
 * Created by luke on 7/16/15.
 */
public class TimeUtils {
    public static String format(double ticks) {
        DecimalFormat df = new DecimalFormat("0.0");
        double ticksPerSecond = 20;
        double d = (ticks / ticksPerSecond);

        return df.format(d);
    }
}
