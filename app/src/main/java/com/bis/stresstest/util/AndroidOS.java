package com.bis.stresstest.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.TrafficStats;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import com.bis.stresstest.app.MyApplication;
import com.bis.stresstest.model.AndroidPlatform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import static java.lang.Double.NaN;

/**
 *
 */
public class AndroidOS {

    private static double lastTotalRxBytes = 0;
    private static double lastTotalTxBytes = 0;
    private static long lastTimeStamp = 0;
    private static long lastTimeStamp1 = 0;
    private static int UPDATE = 100;



    /**
     * 获取OS数据
     *
     * @param activity
     * @return
     */
    public static AndroidPlatform getOSAndroidPlatform(Activity activity) {
        AndroidPlatform androidPlatform = new AndroidPlatform();
        androidPlatform.setTemperature(getCPUTemperature());
        androidPlatform.setCpuArchitecture(getCPUABI());
        androidPlatform.setCpuCore(getNumCores());
        androidPlatform.setCpuUsage(getCpuUsed());
        androidPlatform.setTotalMemorySpace(getTotalMemory());
        androidPlatform.setMemoryFreeSpace(getAvailMemory(activity));
//        androidPlatform.setStorage(getTotalInternalMemorySize(activity));
        androidPlatform.setAvailableSpace(getAvailableInternalMemorySize(MyApplication.getContext()));
        androidPlatform.setNetWorkUp(showNetUpSpeed());
        androidPlatform.setNetWorkDown(showNetDownSpeed());
        String mark = android.os.Build.DISPLAY.toLowerCase();
        androidPlatform.setRomVersion(mark);
//        androidPlatform.setRunTime(DateUtils.getDevicesRunTime());

        return androidPlatform;
    }

    /**
     * @return
     */
    public static double showNetUpSpeed() {
        double nowTotalTxBytes = getTotalTxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        double txSpeed = ((nowTotalTxBytes - lastTotalTxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
        lastTimeStamp = nowTimeStamp;
        lastTotalTxBytes = nowTotalTxBytes;
        double localData;
        localData =  txSpeed;
        if (localData == NaN || String.valueOf(localData).equals("NaN")) {
            return 0;
        }
        return localData*8;//kb/s转kbps
    }

    /**
     * @return
     */
    public static double showNetDownSpeed() {
        double nowTotalRxBytes = getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        double speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp1));//毫秒转换
        lastTimeStamp1 = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        double localData;
        localData = speed;
        if (localData == NaN || String.valueOf(localData).equals("NaN")) {
            return 0;
        }
        return (localData*8/1000)*844;//kb/s转kbps
    }

    /**
     * 获取CPU温度
     *
     * @return
     */
    public static double getCPUTemperature() {
        String name = FileUtils.readExternal("/sys/class/thermal/thermal_zone9/temp");
        return Double.parseDouble(name.trim()) / 10;
    }

    /**
     * 获取网络上行数据
     *
     * @return
     */
    public static double getTotalRxBytes() {
        return TrafficStats.getTotalRxBytes() / 1024;//转为KB
    }

    /**
     * 获取网络下行数据
     *
     * @return
     */
    public static double getTotalTxBytes() {
        return TrafficStats.getTotalTxBytes() / 1024;//转为KB
    }

    /**
     * 获取CPU架构
     *
     * @return
     */
    public static String CPUABI = null;

    /**
     * 获取CPU架构
     *
     * @return
     */
    public static String getCPUABI() {
        if (CPUABI == null) {
            try {
                String os_cpuabi = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("getprop ro.product.cpu.abi").getInputStream())).readLine();
                if (os_cpuabi.contains("x86")) {
                    CPUABI = "x86";
                } else if (os_cpuabi.contains("armeabi-v7a") || os_cpuabi.contains("arm64-v8a")) {
                    CPUABI = "armeabi-v7a";
                } else {
                    CPUABI = "armeabi";
                }
            } catch (Exception e) {
                CPUABI = "armeabi";
            }
        }
        return CPUABI;
    }


    /**
     * 获取CPU核心数
     *
     * @return
     */
    public static int getNumCores() {
        // Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                // Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            // Default to return 1 core
            return 1;
        }
    }

    /**
     * 获取内存可用空间
     *
     * @return
     */
    public static double getAvailMemory(Activity mContext) {// 获取android当前可用内存大小
        if (mContext == null) {
            return 0;
        }
        if ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE) == null) {
            return 0;
        }
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内存
        return Double.parseDouble(Formatter.formatFileSize(mContext, mi.availMem).replace("GB", ""));// 将获取的内存大小规格化
    }

    /**
     * 获取内存总共空间
     *
     * @return
     */
    public static double getTotalMemory() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        double initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
//                Log.e(str2, num + "\t");
            }
            initial_memory = Integer.parseInt(arrayOfString[1]) / 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            initial_memory = initial_memory / 1024;
            localBufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return initial_memory;// Byte转换为KB或者MB，内存大小规格化
    }


    /**
     * 获取cpu使用率
     *
     * @return
     */
    public static double getCpuUsed() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            String[] toks = load.split(" ");
            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            try {
                Thread.sleep(360);
            } catch (Exception e) {
                e.printStackTrace();
            }
            reader.seek(0);
            load = reader.readLine();
            reader.close();
            toks = load.split(" ");
            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            return ((float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1))) * 100;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }


    /**
     * 外部存储是否可用 (存在且具有读写权限)
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取手机内部可用空间大小
     *
     * @return
     */
    public static double getAvailableInternalMemorySize(Context mContext) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        String s = Formatter.formatFileSize(mContext, availableBlocks * blockSize);
        if (s == null || s.equals("")) {
            s = "1";

        }
        return Double.parseDouble(s.replace("GB", ""));
    }


    /**
     * 获取手机内部空间大小
     *
     * @return
     */
    public static double getTotalInternalMemorySize(Context mContext) {
        return 0.0;
    }


    /**
     * 获取手机外部可用空间大小
     *
     * @return
     */
    public static String getAvailableExternalMemorySize(Context mContext) {

        return "-1";

    }

    /**
     * 获取手机外部总空间大小
     *
     * @return
     */
    public static String getTotalExternalMemorySize(Context mContext) {
        if (isExternalStorageAvailable()) {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return Formatter.formatFileSize(mContext, totalBlocks * blockSize);
        } else {
            return "-1";
        }
    }

    public static String getGateWay() {
        String[] arr;
        try {
            Process process = Runtime.getRuntime().exec("ip route list table 0");
            String data = null;
            BufferedReader ie = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String string = in.readLine();

            arr = string.split("\\s+");
            return arr[2];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }

    public static String getIpAddrMaskForInterfaces(String interfaceName) {
        try {
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();    //获取本机所有的网络接口
            while (networkInterfaceEnumeration.hasMoreElements()) { //判断 Enumeration 对象中是否还有数据
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement(); //获取 Enumeration 对象中的下一个数据
                if (!networkInterface.isUp() && !interfaceName.equals(networkInterface.getDisplayName())) { //判断网口是否在使用，判断是否时我们获取的网口
                    continue;
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {    //
                    if (interfaceAddress.getAddress() instanceof Inet4Address) {    //仅仅处理ipv4
                        return calcMaskByPrefixLength(interfaceAddress.getNetworkPrefixLength());   //获取掩码位数，通过 calcMaskByPrefixLength 转换为字符串
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "error";
    }

    //通过子网掩码的位数计算子网掩码
    public static String calcMaskByPrefixLength(int length) {

        int mask = 0xffffffff << (32 - length);
        int partsNum = 4;
        int bitsOfPart = 8;
        int maskParts[] = new int[partsNum];
        int selector = 0x000000ff;

        for (int i = 0; i < maskParts.length; i++) {
            int pos = maskParts.length - 1 - i;
            maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
        }

        String result = "";
        result = result + maskParts[0];
        for (int i = 1; i < maskParts.length; i++) {
            result = result + "." + maskParts[i];
        }
        return result;
    }
}
