package com.rzm.downloadlibrary.utils;

import android.text.TextUtils;
import java.util.Map;

public class HttpUtils {

    public static String getUrlParamsByMap(Map<String, Object> map) {
        if (map == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer("?");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue());
            sb.append("&");
        }
        String s = sb.toString();
        if (s.endsWith("&")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static String getFileName(String downloadUrl) {
        String fileName = "";
        if (TextUtils.isEmpty(downloadUrl)) {
            throw new NullPointerException("downloadUrl is null");
        }
        int i = downloadUrl.lastIndexOf("/");
        if (i == -1) {
            fileName = Md5Utils.md5(downloadUrl) + "";
        } else {
            fileName = downloadUrl.substring(i + 1);
        }
        return fileName;
    }

}
