package com.bhb.huybinh2k.music.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.adapter.SongsAdapter;

import java.util.List;


public class FavoriteSongsFragment extends BaseSongListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mList = favoriteSongsProvider.listFavorite();
        if (mList.size() == 0)
            Toast.makeText(getContext(), "Hãy thêm bài hát yêu thích", Toast.LENGTH_SHORT).show();
        mAdapter = new SongsAdapter(getContext(), mList, true);

        super.onViewCreated(view, savedInstanceState);

        int i = new StorageUtil(getContext()).loadSongIndex();
        List<Song> listPlaying = new StorageUtil(getContext()).loadSongListPlaying();
        if (mActivityMusic.getmFavorite() == 1 && listPlaying.size() != favoriteSongsProvider.listFavorite().size()) {
            mList = favoriteSongsProvider.listFavorite();
        }
        if (i != -1) update(i);
        clickSong();
    }

}