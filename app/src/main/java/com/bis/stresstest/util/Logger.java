package com.bis.stresstest.util;

import android.util.Log;

public class Logger {


    /**
     * @param message
     *
     */
    public static void i(String message){
        Log.i(Config.TAG , message);
//        FileUtils.writeInternal(DateUtils.getCatalogData(), DateUtils.getDateToString() + "：" +message + "\n");

    }

    /**
     * @param message
     *
     */
    public static void e(String message){
        Log.e(Config.TAG , message);
//        FileUtils.writeInternal(DateUtils.getCatalogData(), DateUtils.getDateToString() + "：" +message + "\n");
    }


    /**
     * @param message
     */
    public static void d(String message){
        Log.i(Config.TAG , message);
//        FileUtils.writeInternal(DateUtils.getCatalogData(), DateUtils.getDateToString() + "：" +message + "\n");
    }
}
