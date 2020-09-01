package com.bhb.huybinh2k.music.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.adapter.SongsAdapter;
import com.bhb.huybinh2k.music.database.FavoriteSongsProvider;

import java.util.List;


public class SearchFragment extends BaseSongListFragment {
    private SearchView mSearchView;
    private List<Song> mListAllSong;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mFavoriteSongsProvider = new FavoriteSongsProvider(getContext());
        mSearchView = view.findViewById(R.id.searchview);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mListAllSong = mFavoriteSongsProvider.listAllSongs();

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

        mAdapter.setOnItemClickListener(new SongsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                int index = mList.get(position).getId() - 1;
                mSongIndex = position;
                int countOfPlay = mList.get(position).getCountOfPlay();
                mList.get(position).setCountOfPlay(++countOfPlay);
                if (mList.get(position).getCountOfPlay() == 3 &&
                        mList.get(position).getIsFavorite() == MediaPlaybackFragment.DEFAULT_FAVORITE) {
                    mList.get(position).setIsFavorite(MediaPlaybackFragment.SET_FAVORITE);
                    mFavoriteSongsProvider.update(mList.get(position));
                }
                mActivityMusic.playAudio(index, mListAllSong);
                mActivityMusic.setmIsPlaying(true);
                getFragmentManager().popBackStack();
                if (mActivityMusic.getmFavorite()) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame_song, new AllSongsFragment())
                            .commit();
                }
//                updateUI(index);
            }
        });
    }

    //    public void updateUI(int songIndex) {
//        mAdapter.setmPlayingIdProvider(listSerach.get(songIndex).getIdProvider());
//        mAdapter.notifyDataSetChanged();
//        mRecyclerView.scrollToPosition(songIndex);
//        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
//            if (mSongIndex != -1) {
//                mPlayBar.setVisibility(View.VISIBLE);
//                mActivityMusic.updateUIPlayBar(songIndex);
//            }
//        }
//    }
}