package com.bis.stresstest.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bis.stresstest.app.MyApplication;
import com.bis.stresstest.receiver.MyReceiver;
import com.bis.stresstest.service.BackGroundService;
import com.bis.stresstest.util.Config;
import com.bis.stresstest.util.FileUtils;
import com.bis.stresstest.util.Logger;
import com.bis.stresstest.util.ShellUtils;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Random;

/**
 * Agent 功能主要为提供硬件信息（CPU，内存，硬盘容量等信息）、修改设备IP地址、安装/卸载APP
 * .activity.AgentActivity
 */


public class AgentActivity extends AppCompatActivity implements View.OnClickListener {
    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint({"SdCardPath", "CheckResult"})
    public void onCreate(Bundle savedInstanceState) {
        Logger.i("APP启动");
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
        startForegroundService(new Intent(AgentActivity.this, BackGroundService.class));
//        moveTaskToBack(true);
        new Thread(() -> {
            ShellUtils.execCommand("wm overscan 0,-70,0,-140", true);
            ShellUtils.execCommand("input keyevent BACK", true);
            ShellUtils.execCommand("input keyevent HOME", true);
            ShellUtils.execCommand("am force-stop com.into.stability", true);
            FileUtils.getInstance(this).copyAssetsToSD("stabilityTest.apk", "/data/data/com.bis.stresstest/stabilityTest.apk");
            FileUtils.getInstance(this).copyAssetsToSD("QMESA_64", "/data/data/com.bis.stresstest/QMESA_64");
            FileUtils.getInstance(this).copyAssetsToSD("GPUGfloat.apk", "/data/data/com.bis.stresstest/GPUGfloat.apk");
            FileUtils.getInstance(this).copyAssetsToSD("stressapptest", "/data/data/com.bis.stresstest/stressapptest");
            FileUtils.getInstance(this).copyAssetsToSD("cpu_full", "/data/data/com.bis.stresstest/cpu_full");
            FileUtils.getInstance(this).copyAssetsToSD("flatland64", "/data/data/com.bis.stresstest/flatland64");
            FileUtils.getInstance(this).copyAssetsToSD("start_cpu_gpu.sh", "/data/data/com.bis.stresstest/start_cpu_gpu.sh");
            ShellUtils.execCommand("mv /data/data/com.bis.stresstest/stabilityTest.apk /data/local/tmp/", true);
            ShellUtils.execCommand("mv /data/data/com.bis.stresstest/QMESA_64 /data/local/tmp/", true);
            ShellUtils.execCommand("mv /data/data/com.bis.stresstest/GPUGfloat.apk /data/local/tmp/", true);
            ShellUtils.execCommand("mv /data/data/com.bis.stresstest/stressapptest /data/local/tmp/", true);
            ShellUtils.execCommand("mv /data/data/com.bis.stresstest/cpu_full /data/local/tmp/", true);
            ShellUtils.execCommand("mv /data/data/com.bis.stresstest/flatland64 /data/local/tmp/", true);
            ShellUtils.execCommand("mv /data/data/com.bis.stresstest/start_cpu_gpu.sh /data/local/tmp/", true);
            ShellUtils.execCommand("chmod 777 /data/local/tmp/stressapptest", true);
            ShellUtils.execCommand("chmod 777 /data/local/tmp/test.sh", true);
            ShellUtils.execCommand("chmod 777 /data/local/tmp/cpu_full", true);
            ShellUtils.execCommand("chmod 777 /data/local/tmp/flatland64", true);
            ShellUtils.execCommand("chmod 777 /data/local/tmp/start_cpu_gpu.sh", true);
            ShellUtils.execCommand("pm install -r /data/local/tmp/stabilityTest.apk", true);
            ShellUtils.execCommand("pm install -r /data/local/tmp/GPUGfloat.apk", true);
            ShellUtils.execCommand("am start com.into.stability/com.common.activity.MainActivity", true);


            //865存在下方按钮栏
//            ShellUtils.execCommand("input tap 965 1746", true);
//            ShellUtils.execCommand("input tap 892 1062", true);
//            ShellUtils.execCommand("input tap 871 1478", true);
            ShellUtils.CommandResult result = ShellUtils.execCommand("getprop | grep ro.soc.model", true);
            Logger.i("结果：" + result.getSuccessMsg());
            //865不存在下方按钮栏
            if (ShellUtils.execCommand("getprop | grep ro.soc.model", true).getSuccessMsg().contains("RK3588")) {
                Logger.i("该型号是RK3588");
                ShellUtils.execCommand("settings put global settings_enable_monitor_phantom_procs false", true);
                ShellUtils.execCommand("settings put global cached_apps_freezer disabled", true);
            } else {
                Logger.i("该型号是Orion865");
                ShellUtils.execCommand("input tap 938 1845", true);
                ShellUtils.execCommand("input tap 950 1153", true);
                ShellUtils.execCommand("input tap 828 1680", true);
            }


            ShellUtils.execCommand("am force-stop com.into.stability", true);

            ShellUtils.CommandResult commandResult = ShellUtils.execCommand("cat /data/data/com.bis.stresstest/rebootCount.txt", true);
            if (commandResult.getSuccessMsg().equals("")) {
                return;
            }
            int count = Integer.parseInt(commandResult.getSuccessMsg().replace("\n", ""));
            Logger.i("次数：" + count);
            if (count >= 0) {
                FileUtils.writeTxt(String.valueOf(count + 1), "rebootCount.txt");
                Config.rebootCount = count + 1;
                runOnUiThread(() -> {
                    JSONObject jsonObject;
                    try {
                        if (ShellUtils.execCommand("cat /data/data/com.bis.stresstest/status.txt", true).getSuccessMsg().contains("memTest")
                                || ShellUtils.execCommand("getprop | grep ro.soc.model", true).getSuccessMsg().contains("QCS8550")) {
                            jsonObject = new JSONObject(ShellUtils.execCommand("cat /data/data/com.bis.stresstest/MEMTest.txt", true).getSuccessMsg());
                            new MyReceiver().startMEMTest(jsonObject.getString("remainingTime"));
                        } else {
                            jsonObject = new JSONObject(ShellUtils.execCommand("cat /data/data/com.bis.stresstest/ARMTest.txt", true).getSuccessMsg());
                            new MyReceiver().startARMTest(jsonObject.getString("remainingTime"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                Logger.i("开机自动执行");

            }
        }).start();
        FileUtils.writeTxt("stressTest_2.0.3 ", "version_code.txt");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        view.getId();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 检测该包名所对应的应用是否存在
     *
     * @param packageName
     * @return
     */
    public boolean checkPackage(String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void restartAPP() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        assert intent != null;
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //与正常页面跳转一样可传递序列化数据,在Launch页面内获得
        intent.putExtra("REBOOT", "reboot");
        startActivity(intent);
    }
}