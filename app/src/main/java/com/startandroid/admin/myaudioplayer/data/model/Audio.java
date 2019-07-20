package com.startandroid.admin.myaudioplayer.data.model;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;

public class Audio {

    private String mId;
    private String mName;
    private String mArtist;
    private String mAlbum;
    @NonNull
    private String mPath;
    private long mDuration;

    public Audio(){
    }

    public Audio(String id, String name, String artist, String album, @NonNull String path, long duration){
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

    public void setName(String mName) {
        this.mName = mName;
    }

    public void setArtist(String mSinger) {
        this.mArtist = mSinger;
    }

    public void setAlbum(String mSong) {
        this.mAlbum = mSong;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mAudioLink) {
        mPath = mAudioLink;
    }

    public MediaMetadataCompat convertToMetadata(){
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, ""+mId)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mPath)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mName)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mArtist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mAlbum)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mDuration)
                .build();
    }

    public MediaDescriptionCompat convertToMediaDescription(){
        return convertToMetadata().getDescription();
    }
}
