package com.bhb.huybinh2k.music.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SongDatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "favorite_db";

    static final String DB_TABLE = "favorite_tb";
    public static final String ID = "song_id";
    public static final String ID_PROVIDER = "song_id_provider";
    public static final String SONG_NAME = "song_name";
    public static final String SONG_PATH = "song_path";
    public static final String SONG_ARTIST = "song_artist";
    public static final String IMAGE_PATH = "image_path";
    public static final String DURATION = "duration";
    public static final String FAVORITE = "favorite";
    public static final String COUNT_OF_PLAY = "count";

    public SongDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE " + DB_TABLE + "(" +
                ID + " INTEGER  PRIMARY KEY AUTOINCREMENT," +
                ID_PROVIDER + " INTEGER UNIQUE," +
                SONG_NAME + " TEXT," +
                SONG_PATH + " TEXT," +
                SONG_ARTIST + " TEXT," +
                IMAGE_PATH + " TEXT," +
                DURATION + " LONG," +
                FAVORITE + " INTEGER," +
                COUNT_OF_PLAY + " INTEGER);";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
