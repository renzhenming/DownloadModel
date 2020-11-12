package com.rzm.downloadlibrary.cache;

import android.content.Context;
import android.text.TextUtils;

import com.rzm.downloadlibrary.db.DatabaseHelper;
import com.rzm.downloadlibrary.download.DownloadInfo;
import com.rzm.downloadlibrary.utils.LogUtils;

import java.util.List;

public class DefaultCacheManager implements ICache<DownloadInfo> {

    public static final String TAG = "DefaultCacheManager";
    private MemoryCache memory;
    private LocalCache local;

    public DefaultCacheManager(Context context) {
        memory = new MemoryCache();
        local = new LocalCache(context);
    }

    @Override
    public void setCache(String uniqueKey, DownloadInfo downloadInfo) {
        memory.setCache(uniqueKey,downloadInfo);
        local.setCache(uniqueKey,downloadInfo);
    }

    @Override
    public DownloadInfo getCache(String uniqueKey) {
        if (!TextUtils.isEmpty(uniqueKey)) {
            DownloadInfo cache = memory.getCache(uniqueKey);
            if (cache == null){
                LogUtils.d(TAG+" 内存中没有，尝试去数据库查找");
                cache = local.getCache(uniqueKey);
                if (cache != null){
                    memory.setCache(uniqueKey,cache);
                    LogUtils.d(TAG+" 获取到数据库缓存 当前下载位置 = "+cache.getCurrentPos()+" cache downloadUrl = "+cache.getDownloadUrl());
                }else{
                    LogUtils.d(TAG+" 数据库中也没有 返回null");
                }
            }else{
                LogUtils.d(TAG+" 获取到内存缓存 当前下载位置 = "+cache.getCurrentPos()+"  cache downloadUrl = "+cache.getDownloadUrl());
            }
            return cache;
        }
        return null;
    }

    @Override
    public List<DownloadInfo> getCacheByPkgName(String packageName) {
        return local.queryByPkgName(packageName);
    }

    @Override
    public int updateCache(String uniqueKey, DownloadInfo downloadInfo) {
        memory.updateCache(uniqueKey,downloadInfo);
        local.updateCache(uniqueKey,downloadInfo);
        return 1;
    }
}
