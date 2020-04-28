package com.rzm.downloadmodle.down;

import java.io.File;

import android.os.Environment;

import com.rzm.downloadmodle.AppInfo;


public class DownloadInfo {

	public String id;// apk唯一标识
	public long size;// apk大小
	public String downloadUrl;// 下载链接
	public String name;// apk名称
	public int currentState;// 当前下载状态
	public long currentPos;// 当前下载位置
	public String path;// apk下载在本地的路径

	private static final String GOOGLEMARKET = "googlemarket";// 文件夹名称
	private static final String DOWNLOAD = "download";// 子文件夹名称

	// 获取当前下载进度
	public float getProgress() {
		if (size == 0) {
			return 0;
		}

		return (float) currentPos / size;
	}

	/**
	 * 根据应用信息,拷贝出一个下载对象
	 */
	public static DownloadInfo copy(AppInfo appInfo) {
		DownloadInfo info = new DownloadInfo();
		info.id = appInfo.id;
		info.size = appInfo.size;
		info.downloadUrl = appInfo.downloadUrl;
		info.name = appInfo.name;
		info.currentState = DownloadManager.STATE_NONE;
		info.currentPos = 0;
		info.path = getFilePath(info.name);

		return info;
	}

	/**
	 * 获取apk文件的本地下载路径
	 */
	private static String getFilePath(String name) {
		StringBuffer sb = new StringBuffer();
		String sdcardPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		sb.append(sdcardPath);
		sb.append(File.separator);
		sb.append(GOOGLEMARKET);
		sb.append(File.separator);
		sb.append(DOWNLOAD);

		boolean success = createDir(sb.toString());

		System.out.println("success:" + success);
		System.out.println("dir:" + sb.toString());

		if (success) {
			return sb.toString() + File.separator + name + ".apk";
		}

		return null;
	}

	// 创建文件夹
	// 注意添加读写sdcard的权限
	private static boolean createDir(String dirPath) {
		File dirFile = new File(dirPath);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			// 如果文件夹不存在,或者此文件不是一个文件夹,需要创建文件夹
			return dirFile.mkdirs();
		}

		return true;// 文件夹已经存在
	}

}
