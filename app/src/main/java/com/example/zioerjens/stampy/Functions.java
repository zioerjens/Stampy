package com.example.zioerjens.stampy;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Functions {

    public static String generateNewCode(int length){

        final String CHAR_LIST ="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String code = "";
        for (int i = 0; i < length; i++){
            int randomPos = (int)(Math.random() * CHAR_LIST.length());
            code = code + CHAR_LIST.charAt(randomPos);
        }
        return code;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
