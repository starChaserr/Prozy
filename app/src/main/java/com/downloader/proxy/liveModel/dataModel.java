package com.downloader.proxy.liveModel;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;

public class dataModel {
    public static int mp4 = 0, mp3 = 1;



    private final static MutableLiveData<String> downloadUrl = new MutableLiveData<>(null);
    private final static MutableLiveData<String> filename = new MutableLiveData<>(null);
    private final static MutableLiveData<Integer> progress = new MutableLiveData<>(0);
    private final static MutableLiveData<Integer> downloadStatus = new MutableLiveData<>(0);
    private final static MutableLiveData<Integer> isConverting = new MutableLiveData<>(0);
    private final static MutableLiveData<String> status = new MutableLiveData<>();
    private final static MutableLiveData<Bitmap> thumbnail = new MutableLiveData<>(null);
    private final static MutableLiveData<String> likes = new MutableLiveData<>();
    private final static MutableLiveData<Float> totalDownloadSize = new MutableLiveData<>(0F);
    private final static MutableLiveData<Float> totalDownloadedSize = new MutableLiveData<>(0F);
    private final static MutableLiveData<String> views = new MutableLiveData<>();

    public static void setDownloadUrl(String url){
        downloadUrl.postValue(url);
    }

    public static void setTotalDownloadSize(Float bytes){
        totalDownloadSize.postValue(bytes);
    }

    public static void setTotalDownloadedSize(Float bytes){
        totalDownloadedSize.postValue(bytes);
    }

    public static void setViews(String v){
        views.postValue(v);
    }

    public static void setLikes(String l){
        likes.postValue(l);
    }

    public static void setThumbnail(Bitmap bitmap){
        thumbnail.postValue(bitmap);
    }

    public static void setProgress(int p){
        progress.postValue(p);
    }

    public static void setIsConverting(int t){
        isConverting.postValue(t);
    }

    public static void setDownloadStatus(int p){
        downloadStatus.postValue(p);
    }

    public static void setStatus(String s){
        status.postValue(s);
    }

    public static void setFileName(String Filename){
        filename.postValue(Filename);
    }

    public static MutableLiveData<String> getDownloadUrl(){
        return downloadUrl;
    }

    public static MutableLiveData<String> getFileName(){
        return filename;
    }

    public static MutableLiveData<Bitmap> getThumbnail(){
        return thumbnail;
    }

    public static MutableLiveData<Integer> getProgress(){
        return  progress;
    }

    public static MutableLiveData<Integer> getDownloadStatus(){
        return  downloadStatus;
    }

    public static MutableLiveData<Integer> getIsConverting(){
        return isConverting;
    }

    public static MutableLiveData<String> getStatus(){
        return status;
    }

    public static MutableLiveData<String> getLikes(){
        return likes;
    }

    public static MutableLiveData<String> getViews(){
        return views;
    }

    public static MutableLiveData<Float> getTotalDownloadSize(){
        return totalDownloadSize;
    }

    public static MutableLiveData<Float> getTotalDownloadedSize(){
        return totalDownloadedSize;
    }
}
