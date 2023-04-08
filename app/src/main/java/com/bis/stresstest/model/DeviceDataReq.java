package com.bis.stresstest.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;

import com.bis.stresstest.app.MyApplication;
import com.bis.stresstest.util.Logger;
import com.bis.stresstest.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import androidx.annotation.RequiresApi;

import static com.bis.stresstest.model.DccDeviceBattery.getDccDeviceBatteryInfo;
import static com.bis.stresstest.model.DccDeviceHardware.getDeviceHardwareInfo;
import static com.bis.stresstest.model.DccDeviceNetwork.getDeviceNetworkInfo;
import static com.bis.stresstest.model.DccDeviceScreen.getDeviceScreenInfo;
import static com.bis.stresstest.model.DccDeviceSim.getDccDeviceSimInfo;

/**
 * @author LuoJiaYing
 * @create 2020-09-21 15:08
 */

public class DeviceDataReq {


    public List<DccDeviceApp> apps;

    public DccDeviceBattery battery;

    public DccDeviceHardware hardware;

    public DccDeviceNetwork network;

    public DccDeviceScreen screen;

    public DccDeviceSim sim;

    public String deviceSerial;

    public List<DccDeviceApp> getApps() {
        return apps;
    }

    public DeviceDataReq setApps(List<DccDeviceApp> apps) {
        this.apps = apps;
        return this;
    }

    public DccDeviceBattery getBattery() {
        return battery;
    }

    public DeviceDataReq setBattery(DccDeviceBattery battery) {
        this.battery = battery;
        return this;
    }

    public DccDeviceHardware getHardware() {
        return hardware;
    }

    public DeviceDataReq setHardware(DccDeviceHardware hardware) {
        this.hardware = hardware;
        return this;
    }

    public DccDeviceNetwork getNetwork() {
        return network;
    }

    public DeviceDataReq setNetwork(DccDeviceNetwork network) {
        this.network = network;
        return this;
    }

    public DccDeviceScreen getScreen() {
        return screen;
    }

    public DeviceDataReq setScreen(DccDeviceScreen screen) {
        this.screen = screen;
        return this;
    }

    public DccDeviceSim getSim() {
        return sim;
    }

    public DeviceDataReq setSim(DccDeviceSim sim) {
        this.sim = sim;
        return this;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public DeviceDataReq setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
        return this;
    }


    //0是竖屏 1是横屏
    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static DeviceDataReq getDeviceDataReqInfo(Activity activity) {
        DeviceDataReq deviceDataReq = new DeviceDataReq()
                .setScreen(getDeviceScreenInfo(activity))
                .setSim(getDccDeviceSimInfo(activity))
                .setApps(getApp(activity))
                .setBattery(getDccDeviceBatteryInfo(activity))
                .setHardware(getDeviceHardwareInfo(activity))
                .setDeviceSerial(MyApplication.getDevicesIpAddressAndSerial())
                .setNetwork(getDeviceNetworkInfo(activity));
        Logger.i("发送结果：" +deviceDataReq.toString());

        return deviceDataReq;
    }

    private static List<DccDeviceApp> getApp(Activity activity){
        HashMap<String, String> list = Utils.getThirdAppList(activity);//获取第三方应用包名以及应用名
        List<DccDeviceApp> apps = new ArrayList<>();
        //通过hashMap.keySet()来遍历hashMap的key值
        Set<String> keySet = list.keySet();
        Iterator<String> iterator = keySet.iterator();//容器的迭代器，定义迭代器
        while(iterator.hasNext()){
            String key = iterator.next();//获取游标后面的元素的key值
            DccDeviceApp dccDeviceApp = new DccDeviceApp();
            dccDeviceApp.setAppName(key);
            dccDeviceApp.setPackageNames(list.get(key));
            Logger.i("第三方应用列表：" + dccDeviceApp.getAppName());
            apps.add(dccDeviceApp);
        }
        return apps;
    }

    @Override
    public String toString() {
        return "DeviceDataReq{" +
                "apps=" + apps.size() +
                ", battery=" + battery.toString() +
                ", hardware=" + hardware.toString() +
                ", network=" + network.toString() +
                ", screen=" + screen.toString() +
                ", sim=" + sim.toString() +
                ", deviceSerial='" + deviceSerial + '\'' +
                '}';
    }
}
