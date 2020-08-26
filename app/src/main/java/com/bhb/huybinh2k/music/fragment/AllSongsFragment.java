package com.bhb.huybinh2k.music.fragment;


import android.content.ContentResolver;
import android.content.ContentUris;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.adapter.AllSongsAdapter;
import com.bhb.huybinh2k.music.database.SongDatabaseHelper;


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
        getSongList();
        mList = favoriteSongsProvider.listAllSongs();
        mAdapter = new AllSongsAdapter(getContext(), mList, false);
        super.onViewCreated(view, savedInstanceState);
        int i = new StorageUtil(getContext()).loadSongIndex();
        if (i != -1) update(i);
        clickSong();
    }

    /**
     * Đọc dữ liệu trong máy và add vào list
     */
    public void getSongList() {
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
                    int idProvider = musicCursor.getInt(
                            musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                    Uri sArtworkUri = Uri
                            .parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                    String albumArt = String.valueOf(albumArtUri);

                    Long milliseconds = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    favoriteSongsProvider.insert(
                            new Song(id, idProvider, thisTitle, songpath, thisArtist, albumArt, milliseconds)
                    );
                    id++;

                }
                while (musicCursor.moveToNext());
            }
        }
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