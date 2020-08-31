package com.bhb.huybinh2k.music.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bhb.huybinh2k.music.Song;

import java.util.ArrayList;

public class FavoriteSongsProvider extends ContentProvider {
    private final SQLiteDatabase mDatabase;
    public FavoriteSongsProvider(Context context) {
        SongDatabaseHelper helper = new SongDatabaseHelper(context);
        this.mDatabase = helper.getWritableDatabase();
    }
//    public static final String ALL_SONGS_TABLE = SongDatabaseHelper.ALL_SONGS_TABLE;
//    public static final String ID = SongDatabaseHelper.ID;
//    public static final String ID_PROVIDER = SongDatabaseHelper.ID_PROVIDER;
//    public static final String SONG_NAME = SongDatabaseHelper.SONG_NAME;
//    public static final String SONG_PATH = SongDatabaseHelper.SONG_PATH;
//    public static final String SONG_ARTIST = SongDatabaseHelper.SONG_ARTIST;
//    public static final String IMAGE_PATH = SongDatabaseHelper.IMAGE_PATH;
//    public static final String DURATION = SongDatabaseHelper.DURATION;
//    public static final String FAVORITE = SongDatabaseHelper.FAVORITE;
//    public static final String COUNT_OF_PLAY = SongDatabaseHelper.COUNT_OF_PLAY;

    public void insert(Song song)
    {
        ContentValues values = new ContentValues();
        values.put(SongDatabaseHelper.ID_PROVIDER,song.getIdProvider());
        values.put(SongDatabaseHelper.SONG_NAME,song.getSongName());
        values.put(SongDatabaseHelper.SONG_PATH,song.getSongPath());
        values.put(SongDatabaseHelper.SONG_ARTIST,song.getArtist());
        values.put(SongDatabaseHelper.IMAGE_PATH,song.getImg());
        values.put(SongDatabaseHelper.DURATION,song.getDuration());
        values.put(SongDatabaseHelper.FAVORITE,song.getIsFavorite());
        values.put(SongDatabaseHelper.COUNT_OF_PLAY,song.getCountOfPlay());
        mDatabase.insert(SongDatabaseHelper.ALL_SONGS_TABLE,null,values);
    }

    public ArrayList<Song> listAllSongs()
    {
        ArrayList<Song> list = new ArrayList<>();

        String sql ="SELECT * FROM "+ SongDatabaseHelper.ALL_SONGS_TABLE;
        Cursor c = mDatabase.rawQuery(sql, null);
        while (c.moveToNext())
        {
            int id = c.getInt(c.getColumnIndex(SongDatabaseHelper.SONG_ID));
            int idProvider = c.getInt(c.getColumnIndex(SongDatabaseHelper.ID_PROVIDER));
            String songName = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_NAME));
            String songPath = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_PATH));
            String songArtist = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_ARTIST));
            String imagePath = c.getString(c.getColumnIndex(SongDatabaseHelper.IMAGE_PATH));
            long duration = c.getLong(c.getColumnIndex(SongDatabaseHelper.DURATION));
            int isFavorite = c.getInt(c.getColumnIndex(SongDatabaseHelper.FAVORITE));
            int count = c.getInt(c.getColumnIndex(SongDatabaseHelper.COUNT_OF_PLAY));
            Song song = new Song(id,idProvider,songName,songPath,imagePath,songArtist,duration,isFavorite,count);
            list.add(song);
        }
        c.close();
        return list;
    }
    public ArrayList<Song> searchSongByName(String s)
    {
        ArrayList<Song> list = new ArrayList<>();
        String[] args = {"%"+s+"%"};
        Cursor c = mDatabase.query(SongDatabaseHelper.ALL_SONGS_TABLE, new String[]{
                SongDatabaseHelper.SONG_ID,
                SongDatabaseHelper.ID_PROVIDER,
                SongDatabaseHelper.SONG_NAME,
                SongDatabaseHelper.SONG_PATH,
                SongDatabaseHelper.SONG_ARTIST,
                SongDatabaseHelper.IMAGE_PATH,
                SongDatabaseHelper.DURATION,
                SongDatabaseHelper.FAVORITE,
                SongDatabaseHelper.COUNT_OF_PLAY
        },SongDatabaseHelper.SONG_NAME + " LIKE ?",args,null,null,null,null);
        while (c.moveToNext())
        {
            int id = c.getInt(c.getColumnIndex(SongDatabaseHelper.SONG_ID));
            int idProvider = c.getInt(c.getColumnIndex(SongDatabaseHelper.ID_PROVIDER));
            String songName = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_NAME));
            String songPath = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_PATH));
            String songArtist = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_ARTIST));
            String imagePath = c.getString(c.getColumnIndex(SongDatabaseHelper.IMAGE_PATH));
            long duration = c.getLong(c.getColumnIndex(SongDatabaseHelper.DURATION));
            int isFavorite = c.getInt(c.getColumnIndex(SongDatabaseHelper.FAVORITE));
            int count = c.getInt(c.getColumnIndex(SongDatabaseHelper.COUNT_OF_PLAY));
            Song song = new Song(id,idProvider,songName,songPath,imagePath,songArtist,duration,isFavorite,count);
            list.add(song);
        }
        c.close();
        return list;
    }

    public ArrayList<Song> listFavorite(){
        ArrayList<Song> list = new ArrayList<>();

        String sql ="SELECT * FROM "+ SongDatabaseHelper.ALL_SONGS_TABLE +" WHERE "+SongDatabaseHelper.FAVORITE+"=?";
        String[] args = {"2"};
        Cursor c = mDatabase.rawQuery(sql, args);
        while (c.moveToNext())
        {
            int id = c.getInt(c.getColumnIndex(SongDatabaseHelper.SONG_ID));
            int idProvider = c.getInt(c.getColumnIndex(SongDatabaseHelper.ID_PROVIDER));
            String songName = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_NAME));
            String songPath = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_PATH));
            String songArtist = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_ARTIST));
            String imagePath = c.getString(c.getColumnIndex(SongDatabaseHelper.IMAGE_PATH));
            long duration = c.getLong(c.getColumnIndex(SongDatabaseHelper.DURATION));
            int isFavorite = c.getInt(c.getColumnIndex(SongDatabaseHelper.FAVORITE));
            int count = c.getInt(c.getColumnIndex(SongDatabaseHelper.COUNT_OF_PLAY));
            Song song = new Song(id,idProvider,songName,songPath,imagePath,songArtist,duration,isFavorite,count);
            list.add(song);
        }
        c.close();
        return list;
    }

    public Song getSongByIdProvider(int idprovider){
        Song song = null;
        String sql ="SELECT * FROM "+ SongDatabaseHelper.ALL_SONGS_TABLE +" WHERE "+SongDatabaseHelper.ID_PROVIDER+"=?";
        String[] args = {String.valueOf(idprovider)};
        Cursor c = mDatabase.rawQuery(sql, args);
        while (c.moveToNext())
        {
            int id = c.getInt(c.getColumnIndex(SongDatabaseHelper.SONG_ID));
            int idProvider = c.getInt(c.getColumnIndex(SongDatabaseHelper.ID_PROVIDER));
            String songName = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_NAME));
            String songPath = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_PATH));
            String songArtist = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_ARTIST));
            String imagePath = c.getString(c.getColumnIndex(SongDatabaseHelper.IMAGE_PATH));
            long duration = c.getLong(c.getColumnIndex(SongDatabaseHelper.DURATION));
            int isFavorite = c.getInt(c.getColumnIndex(SongDatabaseHelper.FAVORITE));
            int count = c.getInt(c.getColumnIndex(SongDatabaseHelper.COUNT_OF_PLAY));
            song = new Song(id,idProvider,songName,songPath,imagePath,songArtist,duration,isFavorite,count);
        }
        c.close();
        return song;
    }


    public void update(Song song)
    {
        ContentValues values = new ContentValues();
        values.put(SongDatabaseHelper.COUNT_OF_PLAY,song.getCountOfPlay());
        values.put(SongDatabaseHelper.FAVORITE,song.getIsFavorite());
        String whereClause = SongDatabaseHelper.ID_PROVIDER+"=?";
        String[] whereArgs = {String.valueOf(song.getIdProvider())};
        mDatabase.update(SongDatabaseHelper.ALL_SONGS_TABLE,values,whereClause,whereArgs);
    }

//    public void delete(int idProvider)
//    {
//        String whereClause = SongDatabaseHelper.ID_PROVIDER+ "=?";
//        String whereArgs[] = {String.valueOf(idProvider)};
//        mDatabase.delete(SongDatabaseHelper.ALL_SONGS_TABLE,whereClause,whereArgs);
//
//    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
