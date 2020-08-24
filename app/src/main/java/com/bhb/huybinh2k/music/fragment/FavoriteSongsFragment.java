package com.bhb.huybinh2k.music.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.adapter.AllSongsAdapter;

import java.util.List;

public class FavoriteSongsFragment extends BaseSongListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mList.clear();
        mList = favoriteSongDB.read();
        mAdapter = new AllSongsAdapter(getContext(), R.layout.list_music, mList, true);
        super.onViewCreated(view, savedInstanceState);
        int i = new StorageUtil(getContext()).loadSongIndex();
        if (i != -1) update(i);
    }
}