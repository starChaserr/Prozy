package com.downloader.proxy.utils;

import android.os.Environment;

public class defaultDirectory {//Downloads Folder.
    public static String get() {
//        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
        return Environment.DIRECTORY_DOWNLOADS;
    }
}
