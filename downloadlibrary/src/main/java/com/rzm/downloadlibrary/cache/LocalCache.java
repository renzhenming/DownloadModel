package com.rzm.downloadlibrary.cache;

import android.content.Context;

import com.rzm.downloadlibrary.db.DaoSupportFactory;
import com.rzm.downloadlibrary.db.IDaoSupport;
import com.rzm.downloadlibrary.download.DownloadInfo;
import java.util.List;

public class LocalCache {

    private final IDaoSupport<Object> dao;

    public LocalCache(Context context){
        dao = DaoSupportFactory.getFactory(context).getDao(DownloadInfo.class);
    }

    public void setCache(String id, DownloadInfo downloadInfo) {
        final List<DownloadInfo> list = dao.querySupport().selection("id = ?").selectionArgs(id).query();
        if (list == null || list.size() == 0) {
            dao.insert(downloadInfo);
        }else{
            dao.update(downloadInfo, "id=?", new String[]{id});
        }
    }

    public DownloadInfo getCache(String id) {
        final List<DownloadInfo> list = dao.querySupport().selection("id = ?").selectionArgs(id).query();
        if (list != null && list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    public int updateCache(String id, DownloadInfo downloadInfo) {
        return dao.update(downloadInfo, "id=?", new String[]{id});
    }
}
