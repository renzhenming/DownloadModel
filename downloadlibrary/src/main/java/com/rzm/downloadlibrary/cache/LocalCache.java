package com.rzm.downloadlibrary.cache;

import android.content.Context;

import com.rzm.downloadlibrary.db.DatabaseHelper;
import com.rzm.downloadlibrary.download.DownloadInfo;
import java.util.List;

public class LocalCache {

    private final DatabaseHelper databaseHelper;

    public LocalCache(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    public long setCache(String uniqueKey, DownloadInfo downloadInfo) {
        return databaseHelper.insert(downloadInfo);
    }

    public DownloadInfo getCache(String uniqueKey) {
        List<DownloadInfo> downloadInfos = databaseHelper.queryByKey(uniqueKey);
        if (downloadInfos != null && downloadInfos.size() > 0) {
            return downloadInfos.get(0);
        } else {
            return null;
        }
    }

    public int updateCache(String uniqueKey, DownloadInfo downloadInfo) {
        int update = databaseHelper.update(uniqueKey, downloadInfo);
        return update;
    }

    public int deleteCache(String uniqueKey) {
        int delete = databaseHelper.delete(uniqueKey);
        return delete;
    }

    public List<DownloadInfo> queryByPkgName(String packageName) {
        return databaseHelper.queryByPkgName(packageName);
    }
}