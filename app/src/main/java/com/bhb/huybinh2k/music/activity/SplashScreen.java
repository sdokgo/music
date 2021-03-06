package com.bhb.huybinh2k.music.activity;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import com.bhb.huybinh2k.music.MediaPlaybackService;
import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.database.FavoriteSongsProvider;

public class SplashScreen extends AppCompatActivity {

    private static final int REQUEST_CODE = 113;
    private static final String ALBUM_ART = "content://media/external/audio/albumart";
    boolean checkPermision;
    Runnable mWait1s = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(SplashScreen.this, ActivityMusic.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMyServiceRunning(MediaPlaybackService.class)) {
            Intent intent = new Intent(SplashScreen.this, ActivityMusic.class);
            startActivity(intent);
            finish();
        } else {
            checkPermision();
            if (checkPermision) {
                getSongList();
                Thread thread = new Thread(mWait1s);
                thread.start();
            }
        }
    }

    private void checkPermision() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            } else {
                checkPermision = true;
            }
        }
    }


    /**
     * Đọc dữ liệu trong máy và add vào list
     */
    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null) {
            if (musicCursor.moveToFirst()) {
                int id = 1;
                do {
                    String songTitle = musicCursor.getString(
                            musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String songArtist = musicCursor.getString(
                            musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String songPath = musicCursor.getString(
                            musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    long albumId = musicCursor.getLong(musicCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                    int idProvider = musicCursor.getInt(
                            musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                    Uri sArtworkUri = Uri
                            .parse(ALBUM_ART);
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                    String albumArt = String.valueOf(albumArtUri);

                    long milliseconds = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    new FavoriteSongsProvider(this).insert(
                            new Song(id, idProvider, songTitle, songPath, songArtist, albumArt, milliseconds)
                    );
                    id++;

                }
                while (musicCursor.moveToNext());
            }
            musicCursor.close();
        }

    }

    /**
     * check xem service có đang chạy hay ko
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
