package com.rzm.downloadmodle.down;

import android.content.Context;

import com.rzm.downloadmodle.down.db.DaoSupportFactory;
import com.rzm.downloadmodle.down.db.IDaoSupport;

import java.util.List;

public class LocalCache {

    private final IDaoSupport<Object> dao;

    public LocalCache(Context context){
        dao = DaoSupportFactory.getFactory(context).getDao(DownloadInfo.class);
    }

    public void setCache(String id, DownloadInfo downloadInfo) {
        dao.insert(downloadInfo);
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
