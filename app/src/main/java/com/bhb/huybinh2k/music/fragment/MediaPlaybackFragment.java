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
    private List<Song> mListPlaying = new ArrayList<>();
    private RelativeLayout mLayoutPlayBar;
    private ImageView mImageSong,
            mImageIcon,
            mImagePause,
            mImageShuffle,
            mImageRepeat;
    private ImageView mImageNext,
            mImagePrev,
            mImageLike,
            mImageDislike,
            mImageMore;
    private TextView mSinger,
            mSongName,
            mRunTime,
            mDuration;
    private ActivityMusic mActivityMusic;
    private StorageUtil mStorageUtil;
    private int mSongIndex,
            mOrientation,
            mShuffle,
            mRepeat;
    private boolean mLockScreen = false;
    private SeekBar mSeekBar;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("mm:ss");
    private FavoriteSongsProvider mFavoriteSongsProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("log", "onCreateViewMediaPlaybackFragment");
        View view = inflater.inflate(R.layout.fragment_media_playback, container, false);
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
        mSeekBar = view.findViewById(R.id.seekbar);
        mSeekBar.setPadding(2, 0, 2, 0);
        mImageShuffle = view.findViewById(R.id.shuffle);
        mImageRepeat = view.findViewById(R.id.repeat);
        mLayoutPlayBar = getActivity().findViewById(R.id.layoutPlayBar);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d("log", "onViewCreatedViewMediaPlaybackFragment");
        super.onViewCreated(view, savedInstanceState);
        mSongIndex = mStorageUtil.loadSongIndex();
        mFavoriteSongsProvider = new FavoriteSongsProvider(getContext());
        mOrientation = getResources().getConfiguration().orientation;
        updateImageRepeatShuffle();
        mActivityMusic = (ActivityMusic) getActivity();
        mActivityMusic.setmIUpdateMediaPlaybackFragment(this);

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
                skipToNext();
            }
        });
        mImagePrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skipToPrevious();
            }
        });
        mImagePause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickPlayPause();
            }
        });
        mImageMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToFavorite(view);
            }
        });
        changeSeekbar();
        updateImageLikeDislike();
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

        if (!mActivityMusic.isServiceBound()) {
            mListPlaying = mFavoriteSongsProvider.listAllSongs();
            if (mListPlaying.size() > 0) {
                mActivityMusic.playAudio(0, mListPlaying);
            } else {
                Toast.makeText(getContext(), R.string.no_music, Toast.LENGTH_SHORT).show();
            }
        }
        if (mSongIndex != -1) {
            mListPlaying = mStorageUtil.loadListSongPlaying();
            updateUI(mSongIndex);
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

    private void changeSeekbar(){
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mActivityMusic.getmMediaService().seekTo(seekBar.getProgress());
                mRunTime.setText(mDateFormat.format(
                        mActivityMusic.getmMediaService().getmMediaPlayer().getCurrentPosition()));
            }
        });
    }

    private void clickLike() {
        switch (mListPlaying.get(mSongIndex).getIsFavorite()) {
            case 0:
            case 1:
                mListPlaying.get(mSongIndex).setIsFavorite(2);
                mFavoriteSongsProvider.update(mListPlaying.get(mSongIndex));
                mImageLike.setImageResource(R.drawable.ic_thumbs_up_selected);
                mImageDislike.setImageResource(R.drawable.ic_thumbs_down_default);
                break;
            case 2:
                mListPlaying.get(mSongIndex).setIsFavorite(0);
                mFavoriteSongsProvider.update(mListPlaying.get(mSongIndex));
                mImageLike.setImageResource(R.drawable.ic_thumbs_up_default);
                break;
        }
    }

    private void addToFavorite(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.add_to_favorite, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.addToFavorite) {
                    mListPlaying.get(mSongIndex).setIsFavorite(2);
                    mFavoriteSongsProvider.update(mListPlaying.get(mSongIndex));
                    Toast.makeText(getContext(), R.string.add_succes, Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void clickDislike(){
        switch (mListPlaying.get(mSongIndex).getIsFavorite()) {
            case 0:
            case 2:
                mListPlaying.get(mSongIndex).setIsFavorite(1);
                mFavoriteSongsProvider.update(mListPlaying.get(mSongIndex));
                mImageDislike.setImageResource(R.drawable.ic_thumbs_down_selected);
                mImageLike.setImageResource(R.drawable.ic_thumbs_up_default);
                break;
            case 1:
                mListPlaying.get(mSongIndex).setIsFavorite(0);
                mFavoriteSongsProvider.update(mListPlaying.get(mSongIndex));
                mImageDislike.setImageResource(R.drawable.ic_thumbs_down_default);
                break;
        }

    }


    /**
     * Cập nhật image cho like/dislike
     */
    private void updateImageLikeDislike(){
        Song song = mFavoriteSongsProvider.getSongByIdProvider(mListPlaying.get(mSongIndex).getIdProvider());
        switch (song.getIsFavorite()) {
            case 2:
                mImageLike.setImageResource(R.drawable.ic_thumbs_up_selected);
                mImageDislike.setImageResource(R.drawable.ic_thumbs_down_default);
                break;
            case 1:
                mImageLike.setImageResource(R.drawable.ic_thumbs_up_default);
                mImageDislike.setImageResource(R.drawable.ic_thumbs_down_selected);
                break;
            case 0:
                mImageLike.setImageResource(R.drawable.ic_thumbs_up_default);
                mImageDislike.setImageResource(R.drawable.ic_thumbs_down_default);
                break;
        }
    }


    /**
     * Cập nhật lại image cho repeat và shuffle
     */
    private void updateImageRepeatShuffle() {
        if (mShuffle == -1) {
            mShuffle = 0;
        }
        if (mShuffle == 0) mImageShuffle.setImageResource(R.drawable.ic_shuffle_white);
        else if (mShuffle == 1) mImageShuffle.setImageResource(R.drawable.ic_shuffle_black);

        if (mRepeat == -1) {
            mRepeat = 0;
        }
        switch (mRepeat) {
            case 0:
                mImageRepeat.setImageResource(R.drawable.ic_repeat_white);
                break;
            case 1:
                mImageRepeat.setImageResource(R.drawable.ic_repeat_black);
                break;
            case 2:
                mImageRepeat.setImageResource(R.drawable.ic_repeat_one);
                break;
        }
    }


    /**
     * sự kiện khi click image shuffle
     */
    private void clickShuffle() {
        if (mShuffle == 0) {
            mShuffle = 1;
            mImageShuffle.setImageResource(R.drawable.ic_shuffle_black);
        } else {
            mShuffle = 0;
            mImageShuffle.setImageResource(R.drawable.ic_shuffle_white);
        }
        mActivityMusic.getmMediaService().updateShuffleRepeat(mShuffle, mRepeat);
    }

    /**
     * sự kiện khi click image repeat
     */
    private void clickRepeat() {
        if (mRepeat == 0) {
            mRepeat = 1;
            mImageRepeat.setImageResource(R.drawable.ic_repeat_black);
            mActivityMusic.getmMediaService().updateShuffleRepeat(mShuffle, mRepeat);
        } else if (mRepeat == 1) {
            mRepeat = 2;
            mImageRepeat.setImageResource(R.drawable.ic_repeat_one);
            mStorageUtil.storeRepeat(mRepeat);
            mActivityMusic.getmMediaService().updateShuffleRepeat(mShuffle, mRepeat);
        } else {
            mRepeat = 0;
            mImageRepeat.setImageResource(R.drawable.ic_repeat_white);
            mStorageUtil.storeRepeat(mRepeat);
            mActivityMusic.getmMediaService().updateShuffleRepeat(mShuffle, mRepeat);
        }
    }

    /**
     * sự kiện khi click image play/pause
     */
    private void clickPlayPause() {
        if (mActivityMusic.getmIsPlaying()) {
            mActivityMusic.getmMediaService().pauseMedia();
            mImagePause.setImageResource(R.drawable.ic_play_circle);
            mActivityMusic.getmMediaService().buildNotification(PlaybackStatus.PAUSE);
            mActivityMusic.setmIsPlaying(false);
        } else {
            mImagePause.setImageResource(R.drawable.ic_pause_circle);
            mActivityMusic.getmMediaService().resumeMedia();
            mActivityMusic.getmMediaService().buildNotification(PlaybackStatus.PLAYING);
            updateTime();
            mActivityMusic.setmIsPlaying(true);
        }
    }


    /**
     * cập nhật image play/pause khi nhận được receiver
     */
    private void playPauseReceiver() {
        if (mActivityMusic.getmIsPlaying()) {
            mImagePause.setImageResource(R.drawable.ic_play_circle);
            mActivityMusic.setmIsPlaying(false);
        } else if (!mActivityMusic.getmIsPlaying()) {
            mImagePause.setImageResource(R.drawable.ic_pause_circle);
            mActivityMusic.setmIsPlaying(true);
        }
    }

    /**
     * cập nhật image play/pause
     */
    private void updateImagePlayPause() {
        if (!mActivityMusic.getmIsPlaying()) {
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
        if (mActivityMusic.getmResumePosition() != -1) {
            mRunTime.setText(mDateFormat.format(mActivityMusic.getmResumePosition()));
            mSeekBar.setProgress(mActivityMusic.getmResumePosition());
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
                if (mActivityMusic.getmMediaService().getmMediaPlayer().isPlaying()) {
                    mSeekBar.setProgress(mActivityMusic.getmMediaService().getmMediaPlayer().getCurrentPosition());
                    mRunTime.setText(mDateFormat.format(
                            mActivityMusic.getmMediaService().getmMediaPlayer().getCurrentPosition()));
                    handler.postDelayed(this, 500);
                }
            }
        }, 500);
    }

    /**
     * Nhận broadcastReceiver
     */
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSongIndex = intent.getIntExtra(ActivityMusic.GET_SONG_INDEX, -1);
            int x = intent.getIntExtra(ActivityMusic.PLAY_PAUSE, -1);
            if (mSongIndex != -1) {
                updateUI(mSongIndex);
            }
            if (x != -1) {
                if (x==1){
                    mActivityMusic.setmIsPlaying(false);
                }else {
                    mActivityMusic.setmIsPlaying(true);
                }
                playPauseReceiver();
            }
        }
    };


    /**
     * sự kiện khi click next
     */
    private void skipToNext() {
        mActivityMusic.getmMediaService().skipToNext();
        updateUI(mActivityMusic.getmMediaService().getmSongIndexService());
        mActivityMusic.setmIsPlaying(true);
        mSeekBar.setProgress(0);
        mActivityMusic.getmMediaService().buildNotification(PlaybackStatus.PLAYING);
        sendBroadcast();
    }

    /**
     * sự kiện khi click previous
     */
    private void skipToPrevious() {
        //Nếu bài hát phát chưa được 3s thì chuyển sang bài trước đó
        if (mActivityMusic.getmMediaService().getmMediaPlayer().getCurrentPosition() < 3000) {
            mActivityMusic.getmMediaService().skipToPrevious();
            updateUI(mActivityMusic.getmMediaService().getmSongIndexService());
            mActivityMusic.setmIsPlaying(true);

            mActivityMusic.getmMediaService().buildNotification(PlaybackStatus.PLAYING);

            sendBroadcast();
        } else {
            //Nếu phát nhiều hơn 3s thì phát lại từ đầu
            mActivityMusic.getmMediaService().getmMediaPlayer().seekTo(0);
        }
    }

    private void sendBroadcast() {
        Intent intent = new Intent();
        intent.setAction(ActivityMusic.BROADCAST_RECEIVER);
        intent.putExtra(ActivityMusic.GET_SONG_INDEX, mActivityMusic.getmMediaService().getmSongIndexService());
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("log", "onStartMediaPlaybackFragment");
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
        Log.d("log", "onCreateMediaPlaybackFragment");
        mStorageUtil = new StorageUtil(getActivity().getApplicationContext());
        mShuffle = mStorageUtil.loadShuffle();
        mRepeat = mStorageUtil.loadRepeat();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("log", "onPauseMediaPlaybackFragment");
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mLockScreen = true;
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("log", "onStopMediaPlaybackFragment");
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("log", "onDestroyViewMediaPlaybackFragment");
        mStorageUtil.storeRepeat(mRepeat);
        mStorageUtil.storeShuffle(mShuffle);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mActivityMusic.setmIsBack(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("log", "onDestroyViewMediaPlaybackFragment");
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutPlayBar.setVisibility(View.VISIBLE);
            mActivityMusic.updateImagePlayPause();
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    @Override
    public void update(int songindex) {
        updateUI(songindex);
    }
}