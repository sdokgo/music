package com.bhb.huybinh2k.music.fragment;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bhb.huybinh2k.music.LogSetting;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.activity.ActivityMusic;
import com.bhb.huybinh2k.music.adapter.SongsAdapter;

import java.util.List;


public class AllSongsFragment extends BaseSongListFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (LogSetting.IS_DEBUG) {
            Log.d(ActivityMusic.TAG, "onCreateViewAllSongsFragment");
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (LogSetting.IS_DEBUG) {
            Log.d(ActivityMusic.TAG, "onCreateViewAllSongsFragment");
        }
        mList.clear();
        mList = mFavoriteSongsProvider.listAllSongs();
        mAdapter = new SongsAdapter(getContext(), mList, false);

        super.onViewCreated(view, savedInstanceState);

        int index = new StorageUtil(getContext()).loadSongIndex();
        List<Song> listPlaying = new StorageUtil(getContext()).loadListSongPlaying();
        if (listPlaying != null) {
            if (!mActivityMusic.getmFavorite() && listPlaying.size() != mFavoriteSongsProvider.listAllSongs().size()) {
                mList = mFavoriteSongsProvider.listAllSongs();
            }
        }

        if (index != ActivityMusic.DEFAULT_VALUE && listPlaying.size() == mList.size()) update(index);
        clickSong();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (LogSetting.IS_DEBUG) {
            Log.d(ActivityMusic.TAG, "onStartAllSongsFragment");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (LogSetting.IS_DEBUG) {
            Log.d(ActivityMusic.TAG, "onStopAllSongsFragment");
        }
    }
}