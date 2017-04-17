package com.imogene.apptips;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

/**
 * Created by Admin on 25.04.2016.
 */
class Util {

    static int convertDpInPixels(Context context, int dp){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return dp * displayMetrics.densityDpi / 160;
    }

    static float sinDegrees(float degrees){
        return (float) Math.sin(Math.toRadians(degrees));
    }

    static boolean checkApiVersion(int apiVersion){
        return Build.VERSION.SDK_INT >= apiVersion;
    }
}
