package com.bis.stresstest.model;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import static com.bis.stresstest.util.AndroidOS.getOSAndroidPlatform;


/**
 * <p>
 * 
 * </p>
 *
 * @author luojiaying
 * @since 2020-09-21
 */

public class DccDeviceHardware implements Serializable {

    /**
     * cpu核心数
     */
    public Integer cpuCores;

    /**
     * 最高频率（单位GHZ）
     */
    public Double highestFrequency;

    /**
     * 内存总量
     */
    public Double totalMemory;

    /**
     * 磁盘总量
     */
    public Double totalDisk;

    /**
     * 温度
     */
    public Double temperature;

    /**
     * 运行时间
     */
    public Double operationHours;

    /**
     * cpu使用率
     */
    public Double cpuUsage;

    /**
     * 内存使用率
     */
    public Double memoryUsage;

    /**
     * 磁盘使用率
     */
    public Double diskUsage;

    @Override
    public String toString() {
        return "DccDeviceHardware{" +
                "cpuCores=" + cpuCores +
                ", highestFrequency=" + highestFrequency +
                ", totalMemory=" + totalMemory +
                ", totalDisk=" + totalDisk +
                ", temperature=" + temperature +
                ", operationHours=" + operationHours +
                ", cpuUsage=" + cpuUsage +
                ", memoryUsage=" + memoryUsage +
                ", diskUsage=" + diskUsage +
                '}';
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public DccDeviceHardware setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
        return this;
    }

    public Double getHighestFrequency() {
        return highestFrequency;
    }

    public DccDeviceHardware setHighestFrequency(Double highestFrequency) {
        this.highestFrequency = highestFrequency;
        return this;
    }

    public Double getTotalMemory() {
        return totalMemory;
    }

    public DccDeviceHardware setTotalMemory(Double totalMemory) {
        this.totalMemory = totalMemory;
        return this;
    }

    public Double getTotalDisk() {
        return totalDisk;
    }

    public DccDeviceHardware setTotalDisk(Double totalDisk) {
        this.totalDisk = totalDisk;
        return this;
    }

    public Double getTemperature() {
        return temperature;
    }

    public DccDeviceHardware setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public Double getOperationHours() {
        return operationHours;
    }

    public DccDeviceHardware setOperationHours(Double operationHours) {
        this.operationHours = operationHours;
        return this;
    }

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public DccDeviceHardware setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
        return this;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public DccDeviceHardware setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
        return this;
    }

    public Double getDiskUsage() {
        return diskUsage;
    }

    public DccDeviceHardware setDiskUsage(Double diskUsage) {
        this.diskUsage = diskUsage;
        return this;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static DccDeviceHardware getDeviceHardwareInfo(Activity activity){

        AndroidPlatform androidPlatform = getOSAndroidPlatform(activity);

        return new DccDeviceHardware()
                .setDiskUsage(1-(androidPlatform.availableSpace/androidPlatform.storage))//1-可用大小/总大小 = 使用率
                .setTemperature(androidPlatform.temperature)
                .setOperationHours(androidPlatform.runTime)
                .setMemoryUsage(androidPlatform.memoryFreeSpace/androidPlatform.totalMemorySpace)//可用内存大小/总大小
                .setHighestFrequency(Double.parseDouble(getMaxCpuFreq())/100000)
                .setCpuUsage(androidPlatform.cpuUsage)
                .setCpuCores(androidPlatform.cpuCore)
                .setTotalDisk(androidPlatform.storage)
                .setTotalMemory(androidPlatform.totalMemorySpace);
    }

    public static String getMaxCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = { "/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }


}
