package com.bhb.huybinh2k.music.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bhb.huybinh2k.music.R;

public class SplashScreen extends AppCompatActivity {

    private static final int REQUEST_CODE = 113;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

    }
    @Override
    protected void onResume() {
        super.onResume();
        checkPermision();
    }

    Runnable wait1s = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                Intent intent = new Intent(SplashScreen.this, ActivityMusic.class);
                startActivity(intent);
                finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void checkPermision() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            } else {
                Thread thread = new Thread(wait1s);
                thread.start();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
