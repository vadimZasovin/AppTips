package com.imogene.apptips;

import android.os.Build;

/**
 * Created by Admin on 25.04.2016.
 */
class Util {

    static float sinDegrees(float degrees){
        return (float) Math.sin(Math.toRadians(degrees));
    }

    static boolean checkApiVersion(int apiVersion){
        return Build.VERSION.SDK_INT >= apiVersion;
    }

    static void checkNonNullParameter(Object parameter, String name){
        if(parameter == null){
            throw new IllegalArgumentException(
                    "The " + name + " parameter must not be null.");
        }
    }
}
