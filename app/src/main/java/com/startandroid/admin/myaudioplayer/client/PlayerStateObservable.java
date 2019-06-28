package com.startandroid.admin.myaudioplayer.client;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

import io.reactivex.subjects.BehaviorSubject;

public class PlayerStateObservable {

    private BehaviorSubject<Boolean> mIsPlaying = BehaviorSubject.create();
    private BehaviorSubject<PlaybackStateCompat> mPlaybackState = BehaviorSubject.create();
    private BehaviorSubject<MediaMetadataCompat> mMetadata = BehaviorSubject.create();
    private BehaviorSubject<List<MediaSessionCompat.QueueItem>> mQueueItems = BehaviorSubject.create();
    private BehaviorSubject<Integer> mQueueIndex = BehaviorSubject.create();

    void setPlaying(boolean playing) {
        mIsPlaying.onNext(playing);
    }

    public BehaviorSubject<Boolean> getIsPlaying() {
        return mIsPlaying;
    }

    public BehaviorSubject<PlaybackStateCompat> getPlaybackState() {
        return mPlaybackState;
    }

    void setPlaybackState(PlaybackStateCompat playbackState) {
        mPlaybackState.onNext(playbackState);
        mIsPlaying.onNext(playbackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    public BehaviorSubject<MediaMetadataCompat> getMetadata() {
        return mMetadata;
    }

    void setMetadata(MediaMetadataCompat metadata) {
        mMetadata.onNext(metadata);
    }

    void setQueueItems (List<MediaSessionCompat.QueueItem> items) {
        mQueueItems.onNext(items);
    }

    public BehaviorSubject<List<MediaSessionCompat.QueueItem>> getQueueItems() {
        return mQueueItems;
    }

    public BehaviorSubject<Integer> getQueueIndex(){
        return mQueueIndex;
    }

    public void setQueueIndex(int index){
        mQueueIndex.onNext(index);
    }

}
