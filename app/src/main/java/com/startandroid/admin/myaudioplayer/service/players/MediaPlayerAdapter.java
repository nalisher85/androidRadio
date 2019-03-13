package com.startandroid.admin.myaudioplayer.service.players;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.startandroid.admin.myaudioplayer.contentcatalogs.MusicLibrary;
import com.startandroid.admin.myaudioplayer.service.PlaybackInfoListener;
import com.startandroid.admin.myaudioplayer.service.PlayerAdapter;

public class MediaPlayerAdapter extends PlayerAdapter {

    private final Context mContext;
    private MediaPlayer mMediaPlayer;
    private String mFileName;
    private MediaMetadataCompat mCurrentMedia;
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
                mPlaybackInfoListener.onPlaybackCompleted();
                setNewState(PlaybackStateCompat.STATE_PAUSED);
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
        return actions;    }

    @Override
    protected void onPlay() {
        if(mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
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
    public void seekTo(long position) {
        if (mMediaPlayer != null) {
            if(!mMediaPlayer.isPlaying()) {
                mSeekWhileNotPlaying = (int)position;
            }
            mMediaPlayer.seekTo((int)position);
            setNewState(mState);
        }
    }

    @Override
    public void setVolume(float volume) {
        if(mMediaPlayer != null) {
            mMediaPlayer.setVolume(volume, volume);
        }

    }
}




