package com.downloader.proxy.utils;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class snack {
    public static void show(String Text, View v){
        Snackbar.make(v, Text, Snackbar.LENGTH_SHORT).show();
    }
}
