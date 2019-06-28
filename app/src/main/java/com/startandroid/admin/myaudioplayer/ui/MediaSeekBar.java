package com.startandroid.admin.myaudioplayer.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.util.ObjectsCompat;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;

import com.startandroid.admin.myaudioplayer.client.MediaBrowserClient;
import com.startandroid.admin.myaudioplayer.service.PlayerAdapter;

import java.util.Objects;

public class MediaSeekBar extends AppCompatSeekBar {

    private MediaControllerCompat mMediaController;

    private boolean mIsTracking = false;
    private MediaSeekBarListener mSeekBarListener;
    private ValueAnimator mProgressAnimator;
    private CompositeDisposable mDisposable = new CompositeDisposable();
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
            mMediaController = null;
        }
    }

    public void initializeMediaSeekBar(final MediaBrowserClient mediaBrowser, MediaSeekBarListener listener){
        mSeekBarListener = listener;
        if(mediaBrowser.getMediaController() != null){
            mMediaController = mediaBrowser.getMediaController();
        }

        mDisposable.add(
                mediaBrowser.getPlayerStateObservable().getMetadata()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                metadata -> {
                                    final int max = metadata != null
                                            ? (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                                            : 0;
                                    if (getProgress() > 0) setProgress(getProgress());
                                    else setProgress(0);
                                    setMax(max);
                                    Log.d("myLog", "MediaSeekbar->ObsevableMetadataChanged->max= "+max);
                                },
                                Throwable::printStackTrace
                        )
        );

        mDisposable.add(
                mediaBrowser.getPlayerStateObservable().getPlaybackState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                state -> {
                                    Log.d("myLog", "MediaSeekbar->ObsevablePlaybackStateChanged");

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

                                        mProgressAnimator = ValueAnimator.ofInt(progress, getMax()).setDuration(timeToEnd);
                                        mProgressAnimator.setInterpolator(new LinearInterpolator());
                                        mProgressAnimator.addUpdateListener(mAnimatorUpdateListener);
                                        mProgressAnimator.start();
                                    }
                                },
                                Throwable::printStackTrace
                        )
        );

    }

    interface MediaSeekBarListener{
        void onProgressChanged(int progress);
    }
}
