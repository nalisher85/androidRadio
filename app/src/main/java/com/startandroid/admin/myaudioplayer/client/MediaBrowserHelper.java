package com.startandroid.admin.myaudioplayer.client;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.IMediaControllerCallback;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;


import com.startandroid.admin.myaudioplayer.MyApplication;
import com.startandroid.admin.myaudioplayer.data.model.MediaType;
import com.startandroid.admin.myaudioplayer.service.MediaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.media.MediaBrowserServiceCompat;

public class MediaBrowserHelper implements IMediaBrowser {

    private static MediaBrowserHelper INSTANCE = null;

    private final MediaBrowserConnectionCallback mMediaBrowserConnectionCallbacks;
    private final MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;
    private Context mContext;
    private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private MediaControllerCallback mMediaControllerCallback;
    private MediaControllerSubscription mMediaControllerSubscription;
    private List<ConnectionCallback> mConnCallbacks = new ArrayList<>();


    public MediaBrowserHelper(Class<? extends MediaBrowserServiceCompat> mediaBrowserServiceClass) {
        mContext = MyApplication.getContext();
        mMediaBrowserServiceClass = mediaBrowserServiceClass;

        mMediaBrowserConnectionCallbacks = new MediaBrowserConnectionCallback();
        mMediaBrowserSubscriptionCallback = new MediaBrowserSubscriptionCallback();
        mMediaControllerCallback = new MediaControllerCallback();
    }

    public static MediaBrowserHelper getInstance(Class<? extends MediaBrowserServiceCompat> mediaBrowserService){

        if (INSTANCE == null) {
            INSTANCE = new MediaBrowserHelper(mediaBrowserService);
        }
        return INSTANCE;
    }

    @Override
    public void connect() {
        Log.d("myLog1", "IMediaBrowser -> connect");
        mMediaControllerSubscription = new MediaControllerSubscription();
        if (mMediaBrowser == null) {
            mMediaBrowser = new MediaBrowserCompat(
                    mContext,
                    new ComponentName(mContext, mMediaBrowserServiceClass),
                    mMediaBrowserConnectionCallbacks,
                    null);
            mMediaBrowser.connect();
        }
    }

    @Override
    public void disconnect() {
        Log.d("myLog1", "IMediaBrowser -> disconnect");
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaControllerCallback);
            mMediaController = null;
            mMediaControllerSubscription = null;
        }
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            mMediaBrowser.disconnect();
            mMediaBrowser = null;
        }
        mConnCallbacks.clear();
    }

    @Override
    public MediaControllerSubscription getMediaControllerSubscription() {
        return mMediaControllerSubscription;
    }

    @Override
    public MediaMetadataCompat getCurrantMetadata() {
        return mMediaController.getMetadata();
    }

    @Override
    public void registerConnectionCallback(ConnectionCallback callback) {
        mConnCallbacks.add(callback);
    }

    @Override
    public void addQueueItem(MediaDescriptionCompat mediaDescription) {
        mMediaController.addQueueItem(mediaDescription);
    }

    @Override
    public void removeQueueItem(MediaDescriptionCompat description) {
        mMediaController.removeQueueItem(description);
    }

    @Override
    public void clearPlayList() {
        if (mMediaController == null || mMediaController.getQueue() == null) return;
        mMediaController.getTransportControls()
                .sendCustomAction(MediaService.USER_ACTION_CLEAR_PLAY_LIST, null);
    }

    @Override
    public void prepare() {
        mMediaController.getTransportControls().prepare();
    }

    @Override
    public void play() {
        mMediaController.getTransportControls().play();
    }

    @Override
    public void pause() {
        mMediaController.getTransportControls().pause();
    }

    @Override
    public void stop() {
        mMediaController.getTransportControls().stop();
    }

    @Override
    public void playPause() {
        if (mMediaController.getPlaybackState() == null
                || mMediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING
                || mMediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_BUFFERING) {

            mMediaController.getTransportControls().pause();

        } else {
            mMediaController.getTransportControls().play();
        }
    }

    @Override
    public void seekTo(int position) {
        mMediaController.getTransportControls().seekTo(position);
    }

    @Override
    public void skipToNext() {

        if (mMediaController != null && mMediaController.getQueue().size() > 1){
            mMediaController.getTransportControls().skipToNext();
        }
    }

    @Override
    public void skipToPrevious() {

        if (mMediaController != null && mMediaController.getQueue().size() > 1){
            mMediaController.getTransportControls().skipToPrevious();
        }
    }

    @Override
    public void skipToQueueItem(long id) {
        mMediaController.getTransportControls().skipToQueueItem(id);
    }

    @Override
    public void setRepeatMode(int repeatMode) {
        mMediaController.getTransportControls().setRepeatMode(repeatMode);
    }

    @Override
    public void setShuffleMode(int shuffleMode) {
        mMediaController.getTransportControls().setShuffleMode(shuffleMode);
    }

    @Override
    public MediaType currantMediaType() {
        if (mMediaController == null || mMediaController.getMetadata() == null) return null;
        MediaDescriptionCompat description = mMediaController.getMetadata().getDescription();

        if (description.getMediaUri() == null) return null;
        String uriScheme = description.getMediaUri().getScheme();

        if (Objects.equals(uriScheme, "http") || Objects.equals(uriScheme, "https")) {
            return MediaType.RADIO;
        } else {
            return MediaType.AUDIO;
        }
    }

    private void setUpMediaControllerSubscription(){
        if (mMediaController.getMetadata() != null) {
            mMediaControllerSubscription.setMetadata(mMediaController.getMetadata());
        }

        if (mMediaController.getPlaybackState() != null) {
            mMediaControllerSubscription.setPlaybackState(mMediaController.getPlaybackState());
        } else mMediaControllerSubscription.setPlaying(false);

        //Current playing position
        if (mMediaController.getExtras() != null){
            int queueIndex = mMediaController.getExtras().getInt(MediaService.KEY_QUEUE_INDEX);
            mMediaControllerSubscription.setQueueIndex(queueIndex);
        }

        int shuffleMode = mMediaController.getShuffleMode();
        mMediaControllerSubscription.setShuffleMode(shuffleMode);

        int repeatMode = mMediaController.getRepeatMode();
        mMediaControllerSubscription.setRepeatMode(repeatMode);

    }

    private boolean playerHasState(){
        return mMediaController != null && mMediaController.getMetadata() != null;
    }

    //-------------------------------------------------------------------------------------------

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            Log.d("myLog", "IMediaBrowser -> MediaBrowserConnectionCallback -> onConnected");

            MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

            try {
                mMediaController =
                        new MediaControllerCompat(mContext, token);
                mMediaController.registerCallback(mMediaControllerCallback);
            } catch (RemoteException e) {
                Log.d("myLog", "Variable Context is null \n" + e.toString());
            }
            mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mMediaBrowserSubscriptionCallback);

            setUpMediaControllerSubscription();

            if (!mConnCallbacks.isEmpty()) {
                for (ConnectionCallback callback : mConnCallbacks) {
                    callback.onConnected();
                }
            }
        }

        @Override
        public void onConnectionSuspended() {
            Log.d("myLog", "IMediaBrowser -> MediaBrowserConnectionCallback -> onConnectionSuspended");
            super.onConnectionSuspended();
            if (!mConnCallbacks.isEmpty()) {
                for (ConnectionCallback callback : mConnCallbacks) {
                    callback.onConnectionSuspended();
                }
            }

        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
            Log.d("myLog", "onConnectionFailed");
            for (ConnectionCallback callback : mConnCallbacks) {
                if (callback != null) {
                    callback.onConnectionFailed();
                }
            }
        }
    }

    private class MediaBrowserSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback {
        @Override
        public void onChildrenLoaded(@NonNull String parentId,
                                     @NonNull List<MediaBrowserCompat.MediaItem> children) {
            Log.d("myLog1", "IMediaBrowser -> onChildrenLoaded");

            if (mMediaController.getQueue().isEmpty()) {

                for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                    addQueueItem(mediaItem.getDescription());
                }

            } else mMediaControllerSubscription.setQueueItems(mMediaController.getQueue());

        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.d("myLog", "IMediaBrowser->MediaControllerCallback->onMetadataChanged hash-"+this.hashCode());

            if (metadata == null) return;
            mMediaControllerSubscription.setMetadata(metadata);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d("myLog", "IMediaBrowser->MediaControllerCallback->onPlaybackStateChanged="+state);

            if (state != null) {
                mMediaControllerSubscription.setPlaybackState(state);
            }
        }

        /**
         * Override to handle the session being ready.
         *
         * @see MediaControllerCompat#isSessionReady
         */
        @Override
        public void onSessionReady() {
            super.onSessionReady();
        }

        /**
         * Override to handle the session being destroyed. The session is no
         * longer valid after this call and calls to it will be ignored.
         */
        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        /**
         * Override to handle custom events sent by the session owner without a
         * specified interface. Controllers should only handle these for
         * sessions they own.
         *
         * @param event  The event from the session.
         * @param extras Optional parameters for the event.
         */
        @Override
        public void onSessionEvent(String event, Bundle extras) {
            super.onSessionEvent(event, extras);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            mMediaControllerSubscription.setQueueItems(queue);
        }

        /**
         * Override to handle changes to the queue title.
         *
         * @param title The title that should be displayed along with the play
         *              queue such as "Now Playing". May be null if there is no
         *              such title.
         */
        @Override
        public void onQueueTitleChanged(CharSequence title) {
            super.onQueueTitleChanged(title);
        }

        /**
         * Override to handle changes to the {@link MediaSessionCompat} extras.
         *
         * @param extras The extras that can include other information
         *               associated with the {@link MediaSessionCompat}.
         */
        @Override
        public void onExtrasChanged(Bundle extras) {
            if (extras.containsKey(MediaService.KEY_QUEUE_INDEX)) {
                mMediaControllerSubscription.setQueueIndex(extras.getInt(MediaService.KEY_QUEUE_INDEX));
            }
        }

        /**
         * Override to handle changes to the audio info.
         *
         * @param info The current audio info for this session.
         */
        @Override
        public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info) {
            super.onAudioInfoChanged(info);
        }

        /**
         * Override to handle changes to the captioning enabled status.
         *
         * @param enabled {@code true} if captioning is enabled, {@code false} otherwise.
         */
        @Override
        public void onCaptioningEnabledChanged(boolean enabled) {
            super.onCaptioningEnabledChanged(enabled);
        }

        /**
         * Override to handle changes to the repeat mode.
         *
         * @param repeatMode The repeat mode. It should be one of followings:
         *                   {@link PlaybackStateCompat#REPEAT_MODE_NONE},
         *                   {@link PlaybackStateCompat#REPEAT_MODE_ONE},
         *                   {@link PlaybackStateCompat#REPEAT_MODE_ALL},
         *                   {@link PlaybackStateCompat#REPEAT_MODE_GROUP}
         */
        @Override
        public void onRepeatModeChanged(int repeatMode) {
            super.onRepeatModeChanged(repeatMode);
            mMediaControllerSubscription.setRepeatMode(repeatMode);
        }

        /**
         * Override to handle changes to the shuffle mode.
         *
         * @param shuffleMode The shuffle mode. Must be one of the followings:
         *                    {@link PlaybackStateCompat#SHUFFLE_MODE_NONE},
         *                    {@link PlaybackStateCompat#SHUFFLE_MODE_ALL},
         *                    {@link PlaybackStateCompat#SHUFFLE_MODE_GROUP}
         */
        @Override
        public void onShuffleModeChanged(int shuffleMode) {
            super.onShuffleModeChanged(shuffleMode);
            mMediaControllerSubscription.setShuffleMode(shuffleMode);
        }

        /**
         * @hide
         */
        @SuppressLint("RestrictedApi")
        @Override
        public IMediaControllerCallback getIControllerCallback() {
            return super.getIControllerCallback();
        }


        @Override
        public void binderDied() {
            super.binderDied();
        }
    }


}
