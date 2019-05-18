package com.startandroid.admin.myaudioplayer.ui;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public class PlayerState {

    private boolean isPlaying = false;
    private PlaybackStateCompat playbackState;
    private MediaMetadataCompat metadata;

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public PlaybackStateCompat getPlaybackState() {
        return playbackState;
    }

    public void setPlaybackState(PlaybackStateCompat playbackState) {
        this.playbackState = playbackState;
        isPlaying = playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    public MediaMetadataCompat getMetadata() {
        return metadata;
    }

    public void setMetadata(MediaMetadataCompat metadata) {
        this.metadata = metadata;
    }
}
