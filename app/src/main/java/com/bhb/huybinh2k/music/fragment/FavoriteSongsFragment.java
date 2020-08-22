package com.bhb.huybinh2k.music.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;

import java.util.List;

public class FavoriteSongsFragment extends BaseSongListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        List<Song> mListSong = new StorageUtil(getContext()).loadSongList();
        for (int i=0;i<mListSong.size();i++){
            if (mListSong.get(i).getIsFavorite()==2){
                mListSong.get(i).setId(i+1);
                mList.add(mListSong.get(i));
            }
        }
        super.onViewCreated(view, savedInstanceState);
    }
}