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

public class DccDeviceBattery implements Serializable {

    /**
     * 健康状态（0健康  1良好  2损坏）
     */
    public Integer health;
    /**
     * 状态（默认值 1 充电中）
     */
    public Integer status;

    /**
     * 温度
     */
    public Double temperature;

    /**
     * 电压
     */
    public Double voltage;

    @Override
    public String toString() {
        return "DccDeviceBattery{" +
                "health=" + health +
                ", status=" + status +
                ", temperature=" + temperature +
                ", voltage=" + voltage +
                '}';
    }

    public Integer getHealth() {
        return health;
    }

    public DccDeviceBattery setHealth(Integer health) {
        this.health = health;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public DccDeviceBattery setStatus(Integer status) {
        this.status = status;
        return  this;
    }

    public Double getTemperature() {
        return temperature;
    }

    public DccDeviceBattery setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public Double getVoltage() {
        return voltage;
    }

    public DccDeviceBattery setVoltage(Double voltage) {
        this.voltage = voltage;
        return this;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static DccDeviceBattery getDccDeviceBatteryInfo(Activity activity){
        return new DccDeviceBattery()
                .setHealth(1)
                .setStatus(1)
                .setTemperature(25.6)
                .setVoltage(3.85);
    }


}
