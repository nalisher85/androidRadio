package com.startandroid.admin.myaudioplayer.service.players;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.startandroid.admin.myaudioplayer.contentcatalogs.MusicLibrary;
import com.startandroid.admin.myaudioplayer.service.PlaybackInfoListener;
import com.startandroid.admin.myaudioplayer.service.PlayerAdapter;

import java.io.IOException;

public class MediaPlayerAdapter extends PlayerAdapter {

    private final Context mContext;
    private MediaPlayer mMediaPlayer;
    private String mFileName;
    private MediaMetadataCompat mCurrentMedia;
    private String mMediaUri;
    private int mState;
    private boolean mCurrentMediaPlayedToCompletion;
    private PlaybackInfoListener mPlaybackInfoListener;

    private int mSeekWhileNotPlaying = -1;

    public MediaPlayerAdapter(@NonNull Context context, PlaybackInfoListener listener) {
        super(context);
        mContext = context.getApplicationContext();
        mPlaybackInfoListener = listener;
    }


    private void initializeMediaPlayer(){
        if(mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
                Log.d("mediaPlayer", "mMediaPlayer->onCompleate");
                mCurrentMediaPlayedToCompletion = true;
                setNewState(PlaybackStateCompat.STATE_PAUSED);
                mPlaybackInfoListener.onPlaybackCompleted();
            });

            mMediaPlayer.setOnTimedTextListener(new MediaPlayer.OnTimedTextListener() {
                @Override
                public void onTimedText(MediaPlayer mp, TimedText text) {
                    Log.d("mediaPlayer", "mMediaPlayer->onTimedText = "+text);
                }
            });

            mMediaPlayer.setOnPreparedListener(listener -> {
                Log.d("mediaPlayer", "mMediaPlayer->OnPrepared");
                play();
            });
            mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    Log.d("mediaPlayer", "mMediaPlayer->onBufferingUpdate = "+percent+" percent");
                }
            });

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d("mediaPlayer", "mMediaPlayer->onError->what = "+what+", extra = "+extra);
                    return false;
                }
            });

            mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    Log.d("mediaPlayer", "mMediaPlayer->onInfo->what = "+what+", extra = "+extra);
                    return true;
                }
            });
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
        if(mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            if(mSeekWhileNotPlaying > 0) {
                mMediaPlayer.seekTo(mSeekWhileNotPlaying);
            }
            mMediaPlayer.start();
            setNewState(PlaybackStateCompat.STATE_PLAYING);
        }
    }

    @Override
    protected void onPause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
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
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    public void playFromUri(@NonNull Uri uri) {
        boolean  mediaChanged = !(mMediaUri != null && mMediaUri.equals(uri.getPath()));
        if(mCurrentMediaPlayedToCompletion){
            mediaChanged = true;
            mCurrentMediaPlayedToCompletion = false;
        }
        if ( !mediaChanged) {
            if (!isPlaying()) {
                play();
            }
            return;
        }
        release();
        initializeMediaPlayer();
        mMediaUri = uri.toString();
        try {
            mMediaPlayer.setDataSource(mMediaUri);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //----------test methods------------------------------------------
    @Override
    public void playFromMedia(MediaMetadataCompat metadata) {
        mCurrentMedia = metadata;
        final String mediaId = metadata.getDescription().getMediaId();
        playFile(MusicLibrary.getMusicFilename(mediaId));

    }

    private void playFile(String musicFilename) {
        boolean mediaChanged = (mFileName == null || !musicFilename.equals(mFileName));
        if (mCurrentMediaPlayedToCompletion) {
            // Last audio file was played to completion, the resourceId hasn't changed, but the
            // player was released, so force a reload of the media file for playback.
            mediaChanged = true;
            mCurrentMediaPlayedToCompletion = false;
        }
        if (!mediaChanged) {
            if (!isPlaying()) {
                play();
            }
            return;
        } else {
            release();
        }

        mFileName = musicFilename;

        initializeMediaPlayer();

        try {
            AssetFileDescriptor assetFileDescriptor = mContext.getAssets().openFd(mFileName);
            mMediaPlayer.setDataSource(
                    assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getLength());
        } catch (Exception e) {
            throw new RuntimeException("Failed to open file: " + mFileName, e);
        }

        try {
            mMediaPlayer.prepare();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open file: " + mFileName, e);
        }

        play();
    }
//------------------------------------------------------------------------


    private void release() {
        if(mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
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
            if(!mMediaPlayer.isPlaying()) {
                mSeekWhileNotPlaying = (int)position;
            }
            mMediaPlayer.seekTo((int)position);
            setNewState(mState);
            mSeekWhileNotPlaying = -1;
        } else mSeekWhileNotPlaying = (int)position;
    }

    @Override
    public void setVolume(float volume) {
        if(mMediaPlayer != null) {
            mMediaPlayer.setVolume(volume, volume);
        }

    }
}




