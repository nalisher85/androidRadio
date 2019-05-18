package com.startandroid.admin.myaudioplayer.client;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import androidx.media.MediaBrowserServiceCompat;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.IMediaControllerCallback;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;


import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.AudioModel;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;
import com.startandroid.admin.myaudioplayer.ui.PlayerState;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MediaBrowserClient {

    private String LOG_TAG = MediaBrowserClient.class.getSimpleName();

    private final MediaBrowserConnectionCallback mMediaBrowserConnectionCallbacks;
    private final MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;
    private Context mContext;
    private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;
    private final List<MediaBrowserCallbacks> mCallbackList = new CopyOnWriteArrayList<>();

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private MediaControllerCallback mMediaControllerCallback;
    private PlayerState mPlayerState;

    public MediaBrowserClient(Context ctx,
                              Class<? extends MediaBrowserServiceCompat> serviceClss) {
        mContext = ctx.getApplicationContext();
        mMediaBrowserServiceClass = serviceClss;

        mMediaBrowserConnectionCallbacks = new MediaBrowserConnectionCallback();
        mMediaBrowserSubscriptionCallback = new MediaBrowserSubscriptionCallback();
        mMediaControllerCallback = new MediaControllerCallback();
        mPlayerState = new PlayerState();

    }

    private boolean isPlayerHasState(){
        return mMediaController != null && mMediaController.getMetadata() != null;
    }

    private void setPlayerState(){
        if (mMediaController.getMetadata() != null) {
            mPlayerState.setMetadata(mMediaController.getMetadata());
        }
        if (mMediaController.getPlaybackState() != null)
            mPlayerState.setPlaybackState(mMediaController.getPlaybackState());
        else mPlayerState.setPlaying(false);
    }

    public PlayerState getPlayerState() {
        return mPlayerState;
    }

    public void registerCallback(MediaBrowserCallbacks callback){
        mCallbackList.add(callback);
    }

    public MediaControllerCompat getMediaController(){
        return mMediaController;
    }

    public MediaControllerCompat.TransportControls getTransportControls () {
        return mMediaController.getTransportControls();
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

    public void addToQueueItems(List<AudioModel> mediaItems, boolean clearOldQueue) {
        if (clearOldQueue) clearPlayList();
        for (AudioModel audio : mediaItems) {
            addToQueueItems(buildMediaDescriptionsFrom(audio));
        }
        mMediaController.getTransportControls().prepare();
        if (clearOldQueue) mMediaController.getTransportControls().play();
    }

    public void addToQueueItems(RadioStationModel radioStationModel) {
        clearPlayList();
        addToQueueItems(buildMediaDescriptionsFrom(radioStationModel));
        mMediaController.getTransportControls().prepare();
        mMediaController.getTransportControls().play();
    }

    private void addToQueueItems(MediaDescriptionCompat mediaDescription) {
        mMediaController.addQueueItem(mediaDescription);
    }

    private void clearPlayList () {
        if (mMediaController.getQueue() == null) return;
        for (MediaSessionCompat.QueueItem queueItem : mMediaController.getQueue()) {
            mMediaController.removeQueueItem(queueItem.getDescription());
        }
    }

    private MediaDescriptionCompat buildMediaDescriptionsFrom(AudioModel audioModel){
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                .setMediaId(audioModel.getId())
                .setTitle(audioModel.getName())
                .setSubtitle(audioModel.getArtist())
                .setMediaUri(Uri.parse(audioModel.getPath()));
        return builder.build();
    }

    private MediaDescriptionCompat buildMediaDescriptionsFrom(RadioStationModel radioStation){
        Uri u = makeUriFromUrl(radioStation.getPath());
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                .setMediaId(""+radioStation.getId())
                .setTitle(radioStation.getStationName())
                .setMediaUri(makeUriFromUrl(radioStation.getPath()));
        return builder.build();
    }

    private Uri makeUriFromUrl(String urlString) {
        URL url = null;
        Uri.Builder builder = new Uri.Builder();
        try {
            urlString = URLDecoder.decode(urlString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (url != null) {
            builder.scheme(url.getProtocol())
                    .encodedAuthority(url.getAuthority())
                    .path(url.getPath());
        }
        return builder.build();
    }

    public void onMediaButtonClicked (int btnId) {
        switch (btnId) {
            case R.id.bottomsheet_prev_button:
                mMediaController.getTransportControls().skipToPrevious();
                break;
            case R.id.bottomsheet_peek_button:
            case R.id.audio_mediapanel_play_btn:
            case R.id.radio_mediapanel_play_btn:
                if (mPlayerState.isPlaying()) {
                    mMediaController.getTransportControls().pause();
                } else {
                    mMediaController.getTransportControls().play();
                }
                break;
            case R.id.bottomsheet_next_button:
                mMediaController.getTransportControls().skipToNext();
                break;
        }
    }

    //----------------------------------------------------------------------

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

            if (isPlayerHasState()) setPlayerState();
            if (!mCallbackList.isEmpty()) {
                for (MediaBrowserCallbacks callback : mCallbackList) {
                    callback.onConnected();
                }
            }
        }

        @Override
        public void onConnectionSuspended() {
            Log.d("myLog", "MediaBrowser -> MediaBrowserConnectionCallback -> onConnectionSuspended");
            super.onConnectionSuspended();
            if (!mCallbackList.isEmpty()) {
                for (MediaBrowserCallbacks callback : mCallbackList) {
                    callback.onConnectionSuspended();
                }
            }

        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
            Log.d(LOG_TAG, "onConnectionFailed");
            for (MediaBrowserCallbacks callback : mCallbackList) {
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

            for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                addToQueueItems(mediaItem.getDescription());
            }
            for (MediaBrowserCallbacks callback : mCallbackList) {
                if (callback != null) {
                    callback.onChildrenLoaded(children);
                }
            }
            mMediaController.getTransportControls().prepare();
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (state != null) {
                mPlayerState.setPlaybackState(state);
                for (MediaBrowserCallbacks callback : mCallbackList) {
                    if (callback != null) {
                        callback.onPlaybackStateChanged(state);
                    }
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata == null) return;
            mPlayerState.setMetadata(metadata);
            for (MediaBrowserCallbacks callback : mCallbackList) {
                if (callback != null) {
                    callback.onMetadataChanged(metadata);
                }
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

        /**
         * Override to handle changes to items in the queue.
         *
         * @param queue A list of items in the current play queue. It should
         *              include the currently playing item as well as previous and
         * @see MediaSessionCompat.QueueItem
         */
        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
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
            super.onExtrasChanged(extras);
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

    public interface MediaBrowserCallbacks {

        void onConnected();

        void onConnectionSuspended();

        void onConnectionFailed();

        void onChildrenLoaded(@NonNull List<MediaBrowserCompat.MediaItem> children);

        void onPlaybackStateChanged(PlaybackStateCompat state);

        void onMetadataChanged(MediaMetadataCompat metadata);

        void onSessionReady();

        void onSessionDestroyed();

        void onSessionEvent(String event, Bundle extras);

        void onQueueChanged(List<MediaSessionCompat.QueueItem> queue);

       void onQueueTitleChanged(CharSequence title);

       void onExtrasChanged(Bundle extras);

        void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info);

       void onCaptioningEnabledChanged(boolean enabled);

       void onRepeatModeChanged(int repeatMode);

       void onShuffleModeChanged(int shuffleMode);

    }


}
