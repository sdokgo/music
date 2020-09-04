package com.bhb.huybinh2k.music.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bhb.huybinh2k.music.LogSetting;
import com.bhb.huybinh2k.music.MediaPlaybackService;
import com.bhb.huybinh2k.music.PlaybackStatus;
import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.activity.ActivityMusic;
import com.bhb.huybinh2k.music.database.FavoriteSongsProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackFragment extends Fragment implements ActivityMusic.IUpdateMediaPlaybackFragment {
    public static final int DEFAULT_FAVORITE = 0;
    public static final int NOT_FAVORITE = 1;
    public static final int SET_FAVORITE = 2;
    public static final int NO_SHUFFLE = 0;
    public static final int SHUFFLE = 1;
    public static final int NO_REPEAT = 0;
    public static final int REPEAT_ALL = 1;
    public static final int REPEAT_ONE = 2;
    private List<Song> mListPlaying = new ArrayList<>();
    private RelativeLayout mLayoutPlayBar;
    private ImageView mImageSong;
    private ImageView mImageIcon;
    private ImageView mImagePause;
    private ImageView mImageShuffle;
    private ImageView mImageRepeat;
    private ImageView mImageNext;
    private ImageView mImagePrev;
    private ImageView mImageLike;
    private ImageView mImageDislike;
    private ImageView mImageMore;
    private TextView mSinger;
    private TextView mSongName;
    private TextView mRunTime;
    private TextView mDuration;
    private ActivityMusic mActivityMusic;
    private StorageUtil mStorageUtil;
    private int mSongIndex;
    private int mOrientation;
    private int mShuffle;
    private int mRepeat;
    private boolean mLockScreen;
    private SeekBar mSeekBar;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("mm:ss");
    private FavoriteSongsProvider mFavoriteSongsProvider;
    /**
     * Nhận broadcastReceiver
     */
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSongIndex = intent.getIntExtra(ActivityMusic.GET_SONG_INDEX, ActivityMusic.DEFAULT_VALUE);
            int x = intent.getIntExtra(ActivityMusic.PLAY_PAUSE, ActivityMusic.DEFAULT_VALUE);
            if (mSongIndex != ActivityMusic.DEFAULT_VALUE) {
                updateUI(mSongIndex);
            }
            if (x != ActivityMusic.DEFAULT_VALUE) {
                if (x == ActivityMusic.PAUSE) {
                    mActivityMusic.isPlaying = false;
                } else {
                    mActivityMusic.isPlaying = true;
                }
                playPauseReceiver();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (LogSetting.IS_DEBUG) {
            Log.d("log", "onCreateViewMediaPlaybackFragment");
        }
        View view = inflater.inflate(R.layout.fragment_media_playback, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mImageSong = view.findViewById(R.id.img_song);
        mSongName = view.findViewById(R.id.tenbaihat_media);
        mSinger = view.findViewById(R.id.tencasi_media);
        mImageIcon = view.findViewById(R.id.img_header);
        mImagePause = view.findViewById(R.id.pause);
        mImageMore = view.findViewById(R.id.more);
        mImageNext = view.findViewById(R.id.next);
        mImagePrev = view.findViewById(R.id.prev);
        mImageLike = view.findViewById(R.id.like);
        mImageDislike = view.findViewById(R.id.dislike);
        mRunTime = view.findViewById(R.id.runtime);
        mDuration = view.findViewById(R.id.song_duration);
        mSeekBar = view.findViewById(R.id.seek_bar);
        changeSeekBar();
        mImageShuffle = view.findViewById(R.id.shuffle);
        mImageRepeat = view.findViewById(R.id.repeat);
        mLayoutPlayBar = getActivity().findViewById(R.id.layoutPlayBar);
        mListPlaying = mStorageUtil.loadListSongPlaying();
        mSongIndex = mStorageUtil.loadSongIndex();
        mImageShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickShuffle();
            }
        });
        mImageRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRepeat();
            }
        });
        mImageNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mActivityMusic.isServiceBound) {
                    if (mSongIndex == mListPlaying.size() - 1) {
                        mSongIndex = 0;
                    } else {
                        ++mSongIndex;
                    }
                    mActivityMusic.playAudio(mSongIndex, mListPlaying);
                    updateUI(mSongIndex);
                } else {
                    skipToNext();
                }
            }
        });
        mImagePrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mActivityMusic.isServiceBound) {
                    if (mSongIndex == 0) {
                        mSongIndex = mListPlaying.size() - 1;
                    } else {
                        --mSongIndex;
                    }
                    mActivityMusic.playAudio(mSongIndex, mListPlaying);
                    updateUI(mSongIndex);
                } else {
                    skipToPrevious();
                }
            }
        });
        mImagePause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mActivityMusic.isServiceBound) {
                    mActivityMusic.playAudio(mSongIndex, mListPlaying);
                    changeSeekBar();
                    updateUI(mSongIndex);
                } else {
                    clickPlayPause();
                }

            }
        });
        mImageMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToFavorite(view);
            }
        });
        mImageDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickDislike();
            }
        });
        mImageLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickLike();
            }
        });
        mSongIndex = mStorageUtil.loadSongIndex();
        mFavoriteSongsProvider = new FavoriteSongsProvider(getContext());
        mOrientation = getResources().getConfiguration().orientation;
        mActivityMusic = (ActivityMusic) getActivity();
        mActivityMusic.setIUpdateMediaPlaybackFragment(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (LogSetting.IS_DEBUG) {
            Log.d(ActivityMusic.TAG, "onViewCreatedViewMediaPlaybackFragment");
        }
        super.onViewCreated(view, savedInstanceState);

        updateImageRepeatShuffle();

        if (!mActivityMusic.isServiceBound) {
        }
        if (mSongIndex != ActivityMusic.DEFAULT_VALUE) {
            updateUI(mSongIndex);
            updateImageLikeDislike();
        } else {
            disableFirstTime(false);
        }


        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            ImageView mImageList = view.findViewById(R.id.playlist);
            mImageList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFragmentManager().popBackStack();
                }
            });
        }
    }

    public void changeSeekBar() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mActivityMusic.isServiceBound) {
                    mActivityMusic.mediaService.seekTo(seekBar.getProgress());
                    mRunTime.setText(mDateFormat.format(
                            mActivityMusic.mediaService.mediaPlayer.getCurrentPosition()));
                }


            }
        });
    }

    private void clickLike() {
        switch (mListPlaying.get(mSongIndex).getIsFavorite()) {
            case DEFAULT_FAVORITE:
            case NOT_FAVORITE:
                mListPlaying.get(mSongIndex).setIsFavorite(SET_FAVORITE);
                mFavoriteSongsProvider.updateSongOfDB(mListPlaying.get(mSongIndex));
                mImageLike.setImageResource(R.drawable.ic_thumbs_up_selected);
                mImageDislike.setImageResource(R.drawable.ic_thumbs_down_default);
                break;
            case SET_FAVORITE:
                mListPlaying.get(mSongIndex).setIsFavorite(DEFAULT_FAVORITE);
                mFavoriteSongsProvider.updateSongOfDB(mListPlaying.get(mSongIndex));
                mImageLike.setImageResource(R.drawable.ic_thumbs_up_default);
                break;
        }
    }

    public void disableFirstTime(boolean b) {
        mImageNext.setEnabled(b);
        mImagePrev.setEnabled(b);
        mImageLike.setEnabled(b);
        mImageDislike.setEnabled(b);
        mImageMore.setEnabled(b);
        mSeekBar.setEnabled(b);
        mImagePause.setEnabled(b);
    }


    private void addToFavorite(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.add_to_favorite, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.addToFavorite) {
                    mListPlaying.get(mSongIndex).setIsFavorite(SET_FAVORITE);
                    mFavoriteSongsProvider.updateSongOfDB(mListPlaying.get(mSongIndex));
                    Toast.makeText(getContext(), R.string.add_succes, Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void clickDislike() {
        switch (mListPlaying.get(mSongIndex).getIsFavorite()) {
            case DEFAULT_FAVORITE:
            case SET_FAVORITE:
                mListPlaying.get(mSongIndex).setIsFavorite(NOT_FAVORITE);
                mFavoriteSongsProvider.updateSongOfDB(mListPlaying.get(mSongIndex));
                mImageDislike.setImageResource(R.drawable.ic_thumbs_down_selected);
                mImageLike.setImageResource(R.drawable.ic_thumbs_up_default);
                break;
            case NOT_FAVORITE:
                mListPlaying.get(mSongIndex).setIsFavorite(DEFAULT_FAVORITE);
                mFavoriteSongsProvider.updateSongOfDB(mListPlaying.get(mSongIndex));
                mImageDislike.setImageResource(R.drawable.ic_thumbs_down_default);
                break;
        }

    }

    /**
     * Cập nhật image cho like/dislike
     */
    private void updateImageLikeDislike() {
        List<Song> listPlaying = mStorageUtil.loadListSongPlaying();
        Song song = mFavoriteSongsProvider.getSongByIdProvider(listPlaying.get(mSongIndex).getIdProvider());
        switch (song.getIsFavorite()) {
            case SET_FAVORITE:
                mImageLike.setImageResource(R.drawable.ic_thumbs_up_selected);
                mImageDislike.setImageResource(R.drawable.ic_thumbs_down_default);
                break;
            case NOT_FAVORITE:
                mImageLike.setImageResource(R.drawable.ic_thumbs_up_default);
                mImageDislike.setImageResource(R.drawable.ic_thumbs_down_selected);
                break;
            case DEFAULT_FAVORITE:
                mImageLike.setImageResource(R.drawable.ic_thumbs_up_default);
                mImageDislike.setImageResource(R.drawable.ic_thumbs_down_default);
                break;
        }
    }

    /**
     * Cập nhật lại image cho repeat và shuffle
     */
    private void updateImageRepeatShuffle() {
        if (mShuffle == ActivityMusic.DEFAULT_VALUE) {
            mShuffle = NO_SHUFFLE;
        }
        if (mShuffle == NO_SHUFFLE) mImageShuffle.setImageResource(R.drawable.ic_shuffle_white);
        else if (mShuffle == SHUFFLE) mImageShuffle.setImageResource(R.drawable.ic_shuffle_black);

        if (mRepeat == ActivityMusic.DEFAULT_VALUE) {
            mRepeat = NO_REPEAT;
        }
        switch (mRepeat) {
            case NO_REPEAT:
                mImageRepeat.setImageResource(R.drawable.ic_repeat_white);
                break;
            case REPEAT_ALL:
                mImageRepeat.setImageResource(R.drawable.ic_repeat_black);
                break;
            case REPEAT_ONE:
                mImageRepeat.setImageResource(R.drawable.ic_repeat_one);
                break;
        }
    }

    /**
     * sự kiện khi click image shuffle
     */
    private void clickShuffle() {
        if (mShuffle == NO_SHUFFLE) {
            mShuffle = SHUFFLE;
            mImageShuffle.setImageResource(R.drawable.ic_shuffle_black);
        } else {
            mShuffle = NO_SHUFFLE;
            mImageShuffle.setImageResource(R.drawable.ic_shuffle_white);
        }
        mActivityMusic.mediaService.updateShuffleRepeat(mShuffle, mRepeat);
    }

    /**
     * sự kiện khi click image repeat
     */
    private void clickRepeat() {
        switch (mRepeat) {
            case NO_REPEAT:
                mRepeat = REPEAT_ALL;
                mImageRepeat.setImageResource(R.drawable.ic_repeat_black);
                if (mActivityMusic.isServiceBound) {
                    mActivityMusic.mediaService.updateShuffleRepeat(mShuffle, mRepeat);
                }
                break;
            case REPEAT_ALL:
                mRepeat = REPEAT_ONE;
                mImageRepeat.setImageResource(R.drawable.ic_repeat_one);
                mStorageUtil.storeRepeat(mRepeat);
                if (mActivityMusic.isServiceBound) {
                    mActivityMusic.mediaService.updateShuffleRepeat(mShuffle, mRepeat);
                }
                break;
            case REPEAT_ONE:
                mRepeat = NO_REPEAT;
                mImageRepeat.setImageResource(R.drawable.ic_repeat_white);
                mStorageUtil.storeRepeat(mRepeat);
                if (mActivityMusic.isServiceBound) {
                    mActivityMusic.mediaService.updateShuffleRepeat(mShuffle, mRepeat);
                }
                break;
        }
    }

    /**
     * sự kiện khi click image play/pause
     */
    private void clickPlayPause() {
        if (mActivityMusic.isPlaying) {
            mActivityMusic.mediaService.pauseMedia();
            mImagePause.setImageResource(R.drawable.ic_play_circle);
            mActivityMusic.mediaService.buildNotification(PlaybackStatus.PAUSE);
            mActivityMusic.resumePosition = mActivityMusic.mediaService.mResumePosition;
            mActivityMusic.isPlaying = false;
        } else {
            mImagePause.setImageResource(R.drawable.ic_pause_circle);
            mActivityMusic.mediaService.resumeMedia();
            mActivityMusic.mediaService.buildNotification(PlaybackStatus.PLAYING);
            updateTime();
            mActivityMusic.isPlaying = true;
        }
    }

    /**
     * cập nhật image play/pause khi nhận được receiver
     */
    private void playPauseReceiver() {
        if (mActivityMusic.isPlaying) {
            mImagePause.setImageResource(R.drawable.ic_play_circle);
            mActivityMusic.isPlaying = false;
        } else {
            mImagePause.setImageResource(R.drawable.ic_pause_circle);
            mActivityMusic.isPlaying = true;
        }
    }

    /**
     * cập nhật image play/pause
     */
    private void updateImagePlayPause() {
        if (!mActivityMusic.isPlaying) {
            mImagePause.setImageResource(R.drawable.ic_play_circle);
        } else {
            mImagePause.setImageResource(R.drawable.ic_pause_circle);
        }
    }

    public void update(Bundle bundle) {
        if (bundle != null) {
            mSongIndex = bundle.getInt(AllSongsFragment.SONG_INDEX);
            updateUI(mSongIndex);
        }
    }

    /**
     * Cập nhật lại giao diện bài hát
     */
    public void updateUI(int index) {
        mLayoutPlayBar.setVisibility(View.GONE);
        updateImagePlayPause();
        mListPlaying = mStorageUtil.loadListSongPlaying();
        Song song = mListPlaying.get(index);
        mImageSong.setImageURI(Uri.parse(song.getImg()));
        mImageIcon.setImageURI(Uri.parse(song.getImg()));
        mSongName.setText(song.getSongName());
        mSinger.setText(song.getArtist());
        mDuration.setText(mDateFormat.format(song.getDuration()));
        mSeekBar.setMax((int) song.getDuration());
        if (mActivityMusic.resumePosition != ActivityMusic.DEFAULT_VALUE) {
            mRunTime.setText(mDateFormat.format(mActivityMusic.resumePosition));
            mSeekBar.setProgress(mActivityMusic.resumePosition);
        }
        updateImageLikeDislike();
        updateTime();

    }

    /**
     * update thời gian chạy của bài hát và seekbar
     */
    private void updateTime() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mActivityMusic.isServiceBound) {
                    if (mActivityMusic.mediaService.mediaPlayer.isPlaying()) {
                        mSeekBar.setProgress(mActivityMusic.mediaService.mediaPlayer.getCurrentPosition());
                        mRunTime.setText(mDateFormat.format(
                                mActivityMusic.mediaService.mediaPlayer.getCurrentPosition()));
                        handler.postDelayed(this, 500);
                    }
                }
            }
        }, 500);
    }

    /**
     * sự kiện khi click next
     */
    private void skipToNext() {
        mActivityMusic.mediaService.skipToNext();
        updateUI(mActivityMusic.mediaService.songIndexService);
        mActivityMusic.isPlaying = true;
        mSeekBar.setProgress(0);
        mActivityMusic.mediaService.buildNotification(PlaybackStatus.PLAYING);
        sendBroadcast();
    }

    /**
     * sự kiện khi click previous
     */
    private void skipToPrevious() {
        //Nếu bài hát phát chưa được 3s thì chuyển sang bài trước đó
        if (mActivityMusic.mediaService.mediaPlayer.getCurrentPosition() < 3000) {
            mActivityMusic.mediaService.skipToPrevious();
            updateUI(mActivityMusic.mediaService.songIndexService);
            mActivityMusic.isPlaying = true;
            mActivityMusic.mediaService.buildNotification(PlaybackStatus.PLAYING);
            sendBroadcast();
        } else {
            //Nếu phát nhiều hơn 3s thì phát lại từ đầu
            mActivityMusic.mediaService.mediaPlayer.seekTo(0);
        }
    }

    /**
     * gửi broadcast cho service
     */
    private void sendBroadcast() {
        Intent intent = new Intent();
        intent.setAction(ActivityMusic.BROADCAST_RECEIVER);
        intent.putExtra(ActivityMusic.GET_SONG_INDEX, mActivityMusic.mediaService.songIndexService);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (LogSetting.IS_DEBUG) {
            Log.d(ActivityMusic.TAG, "onStartMediaPlaybackFragment");
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActivityMusic.BROADCAST_RECEIVER);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mLockScreen) {
                mSongIndex = new StorageUtil(getContext()).loadSongIndex();
                updateUI(mSongIndex);
                mLockScreen = false;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LogSetting.IS_DEBUG) {
            Log.d(ActivityMusic.TAG, "onCreateMediaPlaybackFragment");
        }
        mStorageUtil = new StorageUtil(getActivity().getApplicationContext());
        mShuffle = mStorageUtil.loadShuffle();
        mRepeat = mStorageUtil.loadRepeat();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (LogSetting.IS_DEBUG) {
            Log.d(ActivityMusic.TAG, "onPauseMediaPlaybackFragment");
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mLockScreen = true;
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (LogSetting.IS_DEBUG) {
            Log.d(ActivityMusic.TAG, "onStopMediaPlaybackFragment");
        }
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (LogSetting.IS_DEBUG) {
            Log.d(ActivityMusic.TAG, "onDestroyViewMediaPlaybackFragment");
        }
        mStorageUtil.storeRepeat(mRepeat);
        mStorageUtil.storeShuffle(mShuffle);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mActivityMusic.isBack = true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (LogSetting.IS_DEBUG) {
            Log.d(ActivityMusic.TAG, "onDestroyViewMediaPlaybackFragment");
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutPlayBar.setVisibility(View.VISIBLE);
            mActivityMusic.updateImagePlayPause();
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    @Override
    public void update(int songIndex) {
        updateUI(songIndex);
    }
}