package com.rzm.downloadlibrary.download;

import java.io.Serializable;

public class DownloadInfo implements Serializable {

    // 下载未开始
    public static final int STATE_NONE = 0;
    // 等待下载
    public static final int STATE_WAITING = 1;
    // 正在下载
    public static final int STATE_DOWNLOADING = 2;
    // 下载暂停
    public static final int STATE_PAUSE = 3;
    // 下载失败
    public static final int STATE_ERROR = 4;
    // 下载成功
    public static final int STATE_SUCCESS = 5;

    private String id;
    // 资源名称
    private String name;
    // apk大小
    private long size;
    // 下载链接
    private String downloadUrl;
    // 当前下载状态
    private int currentState = STATE_NONE;
    // 当前下载位置
    private long currentPos;
    // apk下载在本地的路径
    private String path;
    //下载唯一标识,可以是任意唯一值
    private String uniqueKey;
    //包名
    private String packageName;

    // 获取当前下载进度
    public float getProgress() {
        if (size <= 0) {
            return 0;
        }
        if (currentPos < 0) {
            currentPos = 0;
        }
        return (float) currentPos / size;
    }

    private DownloadInfo() {
    }

    public static class Builder {

        private final DownloadInfo downloadInfo;

        public Builder() {
            downloadInfo = new DownloadInfo();
        }

        public Builder setName(String name) {
            downloadInfo.name = name;
            return this;
        }

        public Builder setSize(long size) {
            downloadInfo.size = size;
            return this;
        }

        public Builder setDownloadUrl(String downloadUrl) {
            downloadInfo.downloadUrl = downloadUrl;
            return this;
        }

        public Builder setCurrentState(int currentState) {
            downloadInfo.currentState = currentState;
            return this;
        }

        public Builder setCurrentPos(long currentPos) {
            downloadInfo.currentPos = currentPos;
            return this;
        }

        public Builder setPath(String path) {
            downloadInfo.path = path;
            return this;
        }

        public Builder setUniqueKey(String uniqueKey) {
            downloadInfo.uniqueKey = uniqueKey;
            return this;
        }

        public Builder setPackageNanme(String packageName) {
            downloadInfo.packageName = packageName;
            return this;
        }

        public DownloadInfo build() {
            return downloadInfo;
        }
    }

    public int getCurrentState() {
        return currentState;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public long getCurrentPos() {
        return currentPos;
    }

    public String getPath() {
        return path;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public void setCurrentPos(long currentPos) {
        this.currentPos = currentPos;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * 检测当前状态是否需要开始下载
     * @param currentState
     * @return
     */
    public static boolean checkStart(int currentState){
        if (currentState == DownloadInfo.STATE_NONE
                || currentState == DownloadInfo.STATE_PAUSE
                || currentState == DownloadInfo.STATE_ERROR){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 检测当前状态是否需要开始暂停
     * @param currentState
     * @return
     */
    public static boolean checkPause(int currentState){
        if (currentState == DownloadInfo.STATE_DOWNLOADING
                || currentState == DownloadInfo.STATE_WAITING){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 检测当前状态是否需要开始安装
     * @param currentState
     * @return
     */
    public static boolean checkInstall(int currentState){
        if (currentState == DownloadInfo.STATE_SUCCESS){
            return true;
        }else{
            return false;
        }
    }
}
