package com.bhb.huybinh2k.music.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SongDatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "song_database";
    static final String ALL_SONGS_TABLE = "all_song_table";
    static final String ID = "song_id";
    static final String ID_PROVIDER = "id_provider";
    static final String SONG_NAME = "song_name";
    static final String SONG_PATH = "song_path";
    static final String SONG_ARTIST = "song_artist";
    static final String IMAGE_PATH = "image_path";
    static final String DURATION = "duration";
    static final String FAVORITE = "favorite";
    static final String COUNT_OF_PLAY = "count";

    public SongDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sqlAllSongs = "CREATE TABLE " + ALL_SONGS_TABLE + "(" +
                ID + " INTEGER  PRIMARY KEY AUTOINCREMENT," +
                ID_PROVIDER + " INTEGER UNIQUE," +
                SONG_NAME + " TEXT," +
                SONG_PATH + " TEXT," +
                SONG_ARTIST + " TEXT," +
                IMAGE_PATH + " TEXT," +
                DURATION + " LONG," +
                FAVORITE + " INTEGER," +
                COUNT_OF_PLAY + " INTEGER);";
        sqLiteDatabase.execSQL(sqlAllSongs);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
