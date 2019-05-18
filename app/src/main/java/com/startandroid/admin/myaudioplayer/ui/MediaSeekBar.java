package com.startandroid.admin.myaudioplayer.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;

import com.startandroid.admin.myaudioplayer.client.MediaBrowserClient;

import java.util.List;

public class MediaSeekBar extends AppCompatSeekBar {

    private MediaControllerCompat mMediaController;
    private MediaControllerCompat.Callback mMediaControllerCallback;

    private boolean mIsTracking = false;
    private MediaSeekBarListener mSeekBarListener;
    private ValueAnimator mProgressAnimator;
    private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mSeekBarListener.onProgressChanged(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mIsTracking = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mMediaController.getTransportControls().seekTo(getProgress());
            mIsTracking = false;
        }
    };
    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener =
            new ValueAnimator.AnimatorUpdateListener () {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (mIsTracking) {
                        animation.cancel();
                        return;
                    }
                    final int animatedIntValue = (int) animation.getAnimatedValue();
                    setProgress(animatedIntValue);
                }
            };

    public MediaSeekBar(Context context) {
        super(context);
        super.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

    public MediaSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

    public MediaSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

    @Override
    public final void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        // Prohibit adding seek listeners to this subclass.
        throw new UnsupportedOperationException("Cannot add listeners to a MediaSeekBar");
    }

    public void disconnectController() {
        if (mMediaController != null) {
            //mMediaController.unregisterCallback(mMediaControllerCallback);
            //mMediaControllerCallback = null;
            mMediaController = null;
        }
    }

    public void setMediaController (final MediaControllerCompat mediaController) {
        mMediaController = mediaController;
    }

    public void initializeMediaSeekBar(final MediaBrowserClient mediaBrowser, MediaSeekBarListener listener){
        MediaBrowserClientCallback callback = new MediaBrowserClientCallback();
        mSeekBarListener = listener;
        if(mediaBrowser.getMediaController() != null){
            mMediaController = mediaBrowser.getMediaController();
        }
        mediaBrowser.registerCallback(callback);
        callback.onConnected();
    }

    private class MediaBrowserClientCallback implements MediaBrowserClient.MediaBrowserCallbacks {

        @Override
        public void onConnected() {
            if (mMediaController.getPlaybackState() != null){
                onMetadataChanged(mMediaController.getMetadata());
                onPlaybackStateChanged(mMediaController.getPlaybackState());
            }
        }

        @Override
        public void onConnectionSuspended() {

        }

        @Override
        public void onConnectionFailed() {

        }

        @Override
        public void onChildrenLoaded(@NonNull List<MediaBrowserCompat.MediaItem> children) {

        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (mProgressAnimator != null) {
                mProgressAnimator.cancel();
                mProgressAnimator = null;
            }

            final int progress = state != null
                    ? (int) state.getPosition()
                    : 0;
            setProgress(progress);

            if (state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                final int timeToEnd = (int) ((getMax() - progress) / state.getPlaybackSpeed());

                mProgressAnimator = ValueAnimator.ofInt(progress, getMax()).setDuration(timeToEnd);
                mProgressAnimator.setInterpolator(new LinearInterpolator());
                mProgressAnimator.addUpdateListener(mAnimatorUpdateListener);
                mProgressAnimator.start();
                Log.d("myLog", "MediaSeekbar->onPlaybackStateChanged");
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            final int max = metadata != null
                    ? (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                    : 0;
            setProgress(0);
            setMax(max);
            Log.d("myLog", "MediaSeekbar->onMetadataChanged->max= "+max);
        }

        @Override
        public void onSessionReady() {

        }

        @Override
        public void onSessionDestroyed() {

        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {

        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {

        }

        @Override
        public void onQueueTitleChanged(CharSequence title) {

        }

        @Override
        public void onExtrasChanged(Bundle extras) {

        }

        @Override
        public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info) {

        }

        @Override
        public void onCaptioningEnabledChanged(boolean enabled) {

        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeChanged(int shuffleMode) {

        }
    }

    interface MediaSeekBarListener{
        void onProgressChanged(int progress);
    }
}
