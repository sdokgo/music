package com.bhb.huybinh2k.music.fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.adapter.SongsAdapter;
import com.bhb.huybinh2k.music.database.FavoriteSongsProvider;

import java.util.List;


public class AllSongsFragment extends BaseSongListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("log", "onCreateViewAllSongsFragment");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d("log", "onCreateViewAllSongsFragment");
        mList.clear();
        mList = favoriteSongsProvider.listAllSongs();
        mAdapter = new SongsAdapter(getContext(), mList, false);

        super.onViewCreated(view, savedInstanceState);

        int i = new StorageUtil(getContext()).loadSongIndex();
        List<Song> listPlaying = new StorageUtil(getContext()).loadSongListPlaying();
        if (mActivityMusic.getmFavorite()!=1 && listPlaying.size()!= favoriteSongsProvider.listAllSongs().size()){
            mList = favoriteSongsProvider.listAllSongs();
        }
        if (i != -1 && listPlaying.size() == mList.size()) update(i);
        clickSong();
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d("log", "onStartAllSongsFragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("log", "onStopAllSongsFragment");
    }
}