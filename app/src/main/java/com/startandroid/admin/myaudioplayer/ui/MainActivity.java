package com.startandroid.admin.myaudioplayer.ui;

import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.client.MediaBrowserClient;
import com.startandroid.admin.myaudioplayer.contentcatalogs.MusicLibrary;
import com.startandroid.admin.myaudioplayer.service.MediaService;

public class MainActivity extends AppCompatActivity {


    private ImageView mAlbumArt;
    private TextView mSongName;
    private TextView mSongArtist;
    private TextView mChannelTitle;
    private MediaSeekBar mMediaSeekBar;

    private MediaBrowserClient mMediaBrowserClient;
    private boolean mIsPlaying;
    private MediaControllerCallback mMediaControllerCallback;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAlbumArt = findViewById(R.id.album_art);
        mSongName = findViewById(R.id.song_name);
        mSongArtist = findViewById(R.id.song_artist);
        mChannelTitle = findViewById(R.id.channel_title);
        mMediaSeekBar = findViewById(R.id.seekBar);

        MediaButtonClickListener mediaButtonClickListener = new MediaButtonClickListener();
        findViewById(R.id.btn_previous).setOnClickListener(mediaButtonClickListener);
        findViewById(R.id.btn_play_pause).setOnClickListener(mediaButtonClickListener);
        findViewById(R.id.btn_next).setOnClickListener(mediaButtonClickListener);

        mMediaBrowserClient = new MediaBrowserClient(this, MediaService.class);
        mMediaControllerCallback = new MediaControllerCallback();

    }


    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowserClient.connect();

        if (mMediaBrowserClient.getMediaController() != null) {
            mMediaBrowserClient.getMediaController().registerCallback(mMediaControllerCallback);
            mMediaSeekBar.setMediaController(mMediaBrowserClient.getMediaController());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaSeekBar.disconnectController();

        if (mMediaBrowserClient.getMediaController() != null)
            mMediaBrowserClient.getMediaController().unregisterCallback(mMediaControllerCallback);
        mMediaBrowserClient.disconnect();
    }

    private class MediaButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_previous:
                    mMediaBrowserClient.getTransportControls().skipToPrevious();
                    break;
                case R.id.btn_play_pause:
                    if (mIsPlaying) {
                        mMediaBrowserClient.getTransportControls().pause();
                    } else {
                        mMediaBrowserClient.getTransportControls().play();
                    }
                    break;
                case R.id.btn_next:
                    mMediaBrowserClient.getTransportControls().skipToNext();
                    break;
            }
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            mIsPlaying = state != null &&
                    state.getState() == PlaybackStateCompat.STATE_PLAYING;
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata == null) {
                return;
            }
            mSongName.setText(
                    metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            mSongArtist.setText(
                    metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            mAlbumArt.setImageBitmap(MusicLibrary.getAlbumBitmap(
                    MainActivity.this,
                    metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));
        }

    }

}
