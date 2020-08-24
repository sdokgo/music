package com.bhb.huybinh2k.music;

import java.io.Serializable;

public class Song implements Serializable {
    private int id;
    private int idProvider;
    private String songName;
    private String songPath;
    private String imgPath;
    private String artist;
    private long duration;
    private int isFavorite = 0;
    private int countOfPlay = 0;

    public Song(int id, int idProvider, String songName, String songPath, String artist, String img, long duration) {
        this.id = id;
        this.idProvider = idProvider;
        this.songName = songName;
        this.songPath = songPath;
        this.artist = artist;
        this.imgPath = img;
        this.duration = duration;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdProvider() {
        return idProvider;
    }


    public String getSongName() {
        return songName;
    }


    public String getSongPath() {
        return songPath;
    }


    public String getImg() {
        return imgPath;
    }


    public String getArtist() {
        return artist;
    }


    public long getDuration() {
        return duration;
    }


    public int getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(int isFavorite) {
        this.isFavorite = isFavorite;
    }

    public int getCountOfPlay() {
        return countOfPlay;
    }

    public void setCountOfPlay(int countOfPlay) {
        this.countOfPlay = countOfPlay;
    }
}
