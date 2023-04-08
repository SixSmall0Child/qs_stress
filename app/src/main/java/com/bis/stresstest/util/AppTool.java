package com.bis.stresstest.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


/**
 * @姓名 江海锋
 * @时间 2020/4/28 8:51
 * @作用 APP工具类管理
 * <p>
 * 更新日志：2020/4/28 第一次更新
 */
public class AppTool {


    /**
     * 重启
     */
    public static void reboot(Activity activity) {
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand("reboot", true);
        Logger.i("失败：" + commandResult.errorMsg + "成功：" + commandResult.successMsg);
//        PowerManager pManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
//        pManager.reboot("");
    }


    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static synchronized String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean dispatchRobotCurrentVersion(String serviceVersion, String localVersion) {
        String[] serviceVersionGroup = serviceVersion.split(".");
        String[] localVersionGroup = localVersion.split(".");
        if(!(serviceVersionGroup.length >= 3 && localVersionGroup.length >= 3)){
            return true;
        }
        if(checkVersion(serviceVersionGroup[0] ,  localVersionGroup[0])){
            return true;
        }else if(checkVersionsEqual(serviceVersionGroup[0] ,  localVersionGroup[0])){
            if(checkVersion(serviceVersionGroup[1] ,  localVersionGroup[1])){
                return true;
            }else if(checkVersionsEqual(serviceVersionGroup[1] ,  localVersionGroup[1])){
                return checkVersion(serviceVersionGroup[2] ,  localVersionGroup[2]);
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    public static boolean checkVersionsEqual(String str, String str2) {
        return (isInteger(str) && isInteger(str2)) && Integer.parseInt(str) == Integer.parseInt(str2);
    }




    public static boolean checkVersion(String str, String str2) {
        return (isInteger(str) && isInteger(str2)) && Integer.parseInt(str) > Integer.parseInt(str2);
    }


    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }



    /**
     * crius01-userdebug 7.1.2 nhg47k eng.adam.20200724.162057 test-keys
     * crius01-userdebug 7.1.2 nhg47k eng.hotech.20200623.205731 test-keysere
     * @param serviceVersion  服务器版本
     * @param localVersion    本地版本
     */
    public static boolean checkHardwareVersion(String serviceVersion , String localVersion){
        String version1 = serviceVersion.substring(serviceVersion.length() - 25 , serviceVersion.length() - 17);
        String version2 = localVersion.substring(localVersion.length() - 25 , localVersion.length() - 17);
//        boolean isUpdate = DateUtils.isDateOneBigger(version1 , version2);
        if(serviceVersion.equals(localVersion)){
            Logger.i("硬件版本已经是最新啦，无需更新!");
        }
//        System.out.println(version1 + ": " + version2);
        return !serviceVersion.equals(localVersion);
    }


    /**
     * 判断手机是否安装某个应用
     *
     * @param context
     * @param appPackageName 应用包名
     * @return true：安装，false：未安装
     */
    public static boolean isApplicationAvilible(Context context, String appPackageName) {
        PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (appPackageName.equals(pn)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取当前手机已安装应用
     *
     * @param context
     * @return
     */
    public static List<String> getOsInstallNameList(Context context) {
        PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<ApplicationInfo> list = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(list, new ApplicationInfo.DisplayNameComparator(packageManager));// 排序
        List<String> appName = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            //非系统程序
            if ((list.get(i).flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appName.add((list.get(i).loadLabel(packageManager).toString()));//如果非系统应用，则添加至appList
            } else {
                //系统程序
            }
        }
        return appName;
    }

    /**
     * 判断服务是否正在运行
     *
     * @param serviceName 服务类的全路径名称 例如： com.jaychan.demo.service.PushService
     * @param context 上下文对象
     * @return
     */
    public static boolean isServiceRunning(String serviceName, Context context) {
        //活动管理器
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(100); //获取运行的服务,参数表示最多返回的数量

        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            String className = runningServiceInfo.service.getClassName();
            if (className.equals(serviceName)) {
                return true; //判断服务是否运行
            }
        }

        return false;
    }
}
