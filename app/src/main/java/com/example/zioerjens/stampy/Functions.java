package com.example.zioerjens.stampy;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

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

    public static Boolean isYoungerThanSec(String dateTime, int seconds){

        DateFormat format = new SimpleDateFormat("dd-MM-yyyy  HH:mm:ss", Locale.ENGLISH);
        Date date = new Date();
        try {
            date = format.parse(dateTime);
        } catch (Exception e){
            Log.e("isYoungerThanSec","String could not be converted to Date");
            return false;
        }

        Date later = new Date(date.getTime()+(seconds * 1000));

        Date now = new Date();

        if (now.after(later)){
            return false;
        }
        else {
            return true;
        }
    }
}
