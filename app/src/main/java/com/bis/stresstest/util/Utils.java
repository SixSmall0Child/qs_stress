package com.bis.stresstest.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.util.HashMap;
import java.util.List;



public class Utils {

    private static String name;

    /**
     * 获取第三方应用包名以及应用名称
     */
    public static HashMap<String, String> getThirdAppList(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        // 判断是否系统应用：
        HashMap<String, String> thirdAPP = new HashMap<>();
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo pak = packageInfoList.get(i);
            //判断是否为系统预装的应用
            if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                // 新思开发的应用（uiautomator，推流，下载，机器人，snmp）
                switch (pak.packageName) {
                    case "com.github.uiautomator":
                    case "com.bis.rtmppush":
                    case "com.github.uiautomator.test":
                    case "com.bis.downloaddemo":
                    case "com.bis.robot.myapplication":
                    case "com.bis.robot.bmcsystem":
                    case "com.bis.qishuo":
                    case "com.bis.stresstest":
                        break;
                    default:
                        // 第三方应用
                        try {
                            name = packageManager.getApplicationLabel(packageManager.getApplicationInfo(pak.packageName, PackageManager.GET_META_DATA)).toString();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        thirdAPP.put(name, pak.packageName);
                        break;
                }
            }

        }
        return thirdAPP;
    }
}
