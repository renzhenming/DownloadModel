package com.rzm.downloadmodle;

public class AppInfo {

    public String id;
    public String name;
    public String packageName;
    public String iconUrl;
    public float stars;
    public long size;
    public String downloadUrl;
    public String des;


    public String author;//黑马程序员
    public String date;//"2015-06-10"
    public String version;//	1.1.0605.0
    public String downloadNum;//	40万+

    @Override
    public String toString() {
        return "AppInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
