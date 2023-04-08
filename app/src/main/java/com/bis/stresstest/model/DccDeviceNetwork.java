package com.bis.stresstest.model;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.io.Serializable;


/**
 * <p>
 * 
 * </p>
 *
 * @author luojiaying
 * @since 2020-09-21
 */
public class DccDeviceNetwork implements Serializable {

    @Override
    public String toString() {
        return "DccDeviceNetwork{" +
                "connection=" + connection +
                ", flightMode=" + flightMode +
                ", roamingStatus=" + roamingStatus +
                ", networkType=" + networkType +
                '}';
    }

    /**
     * 是否连接(0未连接 1已连接）
     */
    public Integer connection;

    /**
     * 飞行模式（0未开启 1已开启）
     */
    public Integer flightMode;

    /**
     * 漫游状态（0 否 1是）
     */
    public Integer roamingStatus;

    /**
     * 网络类型（0 4G 1 WiFi 2 以太网）
     */
    public Integer networkType;



    public Integer getConnection() {
        return connection;
    }

    public DccDeviceNetwork setConnection(Integer connection) {
        this.connection = connection;
        return  this;
    }

    public Integer getFlightMode() {
        return flightMode;
    }

    public DccDeviceNetwork setFlightMode(Integer flightMode) {
        this.flightMode = flightMode;
        return  this;
    }

    public Integer getRoamingStatus() {
        return roamingStatus;
    }

    public DccDeviceNetwork setRoamingStatus(Integer roamingStatus) {
        this.roamingStatus = roamingStatus;
        return  this;
    }

    public Integer getNetworkType() {
        return networkType;
    }

    public DccDeviceNetwork setNetworkType(Integer networkType) {
        this.networkType = networkType;
        return  this;
    }



    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static DccDeviceNetwork getDeviceNetworkInfo(Activity activity){
        return new DccDeviceNetwork()
                .setConnection(1)
                .setFlightMode(0)
                .setNetworkType(2)
                .setRoamingStatus(0);
    }



}
