package com.bis.stresstest.model;

public class RunningModel {
    private String type;
    private String identify;
    private Data data;

    @Override
    public String toString() {
        return "{\"type\":\"" + type + '\"' +
                ", \"identify\":\"" + identify + '\"' +
                ", \"data\":" + data.toString()+
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

        public String getCpuUsage() {
            return cu;
        }

        public void setCpuUsage(String cpuUsage) {
            this.cu = cpuUsage;
        }

        public String getMemoryUsage() {
            return mu;
        }

        public void setMemoryUsage(String memoryUsage) {
            this.mu = memoryUsage;
        }

        public String getDiskUsage() {
            return du;
        }

        public void setDiskUsage(String diskUsage) {
            this.du = diskUsage;
        }

        public String getTemperature() {
            return tp;
        }

        public void setTemperature(String temperature) {
            this.tp = temperature;
        }

        private String cu;//CPU使用率
        private String mu;//内存使用率
        private String du;//硬盘使用率
        private String tp;//温度

        @Override
        public String toString() {
            return "{\"cu\":\"" + cu + '\"' +
                    ", \"mu\":\"" + mu + '\"' +
                    ", \"du\":\"" + du + '\"' +
                    ", \"tp\":\"" + tp + '\"' +
                    '}';
        }

    }

}
