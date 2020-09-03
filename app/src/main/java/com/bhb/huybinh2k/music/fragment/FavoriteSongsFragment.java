package com.bhb.huybinh2k.music.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.activity.ActivityMusic;
import com.bhb.huybinh2k.music.adapter.SongsAdapter;

import java.util.List;


public class FavoriteSongsFragment extends BaseSongListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mList = mFavoriteSongsProvider.listFavorite();
        if (mList.isEmpty())
            Toast.makeText(getContext(), R.string.add_song_to_favorite, Toast.LENGTH_SHORT).show();
        mAdapter = new SongsAdapter(getContext(), mList, true);

        super.onViewCreated(view, savedInstanceState);

        int index = new StorageUtil(getContext()).loadSongIndex();
        List<Song> listPlaying = new StorageUtil(getContext()).loadListSongPlaying();
        if (listPlaying != null) {
            if (mActivityMusic.isFavoriteFragment && listPlaying.size() != mFavoriteSongsProvider.listFavorite().size()) {
                mList = mFavoriteSongsProvider.listFavorite();
            }
        }
        if (index != ActivityMusic.DEFAULT_VALUE) update(index);
        clickSong();
    }

}