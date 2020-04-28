package com.rzm.downloadmodle.down;

import android.os.Environment;

import java.io.File;

public class DownloadPath implements IPath {
    @Override
    public String downloadPath() {
        return Environment.getExternalStorageDirectory()+ File.separator+"download";
    }
}
