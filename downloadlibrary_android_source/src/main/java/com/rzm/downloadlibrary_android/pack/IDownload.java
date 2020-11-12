package com.rzm.downloadlibrary_android.pack;

public interface IDownload {
    void start(DownloadInfo uniqueKey);

    void pause(String uniqueKey);

    void resume(String uniqueKey);

    void install(String uniqueKey);

    void remove(String uniqueKey);
}
