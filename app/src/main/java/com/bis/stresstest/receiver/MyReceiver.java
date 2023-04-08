package com.bis.stresstest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import com.bis.stresstest.activity.AgentActivity;
import com.bis.stresstest.app.MyApplication;
import com.bis.stresstest.model.SOCModel;
import com.bis.stresstest.util.Config;
import com.bis.stresstest.util.FileUtils;
import com.bis.stresstest.util.Logger;
import com.bis.stresstest.util.MyCountDownTimer;
import com.bis.stresstest.util.ShellUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "MyReceiver";
    NetworkThread networkThread;
    int type;
    int time;
    String target;
    //CPU
    private double minTmp;
    private double maxTmp;
    private double minCPU;
    private double maxCPU;
    private double totalTmp;
    private double totalCPU;
    //GPU
    private double gpuMinTmp;
    private double gpuMaxTmp;
    private double minGPU;
    private double maxGPU;
    private double gpuTotalTmp;
    private double totalGPU;
    int loopTime;
    int rebootCount = 0;

    private SOCModel socModel;
    private ExecutorService mExecutors = Executors.newCachedThreadPool();

    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        getCurSOCModel();
        Bundle bundle = intent.getExtras();
        switch (intent.getAction()) {
            case "android.intent.action.BOOT_COMPLETED":
                Logger.i("应用程序开机自启");
                Intent i = new Intent(context, AgentActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                break;
            //adb shell am broadcast -n com.bis.stresstest/.receiver.MyReceiver -a stresstest.intent.action.startStressTest --esa message 12
            case "stresstest.intent.action.startStressTest": //开始老化测试
                Config.rebootCount = 0;
                Logger.i("开始老化测试");
                ShellUtils.execCommand("rm /data/data/com.bis.stresstest/rebootCount.txt", true);
                ShellUtils.execCommand("input keyevent BACK", true);
                ShellUtils.execCommand("am force-stop com.into.stability", true);
                startARMTest(bundle.getStringArray("message")[0]);
                break;
            //adb shell am broadcast -n com.bis.stresstest/.receiver.MyReceiver -a stresstest.intent.action.stopStressTest
            case "stresstest.intent.action.stopStressTest": //停止老化测试
                Logger.i("停止老化测试");
                ShellUtils.execCommand("rm /data/data/com.bis.stresstest/rebootCount.txt", true);
                ShellUtils.execCommand("input keyevent BACK", true);
                ShellUtils.execCommand("am force-stop com.into.stability", true);
                ShellUtils.execCommand("killall -9 stressapptest", true);
                restartAPP();
                break;
            //adb shell am broadcast -n com.bis.stresstest/.receiver.MyReceiver -a stresstest.intent.action.startGPUTest --esa message 1440
            case "stresstest.intent.action.startGPUTest": //开始GPU老化测试
                Logger.i("开始GPU老化测试");
                ShellUtils.execCommand("rm /data/data/com.bis.stresstest/rebootCount.txt", true);
                ShellUtils.execCommand("input keyevent BACK", true);
                startGPUTest(bundle.getStringArray("message")[0]);
                break;
            //adb shell am broadcast -n com.bis.stresstest/.receiver.MyReceiver -a stresstest.intent.action.stopGPUTest
            case "stresstest.intent.action.stopGPUTest": //停止GPU老化测试
                Logger.i("停止GPU老化测试");
                ShellUtils.execCommand("rm /data/data/com.bis.stresstest/rebootCount.txt", true);
                ShellUtils.execCommand("input keyevent BACK", true);
                ShellUtils.execCommand("am force-stop com.ioncannon.cpuburn.gpugflops", true);
//            restartAPP();
                //am broadcast -n com.bis.stresstest/.receiver.MyReceiver -a stresstest.intent.action.startNetWorkTest --esa message 192.168.116.141,30
                break;
            case "stresstest.intent.action.startNetWorkTest": //开始网络测试
                Config.rebootCount = 0;
                ShellUtils.execCommand("rm /data/data/com.bis.stresstest/logFile.txt", true);
                ShellUtils.execCommand("rm /data/data/com.bis.stresstest/result.txt", true);
                String[] strings = bundle.getStringArray("message");
                Logger.i("开始网络测试,收到信息:" + strings[0]);
                Config.NETWORK_TEST_STATUS = 0;
                if (networkThread == null) {
                    networkThread = new NetworkThread();
                }
                if (strings[0].equals("1")) {
                    type = 1;
                } else {
                    type = 2;
                    time = Integer.parseInt(strings[1]);
                }
                target = strings[0];
                networkThread.start();
                //am broadcast -n com.bis.stresstest/.receiver.MyReceiver -a stresstest.intent.action.stopNetWorkTest
                break;
            case "stresstest.intent.action.stopNetWorkTest": //停止网络测试
                Logger.i("停止网络测试");
                ShellUtils.execCommand("killall -9 iperf", true);
                restartAPP();
                break;
            //am broadcast -n com.bis.stresstest/.receiver.MyReceiver -a stresstest.intent.action.startMEMTest --esa message 30
            case "stresstest.intent.action.startMEMTest": //开始内存测试
                String[] mem_Strings = bundle.getStringArray("message");
                Logger.i("内存测试,收到信息:" + mem_Strings[0]);
                ShellUtils.execCommand("rm /data/data/com.bis.stresstest/rebootCount.txt", true);
                ShellUtils.execCommand("chmod 755 /data/local/tmp/QMESA_64", true);
                if (Config.mem_timer != null) {
                    Logger.i("定时器取消");
                    Config.mem_timer.cancel();
                    Config.mem_timer = null;
                }

                startMEMTest(mem_Strings[0]);
                //am broadcast -n com.bis.stresstest/.receiver.MyReceiver -a stresstest.intent.action.stopMEMTest
                break;
            case "stresstest.intent.action.stopMEMTest": //停止内存测试
                Logger.i("停止内存测试");
                ShellUtils.execCommand("rm /data/data/com.bis.stresstest/rebootCount.txt", true);
                restartAPP();
                break;
        }
    }


    class NetworkThread extends Thread {
        @Override
        public void run() {
            if (ShellUtils.execCommand("getprop | grep ro.soc.model", true).getSuccessMsg().contains("RK3588")) {
                ShellUtils.execCommand("settings put global settings_enable_monitor_phantom_procs false", true);
                ShellUtils.execCommand("settings put global cached_apps_freezer disabled", true);
            }
            ShellUtils.CommandResult isGB = ShellUtils.execCommand("ethtool eth0", true);
            ShellUtils.execCommand("killall iperf", true);
            boolean is2GB = isGB.successMsg.contains("2500Mb/s");
            if (type == 1) {
                Logger.i("作为服务端开始测试");
                ShellUtils.CommandResult result = ShellUtils.execCommand2("iperf -s -i 1", true, is2GB);
//                ShellUtils.CommandResult result = ShellUtils.execCommand2("iperf3 -s -i 1", true);
                Logger.i("服务端结束测试");
            } else {
                Logger.i("作为客户端开始测试,访问地址为：" + target);
                //iperf -c 192.168.50.60 -t 30 -i 1
                //iperf3 -c  192.168.1.17   -t 30 -i 1
                ShellUtils.CommandResult result = ShellUtils.execCommand2("iperf -c " + target + " -t " + time + " -i 1", true, is2GB);
//                ShellUtils.CommandResult result = ShellUtils.execCommand2("iperf3 -c " + target + q    " -t " + time + " -i 1 -w 1M", true);
                Logger.i("客户端结束测试");
            }
        }
    }

    private void restartAPP() {
        Intent LaunchIntent = MyApplication.getContext().getPackageManager().getLaunchIntentForPackage(MyApplication.getContext().getPackageName());
        MyApplication.getContext().startActivity(LaunchIntent);
        /**杀死整个进程**/
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void startARMTest(String strings) {
        if (this.socModel == SOCModel.Orion865 || this.socModel == SOCModel.RK3588) {
            new Thread(() -> {
                try {
                    if (ShellUtils.execCommand("getprop | grep ro.soc.model", true).getSuccessMsg().contains("RK3588")) {
                        ShellUtils.execCommand("settings put global settings_enable_monitor_phantom_procs false", true);
                        ShellUtils.execCommand("settings put global cached_apps_freezer disabled", true);
                        ShellUtils.execCommand("/data/local/tmp/stressapptest -s 86400 -i 4 -C 4 -W -M 6144 >/dev/null 2>&1 &", true);
                        Logger.i("RK3588开始CPU测试");
                    } else {
                        Logger.i("Orion865开始CPU测试");
                        ShellUtils.execCommand("input keyevent BACK", true);
                        ShellUtils.execCommand("am force-stop com.into.stability", true);
                        ShellUtils.execCommand("am start com.into.stability/com.common.activity.MainActivity", true);
                        Thread.sleep(1000);
                        //865不存在下方按钮栏
                        ShellUtils.execCommand("input tap 477 459", true);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            mExecutors.execute(new Runnable() {
                @Override
                public void run() {
                    ShellUtils.execCommand("./data/local/tmp/start_cpu_gpu.sh", true);
                    for (int i = 0; i < 2; i++) {
                        final int temp = i;
                        mExecutors.execute(new Runnable() {
                            @Override
                            public final void run() {
                                ShellUtils.CommandResult commandResult = ShellUtils.execCommand("/data/local/tmp/cpu_full 24", true);
                                Log.d(TAG, temp + "startGPUTest: cpu_full " + commandResult);
                            }
                        });
                    }
                }
            });
        }


        rebootCount = Config.rebootCount;
        Logger.i("开始老化测试,收到信息:" + strings);
        if (rebootCount == 0) {
            FileUtils.writeTxt("0", "rebootCount.txt");
        }
        minTmp = 0;
        maxTmp = 0;
        minCPU = 0;
        maxCPU = 0;
        totalTmp = 0;
        totalCPU = 0;
        loopTime = 1;
        int remainingTime = Integer.parseInt(strings) * 60 * 1000;
        Logger.i("时间应该是：" + (long) Integer.parseInt(strings) * 60 + "秒");
        CountDownTimer timer = new CountDownTimer((long) Integer.parseInt(strings) * 60 * 1000, 2000) {
            public void onTick(long millisUntilFinished) {
                loopTime++;
                if (loopTime < 4) {
                    return;
                }
                setCPUData("ARMTest.txt", remainingTime);
            }

            public void onFinish() {
                Logger.i("老化测试结束");
                ShellUtils.execCommand("rm /data/data/com.bis.stresstest/rebootCount.txt", true);
                ShellUtils.execCommand("input keyevent BACK", true);
                ShellUtils.execCommand("am force-stop com.into.stability", true);
                ShellUtils.execCommand("killall -9 stressapptest", true);
                restartAPP();
            }
        };
        timer.start();
    }

    public void startGPUTest(String strings) {
        Config.rebootCount = 0;
        if (this.socModel == SOCModel.Orion865 || this.socModel == SOCModel.RK3588) {
            new Thread(() -> {
                ShellUtils.execCommand("input keyevent BACK", true);
                ShellUtils.execCommand("am force-stop com.ioncannon.cpuburn.gpugflops", true);
                ShellUtils.execCommand("am start com.ioncannon.cpuburn.gpugflops/com.ioncannon.cpuburn.gpugflops.CPUBurnActivity", true);
                try {
                    Thread.sleep(1000);
                    ShellUtils.CommandResult result = ShellUtils.execCommand("getprop | grep ro.soc.model", true);
                    Logger.i("结果：" + result.getSuccessMsg());
                    if (ShellUtils.execCommand("getprop | grep ro.soc.model", true).getSuccessMsg().contains("RK3588")) {
                        Logger.i("RK3588开始GPU测试");
                        ShellUtils.execCommand("settings put global settings_enable_monitor_phantom_procs false", true);
                        ShellUtils.execCommand("settings put global cached_apps_freezer disabled", true);
                        ShellUtils.execCommand("input tap 31 1032", true);//拷GPU
                        ShellUtils.execCommand("input tap 179 1037", true);//拷GPU Scalar
                        ShellUtils.execCommand("input tap 113 940", true);//CPU模式
                        Thread.sleep(800);
                        ShellUtils.execCommand("input tap 434 605", true);//FP32浮点
                        Thread.sleep(800);
                        ShellUtils.execCommand("input tap 829 1480", true);//确定按钮
                        ShellUtils.execCommand("input tap 324 940", true);//线程数
                        Thread.sleep(800);
                        ShellUtils.execCommand("input tap 278 1172", true);//选8个
                        Thread.sleep(800);
                        ShellUtils.execCommand("input tap 829 1480", true);//确定按钮
                        ShellUtils.execCommand("input tap 268 1108", true);//开始
                    } else {
                        Logger.i("Orion865开始GPU测试");
                        ShellUtils.execCommand("input tap 52 1472", true);
                        ShellUtils.execCommand("input tap 284 1477", true);
                        ShellUtils.execCommand("input tap 97 1344", true);
                        ShellUtils.execCommand("input tap 395 582", true);
                        ShellUtils.execCommand("input tap 876 1677", true);
                        ShellUtils.execCommand("input tap 331 1340", true);
                        ShellUtils.execCommand("input tap 355 1414", true);
                        ShellUtils.execCommand("input tap 876 1677", true);
                        ShellUtils.execCommand("input tap 308 1569", true);
                    }
                    //纯GPU测试
//                ShellUtils.execCommand("input tap 39 1477", true);
//                ShellUtils.execCommand("input tap 260 1477", true);
//                ShellUtils.execCommand("input tap 308 1569", true);
                    //纯CPU
//                ShellUtils.execCommand("input tap 105 1340", true);
//                ShellUtils.execCommand("input tap 403 571", true);
//                ShellUtils.execCommand("input tap 866 1677", true);
//                ShellUtils.execCommand("input tap 341 1340", true);
//                ShellUtils.execCommand("input tap 347 1393", true);
//                ShellUtils.execCommand("input tap 866 1677", true);
//                ShellUtils.execCommand("input tap 308 1569", true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            this.mExecutors.execute(new Runnable() {
                @Override
                public final void run() {
                    ShellUtils.execCommand("./data/local/tmp/start_cpu_gpu.sh", true);
                    for (int i = 0; i < 2; i++) {
                        final int temp = i;
                        mExecutors.execute(new Runnable() {
                            @Override
                            public final void run() {
                                ShellUtils.CommandResult commandResult = ShellUtils.execCommand("/data/local/tmp/cpu_full 24", true);
                                Log.d(TAG, temp + "startGPUTest: cpu_full " + commandResult);
                            }
                        });
                    }
                    for (int i2 = 0; i2 < 3; i2++) {
                        final int temp = i2;
                        mExecutors.execute(new Runnable() {
                            @Override
                            public final void run() {
                                ShellUtils.CommandResult commandResult = ShellUtils.execCommand("/data/local/tmp/flatland64", true);
                                Log.d(TAG, temp + "startGPUTest: flatland64 " + commandResult);
                            }
                        });
                    }
                }
            });

        }


        rebootCount = Config.rebootCount;
        Logger.i("开始老化测试,收到信息:" + strings);
        if (rebootCount == 0) {
            FileUtils.writeTxt("0", "rebootCount.txt");
        }
        minTmp = 0;
        maxTmp = 0;
        minCPU = 0;
        maxCPU = 0;
        totalTmp = 0;
        totalCPU = 0;
        loopTime = 1;
        int remainingTime = Integer.parseInt(strings) * 60 * 1000;
        CountDownTimer timer = new CountDownTimer(Integer.parseInt(strings) * 60 * 1000, 2000) {
            public void onTick(long millisUntilFinished) {
                loopTime++;
                if (loopTime < 4) {
                    return;
                }
                setCPUData("GPUTest_CPUData.txt", remainingTime);
                setGPUData("GPUTest_GPUData.txt", remainingTime);
            }

            public void onFinish() {
                Logger.i("老化测试结束");
                ShellUtils.execCommand("rm /data/data/com.bis.stresstest/rebootCount.txt", true);
                ShellUtils.execCommand("input keyevent BACK", true);
                ShellUtils.execCommand("am force-stop com.ioncannon.cpuburn.gpugflops", true);
//                restartAPP();
            }
        };
        timer.start();
    }


    public void startMEMTest(String strings) {
        new Thread(() -> {
            ShellUtils.CommandResult result = ShellUtils.execCommand("ps -fe|grep QMESA_64 |grep -v grep | awk '{print $2}'", true);
            ShellUtils.execCommand("kill -9 " + result.getSuccessMsg(), true);
            ShellUtils.execCommand("/data/local/tmp/QMESA_64 -startSize 320MB -endSize 320MB -totalSize 10000MB  -errorCheck T -secs " + (long) Integer.parseInt(strings) * 60 * 1000 + " -numThreads 10 > /sdcard/qmlog1.txt &", true);
        }).start();

        rebootCount = Config.rebootCount;
        Logger.i("开始内存老化测试,收到信息:" + strings);
        if (rebootCount == 0) {
            FileUtils.writeTxt("0", "rebootCount.txt");
            FileUtils.writeTxt("memTest", "status.txt");
        }
        minTmp = 0;
        maxTmp = 0;
        minCPU = 0;
        maxCPU = 0;
        totalTmp = 0;
        totalCPU = 0;
        loopTime = 1;
        int remainingTime = Integer.parseInt(strings) * 60 * 1000;
        Config.mem_timer = new MyCountDownTimer((long) Integer.parseInt(strings) * 60 * 1000, 2000) {
            public void onTick(long millisUntilFinished) {
                loopTime++;
                if (loopTime < 4) {
                    return;
                }
                setCPUData("MEMTest.txt", remainingTime);
            }

            public void onFinish() {
                Logger.i("内存老化测试结束");
                ShellUtils.execCommand("rm /data/data/com.bis.stresstest/rebootCount.txt", true);
                ShellUtils.execCommand("rm /data/data/com.bis.stresstest/status.txt", true);
                restartAPP();
            }
        };
        Config.mem_timer.start();
    }

    /**
     * 获取cpu使用率
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
     * 获取CPU温度
     */
    public double getCPUTemperature() {
        if (this.socModel == SOCModel.RK3588) {
            String temp1 = FileUtils.readExternal("/sys/class/thermal/thermal_zone1/temp");
            String temp2 = FileUtils.readExternal("/sys/class/thermal/thermal_zone2/temp");
            String temp3 = FileUtils.readExternal("/sys/class/thermal/thermal_zone3/temp");
            double temp = Double.parseDouble(temp1) + Double.parseDouble(temp2) + Double.parseDouble(temp3);
            return temp / 3000.0d;
        } else if (this.socModel == SOCModel.SM8550) {
            String temp4 = FileUtils.readExternal("/sys/class/thermal/thermal_zone32/temp");
            return Double.parseDouble(temp4.trim()) / 1000.0d;
        } else {
            String temp5 = FileUtils.readExternal("/sys/class/thermal/thermal_zone9/temp");
            return Double.parseDouble(temp5.trim()) / 1000.0d;
        }

    }

    private void setCPUData(String fileName, int remainingTime) {
        double cpuUseage = getCpuUsed();
        if (cpuUseage > maxCPU) {
            maxCPU = cpuUseage;
        }
//        if (minCPU == 0) {
//            minCPU = cpuUseage;
//        } else
        if (cpuUseage < minCPU) {
            minCPU = cpuUseage;
        }

        double cpuTmp = getCPUTemperature();
        if (cpuTmp > maxTmp) {
            maxTmp = cpuTmp;
        }
        if (minTmp == 0) {
            minTmp = cpuTmp;
        } else if (cpuTmp < minTmp) {
            minTmp = cpuTmp;
        }
        totalTmp = totalTmp + cpuTmp;
        totalCPU = totalCPU + cpuUseage;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("count", loopTime - 2);
            jsonObject.put("use", totalCPU);
            jsonObject.put("temp", totalTmp);
            jsonObject.put("useMax", maxCPU);
            jsonObject.put("useMin", minCPU);
            jsonObject.put("tempMax", maxTmp);
            jsonObject.put("tempMin", minTmp);
            jsonObject.put("reboot", rebootCount);
            jsonObject.put("remainingTime", (remainingTime - loopTime) / 60 / 1000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Logger.i("CPU数据:" + ",cpuUseage=" + cpuUseage + ",cpuTmp=" + cpuTmp + "," + jsonObject);
        FileUtils.writeTxt(jsonObject.toString(), fileName);
    }

    private void setGPUData(String fileName, int remainingTime) {
        String result;
        if (this.socModel == SOCModel.RK3588) {
            result = ShellUtils.execCommand("cat /sys/devices/platform/fb000000.gpu/devfreq/fb000000.gpu/load", true).getSuccessMsg().split("@")[0];
        } else if (this.socModel == SOCModel.SM8550) {
            result = ShellUtils.execCommand("cat /sys/class/kgsl/kgsl-3d0/gpu_busy_percentage", true).getSuccessMsg().replace("%", "").trim();
        } else {
            result = ShellUtils.execCommand("cat /sys/class/devfreq/*qcom,kgsl-3d0/gpu_load", true).getSuccessMsg();
        }

        double gpuUseage = Double.parseDouble(result);
        if (gpuUseage > maxGPU) {
            maxGPU = gpuUseage;
        }
//        if (minGPU == 0) {
//            minGPU = gpuUseage;
//        } else
        if (gpuUseage < minGPU) {
            minGPU = gpuUseage;
        }

        double gpuTmp = 0;
        if (this.socModel == SOCModel.RK3588) {
            gpuTmp = Double.parseDouble(ShellUtils.execCommand("cat /sys/class/thermal/thermal_zone5/temp", true).getSuccessMsg()) / 1000.0d;
        } else if (this.socModel == SOCModel.SM8550) {
            gpuTmp = Double.parseDouble(ShellUtils.execCommand("cat /sys/class/thermal/thermal_zone63/temp", true).getSuccessMsg()) / 1000.0d;
        } else {
            gpuTmp = Double.parseDouble(ShellUtils.execCommand("cat /sys/class/kgsl/kgsl-3d0/temp", true).getSuccessMsg()) / 1000.0d;
        }


        if (gpuTmp > gpuMaxTmp) {
            gpuMaxTmp = gpuTmp;
        }
        if (gpuMinTmp == 0) {
            gpuMinTmp = gpuTmp;
        } else if (gpuTmp < gpuMinTmp) {
            gpuMinTmp = gpuTmp;
        }
        gpuTotalTmp = gpuTotalTmp + gpuTmp;
        totalGPU = totalGPU + gpuUseage;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("count", loopTime - 2);
            jsonObject.put("use", totalGPU);
            jsonObject.put("temp", gpuTotalTmp);
            jsonObject.put("useMax", maxGPU);
            jsonObject.put("useMin", minGPU);
            jsonObject.put("tempMax", gpuMaxTmp);
            jsonObject.put("tempMin", gpuMinTmp);
            jsonObject.put("reboot", rebootCount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Logger.i("GPU数据:" + ",gpuUseage=" + gpuUseage + ",gpuTmp=" + gpuTmp + "," + jsonObject);
        FileUtils.writeTxt(jsonObject.toString(), fileName);
    }


    private void getCurSOCModel() {
        if (ShellUtils.execCommand("getprop | grep ro.soc.model", true).getSuccessMsg().contains("RK3588")) {
            this.socModel = SOCModel.RK3588;
        } else if (ShellUtils.execCommand("getprop | grep ro.soc.model", true).getSuccessMsg().contains("QCS8550")) {
            this.socModel = SOCModel.SM8550;
        } else {
            this.socModel = SOCModel.Orion865;
        }
    }

}
