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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bhb.huybinh2k.music.IOnClickSongListener;
import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.activity.ActivityMusic;
import com.bhb.huybinh2k.music.adapter.AllSongsAdapter;

import java.util.ArrayList;
import java.util.List;

public class BaseSongListFragment extends Fragment implements ActivityMusic.IUpdateAllSongsFragment {
    protected List<Song> mList = new ArrayList<>(), mFavoriteList = new ArrayList<>();
    protected RecyclerView mRecyclerView;
    protected ActivityMusic mActivityMusic;
    protected RelativeLayout mPlayBar;
    protected int mOrientation, mSongIndex;
    protected boolean mLockScreen = false, mReplace = false;
    protected MediaPlaybackFragment mediaPlaybackFragment;
    protected AllSongsAdapter mAdapter;
    public static final String SONG_INDEX = "com.bhb.huybinh2k.SONG_INDEX";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_base_song_list, container, false);
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

        mAdapter = new AllSongsAdapter(getContext(), R.layout.list_music, mList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mActivityMusic.setmIUpdateAllSongsFragment(this);

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
                            .replace(R.id.framesong, mediaPlaybackFragment)
                            .addToBackStack("allsongsfragment").commit();
                }
            });
            if (mReplace == true) {
                update(mSongIndex);
                mReplace = false;
            }
        } else {
            update(mSongIndex);
            mRecyclerView.scrollToPosition(mSongIndex);
            mAdapter.setiOnClickSongListener(mIOnClickSongListener);
        }

        mAdapter.setOnItemClickListener(new AllSongsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                mSongIndex = position;
                int countOfPlay = mList.get(position).getCountOfPlay();
                mList.get(position).setCountOfPlay(++countOfPlay);
                if (mList.get(position).getCountOfPlay() >= 3 && mList.get(position).getIsFavorite() == 0) {
                    mList.get(position).setIsFavorite(2);
                    new StorageUtil(getContext()).storeSongList(mList);
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
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onStart() {
        super.onStart();
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
        getActivity().unregisterReceiver(broadcastReceiver);
        mReplace = true;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mLockScreen = true;
        }
    }


    /**
     * Nhận BroadcastReceiver tu service
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

    /**
     * Cập nhật lại UI
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