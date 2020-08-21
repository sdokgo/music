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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bhb.huybinh2k.music.IOnClickSongListener;
import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.activity.ActivityMusic;
import com.bhb.huybinh2k.music.adapter.AllSongsAdapter;

import java.util.ArrayList;
import java.util.List;

public class AllSongsFragment extends Fragment implements ActivityMusic.IUpdateAllSongsFragment {
    private List<Song> mList, mFavoriteList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private ActivityMusic mActivityMusic;
    private AllSongsAdapter mAdapter;
    private RelativeLayout mPlayBar;
    private int mOrientation, mSongIndex;
    private boolean mLockScreen = false, mReplace = false;
    public static final String SONG_INDEX = "com.bhb.huybinh2k.SONG_INDEX";
    private MediaPlaybackFragment mediaPlaybackFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_songs, container, false);
        Log.d("log", "onCreateViewAllSongsFragment");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("log", "onViewCreatedAllSongsFragment");
        mRecyclerView = view.findViewById(R.id.list_all_music);
        mPlayBar = getActivity().findViewById(R.id.layoutPlayBar);
        mList = new ArrayList<Song>();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        getSongList();
        mActivityMusic = (ActivityMusic) getActivity();
        mActivityMusic.setmIUpdateAllSongsFragment(this);
        mAdapter = new AllSongsAdapter(getContext(), R.layout.list_music, mList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mOrientation = getResources().getConfiguration().orientation;
        mSongIndex = new StorageUtil(getContext()).loadSongIndex();

        if (savedInstanceState != null) {
            mSongIndex = savedInstanceState.getInt(SONG_INDEX);
            update(mSongIndex);
        }
        mediaPlaybackFragment = new MediaPlaybackFragment();
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mPlayBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.framelayout, mediaPlaybackFragment)
                            .addToBackStack("allsongsfragment").commit();
                }
            });
            if (mReplace == true) {
                update(mSongIndex);
                mReplace = false;
            }
        } else {
            mRecyclerView.scrollToPosition(mSongIndex);
            mAdapter.setiOnClickSongListener(mIOnClickSongListener);
        }

        mAdapter.setOnItemClickListener(new AllSongsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                mSongIndex = position;
                Song song = mList.get(position);
                int countOfPlay = song.getCountOfPlay();
                song.setCountOfPlay(++countOfPlay);
                if (song.getCountOfPlay() == 3 && song.getIsFavorite() == 0) {
                    mFavoriteList.add(song);
                    song.setIsFavorite(2);
                }
                mActivityMusic.playAudio(position, mList);
                mActivityMusic.setmIsPlaying(0);
                update(position);
            }
        });


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("log", "onCreateAllSongsFragment");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("log", "onStartAllSongsFragment");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(mActivityMusic.BROADCAST_RECEIVER);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mLockScreen == true) {
                mSongIndex = new StorageUtil(getContext()).loadSongIndex();
                update(mSongIndex);
                mRecyclerView.scrollToPosition(mSongIndex);
                mLockScreen = false;
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("log", "onStopAllSongsFragment");
        getActivity().unregisterReceiver(broadcastReceiver);
        mReplace = true;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mLockScreen = true;
        }
    }

    /**
     * Nhận BroadcastReceiver
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int i = intent.getIntExtra(mActivityMusic.GET_SONG_INDEX, -1);
            if (i != -1) {
                update(i);
                mSongIndex = i;
                mRecyclerView.scrollToPosition(mSongIndex);
            }
        }
    };


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SONG_INDEX, mSongIndex);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("log", "onDestroyAllSongsFragment");
        new StorageUtil(getContext()).storeFavoriteSong(mFavoriteList);
    }


    /**
     * Đọc dữ liệu trong máy và add vào list
     */
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

    /**
     * Cập nhật lại listview
     */
    @Override
    public void update(int songIndex) {
        mAdapter.setPlayingPosition(songIndex);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(songIndex);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mPlayBar.setVisibility(View.VISIBLE);
            mActivityMusic.updateUIPlayBar(songIndex);
        }
    }


    private IOnClickSongListener mIOnClickSongListener;

    public void setmIOnClickSongListener(IOnClickSongListener iOnClickSongListener) {
        this.mIOnClickSongListener = iOnClickSongListener;
        if (mAdapter != null) {
            mAdapter.setiOnClickSongListener(iOnClickSongListener);
        }
    }
}