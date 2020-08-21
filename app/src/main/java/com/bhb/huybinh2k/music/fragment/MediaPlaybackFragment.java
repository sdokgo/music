package com.bhb.huybinh2k.music.fragment;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bhb.huybinh2k.music.PlaybackStatus;
import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.activity.ActivityMusic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackFragment extends Fragment implements ActivityMusic.IUpdateMediaPlaybackFragment {
    private List<Song> mList;
    private RelativeLayout mLayoutPlayBar;
    private ImageView mImageSong, mImageIcon, mImagePause, mImageShuffle, mImageRepeat;
    private ImageView mImageNext, mImagePrev, mImageLike, mImageDislike, mImageList;
    private TextView mSinger, mSongName, mRunTime, mDuration;
    private ActivityMusic mActivityMusic;
    private StorageUtil mStorageUtil;
    private int mSongIndex, mOrientation, mShuffle, mRepeat;
    private boolean mLockScreen = false;
    private SeekBar mSeekBar;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("mm:ss");

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

        mList = new ArrayList<>();
        mSongIndex = mStorageUtil.loadSongIndex();
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


        if (!mActivityMusic.isServiceBound()) {
            getSongList();
            if (mList.size() > 0) {
                mActivityMusic.playAudio(0, mList);
            } else {
                Toast.makeText(getContext(), "No music found", Toast.LENGTH_SHORT).show();
            }

        }

        if (mSongIndex != -1) {
            mList = mStorageUtil.loadSongList();
            updateUI(mSongIndex);
        }

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            mImageList = view.findViewById(R.id.playlist);
            mImageList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFragmentManager().popBackStack();
                }
            });
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
        if (mRepeat == 0) mImageRepeat.setImageResource(R.drawable.ic_repeat_white);
        else if (mRepeat == 1) {
            mImageRepeat.setImageResource(R.drawable.ic_repeat_black);
        } else if (mRepeat == 2) mImageRepeat.setImageResource(R.drawable.ic_repeat_one);
    }


    /**
     * sự kiện khi click image shuffle
     */
    private void clickShuffle() {
        if (mShuffle == 0) {
            mShuffle = 1;
            mImageShuffle.setImageResource(R.drawable.ic_shuffle_black);
            mActivityMusic.getmMediaService().updateShuffleRepeat(mShuffle, mRepeat);
        } else {
            mShuffle = 0;
            mImageShuffle.setImageResource(R.drawable.ic_shuffle_white);
            mActivityMusic.getmMediaService().updateShuffleRepeat(mShuffle, mRepeat);
        }
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
        if (mActivityMusic.getmIsPlaying() == 0) {
            mActivityMusic.getmMediaService().pauseMedia();
            mImagePause.setImageResource(R.drawable.ic_play_circle);
            mActivityMusic.getmMediaService().buildNotification(PlaybackStatus.PAUSE);
            mActivityMusic.setmIsPlaying(1);
        } else if (mActivityMusic.getmIsPlaying() == 1) {
            mImagePause.setImageResource(R.drawable.ic_pause_circle);
            mActivityMusic.getmMediaService().resumeMedia();
            mActivityMusic.getmMediaService().buildNotification(PlaybackStatus.PLAYING);
            updateTime();
            mActivityMusic.setmIsPlaying(0);
        }
    }


    /**
     * cập nhật image play/pause khi nhận được receiver
     */
    private void playPauseReceiver() {
        if (mActivityMusic.getmIsPlaying() == 0) {
            mImagePause.setImageResource(R.drawable.ic_play_circle);
            mActivityMusic.setmIsPlaying(1);
        } else if (mActivityMusic.getmIsPlaying() == 1) {
            mImagePause.setImageResource(R.drawable.ic_pause_circle);
            mActivityMusic.setmIsPlaying(0);
        }
    }

    /**
     * cập nhật image play/pause
     */
    private void updateImagePlayPause() {
        if (mActivityMusic.getmIsPlaying() == 1) {
            mImagePause.setImageResource(R.drawable.ic_play_circle);
        } else {
            mImagePause.setImageResource(R.drawable.ic_pause_circle);
        }
    }

    //    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        Bundle bundle = getArguments();
//        if (bundle!= null){
//            mSongIndex = bundle.getInt(AllSongsFragment.SONG_INDEX);
//            updateUI(mSongIndex);
//        }
//    }
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
        Song song = mList.get(index);
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
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSongIndex = intent.getIntExtra(ActivityMusic.GET_SONG_INDEX, -1);
            int x = intent.getIntExtra(ActivityMusic.PLAY_PAUSE, -1);
            if (mSongIndex != -1) {
                updateUI(mSongIndex);
            }
            if (x != -1) {
                mActivityMusic.setmIsPlaying(x);
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
        mActivityMusic.setmIsPlaying(0);
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
            mActivityMusic.setmIsPlaying(0);

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
        getActivity().registerReceiver(broadcastReceiver, intentFilter);

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mLockScreen == true) {
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
        getActivity().unregisterReceiver(broadcastReceiver);
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


    public void getSongList() {
        mList.clear();
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null) {
            if (musicCursor.moveToFirst()) {
                int id = 1;
                do {
                    String thisTitle = musicCursor.getString(
                            musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String thisArtist = musicCursor.getString(
                            musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String songpath = musicCursor.getString(
                            musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                    Long albumId = musicCursor.getLong(musicCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                    Uri sArtworkUri = Uri
                            .parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                    String albumArt = String.valueOf(albumArtUri);

                    Long milliseconds = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    mList.add(new Song(id, thisTitle, songpath, thisArtist, albumArt, milliseconds));
                    id++;

                }
                while (musicCursor.moveToNext());
            }
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