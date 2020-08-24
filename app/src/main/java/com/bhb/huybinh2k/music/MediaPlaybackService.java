package com.bhb.huybinh2k.music;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import android.media.session.MediaSessionManager;

import com.bhb.huybinh2k.music.activity.ActivityMusic;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class MediaPlaybackService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener {

    public MediaPlayer getmMediaPlayer() {
        return mMediaPlayer;
    }

    private MediaPlayer mMediaPlayer;
    private static final String ACTION_PLAY = "com.bhb.huybinh2k.ACTION_PLAY";
    private static final String ACTION_PAUSE = "com.bhb.huybinh2k.ACTION_PAUSE";
    private static final String ACTION_PREVIOUS = "com.bhb.huybinh2k.ACTION_PREVIOUS";
    private static final String ACTION_NEXT = "com.bhb.huybinh2k.ACTION_NEXT";
    private static final String CHANNEL_ID = "com.bhb.huybinh2k.CHANNEL_ID";
    private static final int NOTIFICATION_ID = 101;
    private List<Song> mSongListService;

    public int getIsPlaying() {
        return isPlaying;
    }

    private int isPlaying = -1;

    public int getmSongIndexService() {
        return mSongIndexService;
    }

    private int mSongIndexService;
    private Song mActiveSongService;

    private MediaSessionManager mMediaSessionManager;
    private MediaSessionCompat mMediaSession;
    private MediaControllerCompat.TransportControls mTransportControls;
    private int mShuffle, mRepeat;
    private int mResumePosition;
    private StorageUtil mStorageUtil;


    /**
     * Cập nhật trạng thái của shuffle và repeat
     */
    public void updateShuffleRepeat(int a, int b) {
        mShuffle = a;
        mRepeat = b;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            createNotificationChannel();
            mStorageUtil = new StorageUtil(getApplicationContext());
            mSongListService = mStorageUtil.loadSongListPlaying();
            mSongIndexService = mStorageUtil.loadSongIndex();
            mShuffle = mStorageUtil.loadShuffle();
            mRepeat = mStorageUtil.loadRepeat();
            if (mSongIndexService != -1 && mSongIndexService < mSongListService.size()) {
                mActiveSongService = mSongListService.get(mSongIndexService);
            } else {
                stopSelf();
                isPlaying = -1;
            }

        } catch (NullPointerException e) {
            stopSelf();
            isPlaying = -1;
        }
        if (mMediaSessionManager == null) {
            initMediaSession();
            initMediaPlayer();
            buildNotification(PlaybackStatus.PLAYING);
            isPlaying = 1;

        }

        handleIncomingAction(intent);

        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * Phát bài hát mới
     */
    public void playNewSong() {
        mSongIndexService = new StorageUtil(getApplicationContext()).loadSongIndex();
        if (mSongIndexService != -1 && mSongIndexService < mSongListService.size()) {
            mActiveSongService = mSongListService.get(mSongIndexService);
        } else {
            stopSelf();
        }
        stopMedia();
        mMediaPlayer.reset();
        initMediaPlayer();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        isPlaying = 1;
    }


//    /**
//     * Nhận BroadcastReceiver phát bài hát mới
//     */
//    private BroadcastReceiver playNewSong = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            mSongIndexService = new StorageUtil(getApplicationContext()).loadSongIndex();
//            if (mSongIndexService != -1 && mSongIndexService < mSongListService.size()) {
//                mActiveSongService = mSongListService.get(mSongIndexService);
//            } else {
//                stopSelf();
//            }
//            stopMedia();
//            mMediaPlayer.reset();
//            initMediaPlayer();
//            updateMetaData();
//            sendBroadcast(1);
//            buildNotification(PlaybackStatus.PLAYING);
//        }
//
//    };

    /**
     * Khởi tạo MediaSession
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSession() {
        if (mMediaSessionManager != null) return;
        mMediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mMediaSession = new MediaSessionCompat(getApplicationContext(), "SongPlayer");
        mTransportControls = mMediaSession.getController().getTransportControls();
        mMediaSession.setActive(true);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();

        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                sendBroadcast(2);
                buildNotification(PlaybackStatus.PLAYING);
                isPlaying = 1;

            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                sendBroadcast(3);
                buildNotification(PlaybackStatus.PAUSE);
                stopForeground(false);
                isPlaying = -1;
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                sendBroadcast(1);
                buildNotification(PlaybackStatus.PLAYING);
                isPlaying = 1;
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
                sendBroadcast(1);
                buildNotification(PlaybackStatus.PLAYING);
                isPlaying = 1;
            }

            @Override
            public void onStop() {
                super.onStop();
                stopSelf();
                removeNotification();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }
        });
    }

    /**
     * Gửi Broadcast khi click vào action trong notification
     */
    private void sendBroadcast(int x) {
        Intent intent = new Intent();
        intent.setAction(ActivityMusic.BROADCAST_RECEIVER);
        switch (x){
            case 1:
                intent.putExtra(ActivityMusic.GET_SONG_INDEX, mSongIndexService);
                sendBroadcast(intent);
                break;
            case 2:
                intent.putExtra(ActivityMusic.PLAY_PAUSE, 1);
                sendBroadcast(intent);
                break;
            case 3:
                intent.putExtra(ActivityMusic.PLAY_PAUSE, 0);
                sendBroadcast(intent);
                break;
        }
    }


    /**
     * Khởi tạo MediaPlayer
     */
    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mActiveSongService.getSongPath());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mMediaPlayer.prepareAsync();
        isPlaying = 1;
    }

    /**
     * Phát bài hát
     */
    private void playMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            isPlaying = 1;
        }
    }

    /**
     * Stop bài hát
     */
    private void stopMedia() {
        if (mMediaPlayer == null) return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            isPlaying = -1;
        }
    }

    /**
     * Tạm dừng bài hát
     */
    public void pauseMedia() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mResumePosition = mMediaPlayer.getCurrentPosition();
            isPlaying = -1;
        }
    }

    /**
     * Tiếp tục phát bài hát
     */
    public void resumeMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mResumePosition);
            mMediaPlayer.start();
            isPlaying = 1;
        }
    }

    /**
     * Sau khi phát xong bài hát
     */
    private void completeSong() {
        //Nếu chọn repeat chỉ 1 bài hát thì chạy lại bài hát vừa kết thúc
        if (mRepeat == 2) {
            stopMedia();
            mMediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();

            buildNotification(PlaybackStatus.PLAYING);

            sendBroadcast(1);
            //Nếu chọn shuffle và repeat khác chỉ 1 bài hát thì phát 1 bài ngẫu nhiên
        } else if (mShuffle == 1) {
            Random random = new Random();
            int r = random.nextInt(mSongListService.size());
            while (r == mSongIndexService) {
                r = random.nextInt(mSongListService.size());
            }
            stopMedia();
            mSongIndexService = r;
            mActiveSongService = mSongListService.get(mSongIndexService);
            new StorageUtil(getApplicationContext()).storeSongIndex(mSongIndexService);
            mMediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();

            buildNotification(PlaybackStatus.PLAYING);
            sendBroadcast(1);

            //Nếu ko chọn cả repeat và shuffle thì dừng
        } else if ((mRepeat == 0) && (mShuffle == 0)) {
            sendBroadcast(3);
            stopMedia();
            buildNotification(PlaybackStatus.PAUSE);
            //Nếu chọn phát lại cả danh sách bài hát và ko chọn shuffle
        } else if ((mRepeat == 1) && (mShuffle == 0)) {
            skipToNext();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
            sendBroadcast(1);
        }
    }

    /**
     * phát bài đang phát đến vị trí được chọn
     */
    public void seekTo(int x) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mMediaPlayer.seekTo(x);
            mMediaPlayer.start();
        } else {
            mResumePosition = x;
        }
    }


    /**
     * phát bài hát tiếp theo
     */
    public void skipToNext() {
        if (mSongIndexService == mSongListService.size() - 1) {
            mSongIndexService = 0;
            mActiveSongService = mSongListService.get(mSongIndexService);
        } else {
            mActiveSongService = mSongListService.get(++mSongIndexService);
        }
        new StorageUtil(getApplicationContext()).storeSongIndex(mSongIndexService);
        stopMedia();
        mMediaPlayer.reset();
        initMediaPlayer();
    }


    /**
     * phát bài hát trước đó
     */
    public void skipToPrevious() {
        if (mSongIndexService == 0) {
            mSongIndexService = mSongListService.size() - 1;
            mActiveSongService = mSongListService.get(mSongIndexService);
        } else {
            mActiveSongService = mSongListService.get(--mSongIndexService);
        }
        new StorageUtil(getApplicationContext()).storeSongIndex(mSongIndexService);
        stopMedia();
        mMediaPlayer.reset();
        initMediaPlayer();
    }


    /**
     * Cập nhật lại dữ liệu cho MediaSession
     */
    private void updateMetaData() {
        mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mActiveSongService.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mActiveSongService.getSongName())
                .build());
    }


    /**
     * Tạo Notification
     */
    public void buildNotification(PlaybackStatus playbackStatus) {
        int notiAction = 0;
        PendingIntent play_pauseAction = null;
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notiAction = R.drawable.ic_pause_circle;
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSE) {
            notiAction = R.drawable.ic_play_circle;
            play_pauseAction = playbackAction(0);
        }
        Bitmap largeIcon = null;
        try {
            largeIcon = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                    Uri.parse(mActiveSongService.getImg()));
        } catch (IOException e) {
            largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.music);
        }

        Intent resultIntent = new Intent(this, ActivityMusic.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setShowWhen(false)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken()).setShowActionsInCompactView(0, 1, 2))
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_baseline_volume_up_24)
                .setContentText(mActiveSongService.getArtist())
                .setContentTitle(mActiveSongService.getSongName())
                .setContentIntent(resultPendingIntent)
                .addAction(R.drawable.ic_previous, "pre", playbackAction(3))
                .addAction(notiAction, "pause", play_pauseAction)
                .addAction(R.drawable.ic_next, "next", playbackAction(2));
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, notificationBuilder.build());

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }


    /**
     * loại bỏ notification
     */
    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }


    public void stopForegroundService() {
            stopForeground(false);
            stopSelf();
    }


    /**
     * tạo channel cho notification
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Music Notification",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    /**
     * PendingIntent cho các sự kiện click action trong notification
     */
    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlaybackService.class);
        switch (actionNumber) {
            case 0:
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;

        }
        return null;
    }

    /**
     * Xử lí các sự kiện  click action trong notification
     */
    private void handleIncomingAction(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            mTransportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            mTransportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            mTransportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mTransportControls.skipToPrevious();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
//        IntentFilter intentFilter = new IntentFilter(ActivityMusic.BROADCAST_PLAY_NEW_AUDIO);
//        registerReceiver(playNewSong, intentFilter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopMedia();
            mMediaPlayer.release();
        }
//        unregisterReceiver(playNewSong);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        completeSong();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        playMedia();
    }

    private final IBinder iBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public class LocalBinder extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }
}
