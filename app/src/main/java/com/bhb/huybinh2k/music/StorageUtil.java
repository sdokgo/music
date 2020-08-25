package com.bhb.huybinh2k.music;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class StorageUtil {
    private static final String STORAGE = "com.bhb.huybinh2k.STORAGE";
    private static final String SHUFFLE = "com.bhb.huybinh2k.SHUFFLE";
    private static final String REPEAT = "com.bhb.huybinh2k.REPEAT";
    private static final String SONG_LIST = "com.bhb.huybinh2k.SONG_LIST";
    private static final String LIST_SONG_PLAYING = "com.bhb.huybinh2k.LIST_SONG_PLAYING";
    private static final String SONG_INDEX = "com.bhb.huybinh2k.SONG_INDEX";
    private static final String IS_FAVORITE = "com.bhb.huybinh2k.IS_FAVORITE";
    private SharedPreferences mPreferences;
    private Context mContext;

    public StorageUtil(Context context) {
        this.mContext = context;
    }

    public void storeSongList(List<Song> list) {
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(SONG_LIST, json);
        editor.apply();
    }
    public List<Song> loadSongList() {
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPreferences.getString(SONG_LIST, null);
        Type type = new TypeToken<List<Song>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeIsFavorite(int i){
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(IS_FAVORITE,i);
        editor.apply();
    }
    public int loadIsFavorite(){
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return mPreferences.getInt(IS_FAVORITE,-1);
    }



    public void storeSongListPlaying(List<Song> list) {
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(LIST_SONG_PLAYING, json);
        editor.apply();
    }

    public List<Song> loadSongListPlaying() {
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPreferences.getString(LIST_SONG_PLAYING, null);
        Type type = new TypeToken<List<Song>>() {
        }.getType();
        return gson.fromJson(json, type);
    }



    public void storeSongIndex(int index) {
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(SONG_INDEX, index);
        editor.apply();
    }

    public int loadSongIndex() {
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return mPreferences.getInt(SONG_INDEX, -1);
    }

    public void storeShuffle(int x) {
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(SHUFFLE, x);
        editor.apply();
    }

    public int loadShuffle() {
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return mPreferences.getInt(SHUFFLE, -1);
    }

    public void storeRepeat(int x) {
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(REPEAT, x);
        editor.apply();
    }

    public int loadRepeat() {
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return mPreferences.getInt(REPEAT, -1);
    }

    public void clearSongList() {
        mPreferences = mContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(SONG_INDEX);
        editor.remove(SONG_LIST);
        editor.commit();
    }
}
