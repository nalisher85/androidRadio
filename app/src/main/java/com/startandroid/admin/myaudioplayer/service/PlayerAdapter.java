package com.startandroid.admin.myaudioplayer.service;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.media.MediaMetadataCompat;

public abstract class PlayerAdapter {

    private static final float MEDIA_VOLUME_DEFAULT = 1.0f;
    private static final float MEDIA_VOLUME_DUCK = 0.2f;

    private static final IntentFilter AUDIO_NOISY_INTENT_FILTER =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private boolean mAudioNoisyReceiverRegistered = false;
    private final BroadcastReceiver mAudioNoisyReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                        if (isPlaying()) {

                        }
                    }
                }
            };

    private final Context mApplicationContext;
    private final AudioManager mAudioManager;
    private final AudioFocusHelper mAudioFocusHelper;

    private boolean mPlayOnAudioFocus = false;

    public PlayerAdapter(@NonNull Context context) {
        mApplicationContext = context.getApplicationContext();
        mAudioManager = (AudioManager)mApplicationContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusHelper = new AudioFocusHelper();
    }

    public abstract boolean isPlaying();
    public abstract void playFromMedia(MediaMetadataCompat metadata);
    public abstract MediaMetadataCompat getCurrentMedia();
    public abstract void seekTo(long position);
    public abstract void setVolume(float volume);

    protected void play() {
        if (!mAudioFocusHelper.requestAudioFocus()) return;
        registerAudioNoisyReceiver();
    }

    protected void pause(){
        if (!mPlayOnAudioFocus) mAudioFocusHelper.abandonAudioFocus();
        unregisterAudioNoisyReceiver();
    }

    protected void stop(){
        mAudioFocusHelper.abandonAudioFocus();
        unregisterAudioNoisyReceiver();
    }

    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mApplicationContext.registerReceiver(mAudioNoisyReceiver, AUDIO_NOISY_INTENT_FILTER);
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if(mAudioNoisyReceiverRegistered) {
            mApplicationContext.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }

    private final class AudioFocusHelper
            implements AudioManager.OnAudioFocusChangeListener {

        private AudioAttributes audioAttributes;
        private AudioFocusRequest audioFocusRequest;

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        public void setAudioAttributes (AudioAttributes atr) {
            if (atr != null) audioAttributes = atr;
            else {
                audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        public void setAudioFocusRequest(AudioFocusRequest afr) {
            if (afr != null) audioFocusRequest = afr;
            else {
                audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(audioAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(this)
                        .build();
            }
        }

        private boolean requestAudioFocus() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                return requestAudioFocusForPreApi26();
            else return requestAudioFocusForApi26();
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private boolean requestAudioFocusForApi26() {
            if (audioAttributes == null) setAudioAttributes(null);
            if (audioFocusRequest == null) setAudioFocusRequest(null);
            final int result = mAudioManager.requestAudioFocus(audioFocusRequest);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        private boolean requestAudioFocusForPreApi26() {
            final int result = mAudioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }


        private void abandonAudioFocus() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                abandonAudioFocusForPreApi26();
            else abandonAudioFocusForApi26();
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private void abandonAudioFocusForApi26() {
            mAudioManager.abandonAudioFocusRequest(audioFocusRequest);
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        private void abandonAudioFocusForPreApi26() {
            mAudioManager.abandonAudioFocus(this);
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mPlayOnAudioFocus && !isPlaying()) {
                        play();
                    } else if (isPlaying()) {
                        setVolume(MEDIA_VOLUME_DEFAULT);
                    }
                    mPlayOnAudioFocus = false;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    setVolume(MEDIA_VOLUME_DUCK);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (isPlaying()) {
                        mPlayOnAudioFocus = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mAudioManager.abandonAudioFocus(this);
                    mPlayOnAudioFocus = false;
                    stop();
                    break;
            }
        }
    }

}
