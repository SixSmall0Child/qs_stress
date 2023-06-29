package com.bis.stresstest.app;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;

import com.bis.stresstest.activity.AgentActivity;
import com.bis.stresstest.util.Config;
import com.bis.stresstest.util.FileUtils;
import com.bis.stresstest.util.ShellUtils;

import java.lang.reflect.Method;


public class MyApplication extends Application {


    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    public static String filePath = "data/local/tmp/";
    private static AgentActivity mainActivity = null;

    public static AgentActivity getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(AgentActivity activity) {
        mainActivity = activity;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        mContext = getApplicationContext();

        //android13后通知需要权限
        ShellUtils.execCommand("pm grant " + getPackageName() + " android.permission.POST_NOTIFICATIONS", true);
        ShellUtils.execCommand("pm grant " + getPackageName() + " android.permission.ACCESS_FINE_LOCATION", true);
    }


    public static Context getContext() {
        return mContext;
    }


    /**
     * 拿IP ， 需要的时候才拿，只拿一次
     */
    public static String getDevicesIpAddress() {
        Config.devicesIp = FileUtils.getIPAddress();
        if (Config.devicesIp == null) {
            return "";
        } else {
            return Config.devicesIp;
        }
    }

    /**
     * 拿序列
     */
    @SuppressLint({"MissingPermission", "NewApi"})
    public static String getDevicesIpAddressAndSerial() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    /**
     * 获取当前进程名
     */
    public String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService
                (Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return processName;
    }

    public boolean isMainProcess(Context context) {
        boolean isMainProcess;
        isMainProcess = context.getApplicationContext().getPackageName().equals
                (getCurrentProcessName(context));
        return isMainProcess;
    }
}
