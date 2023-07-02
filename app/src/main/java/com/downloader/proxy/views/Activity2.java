package com.downloader.proxy.views;

import static com.downloader.proxy.liveModel.dataModel.mp3;
import static com.downloader.proxy.liveModel.dataModel.mp4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.downloader.proxy.R;
import com.downloader.proxy.liveModel.dataModel;
import com.downloader.proxy.utils.Extractor;
import com.downloader.proxy.utils.Formatter;
import com.downloader.proxy.utils.defaultDirectory;
import com.downloader.proxy.utils.snack;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class Activity2 extends AppCompatActivity {

    private TextView title, likes, views, percent, radioTitle;
    private RadioGroup typeGrp;
    private Button download;
    private LinearLayout downloadLayout;
    private LinearProgressIndicator progressIndicator;
    private ImageView thumbnail;


    private String fileName = "", downloadURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        findView();
        init();
    }

    private void findView() {
        title = findViewById(R.id.title);
        likes = findViewById(R.id.likes);
        views = findViewById(R.id.views);
        thumbnail = findViewById(R.id.thumbnail);
        download = findViewById(R.id.downloadBtn);
        downloadLayout = findViewById(R.id.dLayout);
        progressIndicator = findViewById(R.id.dProgress);
        percent = findViewById(R.id.percent);
        typeGrp = findViewById(R.id.outType);
        radioTitle = findViewById(R.id.radioTitle);
        initialUI();
    }

    private void downloadingUI() {
        downloadLayout.setVisibility(View.VISIBLE);
        download.setVisibility(View.GONE);
        typeGrp.setVisibility(View.GONE);
        radioTitle.setVisibility(View.GONE);
    }

    private void initialUI() {
        downloadLayout.setVisibility(View.GONE);
        download.setVisibility(View.VISIBLE);
        typeGrp.setVisibility(View.VISIBLE);
        radioTitle.setVisibility(View.VISIBLE);
    }

    private void init() {
        AtomicReference<Float> totalSize = new AtomicReference<>(0F);
        uiInits();

        dataModel.getDownloadUrl().observe(this, url -> {
            if (url != null) {
                downloadURL = url;
            }
        });

        download.setOnClickListener(v -> {
            if (typeGrp.getCheckedRadioButtonId() == R.id.mp4) {
                downloadingUI();
                Extractor.downloadFromUrl(downloadURL, fileName, fileName, this, defaultDirectory.get(), mp4);
            } else if (typeGrp.getCheckedRadioButtonId() == R.id.mp3) {
                downloadingUI();
                Extractor.downloadFromUrl(downloadURL, fileName, fileName, this, defaultDirectory.get(), mp3);

            } else {
                snack.show("Select a file type", thumbnail);
            }
        });

        dataModel.getProgress().observe(this, p -> progressIndicator.setProgress(p));
        dataModel.getStatus().observe(this, s -> {
            if (s.equals("4")) {
                snack.show("Download interrupted..", thumbnail);
                initialUI();
            } else if (s.equals("5")) {
                dataModel.setDownloadStatus(1);
                finish();
            }
        });
        dataModel.getTotalDownloadSize().observe(this, totalSize::set);
        dataModel.getTotalDownloadedSize().observe(this, d -> {
            String s = Formatter.format(d, 2) + " MB /" + Formatter.format(totalSize.get(), 2) + " MB";
            percent.setText(s);
        });
    }

    private void uiInits() {
        dataModel.getThumbnail().observe(this, i -> {
            if (i != null) {
                thumbnail.setImageBitmap(i);
            }
        });
        dataModel.getFileName().observe(this, s -> {
            if (s != null) {
                title.setText(s);
                fileName = s;
            }
        });
        dataModel.getViews().observe(this, s -> {
            if (s != null) {
                views.setText(s + " Views");
            }
        });
        dataModel.getLikes().observe(this, s -> {
            if (s != null) {
                likes.setText(s + " Likes");
            }
        });
    }
}