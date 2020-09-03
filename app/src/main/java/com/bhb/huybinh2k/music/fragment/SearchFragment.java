package com.bhb.huybinh2k.music.fragment;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.activity.ActivityMusic;
import com.bhb.huybinh2k.music.adapter.SongsAdapter;
import com.bhb.huybinh2k.music.database.FavoriteSongsProvider;

import java.util.List;


public class SearchFragment extends BaseSongListFragment {
    private SearchView mSearchView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mFavoriteSongsProvider = new FavoriteSongsProvider(getContext());
        mSearchView = view.findViewById(R.id.searchview);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivityMusic.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mList.clear();
                List<Song> list = new FavoriteSongsProvider(getContext()).searchSongByName(s);
                mList.addAll(list);
                if (mList.isEmpty()) {
                    Toast.makeText(getContext(), R.string.no_search_results, Toast.LENGTH_SHORT).show();
                }
                mAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mAdapter = new SongsAdapter(getContext(), mList, false);
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        clickSong();
    }

    public void update(int songIndex) {
        if (!mList.isEmpty()){
            mAdapter.setmPlayingIdProvider(mList.get(songIndex).getIdProvider());
        }
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(songIndex);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mSongIndex != ActivityMusic.DEFAULT_VALUE) {
                mPlayBar.setVisibility(View.VISIBLE);
                mActivityMusic.updateUIPlayBar(songIndex);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivityMusic.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
}