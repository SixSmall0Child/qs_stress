package com.bis.stresstest.model;

import android.os.Parcel;
import android.os.Parcelable;

import static java.lang.Double.NaN;

public class AndroidPlatform implements Parcelable {


    //CPU温度
    public double temperature;
    //CPU架构
    public String cpuArchitecture;
    //ROM版本
    public String romVersion;
    //CPU核心数
    public int cpuCore;
    //CPU使用率
    public double cpuUsage;
    //内存总空间
    public double totalMemorySpace;
    //内存可用空间
    public double memoryFreeSpace;
    //手机内部存储总大小
    public double storage;
    //手机内部存储可用大小
    public double availableSpace;
    //网络上行
    public double netWorkUp;
    //网络下行
    public double netWorkDown;
    //已运行时间
    public double runTime;

    public double getRunTime() {
        return runTime;
    }

    public void setRunTime(double runTime) {
        this.runTime = runTime;
    }

    public AndroidPlatform() {
    }

    public AndroidPlatform(Parcel in) {
        temperature = in.readDouble();
        cpuArchitecture = in.readString();
        romVersion = in.readString();
        cpuCore = in.readInt();
        cpuUsage = in.readDouble();
        totalMemorySpace = in.readDouble();
        memoryFreeSpace = in.readDouble();
        storage = in.readDouble();
        availableSpace = in.readDouble();
        netWorkUp = in.readDouble();
        netWorkDown = in.readDouble();
    }

    public static final Creator<AndroidPlatform> CREATOR = new Creator<AndroidPlatform>() {
        @Override
        public AndroidPlatform createFromParcel(Parcel in) {
            return new AndroidPlatform(in);
        }

        @Override
        public AndroidPlatform[] newArray(int size) {
            return new AndroidPlatform[size];
        }
    };

    public static double setDouble(double data) {
        String decimalFormat = new java.text.DecimalFormat("#.00").format(data);
        return Double.parseDouble(decimalFormat);
    }

    public double getTemperature() {
        return toDouble(temperature);
    }

    public void setTemperature(double temperature) {
        this.temperature = setDouble(temperature);
    }

    public String getCpuArchitecture() {
        return cpuArchitecture;
    }

    public void setCpuArchitecture(String cpuArchitecture) {
        this.cpuArchitecture = cpuArchitecture;
    }

    public String getRomVersion() {
        return romVersion;
    }

    public void setRomVersion(String romVersion) {
        this.romVersion = romVersion;
    }

    public int getCpuCore() {
        return cpuCore;
    }

    public void setCpuCore(int cpuCore) {
        this.cpuCore = cpuCore;
    }

    public double getCpuUsage() {
        return toDouble(cpuUsage);
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = setDouble(cpuUsage);
    }

    public double getTotalMemorySpace() {
        return toDouble(totalMemorySpace);
    }

    public void setTotalMemorySpace(double totalMemorySpace) {
        this.totalMemorySpace = setDouble(totalMemorySpace);
    }

    public double getMemoryFreeSpace() {
        return toDouble(memoryFreeSpace);
    }

    public void setMemoryFreeSpace(double memoryFreeSpace) {
        this.memoryFreeSpace = setDouble(memoryFreeSpace);
    }

    public double getStorage() {
        return toDouble(storage);
    }

    public void setStorage(double storage) {
        this.storage = setDouble(storage);
    }

    public double getAvailableSpace() {
        return toDouble(availableSpace);
    }

    public void setAvailableSpace(double availableSpace) {
        this.availableSpace = setDouble(availableSpace);
    }

    public double getNetWorkUp() {
        return toDouble(netWorkUp);
    }

    public void setNetWorkUp(double netWorkUp) {
        this.netWorkUp = setDouble(netWorkUp);
    }

    public double getNetWorkDown() {
        return toDouble(netWorkDown);
    }

    public void setNetWorkDown(double netWorkDown) {
        this.netWorkDown = setDouble(netWorkDown);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(temperature);
        dest.writeString(cpuArchitecture);
        dest.writeString(romVersion);
        dest.writeInt(cpuCore);
        dest.writeDouble(cpuUsage);
        dest.writeDouble(totalMemorySpace);
        dest.writeDouble(memoryFreeSpace);
        dest.writeDouble(storage);
        dest.writeDouble(availableSpace);
        dest.writeDouble(netWorkUp);
        dest.writeDouble(netWorkDown);
    }

    public double toDouble(double _localData) {
        if (_localData == NaN || String.valueOf(_localData).equals("NaN")) {
            return 0;
        }
        return _localData;
    }

    @Override
    public String toString() {
        return "AndroidPlatform{" +
                "temperature=" + temperature +
                ", cpuArchitecture='" + cpuArchitecture + '\'' +
                ", romVersion='" + romVersion + '\'' +
                ", cpuCore=" + cpuCore +
                ", cpuUsage=" + cpuUsage +
                ", totalMemorySpace=" + totalMemorySpace +
                ", memoryFreeSpace=" + memoryFreeSpace +
                ", storage=" + storage +
                ", availableSpace=" + availableSpace +
                ", netWorkUp=" + netWorkUp +
                ", netWorkDown=" + netWorkDown +
                ", runTime=" + runTime +
                '}';
    }
}
