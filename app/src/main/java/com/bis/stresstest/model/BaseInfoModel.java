package com.bis.stresstest.model;

public class BaseInfoModel {
    private String type;
    private String identify;
    private Data data;

    @Override
    public String toString() {
        return "{\"type\":\"" + type + '\"' +
                ", \"identify\":\"" + identify + '\"' +
                ", \"data\":" + data.toString() +
                '}';
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setIdentify(String identify) {
        this.identify = identify;
    }

    public String getIdentify() {
        return identify;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public static class Data {

//        private String macAddress;
//        private String ip;
//        private String SN;
//        private String IMEI;
//        private String architecture;
//        private String romVersion;
//        private String totalmemory;
//        private String totalDisk;
//        private String maxFrequency;
//
//        @Override
//        public String toString() {
//            return "{\"macAddress\":\"" + macAddress + '\"' +
//                    ", \"ip\":\"" + ip + '\"' +
//                    ", \"SN\":\"" + SN + '\"' +
//                    ", \"IMEI\":\"" + IMEI + '\"' +
//                    ", \"architecture\":\"" + architecture + '\"' +
//                    ", \"romVersion\":\"" + romVersion + '\"' +
//                    ", \"totalmemory\":\"" + totalmemory + '\"' +
//                    ", \"totalDisk\":\"" + totalDisk + '\"' +
//                    ", \"maxFrequency\":\"" + maxFrequency + '\"' +
//                    '}';
//        }

        private String mc;//MAC地址
        private String ip;//IP地址
        private String SN;//SN号
        private String md;//型号
        private String rv;//ROM版本
        private String gw;//网关
        private String nm;//子网掩码
        private String dns1;
        private String dns2;

        @Override
        public String toString() {
            return "{\"mc\":\"" + mc + '\"' +
                    ", \"ip\":\"" + ip + '\"' +
                    ", \"SN\":\"" + SN + '\"' +
                    ", \"md\":\"" + md + '\"' +
                    ", \"rv\":\"" + rv + '\"' +
                    ", \"gw\":\"" + gw + '\"' +
                    ", \"nm\":\"" + nm + '\"' +
                    ", \"dns1\":\"" + dns1 + '\"' +
                    ", \"dns2\":\"" + dns2 + '\"' +
                    '}';
        }

        public void setMc(String mc) {
            this.mc = mc;
        }

        public String getMc() {
            return mc;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getIp() {
            return ip;
        }

        public void setSN(String SN) {
            this.SN = SN;
        }

        public String getSN() {
            return SN;
        }

        public void setMd(String md) {
            this.md = md;
        }

        public String getMd() {
            return md;
        }

        public void setRv(String rv) {
            this.rv = rv;
        }

        public String getRv() {
            return rv;
        }

        public void setGw(String gw) {
            this.gw = gw;
        }

        public String getGw() {
            return gw;
        }

        public void setNm(String nm) {
            this.nm = nm;
        }

        public String getNm() {
            return nm;
        }

        public void setDns1(String dns1) {
            this.dns1 = dns1;
        }

        public String getDns1() {
            return dns1;
        }

        public void setDns2(String dns2) {
            this.dns2 = dns2;
        }

        public String getDns2() {
            return dns2;
        }

    }

}