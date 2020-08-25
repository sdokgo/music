package com.bhb.huybinh2k.music.fragment;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.bhb.huybinh2k.music.IOnClickSongListener;
import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.StorageUtil;
import com.bhb.huybinh2k.music.activity.ActivityMusic;
import com.bhb.huybinh2k.music.adapter.AllSongsAdapter;
import com.bhb.huybinh2k.music.database.FavoriteSongsProvider;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends BaseSongListFragment {
    private SearchView searchView;
    private List<Song> listSerach;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        favoriteSongsProvider = new FavoriteSongsProvider(getContext());
        searchView = view.findViewById(R.id.searchview);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listSerach = new StorageUtil(getContext()).loadSongList();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mList.clear();
                for (Song song : listSerach) {
                    if (song.getSongName().equalsIgnoreCase(s)
                            || song.getSongName().contains(s)) mList.add(song);
                }
                if (mList.size() == 0) {
                    Toast.makeText(getContext(), "Không tìm thấy bài hát cần tìm", Toast.LENGTH_SHORT).show();
                }
                if (mAdapter != null) mAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mAdapter = new AllSongsAdapter(getContext(), R.layout.list_music, mList, false);
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        mAdapter.setOnItemClickListener(new AllSongsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                int index = mList.get(position).getId() - 1;
                mSongIndex = position;
                int countOfPlay = mList.get(position).getCountOfPlay();
                mList.get(position).setCountOfPlay(++countOfPlay);
                if (mList.get(position).getCountOfPlay() == 3 && mList.get(position).getIsFavorite() == 0) {
                    mList.get(position).setIsFavorite(2);
                    favoriteSongsProvider.insert(mList.get(position));
                }
                mActivityMusic.playAudio(index, listSerach);
                mActivityMusic.setmIsPlaying(0);
//                getFragmentManager().popBackStack();
                updateUI(index);
            }
        });
    }

    public void updateUI(int songIndex) {
        mAdapter.setmPlayingIdProvider(listSerach.get(songIndex).getIdProvider());
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(songIndex);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mSongIndex != -1) {
                mPlayBar.setVisibility(View.VISIBLE);
                mActivityMusic.updateUIPlayBar(songIndex);
            }
        }
    }
}