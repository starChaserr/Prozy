package com.downloader.proxy.views;

import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.downloader.proxy.R;
import com.downloader.proxy.liveModel.dataModel;
import com.downloader.proxy.utils.Extractor;
import com.downloader.proxy.utils.defaultDirectory;
import com.downloader.proxy.utils.networkCheck;
import com.downloader.proxy.utils.snack;

public class MainActivity extends AppCompatActivity {

    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    private Button download;
    private TextView dirName;
    private EditText link;
    private LinearLayout grabbingInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        getPermissions();
        init();
        onClicks();
    }

    private void findView() {
        download = findViewById(R.id.downloadBtn);
        grabbingInfo = findViewById(R.id.grabbing);
        link = findViewById(R.id.link);
        dirName = findViewById(R.id.destinationName);
    }

    private void init() {
        revertBtnClick();
        dirName.setText(defaultDirectory.get());
        modelListeners();
    }

    private void revertBtnClick() {
        download.setVisibility(View.VISIBLE);
        grabbingInfo.setVisibility(View.GONE);
    }

    private void showGrabber() {
        download.setVisibility(View.GONE);
        grabbingInfo.setVisibility(View.VISIBLE);
    }

    private void onClicks() {
        download.setOnClickListener(v -> {
            String l = link.getText().toString();
            if (networkCheck.isConnected(this)) {
                if (!l.trim().isEmpty()) {
                    showGrabber();
                    modelListeners();
                    link.setText("");
                    Extractor.mp3Link(l, this);
                } else {
                    snack.show("Need a valid link.", link);
                    revertBtnClick();
                }
            } else {
                snack.show("Device may not be connected to internet.", link);
                revertBtnClick();
            }
        });
    }

    private void modelListeners() {
        dataModel.getStatus().observe(this, s -> {
            if (s != null) {
                if (s.equals("0") || s.equals("2")) {
                    showGrabber();
                } else {
                    revertBtnClick();
                    dataModel.getStatus().removeObservers(this);
                    startActivity(new Intent(MainActivity.this, Activity2.class));
                }
            }
        });
        dataModel.getDownloadStatus().observe(this, d -> {
            if (d == 1) {
                snack.show("Download Complete", link);
                dataModel.setDownloadStatus(0);
            }
        });
    }

    private void getPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the code to convert MP4 to MP3
                snack.show("Storage permission granted.", link);
            } else {
                // Permission denied, handle accordingly (e.g., show an error message)
                try {
                    sleep(2000);
                    getPermissions();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}