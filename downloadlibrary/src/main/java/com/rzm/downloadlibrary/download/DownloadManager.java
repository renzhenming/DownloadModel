package com.rzm.downloadlibrary.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntRange;
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadManager {
    private static DownloadManager instance;
    private final Context context;
    private IPath pathManager;
    private ICache<DownloadInfo> cacheManager;
    private IThreadPool threadManager;
    private IConnection connManager;
    private int threadCount = 1;
    private String downloadName = "";
    //线程切换
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    // 观察者的集合
    private ArrayList<DownloadObserver> mObservers = new ArrayList<DownloadObserver>();
    // 下载任务集合, ConcurrentHashMap是线程安全的HashMap
    private ConcurrentHashMap<String, DownloadTask> mDownloadTaskMap = new ConcurrentHashMap<String, DownloadTask>();

    private DownloadManager(Context context) {
        this.context = context;
        pathManager = new DefaultPathManager(context);
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
     * 调试模式
     */
    public void debug() {
        LogUtils.debug(true);
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
     * 开启几个线程下载
     *
     * @param count
     * @return
     */
    public void setThreadCount(@IntRange(from = 1, to = Integer.MAX_VALUE) int count) {
        this.threadCount = count;
    }

    /**
     * 开始下载
     */
    public synchronized void download(DownloadInfo info) {
        if (info == null) {
            throw new NullPointerException("downloadInfo is null");
        }
        String downloadUrl = info.getDownloadUrl();
        if (TextUtils.isEmpty(downloadUrl)) {
            throw new NullPointerException("downloadUrl is null");
        }
        final String uniqueKey = info.getUniqueKey();
        if (TextUtils.isEmpty(uniqueKey)) {
            throw new NullPointerException("uniqueKey is null");
        }
        DownloadInfo downloadInfo = cacheManager.getCache(uniqueKey);
        if (downloadInfo == null) {
            // 如果为空,表示第一次下载
            downloadInfo = info;
        }
        if (downloadInfo.getCurrentState() == DownloadInfo.STATE_DOWNLOADING) {
            LogUtils.d("download already begin, return url = " + downloadInfo.getDownloadUrl());
            return;
        }
        // 下载状态更为正在等待
        downloadInfo.setCurrentState(DownloadInfo.STATE_WAITING);
        notifyDownloadStateChanged(downloadInfo);
        // 将下载对象保存在缓存中
        cacheManager.setCache(uniqueKey, downloadInfo);
        final DownloadInfo finalInfo = downloadInfo;
        final DownloadTask downloadTask = new DownloadTask(context)
                .setDownloadUrl(downloadInfo.getDownloadUrl())
                .setSavePath(pathManager)
                .setThreadCount(threadCount)
                .setDownloadName(downloadName = finalInfo.getName())
                .setConnManager(connManager)
                .setDownloadListener(new DownloadTask.DownloadListener() {

                    @Override
                    public void onStart(String downloadUrl) {
                        LogUtils.d("DownloadTask onStart downloadUrl = " + downloadUrl);
                        try {
                            DownloadInfo clonedInfo = finalInfo.clone();
                            clonedInfo.setCurrentState(DownloadInfo.STATE_DOWNLOADING);
                            clonedInfo.setCurrentPos(0);
                            cacheManager.updateCache(clonedInfo.getUniqueKey(), clonedInfo);
                            notifyDownloadStateChanged(clonedInfo);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onProgress(int current, int total) {
                        try {
                            DownloadInfo clonedInfo = finalInfo.clone();
                            clonedInfo.setCurrentPos(current);
                            clonedInfo.setCurrentState(DownloadInfo.STATE_DOWNLOADING);
                            clonedInfo.setSize(total);
                            cacheManager.updateCache(clonedInfo.getUniqueKey(), clonedInfo);
                            notifyDownloadStateChanged(clonedInfo);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPause(String downloadUrl) {
                        LogUtils.d("DownloadTask onPause downloadUrl = " + downloadUrl);
                        try {
                            DownloadInfo clonedInfo = finalInfo.clone();
                            // 不管下载成功,失败还是暂停, 下载任务已经结束,都需要从当前任务集合中移除
                            mDownloadTaskMap.remove(clonedInfo.getUniqueKey());
                            clonedInfo.setCurrentState(DownloadInfo.STATE_PAUSE);
                            cacheManager.updateCache(uniqueKey, clonedInfo);
                            notifyDownloadStateChanged(clonedInfo);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onSuccess(String downloadUrl, String path) {
                        LogUtils.d("DownloadTask onSuccess downloadUrl = " + downloadUrl + " path = " + path);
                        try {
                            DownloadInfo clonedInfo = finalInfo.clone();
                            // 不管下载成功,失败还是暂停, 下载任务已经结束,都需要从当前任务集合中移除
                            mDownloadTaskMap.remove(clonedInfo.getUniqueKey());
                            clonedInfo.setCurrentState(DownloadInfo.STATE_SUCCESS);
                            clonedInfo.setPath(path);
                            cacheManager.updateCache(clonedInfo.getUniqueKey(), clonedInfo);
                            notifyDownloadStateChanged(clonedInfo);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailed(String downloadUrl, int failCode, String errorMessage) {
                        LogUtils.d("DownloadTask onFailed downloadUrl = " + downloadUrl + " code = " + failCode + " " + errorMessage);
                        try {
                            DownloadInfo clonedInfo = finalInfo.clone();
                            // 不管下载成功,失败还是暂停, 下载任务已经结束,都需要从当前任务集合中移除
                            mDownloadTaskMap.remove(clonedInfo.getUniqueKey());
                            clonedInfo.setCurrentState(DownloadInfo.STATE_ERROR);
                            clonedInfo.setCurrentPos(0);
                            cacheManager.updateCache(clonedInfo.getUniqueKey(), clonedInfo);
                            notifyDownloadStateChanged(clonedInfo);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .build();
        // 启动下载任务
        threadManager.execute(downloadTask);
        // 将下载任务对象维护在集合当中
        mDownloadTaskMap.put(uniqueKey, downloadTask);
    }

    /**
     * 下载暂停
     */
    public synchronized void pause(String uniqueKey) {
        if (TextUtils.isEmpty(uniqueKey)) {
            throw new NullPointerException("uniqueKey is null, pause fail");
        }
        DownloadInfo downloadInfo = cacheManager.getCache(uniqueKey);
        if (downloadInfo == null) {
            throw new NullPointerException("uniqueKey = " + uniqueKey + " is not in downloading");
        }
        int state = downloadInfo.getCurrentState();
        // 如果当前状态是等待下载或者正在下载, 需要暂停当前任务
        if (state == DownloadInfo.STATE_WAITING || state == DownloadInfo.STATE_DOWNLOADING) {
            // 停止当前的下载任务
            DownloadTask downloadTask = mDownloadTaskMap.get(uniqueKey);
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
    public synchronized void install(Context context, String uniqueKey) {
        if (TextUtils.isEmpty(uniqueKey)) {
            throw new NullPointerException("uniqueKey is null");
        }
        DownloadInfo downloadInfo = cacheManager.getCache(uniqueKey);
        if (downloadInfo == null || TextUtils.isEmpty(downloadInfo.getPath())) {
            LogUtils.d("the apk you want to install has not been downloaded uniqueKey = " + uniqueKey);
            return;
        }
        File file = new File(downloadInfo.getPath());
        if (!file.exists()) {
            LogUtils.d("the apk is no longer exist in the path = " + file.getAbsolutePath());
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri data;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//判断版本大于等于7.0
                // 通过FileProvider创建一个content类型的Uri
                data = FileProvider.getUriForFile(context, context.getPackageName() + ".rzm.download.provider", file);
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

    /**
     * 获取下载信息
     *
     * @param uniqueKey 本次下载唯一标识
     * @return
     */
    public DownloadInfo getDownloadInfo(String uniqueKey) {
        if (TextUtils.isEmpty(uniqueKey)) {
            throw new NullPointerException("uniqueKey is null");
        }
        // 先获取info等待进一步验证
        DownloadInfo downloadInfo = cacheManager.getCache(uniqueKey);
        if (downloadInfo == null) return null;

        // 获取到的info为下载中状态，但不在downloadTaskMap中，即因外部原因导致下载中止且未更新状态
        if (downloadInfo.getCurrentState() == DownloadInfo.STATE_DOWNLOADING
                && mDownloadTaskMap.get(uniqueKey) == null) {
            // 此时用户的期望是该Task有显式的暂停中标示，不应为下载中，更不应因此在调用下载方法时被pause()拦截，从而彻底卡住该Task
            // 更新info的状态为暂停中
            downloadInfo.setCurrentState(DownloadInfo.STATE_PAUSE);
        }

        return downloadInfo;
    }

    public List<DownloadInfo> getDownloadInfoByPkgName(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            throw new NullPointerException("getDownloadInfoByPkgName packageName is null");
        }
        return cacheManager.getCacheByPkgName(packageName);
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
