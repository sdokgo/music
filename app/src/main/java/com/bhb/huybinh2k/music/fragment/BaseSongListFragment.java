package com.bhb.huybinh2k.music.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bhb.huybinh2k.music.IOnClickSongListener;
import com.bhb.huybinh2k.music.LogSetting;
import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.activity.ActivityMusic;
import com.bhb.huybinh2k.music.adapter.SongsAdapter;
import com.bhb.huybinh2k.music.database.FavoriteSongsProvider;

import java.util.ArrayList;
import java.util.List;

public class BaseSongListFragment extends Fragment implements ActivityMusic.IUpdateAllSongsFragment {
    public static final String SONG_INDEX = "com.bhb.huybinh2k.SONG_INDEX";
    private static final int MIN_COUNT_ADD_TO_FAVORITE = 3 ;
    protected List<Song> mList = new ArrayList<>();
    protected RecyclerView mRecyclerView;
    protected ActivityMusic mActivityMusic;
    protected RelativeLayout mPlayBar;
    protected int mOrientation;
    protected int mSongIndex;
    protected boolean mLockScreen;
    protected boolean mReplace;
    protected MediaPlaybackFragment mMediaPlaybackFragment;
    protected SongsAdapter mAdapter;
    /**
     * Nhận BroadcastReceiver từ service
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int songIndex = intent.getIntExtra(ActivityMusic.GET_SONG_INDEX, ActivityMusic.DEFAULT_VALUE);
            if (songIndex != ActivityMusic.DEFAULT_VALUE) {
                update(songIndex);
                mSongIndex = songIndex;
                mRecyclerView.scrollToPosition(mSongIndex);
            }
        }
    };
    protected FavoriteSongsProvider mFavoriteSongsProvider;
    private IOnClickSongListener mIOnClickSongListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base_song_list, container, false);
        mFavoriteSongsProvider = new FavoriteSongsProvider(getContext());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.list_all_music);
        mPlayBar = getActivity().findViewById(R.id.layoutPlayBar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        mActivityMusic = (ActivityMusic) getActivity();
        mOrientation = getResources().getConfiguration().orientation;
        mSongIndex = new StorageUtil(getContext()).loadSongIndex();

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mActivityMusic.setIUpdateAllSongsFragment(this);

        if (savedInstanceState != null) {
            mSongIndex = savedInstanceState.getInt(SONG_INDEX);
            if (mSongIndex != ActivityMusic.DEFAULT_VALUE) update(mSongIndex);
        }
        mMediaPlaybackFragment = new MediaPlaybackFragment();
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mPlayBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame_song, mMediaPlaybackFragment)
                            .addToBackStack(getString(R.string.allsongsfragment)).commit();
                }
            });
            if (mReplace) {
                if (mSongIndex != ActivityMusic.DEFAULT_VALUE) update(mSongIndex);
                mReplace = false;
                mList = new StorageUtil(getContext()).loadListSongPlaying();
            }
        } else {
            update(mSongIndex);
            mRecyclerView.scrollToPosition(mSongIndex);
            mAdapter.setmIOnClickSongListener(mIOnClickSongListener);
        }
    }

    public void clickSong() {
        mAdapter.setOnItemClickListener(new SongsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                mSongIndex = position;
                int countOfPlay = mList.get(position).getCountOfPlay();
                mList.get(position).setCountOfPlay(++countOfPlay);
                if (mList.get(position).getCountOfPlay() == MIN_COUNT_ADD_TO_FAVORITE &&
                        mList.get(position).getIsFavorite() == MediaPlaybackFragment.DEFAULT_FAVORITE) {
                    mList.get(position).setIsFavorite(MediaPlaybackFragment.SET_FAVORITE);
                    mFavoriteSongsProvider.update(mList.get(position));
                }
                mActivityMusic.playAudio(position, mList);
                mActivityMusic.setmIsPlaying(true);
                update(position);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActivityMusic.BROADCAST_RECEIVER);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mLockScreen) {
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
        getActivity().unregisterReceiver(mBroadcastReceiver);
        mReplace = true;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mLockScreen = true;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SONG_INDEX, mSongIndex);
    }

    /**
     * Cập nhật lại UI
     */
    @Override
    public void update(int songIndex) {
        if (songIndex != ActivityMusic.DEFAULT_VALUE) {
            List<Song> s = new StorageUtil(getContext()).loadListSongPlaying();
            if (s.size() == mList.size()) {
                mAdapter.setmPlayingIdProvider(mList.get(songIndex).getIdProvider());
                mAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(songIndex);
            }
        }

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mSongIndex != ActivityMusic.DEFAULT_VALUE) {
                mPlayBar.setVisibility(View.VISIBLE);
                mActivityMusic.updateUIPlayBar(songIndex);
            }
        }
    }

    public void setmIOnClickSongListener(IOnClickSongListener iOnClickSongListener) {
        this.mIOnClickSongListener = iOnClickSongListener;
        if (mAdapter != null) {
            mAdapter.setmIOnClickSongListener(iOnClickSongListener);
        }
    }
}