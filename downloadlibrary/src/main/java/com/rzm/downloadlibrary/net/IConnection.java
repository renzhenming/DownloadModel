package com.rzm.downloadlibrary.net;

import java.io.InputStream;

public interface IConnection {
    int getContentLength(String downloadUrl) throws Exception;

    InputStream download(String downloadUrl, int rangeStart, int rangeEnd) throws Exception;
}
