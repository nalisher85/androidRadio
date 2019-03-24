package com.startandroid.admin.myaudioplayer.ui;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.client.MediaBrowserClient;
import com.startandroid.admin.myaudioplayer.service.MediaService;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.toolbar)
    private Toolbar mToolbar;
    @BindView(R.id.bottom_sheet)
    private LinearLayout mBottomSheet;
    @BindView(R.id.bottom_navigation)
    private BottomNavigationView mBottomNavigationView;

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
        setContentView(R.layout.activity_main2);
        Log.d("myLog", "MainActivity -> onCreate");

        setSupportActionBar(mToolbar);
        ButterKnife.bind(this);
        mMediaSeekBar = findViewById(R.id.seekBar);
        mMediaBrowserClient = new MediaBrowserClient(this, MediaService.class);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new NavigationItemSelectedListner());

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("myLog", "MainActivity -> onStart");

        mMediaBrowserClient.connect();

        onMediaBrowserConnectedSubscription =
                mMediaBrowserClient.getOnConnectedObservable()
                        .subscribe(this::onMediaBrowserConnected, this::onConnectionError);
        onMetadataChangedSubscription =
                mMediaBrowserClient.getMediaMetadataObservable()
                        .subscribe(this::onMetadataChanged, this::onMetadataError);
        onPlaybackStateChangedSubscription =
                mMediaBrowserClient.getPlaybackStateObservable().
                        subscribe(this::onPlaybackStateChanged, this::onPlaybackStateError);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("myLog", "MainActivity -> onStop");

        mMediaSeekBar.disconnectController();
        mMediaBrowserClient.disconnect();
        onMediaBrowserConnectedSubscription.dispose();
        onMetadataChangedSubscription.dispose();
        onPlaybackStateChangedSubscription.dispose();
    }

    private void onConnectionError (Throwable e) {
        Log.d("myLog", "Ошибка onConnectionError " + e.getMessage());
    }

    private void onMetadataError (Throwable e) {
        Log.d("myLog", "Ошибка onMetadataError " + e.getMessage());
    }

    private void onPlaybackStateError (Throwable e) {
        Log.d("myLog", "Ошибка onPlaybackStateError " + e.getMessage());
    }

    private void onMediaBrowserConnected(Boolean isConnected) {
        if (!isConnected) return;
        Log.d("myLog", "MainActivity -> onMediaBrowserConnected");
        mMediaController = mMediaBrowserClient.getMediaController();
        MediaButtonClickListener mediaButtonClickListener = new MediaButtonClickListener();
        findViewById(R.id.btn_previous).setOnClickListener(mediaButtonClickListener);
        findViewById(R.id.btn_play_pause).setOnClickListener(mediaButtonClickListener);
        findViewById(R.id.btn_next).setOnClickListener(mediaButtonClickListener);
        mMediaSeekBar.setMediaController(mMediaController);
    }

    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d("myLog", "MainActivity -> onPlaybackStateChanged");
        mIsPlaying = state != null &&
                state.getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    public void onMetadataChanged(MediaMetadataCompat metadata) {
        Log.d("myLog", "MainActivity -> onMetadataChanged");
        if (metadata == null) {
            return;
        }

    }

    private void setFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.bnav_container, fragment).commit();
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

    private class NavigationItemSelectedListner implements BottomNavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.channels:
                    Log.d("myTag", "onNavigationItemSelected->channels");
                    setFragment(new RadioFragment());
                    break;
                case R.id.favorites:
                    Log.d("myTag", "onNavigationItemSelected->favorites");
                    setFragment(new FavoritesFragment());
                    break;
                case R.id.device_tracks:
                    Log.d("myTag", "onNavigationItemSelected->favorites");
                    setFragment(new DevicesTrackFragment());
                    break;
            }
            return false;
        }
    }


}
