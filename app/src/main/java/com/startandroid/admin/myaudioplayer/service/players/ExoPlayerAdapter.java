package com.startandroid.admin.myaudioplayer.service.players;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.startandroid.admin.myaudioplayer.service.PlaybackInfoListener;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ExoPlayerAdapter extends PlayerAdapter {

    private final Context mContext;
    private MediaMetadataCompat mCurrentMedia;
    private String mMediaUri;
    private int mState;
    private boolean mCurrentMediaPlayedToCompletion;
    private PlaybackInfoListener mPlaybackInfoListener;
    private boolean mPlayWhenRedy = false;

    private SimpleExoPlayer mMediaPlayer;
    DefaultTrackSelector mDefaultTrackSelector;


    private int mSeekWhileNotPlaying = -1;


    public ExoPlayerAdapter(@NonNull Context context, PlaybackInfoListener listener) {
        super(context);
        mContext = context.getApplicationContext();
        mPlaybackInfoListener = listener;
    }

    private void initializeMediaPlayer(){
        if(mMediaPlayer == null) {
            mDefaultTrackSelector = new DefaultTrackSelector();
            mMediaPlayer = ExoPlayerFactory.newSimpleInstance(mContext, mDefaultTrackSelector);
            mMediaPlayer.addListener(new PlayerEventListener());
            mMediaPlayer.addAnalyticsListener(new EventLogger(mDefaultTrackSelector));

        }
    }

    private void preparePlayer(Uri url){
        DataSource.Factory factory = new DefaultDataSourceFactory(
                mContext,
                Util.getUserAgent(mContext, mContext.getApplicationInfo().className)
        );

        MediaSource source = new ProgressiveMediaSource.Factory(factory)
                .createMediaSource(url);
        mMediaPlayer.prepare(source);
    }

    private void release() {
        if(mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }


    private void setNewState(@PlaybackStateCompat.State int newState) {

        mState = newState;
        if(mState == PlaybackStateCompat.STATE_STOPPED) {
            mCurrentMediaPlayedToCompletion = true;
        }

        final long reportPosition;
        if (mSeekWhileNotPlaying >= 0) {
            reportPosition = mSeekWhileNotPlaying;

            if (mState == PlaybackStateCompat.STATE_PLAYING) {
                mSeekWhileNotPlaying = -1;
            }
        } else {
            reportPosition = mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
        }

        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(getAvailableActions());
        stateBuilder.setState(mState,
                reportPosition,
                1.0f,
                SystemClock.elapsedRealtime());
        if (mCurrentMediaPlayedToCompletion) {
            Bundle extras = new Bundle();
            extras.putBoolean(KEY_IS_PLAYBACK_COMPLETED, true);
            stateBuilder.setExtras(extras);
        }
        mPlaybackInfoListener.onPlaybackStateChange(stateBuilder.build());
    }

    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;

        switch (mState) {
            case PlaybackStateCompat.STATE_STOPPED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE;
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                actions |= PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SEEK_TO;
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP;
                break;
            default:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }



    @Override
    protected void onPlay() {
        if(mMediaPlayer != null) {
            mMediaPlayer.getPlaybackState();
            mMediaPlayer.setPlayWhenReady(true);
            if(mSeekWhileNotPlaying > 0) {
                mMediaPlayer.seekTo(mSeekWhileNotPlaying);
            }
            setNewState(PlaybackStateCompat.STATE_PLAYING);
        }
    }

    @Override
    protected void onPause() {
        mPlayWhenRedy = false;
        if (mMediaPlayer != null) {
            mMediaPlayer.setPlayWhenReady(false);
            setNewState(PlaybackStateCompat.STATE_PAUSED);
        }
    }

    @Override
    protected void onStop() {
        setNewState(PlaybackStateCompat.STATE_STOPPED);
        release();
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.getPlayWhenReady();
    }

    @Override
    public void playFromUri(@NonNull Uri uri) {
        mPlayWhenRedy = true;
        boolean  mediaChanged = !(mMediaUri != null && mMediaUri.equals(uri.getPath()));
        if(mCurrentMediaPlayedToCompletion){
            mediaChanged = true;
            mCurrentMediaPlayedToCompletion = false;
        }
        if (!mediaChanged) {
            if (!isPlaying()) {
                play();
            }
            return;
        }
        release();
        initializeMediaPlayer();
        mMediaUri = uri.toString();
        preparePlayer(uri);
    }

    @Override
    public MediaMetadataCompat getCurrentMedia() {
        return mCurrentMedia;
    }

    @Override
    public void setCurrentMedia(MediaMetadataCompat currentMedia) {
        mCurrentMedia = currentMedia;
    }

    @Override
    public void seekTo(long position) {
        if (mMediaPlayer != null && !mCurrentMediaPlayedToCompletion) {
            mMediaPlayer.seekTo(position);
            setNewState(mState);
            mSeekWhileNotPlaying = -1;
        } else mSeekWhileNotPlaying = (int)position;
    }

    @Override
    public void setVolume(float volume) {
        if(mMediaPlayer != null) {
            mMediaPlayer.setVolume(volume);
        }

    }


    class PlayerEventListener implements Player.EventListener {

        public PlayerEventListener() {
        }

        /**
         * Called when the timeline and/or manifest has been refreshed.
         *
         * <p>Note that if the timeline has changed then a position discontinuity may also have
         * occurred. For example, the current period index may have changed as a result of periods being
         * added or removed from the timeline. This will <em>not</em> be reported via a separate call to
         * {@link #onPositionDiscontinuity(int)}.
         *
         * @param timeline The latest timeline. Never null, but may be empty.
         * @param manifest The latest manifest. May be null.
         * @param reason   The {link TimelineChangeReason} responsible for this timeline change.
         */
        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
            Log.d("myLog", "ExoPlayerAdapter->onTimelineChanged");
        }

        /**
         * Called when the available or selected tracks change.
         *
         * @param trackGroups     The available tracks. Never null, but may be of length zero.
         * @param trackSelections The track selections for each renderer. Never null and always of
         *                        length {link #getRendererCount()}, but may contain null elements.
         */
        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.d("myLog", "ExoPlayerAdapter->onTracksChanged");
        }

        /**
         * Called when the player starts or stops loading the source.
         *
         * @param isLoading Whether the source is currently being loaded.
         */
        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.d("myLog", "ExoPlayerAdapter->onLoadingChanged");
        }

        /**
         * Called when the value returned from either {link #getPlayWhenReady()} or {link
         * #getPlaybackState()} changes.
         *
         * @param playWhenReady Whether playback will proceed when ready.
         * @param playbackState One of the {@code STATE} constants.
         */
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            switch (playbackState){
                case Player.STATE_BUFFERING:
                    Log.d("myLog", "ExoPlayerAdapter->onPlayerStateChanged->STATE_BUFFERING");
                    setNewState(PlaybackStateCompat.STATE_BUFFERING);
                    break;
                case Player.STATE_ENDED:
                    Log.d("myLog", "ExoPlayerAdapter->onPlayerStateChanged->STATE_ENDED");
                    mCurrentMediaPlayedToCompletion = true;
                    setNewState(PlaybackStateCompat.STATE_PAUSED);
                    mPlaybackInfoListener.onPlaybackCompleted();
                    break;
                case Player.STATE_IDLE:
                    Log.d("myLog", "ExoPlayerAdapter->onPlayerStateChanged->STATE_IDLE");
                    break;
                case Player.STATE_READY:
                    Log.d("myLog", "ExoPlayerAdapter->onPlayerStateChanged->STATE_READY");
                    if (mPlayWhenRedy) {
                        play();
                    }
                    break;
            }

        }

        /**
         * Called when the value of {link #getRepeatMode()} changes.
         *
         * @param repeatMode The {link RepeatMode} used for playback.
         */
        @Override
        public void onRepeatModeChanged(int repeatMode) {
            Log.d("myLog", "ExoPlayerAdapter->onRepeatModeChanged");
        }

        /**
         * Called when the value of {link #getShuffleModeEnabled()} changes.
         *
         * @param shuffleModeEnabled Whether shuffling of windows is enabled.
         */
        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            Log.d("myLog", "ExoPlayerAdapter->onShuffleModeEnabledChanged");
        }

        /**
         * Called when an error occurs. The playback state will transition to {link #STATE_IDLE}
         * immediately after this method is called. The player instance can still be used, and {@link
         * #release()} must still be called on the player should it no longer be required.
         *
         * @param error The error.
         */
        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.d("myLog", "ExoPlayerAdapter->onPlayerError");
        }

        /**
         * Called when a position discontinuity occurs without a change to the timeline. A position
         * discontinuity occurs when the current window or period index changes (as a result of playback
         * transitioning from one period in the timeline to the next), or when the playback position
         * jumps within the period currently being played (as a result of a seek being performed, or
         * when the source introduces a discontinuity internally).
         *
         * <p>When a position discontinuity occurs as a result of a change to the timeline this method
         * is <em>not</em> called. {@link #onTimelineChanged(Timeline, Object, int)} is called in this
         * case.
         *
         * @param reason The {link DiscontinuityReason} responsible for the discontinuity.
         */
        @Override
        public void onPositionDiscontinuity(int reason) {
            Log.d("myLog", "ExoPlayerAdapter->onPositionDiscontinuity");
        }

        /**
         * Called when the current playback parameters change. The playback parameters may change due to
         * a call to {link #setPlaybackParameters(PlaybackParameters)}, or the player itself may change
         * them (for example, if audio playback switches to passthrough mode, where speed adjustment is
         * no longer possible).
         *
         * @param playbackParameters The playback parameters.
         */
        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Log.d("myLog", "ExoPlayerAdapter->onPlaybackParametersChanged");
        }

        /**
         * Called when all pending seek requests have been processed by the player. This is guaranteed
         * to happen after any necessary changes to the player state were reported to {@link
         * #onPlayerStateChanged(boolean, int)}.
         */
        @Override
        public void onSeekProcessed() {
            Log.d("myLog", "ExoPlayerAdapter->onSeekProcessed");
        }
    }
}
