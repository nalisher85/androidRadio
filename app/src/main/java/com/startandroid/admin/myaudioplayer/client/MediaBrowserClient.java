package com.startandroid.admin.myaudioplayer.client;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
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

public class MediaBrowserClient {

    private String LOG_TAG = MediaBrowserClient.class.getSimpleName();

    private final MediaBrowserConnectionCallback mMediaBrowserConnectionCallbacks;
    private final MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;
    private final Context mContext;
    private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;


    public MediaBrowserClient(Context ctx,
                              Class<? extends MediaBrowserServiceCompat> serviceClss) {
        mContext = ctx;
        mMediaBrowserServiceClass = serviceClss;

        mMediaBrowserConnectionCallbacks = new MediaBrowserConnectionCallback();
        mMediaBrowserSubscriptionCallback = new MediaBrowserSubscriptionCallback();


    }

    public MediaControllerCompat getMediaController(){
        return mMediaController;
    }

    public MediaControllerCompat.TransportControls getTransportControls () {
        return mMediaController.getTransportControls();
    }

    public void connect() {
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
        if (mMediaController != null) {
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
            MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

            try {
                mMediaController =
                        new MediaControllerCompat(mContext, token);
            } catch (RemoteException e) {
                Log.d(LOG_TAG, "Variable Context is null \n" + e.toString());
            }
            mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mMediaBrowserSubscriptionCallback);
        }

        @Override
        public void onConnectionSuspended() {
            super.onConnectionSuspended();
            Log.d(LOG_TAG, "onConnectionSuspended");
        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
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

}
