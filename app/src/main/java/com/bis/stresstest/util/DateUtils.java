package com.bis.stresstest.util;

import android.annotation.SuppressLint;
import android.app.Activity;

import com.bis.stresstest.model.AndroidPlatform;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @姓名 江海锋
 * @时间 2020/4/28 8:51
 * @作用 时间管理工具类
 */
public class DateUtils {

    public static String getDateToString() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    public static String getCatalogData() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        return format.format(new Date());
    }

    public static String getCatalogDatas() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(new Date()) + ".mp4";
    }

    public static String getCatalogDataLog() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(new Date());
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public static String getCurrentLocalDateTime(){
//        long remindTime = SystemClock.currentThreadTimeMillis();
//        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(remindTime ,  0 , ZoneOffset.ofHours(8));
//        return localDateTime.toString();
//    }


    public static String getData() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return date2TimeStamp(format.format(new Date()));
    }

    public static String date2TimeStamp(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.valueOf(sdf.parse(date).getTime() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 定时重启
     * 写入文件 在心跳时进行校验
     * 大于三天， 重启
     * @param activity
     */
    public static void checkTimeDiffer3Day(Activity activity) {
        if(!new File(Config.FileAssets.OUTSIDE_ROBOT_TIME).exists()){
            try {
                new File(Config.FileAssets.OUTSIDE_ROBOT_TIME).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String readTime = FileUtils.readExternal(Config.FileAssets.OUTSIDE_ROBOT_TIME);
        if("".equals(readTime) || isTimeDiffer3(Long.parseLong(readTime))){
            FileUtils.writeTxt(String.valueOf(System.currentTimeMillis()) , Config.FileAssets.ROBOT_TIME);
            readTime = String.valueOf(System.currentTimeMillis());
        }
        if(isTimeDiffer3(Long.parseLong(readTime))){
            Logger.d("定时重启时间到啦");
            FileUtils.delete(activity , Config.FileAssets.OUTSIDE_ROBOT_TIME);
            AppTool.reboot(activity);
        }else {
            Logger.d("已运行："  +getDevicesRunTime()  +"小时，还有" + getRunTime() + "小时重启！");
        }
    }

    /**
     * 获取剩余时间
     * @return
     */
    public static double getRunTime(){
        long readTime = Long.parseLong(FileUtils.readExternal(Config.FileAssets.OUTSIDE_ROBOT_TIME));
        long currentTime = System.currentTimeMillis();
        return AndroidPlatform.setDouble(((double)setRebootTime() - (currentTime - readTime) / 1000)/(60*60));
    }

    /**
     * 获取已运行时间
     * @return
     */
    public static double getDevicesRunTime(){
        long readTime = Long.parseLong(FileUtils.readExternal(Config.FileAssets.OUTSIDE_ROBOT_TIME));
        long currentTime = System.currentTimeMillis();
        return AndroidPlatform.setDouble(((double)(currentTime - readTime) / 1000)/(60*60));
    }

    /**
     * 比较两个日期的大小，日期格式为yyyy-MM-dd
     *
     * @param str1 the first date
     * @param str2 the second date
     * @return true <br/>false
     */
    public static boolean isDateOneBigger(String str1, String str2) {
        boolean isBigger = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date dt1 = null;
        Date dt2 = null;
        try {
            dt1 = sdf.parse(str1);
            dt2 = sdf.parse(str2);
        } catch (ParseException e) {
            Logger.d("版本校验出错：" + e.toString());
            e.printStackTrace();
        }
        if (dt1.getTime() > dt2.getTime()) {
            isBigger = true;
        } else if (dt1.getTime() < dt2.getTime()) {
            isBigger = false;
        }
        return isBigger;
    }



    /**
     * 设置当前定时重启的时间
     * 单位：秒
     * @return
     */
    public static int setRebootTime(){
        return 60 * 60 * 24 * 3;
    }


    /**
     * @param dataTime
     * @return
     */
    public static boolean isTimeDiffer3(long dataTime) {
        long clickTime = System.currentTimeMillis();
        long timeDiffer = clickTime - dataTime;
//        60 * 60 * 24 * 3
        return  (timeDiffer / 1000 > setRebootTime());
    }
}
