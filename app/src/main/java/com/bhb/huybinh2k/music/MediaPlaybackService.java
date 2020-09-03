package com.bhb.huybinh2k.music;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import com.bhb.huybinh2k.music.fragment.MediaPlaybackFragment;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class MediaPlaybackService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener {

    private static final String ACTION_PLAY = "com.bhb.huybinh2k.ACTION_PLAY";
    private static final String ACTION_PAUSE = "com.bhb.huybinh2k.ACTION_PAUSE";
    private static final String ACTION_PREVIOUS = "com.bhb.huybinh2k.ACTION_PREVIOUS";
    private static final String ACTION_NEXT = "com.bhb.huybinh2k.ACTION_NEXT";
    private static final int PLAY = 0;
    private static final int PAUSE = 1;
    private static final int PREVIOUS = 3;
    private static final int NEXT = 2;
    private static final String CHANNEL_ID = "com.bhb.huybinh2k.CHANNEL_ID";
    private static final int NOTIFICATION_ID = 101;
    private static final int SONG_CHANGE = 1;
    private static final int PLAY_SONG = 2;
    private static final int PAUSE_SONG = 3;
    private static final int REQUEST_CODE = 0;
    private static final int FLAGS = 0;
    private final IBinder mIBinder = new LocalBinder();
    public MediaPlayer mediaPlayer;
    public boolean isPlaying;
    public int songIndexService;
    private List<Song> mSongListService;
    private Song mActiveSongService;
    private MediaSessionManager mMediaSessionManager;
    private MediaSessionCompat mMediaSession;
    private MediaControllerCompat.TransportControls mTransportControls;
    private int mShuffle;
    private int mRepeat;
    private int mResumePosition;

    /**
     * Cập nhật trạng thái của shuffle và repeat
     */
    public void updateShuffleRepeat(int a, int b) {
        mShuffle = a;
        mRepeat = b;
    }

    public void updateListSong(List<Song> list) {
        mSongListService = list;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        StorageUtil mStorageUtil = new StorageUtil(getApplicationContext());
        mSongListService = mStorageUtil.loadListSongPlaying();
        songIndexService = mStorageUtil.loadSongIndex();
        mShuffle = mStorageUtil.loadShuffle();
        mRepeat = mStorageUtil.loadRepeat();
        if (songIndexService != ActivityMusic.DEFAULT_VALUE && songIndexService < mSongListService.size()) {
            mActiveSongService = mSongListService.get(songIndexService);
        } else {
            stopSelf();
            isPlaying = false;
        }
        if (mMediaSessionManager == null) {
            initMediaSession();
            initMediaPlayer();
            buildNotification(PlaybackStatus.PLAYING);
            isPlaying = true;

        }

        handleIncomingAction(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Phát bài hát mới
     */
    public void playNewSong() {
        songIndexService = new StorageUtil(getApplicationContext()).loadSongIndex();
        if (songIndexService != ActivityMusic.DEFAULT_VALUE && songIndexService < mSongListService.size()) {
            mActiveSongService = mSongListService.get(songIndexService);
        } else {
            stopSelf();
            return;
        }
        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        isPlaying = true;
    }

    /**
     * Khởi tạo MediaSession
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSession() {
        if (mMediaSessionManager != null) return;
        mMediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mMediaSession = new MediaSessionCompat(getApplicationContext(), getString(R.string.MediaSession));
        mTransportControls = mMediaSession.getController().getTransportControls();
        mMediaSession.setActive(true);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                sendBroadcast(PLAY_SONG);
                buildNotification(PlaybackStatus.PLAYING);
                isPlaying = true;

            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                sendBroadcast(PAUSE_SONG);
                buildNotification(PlaybackStatus.PAUSE);
                stopForeground(false);
                isPlaying = false;
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                sendBroadcast(SONG_CHANGE);
                buildNotification(PlaybackStatus.PLAYING);
                isPlaying = true;
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
                sendBroadcast(SONG_CHANGE);
                buildNotification(PlaybackStatus.PLAYING);
                isPlaying = true;
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
        switch (x) {
            case SONG_CHANGE:
                intent.putExtra(ActivityMusic.GET_SONG_INDEX, songIndexService);
                sendBroadcast(intent);
                break;
            case PLAY_SONG:
                intent.putExtra(ActivityMusic.PLAY_PAUSE, ActivityMusic.PAUSE);
                sendBroadcast(intent);
                break;
            case PAUSE_SONG:
                intent.putExtra(ActivityMusic.PLAY_PAUSE, ActivityMusic.PLAYING);
                sendBroadcast(intent);
                break;
        }
    }

    /**
     * Khởi tạo MediaPlayer
     */
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(mActiveSongService.getSongPath());
            // setDataSource bat buoc co try catch IOException
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
            return;
        }
        mediaPlayer.prepareAsync();
        isPlaying = true;
    }

    /**
     * Phát bài hát
     */
    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    /**
     * Stop bài hát
     */
    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            isPlaying = false;
        }
    }

    /**
     * Tạm dừng bài hát
     */
    public void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mResumePosition = mediaPlayer.getCurrentPosition();
            isPlaying = false;
        }
    }

    /**
     * Tiếp tục phát bài hát
     */
    public void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(mResumePosition);
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    /**
     * Sau khi phát xong bài hát
     */
    private void completeSong() {
        //Nếu chọn repeat chỉ 1 bài hát thì chạy lại bài hát vừa kết thúc
        if (mRepeat == MediaPlaybackFragment.REPEAT_ONE) {
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();

            buildNotification(PlaybackStatus.PLAYING);

            sendBroadcast(SONG_CHANGE);
            //Nếu chọn shuffle và repeat khác chỉ 1 bài hát thì phát 1 bài ngẫu nhiên
        } else if (mShuffle == MediaPlaybackFragment.SHUFFLE) {
            Random random = new Random();
            int r = random.nextInt(mSongListService.size());
            while (r == songIndexService) {
                r = random.nextInt(mSongListService.size());
            }
            stopMedia();
            songIndexService = r;
            mActiveSongService = mSongListService.get(songIndexService);
            new StorageUtil(getApplicationContext()).storeSongIndex(songIndexService);
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();

            buildNotification(PlaybackStatus.PLAYING);
            sendBroadcast(SONG_CHANGE);

            //Nếu ko chọn cả repeat và shuffle thì dừng
        } else if ((mRepeat == MediaPlaybackFragment.NO_REPEAT) && (mShuffle == MediaPlaybackFragment.NO_SHUFFLE)) {
            sendBroadcast(PAUSE_SONG);
            stopMedia();
            buildNotification(PlaybackStatus.PAUSE);
            //Nếu chọn phát lại cả danh sách bài hát và ko chọn shuffle
        } else if ((mRepeat == MediaPlaybackFragment.REPEAT_ALL) && (mShuffle == MediaPlaybackFragment.NO_SHUFFLE)) {
            skipToNext();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
            sendBroadcast(PLAY_SONG);
        }
    }

    /**
     * phát bài đang phát đến vị trí được chọn
     */
    public void seekTo(int position) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(position);
            mediaPlayer.start();
        } else {
            mResumePosition = position;
        }
    }

    /**
     * phát bài hát tiếp theo
     */
    public void skipToNext() {
        if (songIndexService == mSongListService.size() - 1) {
            songIndexService = 0;
            mActiveSongService = mSongListService.get(songIndexService);
        } else {
            mActiveSongService = mSongListService.get(++songIndexService);
        }
        new StorageUtil(getApplicationContext()).storeSongIndex(songIndexService);
        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
    }

    /**
     * phát bài hát trước đó
     */
    public void skipToPrevious() {
        if (songIndexService == 0) {
            songIndexService = mSongListService.size() - 1;
            mActiveSongService = mSongListService.get(songIndexService);
        } else {
            mActiveSongService = mSongListService.get(--songIndexService);
        }
        new StorageUtil(getApplicationContext()).storeSongIndex(songIndexService);
        stopMedia();
        mediaPlayer.reset();
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
        PendingIntent playPauseAction = null;
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notiAction = R.drawable.ic_pause_notification;
            playPauseAction = playbackAction(PAUSE);
        } else if (playbackStatus == PlaybackStatus.PAUSE) {
            notiAction = R.drawable.ic_play_notification;
            playPauseAction = playbackAction(PLAY);
        }
        Bitmap largeIcon;
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
                stackBuilder.getPendingIntent(REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setShowWhen(false)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken()).setShowActionsInCompactView(0, 1, 2))
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_baseline_volume_up_24)
                .setContentText(mActiveSongService.getArtist())
                .setContentTitle(mActiveSongService.getSongName())
                .setContentIntent(resultPendingIntent)
                .addAction(R.drawable.ic_previous, getString(R.string.previous), playbackAction(PREVIOUS))
                .addAction(notiAction, getString(R.string.pause), playPauseAction)
                .addAction(R.drawable.ic_next, getString(R.string.next), playbackAction(NEXT));
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, notificationBuilder.build());

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * loại bỏ notification
     */
    public void removeNotification() {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    /**
     * tạo channel cho notification
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.music_notification),
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
            case PLAY:
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, FLAGS);
            case PAUSE:
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, FLAGS);
            case NEXT:
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, FLAGS);
            case PREVIOUS:
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, FLAGS);
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
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        completeSong();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        playMedia();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    public class LocalBinder extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }
}
