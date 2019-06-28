package com.startandroid.admin.myaudioplayer.data;

import android.net.Uri;

import androidx.annotation.NonNull;

public class AudioModel {

    private String mId;
    private String mName;
    private String mArtist;
    private String mAlbum;
    @NonNull
    private String mPath;
    private long mDuration;
    private int mIcon;

    public AudioModel(){
    }

    public AudioModel(String id, String name, String artist, String album, @NonNull String path, long duration){
        mId = id;
        mName = name;
        mArtist = artist;
        mAlbum = album;
        mPath = path;
        mDuration = duration;
    }


    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(int mDuration) {
        this.mDuration = mDuration;
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
        mPath = mAudioLink;
    }
}
