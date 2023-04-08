package com.bis.stresstest.util;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
//import android.net.NetworkUtils;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetUtils {

    public static final int NETWORK_NONE = 0; // 没有网络连接
    public static final int NETWORK_WIFI = 1; // wifi连接
    public static final int NETWORK_2G = 2; // 2G
    public static final int NETWORK_3G = 3; // 3G
    public static final int NETWORK_4G = 4; // 4G
    public static final int NETWORK_MOBILE = 5; // 手机流量
    private static String[] returnStr;

    /*
     * convert subMask string to prefix length
     */
    public static int maskStr2InetMask(String maskStr) {
        StringBuffer sb;
        String str;
        int inetmask = 0;
        int count = 0;
        /*
         * check the subMask format
         */
        Pattern pattern = Pattern.compile("(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$");
        if (pattern.matcher(maskStr).matches() == false) {
            Logger.i("subMask is error");
            return 0;
        }

        String[] ipSegment = maskStr.split("\\.");
        for (int n = 0; n < ipSegment.length; n++) {
            sb = new StringBuffer(Integer.toBinaryString(Integer.parseInt(ipSegment[n])));
            str = sb.reverse().toString();
            count = 0;
            for (int i = 0; i < str.length(); i++) {
                i = str.indexOf("1", i);
                if (i == -1)
                    break;
                count++;
            }
            inetmask += count;
        }
        return inetmask;
    }


    public static Inet4Address getIPv4Address(String text) {
//        try {
//            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
//        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
//        }
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * ip
     */

    public static String getIpAddrForInterfaces(String interfaceName) {
        try {
            Enumeration<NetworkInterface> enNetworkInterface = NetworkInterface.getNetworkInterfaces(); //获取本机所有的网络接口
            while (enNetworkInterface.hasMoreElements()) {  //判断 Enumeration 对象中是否还有数据
                NetworkInterface networkInterface = enNetworkInterface.nextElement();   //获取 Enumeration 对象中的下一个数据
                if (!networkInterface.isUp()) { // 判断网口是否在使用
                    continue;
                }
                if (!interfaceName.equals(networkInterface.getDisplayName())) { // 网口名称是否和需要的相同
                    continue;
                }
                Enumeration<InetAddress> enInetAddress = networkInterface.getInetAddresses();   //getInetAddresses 方法返回绑定到该网卡的所有的 IP 地址。
                while (enInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enInetAddress.nextElement();
                    if (inetAddress instanceof Inet4Address) {  //判断是否未ipv4
                        return inetAddress.getHostAddress();
                    }
//                    判断未lo时
//                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
//                        return inetAddress.getHostAddress();
//                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }


    /**
     * 子网掩码
     */
    public static String getIpAddrMaskForInterfaces(String interfaceName) {
        try {
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();    //获取本机所有的网络接口
            while (networkInterfaceEnumeration.hasMoreElements()) { //判断 Enumeration 对象中是否还有数据
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement(); //获取 Enumeration 对象中的下一个数据
                if (!networkInterface.isUp() && !interfaceName.equals(networkInterface.getDisplayName())) { //判断网口是否在使用，判断是否时我们获取的网口
                    continue;
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {    //
                    if (interfaceAddress.getAddress() instanceof Inet4Address) {    //仅仅处理ipv4
                        return calcMaskByPrefixLength(interfaceAddress.getNetworkPrefixLength());   //获取掩码位数，通过 calcMaskByPrefixLength 转换为字符串
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "error";
    }

    //通过子网掩码的位数计算子网掩码
    public static String calcMaskByPrefixLength(int length) {

        int mask = 0xffffffff << (32 - length);
        int partsNum = 4;
        int bitsOfPart = 8;
        int maskParts[] = new int[partsNum];
        int selector = 0x000000ff;

        for (int i = 0; i < maskParts.length; i++) {
            int pos = maskParts.length - 1 - i;
            maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
        }

        String result = "";
        result = result + maskParts[0];
        for (int i = 1; i < maskParts.length; i++) {
            result = result + "." + maskParts[i];
        }
        return result;
    }


    /**
     * 网关
     */
    public static String getGateWay() {
        String[] arr;
        try {
            Process process = Runtime.getRuntime().exec("ip route list table 0");
            String data = null;
            BufferedReader ie = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String string = in.readLine();

            arr = string.split("\\s+");
            return arr[2];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }


    /**
     * 获取dns
     *
     * @param context
     * @return
     */
    public static String getDns(Context context) {
        /**
         * 获取dns
         */
        String[] dnsServers = getDnsFromCommand();
        if (dnsServers == null || dnsServers.length == 0) {
            dnsServers = getDnsFromConnectionManager(context);
        }
        /**
         * 组装
         */
        StringBuffer sb = new StringBuffer();
        if (dnsServers != null) {
            for (int i = 0; i < dnsServers.length; i++) {
                sb.append(dnsServers[i]);
                //sb.append(" / ");
            }
        }
        //
        return sb.toString();
    }

    //通过 getprop 命令获取
    public static String[] getDnsFromCommand() {
        LinkedList<String> dnsServers = new LinkedList<>();
        try {
            Process process = Runtime.getRuntime().exec("getprop");
            InputStream inputStream = process.getInputStream();
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = lnr.readLine()) != null) {
                int split = line.indexOf("]: [");
                if (split == -1) continue;
                String property = line.substring(1, split);
                String value = line.substring(split + 4, line.length() - 1);
                if (property.endsWith(".dns")
                        || property.endsWith(".dns1")
                        || property.endsWith(".dns2")
                        || property.endsWith(".dns3")
                        || property.endsWith(".dns4")) {
                    InetAddress ip = InetAddress.getByName(value);
                    if (ip == null) continue;
                    value = ip.getHostAddress();
                    if (value == null) continue;
                    if (value.length() == 0) continue;
                    dnsServers.add(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dnsServers.isEmpty() ? new String[0] : dnsServers.toArray(new String[dnsServers.size()]);
    }


    private static String[] getDnsFromConnectionManager(Context context) {
        LinkedList<String> dnsServers = new LinkedList<>();
        if (Build.VERSION.SDK_INT >= 21 && context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null) {
                    for (Network network : connectivityManager.getAllNetworks()) {
                        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                        if (networkInfo != null && networkInfo.getType() == activeNetworkInfo.getType()) {
                            LinkProperties lp = connectivityManager.getLinkProperties(network);
                            for (InetAddress addr : lp.getDnsServers()) {
                                dnsServers.add(addr.getHostAddress());
                            }
                        }
                    }
                }
            }
        }
        return dnsServers.isEmpty() ? new String[0] : dnsServers.toArray(new String[dnsServers.size()]);
    }


    /**
     * 获取外网的IP(要访问Url，要放到后台线程里处理)
     *
     * @param @return
     * @return String
     * @throws
     * @Title: GetNetIp
     * @Description:
     */
    public static String getNetIp() {
        URL infoUrl = null;
        InputStream inStream = null;
        String ipLine = "";
        HttpURLConnection httpConnection = null;
        try {
//            infoUrl = new URL("http://ip168.com/");
            infoUrl = new URL("http://pv.sohu.com/cityjson?ie=utf-8");
            URLConnection connection = infoUrl.openConnection();
            httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    strber.append(line + "\n");
                }
                Pattern pattern = Pattern
                        .compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
                Matcher matcher = pattern.matcher(strber.toString());
                if (matcher.find()) {
                    ipLine = matcher.group();
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
                httpConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Log.e("getNetIp", ipLine);
        return ipLine;
    }

    /**
     * 获取当前网络连接的类型
     *
     * @param context context
     * @return int
     */
    public static int getNetworkState(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); // 获取网络服务
        if (null == connManager) { // 为空则认为无网络
            return NETWORK_NONE;
        }
        // 获取网络类型，如果为空，返回无网络
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return NETWORK_NONE;
        }
        // 判断是否为WIFI
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORK_WIFI;
                }
            }
        }
        // 若不是WIFI，则去判断是2G、3G、4G网
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            /*
             GPRS : 2G(2.5) General Packet Radia Service 114kbps
             EDGE : 2G(2.75G) Enhanced Data Rate for GSM Evolution 384kbps
             UMTS : 3G WCDMA 联通3G Universal Mobile Telecommunication System 完整的3G移动通信技术标准
             CDMA : 2G 电信 Code Division Multiple Access 码分多址
             EVDO_0 : 3G (EVDO 全程 CDMA2000 1xEV-DO) Evolution - Data Only (Data Optimized) 153.6kps - 2.4mbps 属于3G
             EVDO_A : 3G 1.8mbps - 3.1mbps 属于3G过渡，3.5G
             1xRTT : 2G CDMA2000 1xRTT (RTT - 无线电传输技术) 144kbps 2G的过渡,
             HSDPA : 3.5G 高速下行分组接入 3.5G WCDMA High Speed Downlink Packet Access 14.4mbps
             HSUPA : 3.5G High Speed Uplink Packet Access 高速上行链路分组接入 1.4 - 5.8 mbps
             HSPA : 3G (分HSDPA,HSUPA) High Speed Packet Access
             IDEN : 2G Integrated Dispatch Enhanced Networks 集成数字增强型网络 （属于2G，来自维基百科）
             EVDO_B : 3G EV-DO Rev.B 14.7Mbps 下行 3.5G
             LTE : 4G Long Term Evolution FDD-LTE 和 TDD-LTE , 3G过渡，升级版 LTE Advanced 才是4G
             EHRPD : 3G CDMA2000向LTE 4G的中间产物 Evolved High Rate Packet Data HRPD的升级
             HSPAP : 3G HSPAP 比 HSDPA 快些
             */
            // 2G网络
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_2G;
            // 3G网络
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK_3G;
            // 4G网络
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NETWORK_4G;
            default:
                return NETWORK_MOBILE;
        }
    }
}