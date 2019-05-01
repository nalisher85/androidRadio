package com.startandroid.admin.myaudioplayer.data;
public class AudioModel {

    private String mName;
    private String mArtist;
    private String mAlbum;
    private String mPath;
    private int mIcon;

    public AudioModel(String name, String artist, String album, String path){
        mName = name;
        mArtist = artist;
        mAlbum = album;
        mPath = path;
    }

    public String getName() {
        return mName;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public int getIcon() {
        return mIcon;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public void setArtist(String mSinger) {
        this.mArtist = mSinger;
    }

    public void setAlbum(String mSong) {
        this.mAlbum = mSong;
    }

    public void setIcon(int mIcon) {
        this.mIcon = mIcon;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mAudioLink) {
        this.mPath = mAudioLink;
    }
}
