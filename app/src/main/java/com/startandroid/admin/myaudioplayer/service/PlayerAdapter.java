package com.startandroid.admin.myaudioplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.annotation.NonNull;
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
    private final AudioManager mAudioManeger;
    private final AudioFocusChangeListener mAudioFocusChangeListener;

    private boolean mPlayOnAudioFocus = false;

    public PlayerAdapter(@NonNull Context context) {
        mApplicationContext = context.getApplicationContext();
        mAudioManeger = (AudioManager)mApplicationContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusChangeListener = new AudioFocusChangeListener();
    }

    public abstract boolean isPlaying();
    public abstract void playFromMedia(MediaMetadataCompat metadata);
    public abstract MediaMetadataCompat getCurrentMedia();
    public abstract void seekTo(long position);
    public abstract void setVolume(float volume);

    protected void play() {
        if (!requestAudioFocus()) return;
        registerAudioNoisyReceiver();
    }

    protected void pause(){
        if (!mPlayOnAudioFocus) abandonAudioFocus();
        unregisterAudioNoisyReceiver();
    }

    protected void stop(){
        abandonAudioFocus();
        unregisterAudioNoisyReceiver();
    }




    private boolean requestAudioFocus () {
        final int result = mAudioManeger.requestAudioFocus(mAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        mAudioManeger.abandonAudioFocus(mAudioFocusChangeListener);
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






    private class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange){
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
                    if(isPlaying()){
                        mPlayOnAudioFocus = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mAudioManeger.abandonAudioFocus(this);
                    mPlayOnAudioFocus = false;
                    stop();
                    break;
            }
        }
    }

}
