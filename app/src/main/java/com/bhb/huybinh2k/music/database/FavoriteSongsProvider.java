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
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public FavoriteSongsProvider(Context mContext) {
        this.mContext = mContext;
        SongDatabaseHelper helper = new SongDatabaseHelper(mContext);
        this.mDatabase = helper.getWritableDatabase();
    }

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
        long newid = mDatabase.insert(SongDatabaseHelper.DB_TABLE,null,values);
    }

    public ArrayList<Song> read()
    {
        ArrayList<Song> list = new ArrayList<>();

        String sql ="SELECT * FROM "+ SongDatabaseHelper.DB_TABLE ;
        Cursor c = mDatabase.rawQuery(sql, null);
        while (c.moveToNext())
        {
            int id = c.getInt(c.getColumnIndex(SongDatabaseHelper.ID));
            int idProvider = c.getInt(c.getColumnIndex(SongDatabaseHelper.ID_PROVIDER));
            String songName = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_NAME));
            String songPath = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_PATH));
            String songArtist = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_ARTIST));
            String imagePath = c.getString(c.getColumnIndex(SongDatabaseHelper.IMAGE_PATH));
            Long duration = c.getLong(c.getColumnIndex(SongDatabaseHelper.DURATION));
            Song song = new Song(id,idProvider,songName,songPath,songArtist,imagePath,duration);
            list.add(song);

        }
        return list;
    }

    public void delete(int idProvider)
    {
        String whereClause = SongDatabaseHelper.ID_PROVIDER+ "=?";
        String whereArgs[] = {String.valueOf(idProvider)};
        mDatabase.delete(SongDatabaseHelper.DB_TABLE,whereClause,whereArgs);

    }

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
