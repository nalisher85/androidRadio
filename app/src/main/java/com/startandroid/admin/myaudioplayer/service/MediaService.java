package com.startandroid.admin.myaudioplayer.service;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.startandroid.admin.myaudioplayer.client.MediaBrowserClient;
import com.startandroid.admin.myaudioplayer.contentcatalogs.MusicLibrary;
import com.startandroid.admin.myaudioplayer.service.notifications.MediaNotificationManager;
import com.startandroid.admin.myaudioplayer.service.players.MediaPlayerAdapter;

import java.util.ArrayList;
import java.util.List;


public class MediaService extends MediaBrowserServiceCompat {

    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String LOG_TAG = MediaService.class.getSimpleName();

    private MediaSessionCompat mMediaSession;
    private PlayerAdapter mMediaPayer;
    private MediaNotificationManager mMediaNotificationManager;
    private boolean mServiceInStartedState;


    @Override
    public void onCreate() {
        super.onCreate();

        mMediaSession = new MediaSessionCompat(this, LOG_TAG);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionToken(mMediaSession.getSessionToken());
        mMediaSession.setCallback(new MediaSessionCallback());

        mMediaNotificationManager = new MediaNotificationManager(this);

        mMediaPayer = new MediaPlayerAdapter(this);

        /*
        mPlayBackStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(mPlayBackStateBuilder.build());
        */

    }

    @Override
    public void onDestroy() {
        mMediaNotificationManager.onDestroy();
        mMediaPayer.stop();
        mMediaSession.release();
        Log.d(LOG_TAG, "onDestroy: MediaPlayerAdapter stopped, and MediaSession released");
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String s, int i, @Nullable Bundle bundle) {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull String s, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(MusicLibrary.getMediaItems());
    }


    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        private final List<MediaSessionCompat.QueueItem> mPlayList = new ArrayList<>();
        private int mQueueIndex = -1;
        private MediaMetadataCompat mPreparedMedia;

        public MediaSessionCallback() {
            super();
        }


        @Override
        public void onSkipToNext() {
           mQueueIndex = mQueueIndex == (mPlayList.size()-1) ? 0 : ++mQueueIndex;
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            mQueueIndex = mQueueIndex <= 0 ? mPlayList.size() - 1 : --mQueueIndex;
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSeekTo (long pos) {
            mMediaPayer.seekTo(pos);
        }


        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            mPlayList.add(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mQueueIndex == -1) ? 0 : mQueueIndex;
            mMediaSession.setQueue(mPlayList);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            mPlayList.remove(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mPlayList.isEmpty()) ? -1 : mQueueIndex;
            mMediaSession.setQueue(mPlayList);
        }

        @Override
        public void onPrepare() {
            if (mQueueIndex < 0 && mPlayList.isEmpty()){
                return;
            }

            final String mediaId = mPlayList.get(mQueueIndex).getDescription().getMediaId();
            mPreparedMedia = MusicLibrary.getMetadata(MediaService.this, mediaId);
            mMediaSession.setMetadata(mPreparedMedia);

            if(!mMediaSession.isActive()) mMediaSession.setActive(true);
        }

        @Override
        public void onPlay() {
            if (!isReadyToPlay()) {
                // Nothing to play.
                return;
            }

            if (mPreparedMedia == null) {
                onPrepare();
            }

            mMediaPayer.playFromMedia(mPreparedMedia);
            Log.d(LOG_TAG, "onPlayFromMediaId: MediaSession active");
        }

        @Override
        public void onPause() {
            mMediaPayer.pause();
        }

        @Override
        public void onStop() {
            mMediaPayer.stop();
            mMediaSession.setActive(false);
        }

        private boolean isReadyToPlay() {
            return !mPlayList.isEmpty();
        }

    }


}
