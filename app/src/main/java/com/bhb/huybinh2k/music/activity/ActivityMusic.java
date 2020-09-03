package com.bhb.huybinh2k.music.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bhb.huybinh2k.music.IOnClickSongListener;
import com.bhb.huybinh2k.music.LogSetting;
import com.bhb.huybinh2k.music.MediaPlaybackService;
import com.bhb.huybinh2k.music.PlaybackStatus;
import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.fragment.AllSongsFragment;
import com.bhb.huybinh2k.music.fragment.FavoriteSongsFragment;
import com.bhb.huybinh2k.music.fragment.MediaPlaybackFragment;
import com.bhb.huybinh2k.music.fragment.SearchFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class ActivityMusic extends AppCompatActivity implements IOnClickSongListener {
    public static final String GET_SONG_INDEX = "com.bhb.huybinh2k.GET_SONG_INDEX";
    public static final String PLAY_PAUSE = "com.bhb.huybinh2k.PLAY_PAUSE";
    public static final String BROADCAST_RECEIVER = "com.bhb.huybinh2k.BROADCAST_RECEIVER";
    public static final int DEFAULT_VALUE = -1;
    public static final int PLAYING = 0;
    public static final int PAUSE = 1;
    public static final String TAG = "log";
    private static final String SERVICE_BOUND = "com.bhb.huybinh2k.SERVICE_BOUND";
    private static final String IS_PLAYING = "com.bhb.huybinh2k.IS_PLAYING";
    private static final String RESUME_POSITION = "com.bhb.huybinh2k.RESUME_POSITION";
    private static final String FAVORITE = "com.bhb.huybinh2k.FAVORITE";
    public MediaPlaybackService mediaService;
    public int resumePosition;
    public boolean isFavoriteFragment;
    public boolean isPlaying;
    public boolean isBack;
    public boolean isServiceBound;
    private IUpdateMediaPlaybackFragment mIUpdateMediaPlaybackFragment;
    private IUpdateAllSongsFragment mIUpdateAllSongsFragment;
    private RelativeLayout mLayoutPlayBar;
    private ImageView mImagePause;
    private ImageView mImageSong;
    private TextView mTextViewSong;
    private TextView mTextViewSinger;
    public DrawerLayout drawerLayout;
    /**
     * Nhận BroadcastReceiver
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int songIndex = intent.getIntExtra(GET_SONG_INDEX, DEFAULT_VALUE);
            int x = intent.getIntExtra(PLAY_PAUSE, DEFAULT_VALUE);
            if (x != DEFAULT_VALUE) {
                if (x == PAUSE) {
                    isPlaying = false;
                } else {
                    isPlaying = true;
                }
                playPauseReceiver();
            }
            if (songIndex != DEFAULT_VALUE) {
                isPlaying = true;
                updateUIPlayBar(songIndex);
            }
        }
    };
    private StorageUtil mStorageUtil;
    private androidx.appcompat.widget.Toolbar mToolbar;
    private MediaPlaybackFragment mMediaPlaybackFragment;
    private int mOrientation;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) iBinder;
            mediaService = binder.getService();
            if (!isFavoriteFragment) {
                mIUpdateAllSongsFragment.update(mediaService.songIndexService);
            }
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mIUpdateMediaPlaybackFragment.update(mediaService.songIndexService);
            }
            if (!mediaService.isPlaying) {
                mImagePause.setImageResource(R.drawable.ic_play);
                isPlaying = false;
            } else {
                mImagePause.setImageResource(R.drawable.ic_pause);
                isPlaying = true;
            }
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LogSetting.IS_DEBUG) {
            Log.d(TAG, "onCreateActivity");
        }
        setContentView(R.layout.activity_music);
        mLayoutPlayBar = findViewById(R.id.layoutPlayBar);
        mImagePause = findViewById(R.id.playBar_Pause);
        mStorageUtil = new StorageUtil(this);
        mImageSong = findViewById(R.id.img_playbar);
        mTextViewSong = findViewById(R.id.tenbaihat_playbar);
        mTextViewSinger = findViewById(R.id.tencasi_playbar);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        isFavoriteFragment = mStorageUtil.loadIsFavorite();

        if (savedInstanceState != null) {
            isPlaying = savedInstanceState.getBoolean(IS_PLAYING);
            resumePosition = savedInstanceState.getInt(RESUME_POSITION);
            updateImagePlayPause();
            isServiceBound = savedInstanceState.getBoolean(SERVICE_BOUND);
            isFavoriteFragment = savedInstanceState.getBoolean(FAVORITE);
        }

        checkOrentation();
        addNavigation();

        if (isMyServiceRunning(MediaPlaybackService.class)) {
            Intent playerIntent = new Intent(this, MediaPlaybackService.class);
            bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            isServiceBound = true;
        }
    }

    /**
     * Kiểm tra Orentation và đổ fragment
     */
    private void checkOrentation() {
        AllSongsFragment mAllSongsFragment = new AllSongsFragment();
        mMediaPlaybackFragment = new MediaPlaybackFragment();
        mOrientation = getResources().getConfiguration().orientation;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {

            if (isFavoriteFragment) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_song,
                        new FavoriteSongsFragment()).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_song,
                        mAllSongsFragment).commit();
            }
            mImagePause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickPlayPause();
                }
            });
        } else {
            if (isFavoriteFragment) {
                FavoriteSongsFragment favoriteSongsFragment = new FavoriteSongsFragment();
                favoriteSongsFragment.setmIOnClickSongListener(ActivityMusic.this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_song, favoriteSongsFragment)
                        .replace(R.id.frame_song_play, mMediaPlaybackFragment)
                        .commit();
            } else {
                mAllSongsFragment.setmIOnClickSongListener(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_song, mAllSongsFragment)
                        .replace(R.id.frame_song_play, mMediaPlaybackFragment)
                        .commit();
            }

        }
    }

    /**
     * add Navigation
     */
    private void addNavigation() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        int x;
        if (isFavoriteFragment) {
            x = R.id.nav_favorite;
        } else {
            x = R.id.nav_all;
        }
        navigationView.setCheckedItem(x);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                selectItemNavigation(menuItem);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void selectItemNavigation(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_all:
                AllSongsFragment allSongsFragment = new AllSongsFragment();
                allSongsFragment.setmIOnClickSongListener(ActivityMusic.this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_song, allSongsFragment)
                        .commit();
                isFavoriteFragment = false;
                break;
            case R.id.nav_favorite:
                FavoriteSongsFragment favoriteSongsFragment = new FavoriteSongsFragment();
                favoriteSongsFragment.setmIOnClickSongListener(ActivityMusic.this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_song, favoriteSongsFragment)
                        .commit();
                isFavoriteFragment = true;

                break;
        }
        mStorageUtil.storeIsFavorite(isFavoriteFragment);
    }

    /**
     * Phát bài hát, start service và bind service
     */
    public void playAudio(int songIndex, List<Song> list) {
        mStorageUtil.storeListSongPlaying(list);
        mStorageUtil.storeSongIndex(songIndex);
        if (!isServiceBound) {
            Intent playerIntent = new Intent(this, MediaPlaybackService.class);
            startService(playerIntent);
            bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            mediaService.updateListSong(list);
            mediaService.playNewSong();
        }
    }

    /**
     * cập nhật image play/pause khi nhận đc receiver
     */
    private void playPauseReceiver() {
        if (isPlaying) {
            mImagePause.setImageResource(R.drawable.ic_play);
            isPlaying = false;
        } else {
            mImagePause.setImageResource(R.drawable.ic_pause);
            isPlaying = true;
        }
    }

    /**
     * cập nhật image play/pause
     */
    public void updateImagePlayPause() {
        if (!isPlaying) {
            mImagePause.setImageResource(R.drawable.ic_play);
        } else {
            mImagePause.setImageResource(R.drawable.ic_pause);
        }
    }

    /**
     * cập nhật lại giao diên playbar
     */
    public void updateUIPlayBar(int i) {
        List<Song> mList = new StorageUtil(this).loadListSongPlaying();
        Song s = mList.get(i);
        if (mLayoutPlayBar.getVisibility() == View.VISIBLE) {
            updateImagePlayPause();
            mImageSong.setImageURI(Uri.parse(s.getImg()));
            mTextViewSinger.setText(s.getArtist());
            mTextViewSong.setText(s.getSongName());
        }
    }

    /**
     * Sự kiện khi click nào play/pause
     */
    private void clickPlayPause() {
        if (isPlaying) {
            mediaService.pauseMedia();
            mImagePause.setImageResource(R.drawable.ic_play);
            mediaService.buildNotification(PlaybackStatus.PAUSE);
            resumePosition = mediaService.mediaPlayer.getCurrentPosition();
            isPlaying = false;
        } else {
            mediaService.resumeMedia();
            mImagePause.setImageResource(R.drawable.ic_pause);
            mediaService.buildNotification(PlaybackStatus.PLAYING);
            isPlaying = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (LogSetting.IS_DEBUG) {
            Log.d(TAG, "onStartActivity");
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_RECEIVER);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (LogSetting.IS_DEBUG) {
            Log.d(TAG, "onStopActivity");
        }
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (LogSetting.IS_DEBUG) {
            Log.d(TAG, "onDestroyActivity");
        }
        if (isFinishing()) {
            if (!isPlaying && isServiceBound) {
                unbindService(mServiceConnection);
                mediaService.stopSelf();
                mStorageUtil.storeSongIndex(DEFAULT_VALUE);
                mStorageUtil.storeIsFavorite(false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (LogSetting.IS_DEBUG) {
            Log.d(TAG, "BackPressed");
        }
        super.onBackPressed();
        if (!isBack) {
            if (!isPlaying && isServiceBound) {
                unbindService(mServiceConnection);
                mediaService.stopSelf();
                mStorageUtil.storeSongIndex(DEFAULT_VALUE);
                mStorageUtil.storeIsFavorite(false);
            }

        }
        isBack = false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SERVICE_BOUND, isServiceBound);
        outState.putBoolean(IS_PLAYING, isPlaying);
        outState.putBoolean(FAVORITE, isFavoriteFragment);
        if (isServiceBound) {
            outState.putInt(RESUME_POSITION, mediaService.mediaPlayer.getCurrentPosition());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.search) {
            SearchFragment searchFragment = new SearchFragment();
            searchFragment.setmIOnClickSongListener(this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_song, searchFragment)
                    .addToBackStack(null)
                    .commit();
        }
        return super.onOptionsItemSelected(item);
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

    /**
     * @param index
     * Cập nhật dữ liệu MediaPlayBackFragment khi màn hình xoay ngang
     */
    @Override
    public void update(int index) {
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            Bundle bundle = new Bundle();
            bundle.putInt(AllSongsFragment.SONG_INDEX, index);
            mMediaPlaybackFragment.update(bundle);
            mMediaPlaybackFragment.disableOrEnableClick(true);
            mMediaPlaybackFragment.changeSeekBar();
        }
    }

    public void setIUpdateMediaPlaybackFragment(IUpdateMediaPlaybackFragment mIUpdateMediaPlaybackFragment) {
        this.mIUpdateMediaPlaybackFragment = mIUpdateMediaPlaybackFragment;
    }

    public void setIUpdateAllSongsFragment(IUpdateAllSongsFragment mIUpdateAllSongsFragment) {
        this.mIUpdateAllSongsFragment = mIUpdateAllSongsFragment;
    }

    public interface IUpdateMediaPlaybackFragment {
        void update(int songIndex);
    }

    public interface IUpdateAllSongsFragment {
        void update(int songIndex);
    }

}