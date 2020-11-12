package com.rzm.downloadlibrary_android.pack;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.rzm.downloadlibrary_android.DownloadManager;
import com.rzm.downloadlibrary_android.DownloadService;
import com.rzm.downloadlibrary_android.utils.LogUtils;

public class AndroidDownloadManager implements IDownload {
    private static volatile AndroidDownloadManager instance;
    private final Context context;
    private final DownloadManager download;
    private Cursor cursor;

    private AndroidDownloadManager(Context context) {
        this.context = context;
        download = new DownloadManager(context.getContentResolver(),
                context.getPackageName());
        download.setAccessAllDownloads(true);
        Intent intent = new Intent();
        intent.setClass(context, DownloadService.class);
        context.startService(intent);
    }
    public static AndroidDownloadManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AndroidDownloadManager.class) {
                if (instance == null) {
                    instance = new AndroidDownloadManager(context);
                }
            }
        }
        return instance;
    }
    @Override
    public void start(DownloadInfo downloadInfo) {
        LogUtils.d("download start uniqueKey = "+downloadInfo.getUniqueKey());
        DownloadManager.Request request = new DownloadManager.Request( Uri.parse(downloadInfo.getDownloadUrl()));
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, "/");
        request.setDescription("Just for test");
        download.enqueue(request);
    }

    @Override
    public void pause(String uniqueKey) {
        LogUtils.d("download pause uniqueKey = "+uniqueKey);
        DownloadManager.Query baseQuery = new DownloadManager.Query()
                .setOnlyIncludeVisibleInDownloadsUi(true);
        cursor = download.query(baseQuery);
        int columnId = cursor
                .getColumnIndexOrThrow(DownloadManager.COLUMN_ID);
        long downloadId = cursor.getInt(columnId);
        download.pauseDownload(downloadId);
    }

    @Override
    public void resume(String uniqueKey) {
        LogUtils.d("download resume uniqueKey = "+uniqueKey);
        DownloadManager.Query baseQuery = new DownloadManager.Query()
                .setOnlyIncludeVisibleInDownloadsUi(true);
        cursor = download.query(baseQuery);
        int columnId = cursor
                .getColumnIndexOrThrow(DownloadManager.COLUMN_ID);
        long downloadId = cursor.getInt(columnId);
        download.resumeDownload(downloadId);
    }

    @Override
    public void install(String uniqueKey) {
        LogUtils.d("download install uniqueKey = "+uniqueKey);
    }

    @Override
    public void remove(String uniqueKey) {

    }
}
