package com.rzm.downloadlibrary.path;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class DefaultPathManager implements IPath {
    private final Context context;

    public DefaultPathManager(Context context){
        this.context = context;
    }
    @Override
    public String downloadPath() {
        return Environment.getExternalStorageDirectory()+ File.separator+"download_asset";
        //return context.getFilesDir()+ File.separator+"download_asset";
    }
}
