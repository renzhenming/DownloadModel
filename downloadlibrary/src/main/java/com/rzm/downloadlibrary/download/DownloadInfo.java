package com.rzm.downloadlibrary.download;

import android.text.TextUtils;

import com.rzm.downloadlibrary.utils.HttpUtils;

import java.io.Serializable;

public class DownloadInfo implements Serializable {

	public String id;// apk唯一标识
	public long size;// apk大小
	public String downloadUrl;// 下载链接
	public String name;// apk名称
	public int currentState;// 当前下载状态
	public long currentPos;// 当前下载位置
	public String path;// apk下载在本地的路径

	// 获取当前下载进度
	public float getProgress() {
		if (size == 0) {
			return 0;
		}

		return (float) currentPos / size;
	}

	public DownloadInfo(){}
	/**
	 * 根据应用信息,拷贝出一个下载对象
	 */
	public DownloadInfo(String downloadUrl,String id) {
		this.id = id;
		this.downloadUrl =downloadUrl;
		String fileName = HttpUtils.getFileName(downloadUrl);
		this.name = TextUtils.isEmpty(fileName)?"未命名":fileName;
		this.currentState = DownloadManager.STATE_NONE;
		this.currentPos = 0;
	}
}
