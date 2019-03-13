package com.startandroid.admin.myaudioplayer.ui;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.client.MediaBrowserClient;
import com.startandroid.admin.myaudioplayer.contentcatalogs.MusicLibrary;
import com.startandroid.admin.myaudioplayer.service.MediaService;

import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {


    private ImageView mAlbumArt;
    private TextView mSongName;
    private TextView mSongArtist;
    private TextView mChannelTitle;
    private MediaSeekBar mMediaSeekBar;

    private MediaBrowserClient mMediaBrowserClient;
    private MediaControllerCompat mMediaController;
    private boolean mIsPlaying;
    Disposable onMetadataChangedSubscription;
    Disposable onPlaybackStateChangedSubscription;
    Disposable onMediaBrowserConnectedSubscription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAlbumArt = findViewById(R.id.album_art);
        mSongName = findViewById(R.id.song_name);
        mSongArtist = findViewById(R.id.song_artist);
        mChannelTitle = findViewById(R.id.channel_title);
        mMediaSeekBar = findViewById(R.id.seekBar);

        mMediaBrowserClient = new MediaBrowserClient(this, MediaService.class);

    }


    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowserClient.connect();
        mMediaBrowserClient.getOnConnectedObservable().subscribe(this::onMediaBrowserConnected);
        mMediaBrowserClient.getMediaMetadataObservable().subscribe(this::onMetadataChanged);
        mMediaBrowserClient.getPlaybackStateObservable().subscribe(this::onPlaybackStateChanged);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaSeekBar.disconnectController();
        mMediaBrowserClient.disconnect();
    }

    private void onMediaBrowserConnected(Boolean isConnected) {
        if (!isConnected) return;;
        mMediaController = mMediaBrowserClient.getMediaController();
        MediaButtonClickListener mediaButtonClickListener = new MediaButtonClickListener();
        findViewById(R.id.btn_previous).setOnClickListener(mediaButtonClickListener);
        findViewById(R.id.btn_play_pause).setOnClickListener(mediaButtonClickListener);
        findViewById(R.id.btn_next).setOnClickListener(mediaButtonClickListener);

        mMediaSeekBar.setMediaController(mMediaController);


    }

    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        mIsPlaying = state != null &&
                state.getState() == PlaybackStateCompat.STATE_PLAYING;
    }

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

    private class MediaButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d("myLog", "onClickView v.getId" + v.getId());
            switch (v.getId()) {
                case R.id.btn_previous:
                    mMediaController.getTransportControls().skipToPrevious();
                    break;
                case R.id.btn_play_pause:
                    if (mIsPlaying) {
                        mMediaController.getTransportControls().pause();
                    } else {
                        mMediaController.getTransportControls().play();
                    }
                    break;
                case R.id.btn_next:
                    mMediaController.getTransportControls().skipToNext();
                    break;
            }
        }
    }


}
