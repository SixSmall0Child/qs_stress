package com.bis.stresstest.util;

import android.content.Intent;
import android.os.Environment;

public class Config {
    public static String url = null;
    public static final String TAG = "StressTest";
    public static String filePathName = null;
    public static String devicesIp;
    public static int NETWORK_TEST_STATUS = 0;
    public static int TYPE;
    public static int TIME;
    public static String TARGET;
    public static int rebootCount = 0;
    public static MyCountDownTimer mem_timer = null;

    public static String getSdCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 文件夹存放地址
     */
    public static class FileAssets {
        //python文件中转
        public static final String OUTSIDE_FILE = "/data/data/com.bis.stresstest/";
        //python文件中转
        public static final String OUTSIDE_FILE_PICTURES = getSdCardPath() + "/outside/pictures/";
        //定时重启检测文件夹
        public static final String ROBOT_TIME = "reboot_time.txt";
        //定时重启检测文件夹
        public static final String OUTSIDE_ROBOT_TIME = OUTSIDE_FILE + "reboot_time.txt";
        //python文件中转——outside文件
        public static final String PYTHON_OUTSIDE = "outside.py";
        //python文件中转——init
        public static final String PYTHON_OUTSIDE_INIT = "__init__.py";
        //python文件中转——outside文件
        public static final String OUTSIDE = "/sdcard/outside/outside.py";
        //python文件中转——init
        public static final String OUTSIDE_INIT = "/sdcard/outside/__init__.py";
        //用来停止脚本运行的文件
        public static final String STOP_OUTSIDE_UIAUTOMCATOR = "/sdcard/outside/stop_uiautomator2.txt";
        //用来获取录制视频昵称和ID的文件
        public static final String ANCHOR_OUTSIDE_ID = "/sdcard/outside/anchorID.txt";
        //用来停止脚本运行的文件
        public static final String STOP_UIAUTOMCATOR = "stop_uiautomator2.txt";
        //用来修改FTP服务器地址的
        public static final String FTP_SERVER_ADDRESS = "ftpInfo.txt";
        //用来修改FTP服务器地址的
        public static final String FTP_SERVER_ADDRESS_OUTSIDE = "/sdcard/outside/ftpInfo.txt";
        //用来获取录制视频昵称和ID的文件
        public static final String ANCHOR_ID = "anchorID.txt";
        //用来存放服务器IP的文件
        public static final String ANCHOR_IP = "anchorIP.txt";
        //python文件中转——outside文件
        public static final String ANCHOR_IP_ADDRESS = "/sdcard/outside/anchorIP.txt";
        //用来存放服务器IP的文件
        public static final String SERVICE_IP = "serviceIp.txt";
        //python文件中转——outside文件
        public static final String SERVICE_IP_ADDRESS = getSdCardPath() + "/outside/serviceIp.txt";
        //用来存放服务器IP的文件
        public static final String SERVICE_PARAMS = "params.conf";
        //python文件中转——outside文件
        public static final String SERVICE_PARAMS_INFO = "/sdcard/outside/params.conf";
        //用来存放视频的文件夹
        public static final String VIDEO = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/VIDEO/";
        //用来存放日志的文件夹
        public static final String LOGCAT = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/LOGCAT/";
    }
}
