package com.startandroid.admin.myaudioplayer.main;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.appcompat.widget.AppCompatSeekBar;

import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;

import com.startandroid.admin.myaudioplayer.service.players.PlayerAdapter;

public class MediaSeekBar extends AppCompatSeekBar {

    private OnSeekBarChangeListener mSubscriber;

    private boolean mIsTracking = false;
    private ValueAnimator mProgressAnimator;
    private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mSubscriber != null) mSubscriber.onProgressChanged(seekBar, progress, fromUser);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mIsTracking = true;
            if (mSubscriber != null) mSubscriber.onStartTrackingTouch(seekBar);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mSubscriber != null) mSubscriber.onStopTrackingTouch(seekBar);
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

    public void subscribe(OnSeekBarChangeListener subscriber){
        mSubscriber = subscriber;
    }

    public void updateMetadata(MediaMetadataCompat metadata) {
        final int max = metadata != null
                ? (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                : 0;
        //if (getProgress() > 0) setProgress(getProgress());
        //else setProgress(0);
        if(mProgressAnimator != null) mProgressAnimator.cancel();
        setProgress(0);
        setMax(max);
    }

    public void updatePlaybackState(PlaybackStateCompat state) {
        if (mProgressAnimator != null) {
            mProgressAnimator.cancel();
            mProgressAnimator = null;
        }

        final int progress;
        if (state == null
                || (state.getExtras() != null
                && state.getExtras().getBoolean(PlayerAdapter.KEY_IS_PLAYBACK_COMPLETED))) {

            progress = 0;

        } else progress = (int)state.getPosition();

        setProgress(progress);

        if (state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING) {

            final int timeToEnd = (int)((getMax() - progress) / state.getPlaybackSpeed());
            if (timeToEnd < 0) return;

            mProgressAnimator = ValueAnimator.ofInt(progress, getMax()).setDuration(timeToEnd);
            mProgressAnimator.setInterpolator(new LinearInterpolator());
            mProgressAnimator.addUpdateListener(mAnimatorUpdateListener);
            mProgressAnimator.start();
        }
    }
}
