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

    public  void setCache(String id,DownloadInfo t){
        lruCache.put(id,t);
        LogUtils.d(TAG+"lruCache setCache id= "+id);
    }

    public DownloadInfo getCache(String id){
        LogUtils.d(TAG+"lruCache getCache id= "+id + " lruCache.get(id)="+lruCache.get(id));
        return lruCache.get(id);
    }

    public void updateCache(String id, DownloadInfo downloadInfo) {
        setCache( id, downloadInfo);
    }
}
