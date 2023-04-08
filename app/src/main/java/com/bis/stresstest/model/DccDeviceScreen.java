package com.bis.stresstest.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.DisplayMetrics;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author luojiaying
 * @since 2020-09-21
 */

public class DccDeviceScreen implements Serializable {


    /**
     * 屏幕密度
     */
    private String screenDensity;

    /**
     * 屏幕高度位px）
     */
    private Integer screenWidth;

    /**
     * 屏幕高度(单位px)
     */
    private Integer screenHeight;

    /**
     * 屏幕方向（0竖屏 1横屏）
     */
    private Integer screenOrientation;

    /**
     * x轴的dpi
     */
    private Integer screenXdpi;

    /**
     * y轴的dpi
     */
    private Integer screenYdpi;

    @Override
    public String toString() {
        return "DccDeviceScreen{" +
                "screenDensity='" + screenDensity + '\'' +
                ", screenWidth=" + screenWidth +
                ", screenHeight=" + screenHeight +
                ", screenOrientation=" + screenOrientation +
                ", screenXdpi=" + screenXdpi +
                ", screenYdpi=" + screenYdpi +
                '}';
    }

    public String getScreenDensity() {
        return screenDensity;
    }

    public DccDeviceScreen setScreenDensity(String screenDensity) {
        this.screenDensity = screenDensity;
        return this;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public DccDeviceScreen setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
        return this;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public DccDeviceScreen setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
        return this;
    }

    public Integer getScreenOrientation() {
        return screenOrientation;
    }

    public DccDeviceScreen setScreenOrientation(Integer screenOrientation) {
        this.screenOrientation = screenOrientation;
        return this;
    }

    public Integer getScreenXdpi() {
        return screenXdpi;
    }

    public DccDeviceScreen setScreenXdpi(Integer screenXdpi) {
        this.screenXdpi = screenXdpi;
        return this;
    }

    public Integer getScreenYdpi() {
        return screenYdpi;
    }

    public DccDeviceScreen setScreenYdpi(Integer screenYdpi) {
        this.screenYdpi = screenYdpi;
        return this;
    }


    //0是竖屏 1是横屏
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static DccDeviceScreen getDeviceScreenInfo(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return new DccDeviceScreen()
                .setScreenDensity(String.valueOf(dm.densityDpi))
                .setScreenHeight(dm.heightPixels)
                .setScreenWidth(dm.widthPixels)
                .setScreenOrientation(dm.heightPixels > dm.widthPixels ? 0 : 1)
                .setScreenXdpi(320)
                .setScreenYdpi(480);
    }

}
