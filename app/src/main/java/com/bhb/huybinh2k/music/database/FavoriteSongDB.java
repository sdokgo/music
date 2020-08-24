package com.bhb.huybinh2k.music.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.bhb.huybinh2k.music.Song;

import java.util.ArrayList;

public class FavoriteSongDB {
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public FavoriteSongDB(Context mContext) {
        this.mContext = mContext;
        SongDatabaseHelper helper = new SongDatabaseHelper(mContext);
        this.mDatabase = helper.getWritableDatabase();
    }

    public void insert(Song song)
    {
        ContentValues values = new ContentValues();
        values.put(SongDatabaseHelper.ID,song.getId());
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
            String songName = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_NAME));
            String songPath = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_PATH));
            String songArtist = c.getString(c.getColumnIndex(SongDatabaseHelper.SONG_ARTIST));
            String imagePath = c.getString(c.getColumnIndex(SongDatabaseHelper.IMAGE_PATH));
            Long duration = c.getLong(c.getColumnIndex(SongDatabaseHelper.DURATION));
            Song song = new Song(id,songName,songPath,songArtist,imagePath,duration);
            list.add(song);

        }
        return list;
    }

    public void delete(int id)
    {
        String whereClause = SongDatabaseHelper.ID+ "=?";
        String whereArgs[] = {String.valueOf(id)};
        mDatabase.delete(SongDatabaseHelper.DB_TABLE,whereClause,whereArgs);

    }
}
