package com.rzm.downloadmodle.down;

import android.os.Environment;

import java.io.File;

public class DefaultPathManager implements IPath {
    @Override
    public String downloadPath() {
        return Environment.getExternalStorageDirectory()+ File.separator+"download";
    }
}
