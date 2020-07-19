package com.rzm.downloadlibrary.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class DefaultConnectionManager implements IConnection {

    @Override
    public int getContentLength(String downloadUrl) throws Exception {
        //1.确定服务器资源的总大小。  通过使用URLConnection请求资源，获取响应的信息，获取资源的大小
        URL url = new URL(downloadUrl);
        HttpURLConnection cn = getHttpConnection(url);
        cn.setRequestMethod("GET");
        int code = cn.getResponseCode();
        //200 : 请求全部资源成功 206：请求部分资源成功 300 ： 重定向或跳转  400 ： 资源不存在  500 ：服务器异常
        if (code == 200) {
            //获取资源大小
            return cn.getContentLength();
        } else {
            throw new IOException("failed to connect to server,code = " + code + " error msg = " + cn.getResponseMessage());
        }
    }

    /**
     * 由于要请求服务器部分资源，所需请求前需要设置一些参数告诉服务器要请求资源的范围
     * 表示头500个字节：bytes=0-499
     * 表示第二个500字节：bytes=500-999
     * 表示最后500个字节：bytes=-500 . |1 a. N% a, m% r, a% `( u9 I1 _
     * 表示500字节以后的范围：bytes=500-
     * 第一个和最后一个字节：bytes=0-0,-1
     * cn.setRequestProperty("Range", "bytes=" + lastDownloadIndex + "-" + endIndex);告诉服务器请求资源的范围
     *
     * @param downloadUrl
     * @param rangeStart
     * @param rangeEnd
     * @return
     * @throws IOException
     */
    @Override
    public InputStream download(String downloadUrl, int rangeStart, int rangeEnd) throws Exception {
        URL url = new URL(downloadUrl);
        HttpURLConnection cn = getHttpConnection(url);
        cn.setRequestMethod("GET");
        cn.setRequestProperty("Range", "bytes=" + rangeStart + "-" + rangeEnd);//告诉服务器请求资源的范围
        int code = cn.getResponseCode();
        //200 : 请求全部资源成功 206：请求部分资源成功 300 ： 重定向或跳转  400 ： 资源不存在  500 ：服务器异常
        if (code == 206 || code == 200) {
            return cn.getInputStream();
        } else {
            throw new IOException("failed to download assets,code = " + code + " error msg = " + cn.getResponseMessage());
        }
    }

    public HttpURLConnection getHttpConnection(URL url) throws Exception {
        HttpURLConnection conn;
        if (url.getProtocol().equals("https")) {
            conn = (HttpsURLConnection) url.openConnection();
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
            SSLContext.setDefault(ctx);
            ((HttpsURLConnection) conn).setSSLSocketFactory(ctx.getSocketFactory());
            ((HttpsURLConnection) conn).setHostnameVerifier(new DefaultHostnameVerifier());
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(10000);
        return conn;
    }

}
