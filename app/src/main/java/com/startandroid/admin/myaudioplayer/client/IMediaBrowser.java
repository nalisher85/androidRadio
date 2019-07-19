package com.startandroid.admin.myaudioplayer.client;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.startandroid.admin.myaudioplayer.data.model.MediaType;

public interface IMediaBrowser {

    void connect();

    void disconnect();

    MediaControllerSubscription getMediaControllerSubscription();

    MediaMetadataCompat getCurrantMetadata();

    void registerConnectionCallback(ConnectionCallback callback);

    void addQueueItem(MediaDescriptionCompat mediaDescription);

    void removeQueueItem(MediaDescriptionCompat description);

    void clearPlayList();

    void prepare();

    void play();

    void pause();

    void playPause();

    void seekTo(int position);

    void skipToNext();

    void skipToPrevious();

    void skipToQueueItem(long id);

    void setRepeatMode(int repeatMode);

    void setShuffleMode(int shuffleMode);

    MediaType currantMediaType();

    //---------------------------
    interface ConnectionCallback {

        void onConnected();

        void onConnectionSuspended();

        void onConnectionFailed();
    }

}
