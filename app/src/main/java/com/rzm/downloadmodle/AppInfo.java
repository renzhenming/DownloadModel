package com.rzm.downloadmodle;

public class AppInfo {

    public String id;
    public String name;
    public String downloadUrl;
    public String version;
    public String downloadMd5;

    @Override
    public String toString() {
        return "AppInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
