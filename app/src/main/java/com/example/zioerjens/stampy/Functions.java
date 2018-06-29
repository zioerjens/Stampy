package com.example.zioerjens.stampy;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Functions {

    final static int SUCCESS = 0;
    final static int FAILURE = 1;

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

    public static void showValidPopUp(final Activity activity, final int messageId) {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
        View mView = activity.getLayoutInflater().inflate(R.layout.show_success, null);
        TextView title = (TextView) mView.findViewById(R.id.successTitle);

        switch (messageId) {
            case 0:
                mView.findViewById(R.id.success).setBackgroundResource(R.drawable.success);
                title.setText(R.string.stampSuccessful);
                break;
            case 1:
                mView.findViewById(R.id.success).setBackgroundResource(R.drawable.failure);
                title.setText(R.string.stampUnsuccessful);
                break;
        }

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        new CountDownTimer(3000, 1000) { // 5000 = 5 sec

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {

                dialog.dismiss();

            }
        }.start();
    }

    public static Boolean hasInternetConnection(Activity activity){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
