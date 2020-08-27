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

import com.bhb.huybinh2k.music.IOnClickSongListener;
import com.bhb.huybinh2k.music.MediaPlaybackService;
import com.bhb.huybinh2k.music.PlaybackStatus;
import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.database.FavoriteSongsProvider;
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
    private static final String SERVICE_BOUND = "com.bhb.huybinh2k.SERVICE_BOUND";
    private static final String IS_PLAYING = "com.bhb.huybinh2k.IS_PLAYING";
    private static final String RESUME_POSITION = "com.bhb.huybinh2k.RESUME_POSITION";
    private static final String FAVORITE = "com.bhb.huybinh2k.FAVORITE";
    private MediaPlaybackService mMediaService;
    private RelativeLayout mLayoutPlayBar;
    private ImageView mImagePause, mImageSong;
    private TextView mTextViewSong, mTextViewSinger;
    private int mResumePosition = -1;
    private int mFavorite = 0;
    private int mIsPlaying;
    private boolean mIsBack = false;
    private boolean mServiceBound = false;
    private int mOrientation;
    private StorageUtil storageUtil;

    private MediaPlaybackFragment mMediaPlaybackFragment;
    private AllSongsFragment mAllSongsFragment;

    public int getmFavorite() {
        return mFavorite;
    }

    public MediaPlaybackService getmMediaService() {
        return mMediaService;
    }

    public int getmIsPlaying() {
        return mIsPlaying;
    }

    public void setmIsPlaying(int mIsPlaying) {
        this.mIsPlaying = mIsPlaying;
    }

    public void setmIsBack(boolean mIsBack) {
        this.mIsBack = mIsBack;
    }

    public int getmResumePosition() {
        return mResumePosition;
    }

    public boolean isServiceBound() {
        return mServiceBound;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("log", "onCreateActivity");
        setContentView(R.layout.activity_music);
        mLayoutPlayBar = findViewById(R.id.layoutPlayBar);
        mImagePause = findViewById(R.id.playBar_Pause);
        storageUtil = new StorageUtil(this);
        mImageSong = findViewById(R.id.img_playbar);
        mTextViewSong = findViewById(R.id.tenbaihat_playbar);
        mTextViewSinger = findViewById(R.id.tencasi_playbar);
        androidx.appcompat.widget.Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mAllSongsFragment = new AllSongsFragment();
        mMediaPlaybackFragment = new MediaPlaybackFragment();
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_all);


        if (savedInstanceState != null) {
            mIsPlaying = savedInstanceState.getInt(IS_PLAYING);
            mResumePosition = savedInstanceState.getInt(RESUME_POSITION);
            updateImagePlayPause();
            mServiceBound = savedInstanceState.getBoolean(SERVICE_BOUND);
            mFavorite = savedInstanceState.getInt(FAVORITE);
        }
        mFavorite = storageUtil.loadIsFavorite();

        mOrientation = getResources().getConfiguration().orientation;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {

            if (mFavorite == 1) {
                getSupportFragmentManager().beginTransaction().replace(R.id.framesong,
                        new FavoriteSongsFragment()).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.framesong,
                        mAllSongsFragment).commit();
            }
            mImagePause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickPlayPause();
                }
            });
        } else {
            if (mFavorite == 1) {
                FavoriteSongsFragment favoriteSongsFragment = new FavoriteSongsFragment();
                favoriteSongsFragment.setmIOnClickSongListener(ActivityMusic.this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.framesong, favoriteSongsFragment)
                        .replace(R.id.framesongplay, mMediaPlaybackFragment)
                        .commit();
            } else {
                mAllSongsFragment.setmIOnClickSongListener(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.framesong, mAllSongsFragment)
                        .replace(R.id.framesongplay, mMediaPlaybackFragment)
                        .commit();
            }

        }
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        int x;
        if (mFavorite == 1) {
            x = R.id.nav_favorite;
        } else {
            x = R.id.nav_all;
        }
        navigationView.setCheckedItem(x);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_all:
                        AllSongsFragment allSongsFragment = new AllSongsFragment();
                        allSongsFragment.setmIOnClickSongListener(ActivityMusic.this);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.framesong, allSongsFragment)
                                .commit();
                        mFavorite = 0;
                        storageUtil.storeIsFavorite(mFavorite);
                        break;
                    case R.id.nav_favorite:
                        FavoriteSongsFragment favoriteSongsFragment = new FavoriteSongsFragment();
                        favoriteSongsFragment.setmIOnClickSongListener(ActivityMusic.this);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.framesong, favoriteSongsFragment)
                                .commit();
                        mFavorite = 1;
                        storageUtil.storeIsFavorite(mFavorite);
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        if (isMyServiceRunning(MediaPlaybackService.class)) {
            Intent playerIntent = new Intent(this, MediaPlaybackService.class);
            bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            mServiceBound = true;
        }
    }




    public ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) iBinder;
            mMediaService = binder.getService();
            if (mFavorite == 0) {
                mIUpdateAllSongsFragment.update(mMediaService.getmSongIndexService());
            } else if (mFavorite == 1) {

            }
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mIUpdateMediaPlaybackFragment.update(mMediaService.getmSongIndexService());
            }
            if (mMediaService.getIsPlaying() == -1) {
                mImagePause.setImageResource(R.drawable.ic_play);
                mIsPlaying = 1;
            } else {
                mImagePause.setImageResource(R.drawable.ic_pause);
                mIsPlaying = 0;
            }
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceBound = false;
        }
    };


    /**
     * Phát bài hát, start service và bind service
     */
    public void playAudio(int songIndex, List<Song> list) {
        StorageUtil storage = new StorageUtil(this);
        storage.storeSongListPlaying(list);
        storage.storeSongIndex(songIndex);
        if (!mServiceBound) {

            Intent playerIntent = new Intent(this, MediaPlaybackService.class);
            startService(playerIntent);
            bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            mMediaService.updateListSong(list);
            mMediaService.playNewSong();
        }
    }

    /**
     * cập nhật image play/pause khi nhận đc receiver
     */
    private void playPauseReceiver() {
        if (mIsPlaying == 0) {
            mImagePause.setImageResource(R.drawable.ic_play);
            mIsPlaying = 1;
        } else if (mIsPlaying == 1) {
            mImagePause.setImageResource(R.drawable.ic_pause);
            mIsPlaying = 0;
        }
    }

    /**
     * cập nhật image play/pause
     */
    public void updateImagePlayPause() {
        if (mIsPlaying == 1) {
            mImagePause.setImageResource(R.drawable.ic_play);
        } else {
            mImagePause.setImageResource(R.drawable.ic_pause);
        }
    }

    /**
     * cập nhật lại giao diên playbar
     */
    public void updateUIPlayBar(int i) {
        List<Song> mList = new StorageUtil(this).loadSongListPlaying();
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
        if (mIsPlaying == 0) {
            mMediaService.pauseMedia();
            mImagePause.setImageResource(R.drawable.ic_play);
            mMediaService.buildNotification(PlaybackStatus.PAUSE);
            mResumePosition = mMediaService.getmMediaPlayer().getCurrentPosition();
            mIsPlaying = 1;
        } else if (mIsPlaying == 1) {
            mMediaService.resumeMedia();
            mImagePause.setImageResource(R.drawable.ic_pause);
            mMediaService.buildNotification(PlaybackStatus.PLAYING);
            mIsPlaying = 0;
        }
    }


    /**
     * Nhận BroadcastReceiver
     */
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int i = intent.getIntExtra(GET_SONG_INDEX, -1);
            int x = intent.getIntExtra(PLAY_PAUSE, -1);
            if (x != -1) {
                mIsPlaying = x;
                playPauseReceiver();
            }
            if (i != -1) {
                mIsPlaying = 0;
                updateUIPlayBar(i);
            }
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        Log.d("log", "onStartActivity");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_RECEIVER);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("log", "onStopActivity");
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("log", "onDestroyActivity");
        if (isFinishing()) {
            if (mIsPlaying == 1) {
                unbindService(mServiceConnection);
                mMediaService.stopSelf();
                storageUtil.storeSongIndex(-1);
                storageUtil.storeIsFavorite(0);
            }
        }


    }

    @Override
    public void onBackPressed() {
        Log.d("log", "BackPressed");
        super.onBackPressed();
        if (!mIsBack) {
            if (mServiceBound) {
                if (mIsPlaying == 1) {
                    unbindService(mServiceConnection);
                    mMediaService.stopSelf();
                    storageUtil.storeSongIndex(-1);
                    storageUtil.storeIsFavorite(0);
                }
            }
        }
        mIsBack = false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SERVICE_BOUND, mServiceBound);
        outState.putInt(IS_PLAYING, mIsPlaying);
        outState.putInt(FAVORITE, mFavorite);
        if (mServiceBound) {
            outState.putInt(RESUME_POSITION, mMediaService.getmMediaPlayer().getCurrentPosition());
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
                    .replace(R.id.framesong, searchFragment)
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

    @Override
    public void update(int index) {
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            Bundle bundle = new Bundle();
            bundle.putInt(AllSongsFragment.SONG_INDEX, index);
            mMediaPlaybackFragment.update(bundle);
        }
    }

    public interface IUpdateMediaPlaybackFragment {
        void update(int songindex);
    }

    public void setmIUpdateMediaPlaybackFragment(IUpdateMediaPlaybackFragment mIUpdateMediaPlaybackFragment) {
        this.mIUpdateMediaPlaybackFragment = mIUpdateMediaPlaybackFragment;
    }

    IUpdateMediaPlaybackFragment mIUpdateMediaPlaybackFragment;

    public interface IUpdateAllSongsFragment {
        void update(int songIndex);
    }

    IUpdateAllSongsFragment mIUpdateAllSongsFragment;

    public void setmIUpdateAllSongsFragment(IUpdateAllSongsFragment mIUpdateAllSongsFragment) {
        this.mIUpdateAllSongsFragment = mIUpdateAllSongsFragment;
    }

}