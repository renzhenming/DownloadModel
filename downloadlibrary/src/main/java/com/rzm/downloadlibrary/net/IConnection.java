package com.rzm.downloadlibrary.net;

import java.io.IOException;
import java.io.InputStream;

public interface IConnection {
    int getContentLength(String downloadUrl,int requestTimeOut) throws IOException;
    InputStream download(String downloadUrl,int rangeStart,int rangeEnd,int requestTimeOut) throws IOException;
    String[] getResponseInfo();
}
