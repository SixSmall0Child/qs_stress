package com.bis.stresstest.model;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author luojiaying
 * @since 2020-09-21
 */

public class DccDeviceApp implements Serializable {

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageNames() {
        return packageNames;
    }

    public void setPackageNames(String packageNames) {
        this.packageNames = packageNames;
    }

    /**
     * app名称
     */
    public String appName;

    /**
     * 包名
     */
    public String packageNames;


}
