package com.imogene.apptips;

/**
 * Created by Admin on 25.04.2016.
 */
class Util {

    // TODO remove this function
    static float sinDegrees(float degrees){
        return (float) Math.sin(Math.toRadians(degrees));
    }

    static void checkNonNullParameter(Object parameter, String name){
        if(parameter == null){
            throw new IllegalArgumentException(
                    "The " + name + " parameter must not be null.");
        }
    }
}
