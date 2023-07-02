package com.downloader.proxy.utils;

import static com.downloader.proxy.liveModel.dataModel.mp3;
import static com.downloader.proxy.liveModel.dataModel.mp4;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.downloader.proxy.liveModel.dataModel;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class Extractor {
    public static void mp3Link(String Url, Context c) {
        new YouTubeExtractor(c) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                if (ytFiles != null) {
                    dataModel.setStatus("2");
                    int highestAudioQuality = 0;
                    YtFile highestAudioQualityFile = null;

                    for (int i = 0; i < ytFiles.size(); i++) {
                        int format = ytFiles.keyAt(i);
                        YtFile ytFile = ytFiles.get(format);
                        if (ytFile.getFormat().toString().contains("mp4") && ytFile.getFormat().getAudioBitrate()
                                > highestAudioQuality) {
                            highestAudioQuality = ytFile.getFormat().getAudioBitrate();
                            highestAudioQualityFile = ytFile;
                        }
                    }

                    if (highestAudioQualityFile != null) {
                        dataModel.setDownloadUrl(highestAudioQualityFile.getUrl());
                    }
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dataModel.setStatus("0");
            }

            @Override
            protected void onPostExecute(SparseArray<YtFile> ytFiles) {
                super.onPostExecute(ytFiles);
                getDatas(Url, c);
            }

            @Override
            protected void onCancelled(SparseArray<YtFile> ytFileSparseArray) {
                super.onCancelled(ytFileSparseArray);
                dataModel.setStatus("3");
            }
        }.extract(Url, true, true);
    }

    private static void getDatas(String url, Context c) {
        getVideoInfo(url);
        fetchThumbnail(extractVideoId(url), (Activity) c);
        String videoId = extractVideoId(url);

        if (videoId != null) {
            String videoPageUrl = "https://www.youtube.com/watch?v=" + videoId;
            new FetchLikesCountTask().execute(videoPageUrl);
        }
        extractVideoTitle(url, (Activity) c);
    }

    public static String extractVideoId(String url) {
        String videoId = null;

        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v=|v=|\\/v\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|v%2F)[^#\\&\\?\\n]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url); // url is youtube url for which you want to extract the video id.
        if (matcher.find()) {
            videoId = matcher.group();
        }

        return videoId;
    }

    private static String extractViewsCount(String html) {
        String pattern = "\"viewCount\":\"(\\d+)\"";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static String extractLikesCount(Document doc) {
        Element likeButton = doc.selectFirst("yt-icon.style-scope.ytd-toggle-button-renderer");
        if (likeButton != null) {
            String likesCount = likeButton.text().replaceAll("[^\\d]", "");
            Log.e("Likes", "extractLikesCount: " + likesCount);
            return likesCount;
        } else {
            Log.e("Likes", "extractLikesCount: null");
        }
        return "";
    }

    public static void downloadFromUrl(String youtubeDlUrl, String downloadTitle,
                                       String fileName, Context c, String downloadDir, int downloadType) {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);

        if (downloadType == mp4) {
            fileName = fileName + ".mp4";
        } else if (downloadType == mp3) {
            fileName = fileName + ".mp3";
        }

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(downloadDir, fileName);

        DownloadManager manager = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = manager.enqueue(request);

        AsyncTask<Void, Float, Void> downloadTask = new AsyncTask<Void, Float, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                boolean downloading = true;
                while (downloading) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor cursor = manager.query(query);
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int status = cursor.getInt(columnIndex);
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            dataModel.setStatus("5");
                            downloading = false;
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            dataModel.setStatus("4");
                            downloading = false;
                        } else if (status == DownloadManager.STATUS_RUNNING) {
                            int columnIndexProgress = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                            int totalSize = cursor.getInt(columnIndexProgress);
                            int columnIndexBytesDownloaded = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                            int bytesDownloaded = cursor.getInt(columnIndexBytesDownloaded);
                            int progress = (int) ((bytesDownloaded * 100L) / totalSize);
                            float readableTotal = cursor.getFloat(columnIndexProgress) / (1024 * 1024);
                            float readableDownloaded = cursor.getFloat(columnIndexBytesDownloaded) / (1024 * 1024);
                            publishProgress((float) progress, readableTotal, readableDownloaded);
                        }
                    }
                    cursor.close();
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Float... values) {
                updateDownloadProgress(values);
            }

            @Override
            protected void onPostExecute(Void result) {
            }
        };

        downloadTask.execute();
        dataModel.setDownloadUrl(null);
        dataModel.setFileName(null);
    }

    private static void updateDownloadProgress(Float[] values) {
        int progress = values[0].intValue();
        dataModel.setTotalDownloadSize(values[1]);
        dataModel.setTotalDownloadedSize(values[2]);
        dataModel.setProgress(progress);
    }


    private static void extractVideoTitle(String youtubeUrl, Activity c) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(youtubeUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String htmlResponse = response.body().string();

                // Extract the video title using regular expressions
                Pattern pattern = Pattern.compile("<title>(.*?) - YouTube</title>");
                Matcher matcher = pattern.matcher(htmlResponse);
                if (matcher.find()) {
                    String videoTitle = matcher.group(1);
                    // Use the extracted video title
                    c.runOnUiThread(() -> {
                        dataModel.setStatus("1");
                        dataModel.setFileName(videoTitle);
                    });
                }
            }
        });
    }

    private static void fetchThumbnail(String videoId, Activity a) {
        OkHttpClient client = new OkHttpClient();

        String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
        Request request = new Request.Builder()
                .url(thumbnailUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                Log.e("Image", "Error");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap thumbnailBitmap = BitmapFactory.decodeStream(inputStream);
                    // Use the thumbnailBitmap for live display in your app
                    a.runOnUiThread(() -> dataModel.setThumbnail(thumbnailBitmap));
                }
            }
        });
    }

    private static void getVideoInfo(String videoUrl) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(videoUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    String views = extractViewsCount(html);
                    dataModel.setViews(views);
                }
            }
        });
    }

    private static class FetchLikesCountTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                String youtubeUrl = urls[0];
                Document doc = Jsoup.connect(youtubeUrl).get();
                return extractLikesCount(doc);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Likes", "doInBackground IO: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Likes", "doInBackground E: " + e.getMessage());
            }
            return "";
        }

        @Override
        protected void onPostExecute(String likesCount) {
            dataModel.setLikes(likesCount);
        }
    }
}