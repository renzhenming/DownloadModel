package com.rzm.downloadlibrary.cache;

import android.util.LruCache;

import com.rzm.downloadlibrary.download.DownloadInfo;
import com.rzm.downloadlibrary.utils.LogUtils;

public class MemoryCache {
    public static final String TAG = "MemoryCache";

    private LruCache<String, DownloadInfo> lruCache;

    public MemoryCache() {
        lruCache = new LruCache((int) (Runtime.getRuntime().maxMemory()/8));
        LogUtils.d(TAG+"lruCache size = "+(int) (Runtime.getRuntime().maxMemory()/8));
    }

    public  void setCache(String uniqueKey,DownloadInfo t){
        lruCache.put(uniqueKey,t);
    }

    public DownloadInfo getCache(String uniqueKey){
        return lruCache.get(uniqueKey);
    }

    public void updateCache(String uniqueKey, DownloadInfo downloadInfo) {
        setCache( uniqueKey, downloadInfo);
    }
}
