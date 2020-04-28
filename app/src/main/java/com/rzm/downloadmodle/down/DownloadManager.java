package com.rzm.downloadmodle.down;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.rzm.downloadmodle.AppInfo;

public class DownloadManager {

	public static final int STATE_NONE = 0;// 下载未开始
	public static final int STATE_WAITING = 1;// 等待下载
	public static final int STATE_DOWNLOAD = 2;// 正在下载
	public static final int STATE_PAUSE = 3;// 下载暂停
	public static final int STATE_ERROR = 4;// 下载失败
	public static final int STATE_SUCCESS = 5;// 下载成功

	// 所有观察者的集合
	private ArrayList<DownloadObserver> mObservers = new ArrayList<DownloadObserver>();

	// 下载对象的集合, ConcurrentHashMap是线程安全的HashMap
	private ConcurrentHashMap<String, DownloadInfo> mDownloadInfoMap = new ConcurrentHashMap<String, DownloadInfo>();
	// 下载任务集合, ConcurrentHashMap是线程安全的HashMap
	private ConcurrentHashMap<String, DownloadTask> mDownloadTaskMap = new ConcurrentHashMap<String, DownloadTask>();

	private static DownloadManager sInstance = new DownloadManager();
	private IPath downloadPath = new DownloadPath();

	private DownloadManager() {
	}

	public static DownloadManager getInstance() {
		return sInstance;
	}
	public void setDownloadPath(IPath path){
		this.downloadPath = path;
	}

	/**
	 * 开始下载
	 */
	public synchronized void download(AppInfo appInfo) {
		if (appInfo != null) {
			DownloadInfo downloadInfo = mDownloadInfoMap.get(appInfo.id);
			// 如果downloadInfo不为空,表示之前下载过, 就无需创建新的对象, 要接着原来的下载位置继续下载,也就是断点续传
			if (downloadInfo == null) {// 如果为空,表示第一次下载, 需要创建下载对象, 从头开始下载
				downloadInfo = DownloadInfo.copy(appInfo);
			}

			// 下载状态更为正在等待
			downloadInfo.currentState = STATE_WAITING;
			// 通知状态发生变化,各观察者根据此通知更新主界面
			notifyDownloadStateChanged(downloadInfo);

			// 将下载对象保存在集合中
			mDownloadInfoMap.put(appInfo.id, downloadInfo);

			// 初始化下载任务
			final DownloadInfo finalDownloadInfo = downloadInfo;
			DownloadTask downloadTask = new DownloadTask()
					.setDownloadUrl(downloadInfo.downloadUrl)
					.setSavePath(downloadPath)
					.setDownloadListener(new DownloadTask.DownloadListener() {
						@Override
						public void onSuccess(String path) {
							LogUtils.d("下载完成，保存地址为："+path);
							// 不管下载成功,失败还是暂停, 下载任务已经结束,都需要从当前任务集合中移除
							mDownloadTaskMap.remove(finalDownloadInfo.id);
							finalDownloadInfo.currentState = STATE_SUCCESS;
							finalDownloadInfo.path = path;
							notifyDownloadStateChanged(finalDownloadInfo);
						}

						@Override
						public void onFailed(int failCode, String errorMessage) {
							LogUtils.d("下载失败，code = "+failCode+" "+errorMessage);
							// 不管下载成功,失败还是暂停, 下载任务已经结束,都需要从当前任务集合中移除
							mDownloadTaskMap.remove(finalDownloadInfo.id);
							finalDownloadInfo.currentState = STATE_ERROR;
							finalDownloadInfo.currentPos = 0;
							notifyDownloadStateChanged(finalDownloadInfo);
						}

						@Override
						public void onStart() {
							LogUtils.d("下载开始");
							finalDownloadInfo.currentState = STATE_DOWNLOAD;
							finalDownloadInfo.currentPos = 0;
							notifyDownloadStateChanged(finalDownloadInfo);
						}

						@Override
						public void onProgress(int current, int total) {
//							LogUtils.d("总下载进度"+(current*1.0f)/(total*1.0f)*100);
							finalDownloadInfo.currentPos = current;// 更新当前文件下载位置
							finalDownloadInfo.currentState = STATE_DOWNLOAD;
							finalDownloadInfo.size =total;// 更新当前文件下载位置
							notifyDownloadProgressChanged(finalDownloadInfo);// 通知进度更新
						}

						@Override
						public void onPause() {
							LogUtils.d("下载暂停");
							// 不管下载成功,失败还是暂停, 下载任务已经结束,都需要从当前任务集合中移除
							mDownloadTaskMap.remove(finalDownloadInfo.id);
							finalDownloadInfo.currentState = STATE_PAUSE;
						}
					})
					.build();
			// 启动下载任务
			ThreadManager.getThreadPool().execute(downloadTask);
			// 将下载任务对象维护在集合当中
			mDownloadTaskMap.put(appInfo.id, downloadTask);
		}
	}
	@NonNull
	private String getUrl() {
		//http://localhost:8080/GooglePlayServer/download?name=app/com.itheima.www/com.itheima.www.apk&range=0
		StringBuffer urlBuffer = new StringBuffer("");
		urlBuffer.append("");

		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("name","downloadUrl");
		paramsMap.put("range", "downloadSize");
		//转换参数为字符串
		String urlParamsByMap = HttpUtils.getUrlParamsByMap(paramsMap);
		urlBuffer.append(urlParamsByMap);
		return urlBuffer.toString();
	}
	/**
	 * 下载暂停
	 */
	public synchronized void pause(String id) {
		if (!TextUtils.isEmpty(id)) {
			DownloadInfo downloadInfo = mDownloadInfoMap.get(id);
			if (downloadInfo != null) {
				int state = downloadInfo.currentState;
				// 如果当前状态是等待下载或者正在下载, 需要暂停当前任务
				if (state == STATE_WAITING || state == STATE_DOWNLOAD) {
					// 停止当前的下载任务
					DownloadTask downloadTask = mDownloadTaskMap.get(id);
					if (downloadTask != null) {
						//等待状态下直接从等待队列移除
						downloadTask.pause(true);
						ThreadManager.getThreadPool().cancel(downloadTask);
					}

					// 更新下载状态为暂停
					downloadInfo.currentState = STATE_PAUSE;
					notifyDownloadStateChanged(downloadInfo);
				}
			}
		}
	}

	/**
	 * 安装apk
	 */
	public synchronized void install(Context context,String id) {
		DownloadInfo downloadInfo = mDownloadInfoMap.get(id);
		if (downloadInfo != null) {
			File file = new File(downloadInfo.path);
			if (!file.exists()) {
				return;
			}
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Uri data;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//判断版本大于等于7.0
					// 通过FileProvider创建一个content类型的Uri
					data = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
					intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);// 给目标应用一个临时授权
				} else {
					data = Uri.fromFile(file);
				}
				intent.setDataAndType(data, "application/vnd.android.package-archive");
				context.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 根据应用对象,获取对应的下载对象
	public DownloadInfo getDownloadInfo(String id) {
		if (!TextUtils.isEmpty(id)) {
			return mDownloadInfoMap.get(id);
		}

		return null;
	}


	// 2. 注册观察者
	public synchronized void registerObserver(DownloadObserver observer) {
		if (observer != null && !mObservers.contains(observer)) {
			mObservers.add(observer);
		}
	}

	// 3. 注销观察者
	public synchronized void unregisterObserver(DownloadObserver observer) {
		if (observer != null && mObservers.contains(observer)) {
			mObservers.remove(observer);
		}
	}

	// 4. 通知下载状态变化
	public synchronized void notifyDownloadStateChanged(DownloadInfo info) {
		for (DownloadObserver observer : mObservers) {
			observer.onDownloadStateChanged(info);
		}
	}

	// 5. 通知下载进度变化
	public synchronized void notifyDownloadProgressChanged(DownloadInfo info) {
		for (DownloadObserver observer : mObservers) {
			observer.onDownloadProgressChanged(info);
		}
	}

	/**
	 * 1. 定义下载观察者接口
	 */
	public interface DownloadObserver {
		// 下载状态发生变化
		void onDownloadStateChanged(DownloadInfo info);

		// 下载进度发生变化
		void onDownloadProgressChanged(DownloadInfo info);
	}

}
