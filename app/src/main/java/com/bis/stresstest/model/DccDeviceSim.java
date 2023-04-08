package com.bis.stresstest.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.bis.stresstest.util.Logger;
import com.bis.stresstest.util.ShellUtils;

import java.io.Serializable;

import androidx.annotation.RequiresApi;

/**
 * <p>
 *
 * </p>
 *
 * @author luojiaying
 * @since 2020-09-21
 */
public class DccDeviceSim implements Serializable {


    @Override
    public String toString() {
        return "DccDeviceSim{" +
                "waft=" + waft +
                ", network=" + network +
                ", imei='" + imei + '\'' +
                '}';
    }

    /**
     * 信号
     */
    private Integer waft;

    private Integer network;

    private String imei;


    public Integer getWaft() {
        return waft;
    }

    public DccDeviceSim setWaft(Integer waft) {
        this.waft = waft;
        return this;
    }


    public Integer getNetwork() {
        return network;
    }

    public DccDeviceSim setNetwork(Integer network) {
        this.network = network;
        return this;
    }

    public String getImei() {
        return imei;
    }

    public DccDeviceSim setImei(String imei) {
        this.imei = imei;
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static DccDeviceSim getDccDeviceSimInfo(Activity activity){
        TelephonyManager mr = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand("getprop persist.udef.g.imei", true);

        Logger.i("手机IMEI信息:"+commandResult.getSuccessMsg()+",手机网络信息:"+mr.getNetworkOperator()+",手机信号信息:"+mr.getSignalStrength());
        return new DccDeviceSim()
                .setImei(commandResult.getSuccessMsg())
                .setNetwork(3)
                .setWaft(0);
    }

}
