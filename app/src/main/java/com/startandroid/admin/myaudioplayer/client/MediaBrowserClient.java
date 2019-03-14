package com.startandroid.admin.myaudioplayer.client;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;


import java.util.List;

import io.reactivex.subjects.BehaviorSubject;

public class MediaBrowserClient {

    private String LOG_TAG = MediaBrowserClient.class.getSimpleName();

    private final MediaBrowserConnectionCallback mMediaBrowserConnectionCallbacks;
    private final MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;
    private final Context mContext;
    private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;

    final private BehaviorSubject<Boolean> mOnConnectedObservable = BehaviorSubject.create();
    final private BehaviorSubject<PlaybackStateCompat> mPlaybackStateObservable = BehaviorSubject.create();
    final private BehaviorSubject<MediaMetadataCompat> mMediaMetadataObservable = BehaviorSubject.create();


    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private MediaControllerCallback mMediaControllerCallback;

    public MediaBrowserClient(Context ctx,
                              Class<? extends MediaBrowserServiceCompat> serviceClss) {
        mContext = ctx;
        mMediaBrowserServiceClass = serviceClss;

        mMediaBrowserConnectionCallbacks = new MediaBrowserConnectionCallback();
        mMediaBrowserSubscriptionCallback = new MediaBrowserSubscriptionCallback();
        mMediaControllerCallback = new MediaControllerCallback();

    }

    public MediaControllerCompat getMediaController(){
        return mMediaController;
    }

    public MediaControllerCompat.TransportControls getTransportControls () {
        return mMediaController.getTransportControls();
    }

    public BehaviorSubject<Boolean> getOnConnectedObservable() {
        return mOnConnectedObservable;
    }

    public BehaviorSubject<PlaybackStateCompat> getPlaybackStateObservable() {
        return mPlaybackStateObservable;
    }

    public BehaviorSubject<MediaMetadataCompat> getMediaMetadataObservable() {
        return mMediaMetadataObservable;
    }

    public void connect() {
        Log.d("myLog", "MediaBrowser -> connect");
        if (mMediaBrowser == null) {
            mMediaBrowser = new MediaBrowserCompat(
                    mContext,
                    new ComponentName(mContext, mMediaBrowserServiceClass),
                    mMediaBrowserConnectionCallbacks,
                    null);
            mMediaBrowser.connect();
        }
    }

    public void disconnect() {
        Log.d("myLog", "MediaBrowser -> disconnect");

        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaControllerCallback);
            mMediaController = null;
        }
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            mMediaBrowser.disconnect();
            mMediaBrowser = null;
        }
    }


    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            Log.d("myLog", "MediaBrowser -> MediaBrowserConnectionCallback -> onConnected");

            MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
            try {
                mMediaController =
                        new MediaControllerCompat(mContext, token);
                mMediaController.registerCallback(mMediaControllerCallback);
            } catch (RemoteException e) {
                Log.d(LOG_TAG, "Variable Context is null \n" + e.toString());
            }
            mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mMediaBrowserSubscriptionCallback);
            mOnConnectedObservable.onNext(true);
        }

        @Override
        public void onConnectionSuspended() {
            Log.d("myLog", "MediaBrowser -> MediaBrowserConnectionCallback -> onConnectionSuspended");

            super.onConnectionSuspended();
            mOnConnectedObservable.onNext(false);
            Log.d(LOG_TAG, "onConnectionSuspended");

        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
            mOnConnectedObservable.onNext(false);
            Log.d(LOG_TAG, "onConnectionFailed");

        }
    }

    private class MediaBrowserSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback {
        @Override
        public void onChildrenLoaded(@NonNull String parentId,
                                     @NonNull List<MediaBrowserCompat.MediaItem> children) {
            for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                mMediaController.addQueueItem(mediaItem.getDescription());
            }

            mMediaController.getTransportControls().prepare();
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            mPlaybackStateObservable.onNext(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata == null) return;
            mMediaMetadataObservable.onNext(metadata);
        }

    }


}
