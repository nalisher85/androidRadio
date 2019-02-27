package com.startandroid.admin.myaudioplayer.service;

import android.support.v4.media.session.PlaybackStateCompat;

public abstract class PlayBackInfoListener {

    public abstract void onPlaybackStateChange(PlaybackStateCompat state);

    public void onPlaybackCompleted() {}
    }
