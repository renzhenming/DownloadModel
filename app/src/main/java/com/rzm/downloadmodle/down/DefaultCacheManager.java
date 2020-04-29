package com.rzm.downloadmodle.down;

import android.content.Context;
import android.text.TextUtils;

public class DefaultCacheManager implements ICache<DownloadInfo> {

    public static final String TAG = "DefaultCacheManager";
    private MemoryCache memory;
    private LocalCache local;

    public DefaultCacheManager(Context context) {
        memory = new MemoryCache();
        local = new LocalCache(context);
    }

    @Override
    public void setCache(String id, DownloadInfo downloadInfo) {
        memory.setCache(id,downloadInfo);
        local.setCache(id,downloadInfo);
    }

    @Override
    public DownloadInfo getCache(String id) {
        if (!TextUtils.isEmpty(id)) {
            DownloadInfo cache = memory.getCache(id);
            if (cache == null){
                LogUtils.d(TAG+" 内存中没有，尝试去数据库查找");
                cache = local.getCache(id);
                if (cache != null){
                    memory.setCache(id,cache);
                    LogUtils.d(TAG+" 获取到数据库缓存 当前下载位置 = "+cache.currentPos+" cache downloadUrl = "+cache.downloadUrl);
                }else{
                    LogUtils.d(TAG+" 数据库中也没有 返回null");
                }
            }else{
                LogUtils.d(TAG+" 获取到内存缓存 当前下载位置 = "+cache.currentPos+"  cache downloadUrl = "+cache.downloadUrl);
            }
            return cache;
        }
        return null;
    }

    @Override
    public int updateCache(String id, DownloadInfo downloadInfo) {
        memory.updateCache(id,downloadInfo);
        local.updateCache(id,downloadInfo);
        return 1;
    }
}
