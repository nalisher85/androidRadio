package com.startandroid.admin.myaudioplayer.model;

public class AudioModel {

    private String mName;
    private String mSinger;
    private String mSongTitle;
    private String mAudioLink;
    private int mIcon;

    public AudioModel(String name, String singer, String song, int icon){
        mName = name;
        mSinger = singer;
        mSongTitle = song;
        mIcon = icon;
    }

    public String getName() {
        return mName;
    }

    public String getSinger() {
        return mSinger;
    }

    public String getSong() {
        return mSongTitle;
    }

    public int getIcon() {
        return mIcon;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public void setSinger(String mSinger) {
        this.mSinger = mSinger;
    }

    public void setSong(String mSong) {
        this.mSongTitle = mSong;
    }

    public void setIcon(int mIcon) {
        this.mIcon = mIcon;
    }

    public String getAudioLink() {
        return mAudioLink;
    }

    public void setAudioLink(String mAudioLink) {
        this.mAudioLink = mAudioLink;
    }
}
