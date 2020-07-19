package com.rzm.downloadlibrary.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.rzm.downloadlibrary.cache.DefaultCacheManager;
import com.rzm.downloadlibrary.cache.ICache;
import com.rzm.downloadlibrary.net.DefaultConnectionManager;
import com.rzm.downloadlibrary.net.IConnection;
import com.rzm.downloadlibrary.path.DefaultPathManager;
import com.rzm.downloadlibrary.path.IPath;
import com.rzm.downloadlibrary.thread.DefaultThreadPool;
import com.rzm.downloadlibrary.thread.IThreadPool;
import com.rzm.downloadlibrary.utils.LogUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadManager {
    // 下载未开始
    public static final int STATE_NONE = 0;
    // 等待下载
    public static final int STATE_WAITING = 1;
    // 正在下载
    public static final int STATE_DOWNLOAD = 2;
    // 下载暂停
    public static final int STATE_PAUSE = 3;
    // 下载失败
    public static final int STATE_ERROR = 4;
    // 下载成功
    public static final int STATE_SUCCESS = 5;

    private static DownloadManager instance;
    private IPath pathManager;
    private ICache<DownloadInfo> cacheManager;
    private IThreadPool threadManager;
    private IConnection connManager;
    //线程切换
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    // 观察者的集合
    private ArrayList<DownloadObserver> mObservers = new ArrayList<DownloadObserver>();
    // 下载任务集合, ConcurrentHashMap是线程安全的HashMap
    private ConcurrentHashMap<String, DownloadTask> mDownloadTaskMap = new ConcurrentHashMap<String, DownloadTask>();

    private DownloadManager(Context context) {
        pathManager = new DefaultPathManager();
        cacheManager = new DefaultCacheManager(context);
        threadManager = new DefaultThreadPool();
        connManager = new DefaultConnectionManager();
    }

    public static DownloadManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * 下载器的path管理，包括各种存储路径等，对外开放
     *
     * @param path
     */
    public void setPathManager(IPath path) {
        this.pathManager = path;
    }

    /**
     * 下载器的缓存管理，对外开放
     *
     * @param cache
     */
    public void setCacheManager(ICache cache) {
        this.cacheManager = cache;
    }

    /**
     * 线程池接口，可视具体需要更换
     *
     * @param threadPool
     */
    public void setThreadPool(IThreadPool threadPool) {
        this.threadManager = threadPool;
    }

    /**
     * 设置网络工具，可视具体需要更换
     *
     * @param connection
     */
    public void setConnManager(IConnection connection) {
        this.connManager = connection;
    }

    /**
     * 开始下载
     */
    public synchronized void download(DownloadInfo info) {
        if (info == null) {
            throw new NullPointerException("downloadInfo is null");
        }
        String downloadUrl = info.downloadUrl;
        if (TextUtils.isEmpty(downloadUrl)) {
            throw new NullPointerException("downloadUrl is null");
        }
        String downloadMd5 = info.downloadMd5;
        if (TextUtils.isEmpty(downloadMd5)) {
            throw new NullPointerException("downloadMd5 is null");
        }
        DownloadInfo downloadInfo = cacheManager.getCache(downloadMd5);
        if (downloadInfo == null) {
            // 如果为空,表示第一次下载
            downloadInfo = info;
        }
        // 下载状态更为正在等待
        downloadInfo.currentState = STATE_WAITING;
        // 通知状态发生变化,各观察者根据此通知更新主界面
        notifyDownloadStateChanged(downloadInfo);
        // 将下载对象保存在集合中
        cacheManager.setCache(downloadMd5, downloadInfo);
        // 初始化下载任务
        final DownloadInfo finalInfo = downloadInfo;
        DownloadTask downloadTask = new DownloadTask()
                .setDownloadUrl(downloadInfo.downloadUrl)
                .setSavePath(pathManager)
                .setConnManager(connManager)
                .setDownloadListener(new DownloadTask.DownloadListener() {

                    @Override
                    public void onStart() {
                        LogUtils.d("下载开始");
                        finalInfo.currentState = STATE_DOWNLOAD;
                        finalInfo.currentPos = 0;
                        cacheManager.updateCache(finalInfo.downloadMd5, finalInfo);
                        notifyDownloadStateChanged(finalInfo);
                    }

                    @Override
                    public void onProgress(int current, int total) {
                        finalInfo.currentPos = current;
                        finalInfo.currentState = STATE_DOWNLOAD;
                        finalInfo.size = total;
                        cacheManager.updateCache(finalInfo.downloadMd5, finalInfo);
                        notifyDownloadStateChanged(finalInfo);
                    }

                    @Override
                    public void onPause() {
                        LogUtils.d("下载暂停");
                        // 不管下载成功,失败还是暂停, 下载任务已经结束,都需要从当前任务集合中移除
                        mDownloadTaskMap.remove(finalInfo.downloadMd5);
                        finalInfo.currentState = STATE_PAUSE;
                        cacheManager.updateCache(finalInfo.downloadMd5, finalInfo);
                        // 更新下载状态为暂停
                        notifyDownloadStateChanged(finalInfo);
                    }

                    @Override
                    public void onSuccess(String path) {
                        LogUtils.d("下载完成，保存地址为：" + path);
                        // 不管下载成功,失败还是暂停, 下载任务已经结束,都需要从当前任务集合中移除
                        mDownloadTaskMap.remove(finalInfo.downloadMd5);
                        finalInfo.currentState = STATE_SUCCESS;
                        finalInfo.path = path;
                        cacheManager.updateCache(finalInfo.downloadMd5, finalInfo);
                        notifyDownloadStateChanged(finalInfo);
                    }

                    @Override
                    public void onFailed(int failCode, String errorMessage) {
                        LogUtils.d("下载失败，code = " + failCode + " " + errorMessage);
                        // 不管下载成功,失败还是暂停, 下载任务已经结束,都需要从当前任务集合中移除
                        mDownloadTaskMap.remove(finalInfo.downloadMd5);
                        finalInfo.currentState = STATE_ERROR;
                        finalInfo.currentPos = 0;
                        cacheManager.updateCache(finalInfo.downloadMd5, finalInfo);
                        notifyDownloadStateChanged(finalInfo);
                    }
                })
                .build();
        // 启动下载任务
        threadManager.execute(downloadTask);
        // 将下载任务对象维护在集合当中
        mDownloadTaskMap.put(downloadMd5, downloadTask);
    }

    /**
     * 下载暂停
     */
    public synchronized void pause(String downloadMd5) {
        if (TextUtils.isEmpty(downloadMd5)) {
            throw new NullPointerException("downloadMd5 is null");
        }
        DownloadInfo downloadInfo = cacheManager.getCache(downloadMd5);
        if (downloadInfo == null) {
            throw new NullPointerException(downloadMd5 + " is not in downloading");
        }
        int state = downloadInfo.currentState;
        // 如果当前状态是等待下载或者正在下载, 需要暂停当前任务
        if (state == STATE_WAITING || state == STATE_DOWNLOAD) {
            // 停止当前的下载任务
            DownloadTask downloadTask = mDownloadTaskMap.get(downloadMd5);
            if (downloadTask != null) {
                //等待状态下直接从等待队列移除
                downloadTask.pause(true);
                threadManager.cancel(downloadTask);
            }
        }
    }

    /**
     * 安装apk
     */
    public synchronized void install(Context context, String downloadMd5) {
        if (TextUtils.isEmpty(downloadMd5)) {
            throw new NullPointerException("downloadMd5 is null");
        }
        DownloadInfo downloadInfo = cacheManager.getCache(downloadMd5);
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
    public DownloadInfo getDownloadInfo(String downloadMd5) {
        if (TextUtils.isEmpty(downloadMd5)) {
            throw new NullPointerException("downloadMd5 is null");
        }
        return cacheManager.getCache(downloadMd5);
    }


    public synchronized void registerObserver(DownloadObserver observer) {
        if (observer != null && !mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    public synchronized void unregisterObserver(DownloadObserver observer) {
        if (observer != null && mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }

    public synchronized void notifyDownloadStateChanged(final DownloadInfo info) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (DownloadObserver observer : mObservers) {
                    observer.onDownloadStateChanged(info);
                }
            }
        });
    }

    public interface DownloadObserver {
        void onDownloadStateChanged(DownloadInfo info);
    }
}
