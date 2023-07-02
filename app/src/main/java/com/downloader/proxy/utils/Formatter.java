package com.downloader.proxy.utils;

import java.text.DecimalFormat;

public class Formatter {
    public static String format(float Number, int NumsAfterDecimal){
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(NumsAfterDecimal);
        return decimalFormat.format(Number);
    }
}
