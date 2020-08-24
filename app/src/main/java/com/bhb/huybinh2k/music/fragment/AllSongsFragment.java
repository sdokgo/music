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

public class AllSongsFragment extends BaseSongListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mList.clear();
        getSongList();
        mAdapter = new AllSongsAdapter(getContext(), R.layout.list_music, mList, false);
        super.onViewCreated(view, savedInstanceState);
        int i = new StorageUtil(getContext()).loadSongIndex();
        if (i != -1) update(i);
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
                    int idProvider = musicCursor.getInt(
                            musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                    Uri sArtworkUri = Uri
                            .parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                    String albumArt = String.valueOf(albumArtUri);

                    Long milliseconds = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    mList.add(new Song(id, idProvider, thisTitle, songpath, thisArtist, albumArt, milliseconds));
                    id++;

                }
                while (musicCursor.moveToNext());
            }
        }
    }

}