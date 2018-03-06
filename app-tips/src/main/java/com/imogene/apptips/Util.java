package com.imogene.apptips;

/**
 * Created by Admin on 25.04.2016.
 */
class Util {

    static void checkNonNullParameter(Object parameter, String name){
        if(parameter == null){
            throw new IllegalArgumentException(
                    "The " + name + " parameter must not be null.");
        }
    }
}
