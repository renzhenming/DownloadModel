package com.rzm.downloadlibrary.download;

import android.text.TextUtils;

import com.rzm.downloadlibrary.utils.HttpUtils;

import java.io.Serializable;

public class DownloadInfo implements Serializable {
    public String id;
    // 资源名称
    public String name;
    // apk大小
    public long size;
    // 下载链接
    public String downloadUrl;
    // 当前下载状态
    public int currentState;
    // 当前下载位置
    public long currentPos;
    // apk下载在本地的路径
    public String path;
    //下载唯一标识,可以是任意唯一值
    public String downloadMd5;
    //包名
    public String packageName;

    // 获取当前下载进度
    public float getProgress() {
        if (size == 0) {
            return 0;
        }

        return (float) currentPos / size;
    }

    private DownloadInfo() {
    }

    /**
     * 根据应用信息,拷贝出一个下载对象
     */
    public DownloadInfo(String downloadUrl, String id) {
        this.id = id;
        this.downloadUrl = downloadUrl;
        String fileName = HttpUtils.getFileName(downloadUrl);
        this.name = TextUtils.isEmpty(fileName) ? "未命名" : fileName;
        this.currentState = DownloadManager.STATE_NONE;
        this.currentPos = 0;
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

        public Builder setDownloadMd5(String downloadMd5) {
            downloadInfo.downloadMd5 = downloadMd5;
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
}
