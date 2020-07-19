package com.rzm.downloadlibrary.cache;

import android.content.Context;

import com.rzm.downloadlibrary.db.DatabaseHelper;
import com.rzm.downloadlibrary.download.DownloadInfo;
import java.util.List;

public class LocalCache {

    private final DatabaseHelper databaseHelper;

    public LocalCache(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public long setCache(String id, DownloadInfo downloadInfo) {
        return databaseHelper.insert(downloadInfo);
    }

    public DownloadInfo getCache(String id) {
        List<DownloadInfo> downloadInfos = databaseHelper.queryById(id);
        if (downloadInfos != null && downloadInfos.size() > 0) {
            return downloadInfos.get(0);
        } else {
            return null;
        }
    }

    public List<DownloadInfo> getCacheByPkg(String packageName) {
        return databaseHelper.queryById(packageName);
    }

    public int updateCache(String id, DownloadInfo downloadInfo) {
        int update = databaseHelper.update(id, downloadInfo);
        return update;
    }

    public int deleteCache(String id) {
        int delete = databaseHelper.delete(id);
        return delete;
    }
}