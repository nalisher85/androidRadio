package com.startandroid.admin.myaudioplayer.ui;

import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.client.MediaBrowserClient;
import com.startandroid.admin.myaudioplayer.service.MediaService;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.bottom_sheet)
    ConstraintLayout mBottomSheet;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView mBottomNavigationView;
    @BindView(R.id.bottomsheet_peek_logo)
    ImageView mBottomsheetPeekLogo;
    @BindView(R.id.bottomsheet_peek_title)
    TextView mBottomsheetPeekTitle;
    @BindView(R.id.bottomsheet_peek_subtitle)
    TextView mBottomsheetPeekSubtitle;
    @BindView(R.id.bottomsheet_peek_button)
    ImageButton mBottomsheetPeekBtn;
    @BindView(R.id.bottomsheet_rec_button)
    ImageButton mBottomsheetRecBtn;
    @BindView(R.id.bottomsheet_prev_button)
    ImageButton mBottomsheetPrevBtn;
    @BindView(R.id.bottomsheet_play_button)
    ImageButton mBottomsheetPlayBtn;
    @BindView(R.id.bottomsheet_next_button)
    ImageButton mBottomsheetNextBtn;
    @BindView(R.id.bottomsheet_favorite_button)
    ImageButton mBottomsheetFavoriteBtn;
    @BindView(R.id.mediaSeekBar)
    MediaSeekBar mMediaSeekBar;

    private MediaBrowserClient mMediaBrowserClient;
    private MediaControllerCompat mMediaController;
    private boolean mIsPlaying;
    private Disposable onMetadataChangedSubscription;
    private Disposable onPlaybackStateChangedSubscription;
    private Disposable onMediaBrowserConnectedSubscription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("myLog", "MainActivity -> onCreate");
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationItemSelectedListner());
        if (savedInstanceState == null)
            setFragment(new StationFragment());

        mMediaBrowserClient = new MediaBrowserClient(this, MediaService.class);

        final BottomSheetBehavior bsBehavior = BottomSheetBehavior.from(mBottomSheet);
        bsBehavior.setBottomSheetCallback(new BottomSheetCallback());

        ViewCompat.setElevation(mBottomSheet, 21);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("myLog", "MainActivity -> onCreateOptionsMenu");
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        return super.onCreateOptionsMenu(menu);
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
        mBottomsheetPrevBtn.setOnClickListener(mediaButtonClickListener);
        mBottomsheetPlayBtn.setOnClickListener(mediaButtonClickListener);
        mBottomsheetNextBtn.setOnClickListener(mediaButtonClickListener);
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

    private boolean isReadWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d("myLog", "PERMISSION_GRANTED");
                return true;
            } else return false;
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    private boolean requestStoragePermission() {
        Log.d("myLog", "REQUEST PERMISSION");
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        return isReadWriteStoragePermissionGranted();
    }

    private class MediaButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d("myLog", "onClickView v.getId" + v.getId());
            switch (v.getId()) {
                case R.id.bottomsheet_prev_button:
                    mMediaController.getTransportControls().skipToPrevious();
                    break;
                case R.id.bottomsheet_play_button:
                    if (mIsPlaying) {
                        mMediaController.getTransportControls().pause();
                    } else {
                        mMediaController.getTransportControls().play();
                    }
                    break;
                case R.id.bottomsheet_next_button:
                    mMediaController.getTransportControls().skipToNext();
                    break;
            }
        }
    }

    private class BottomNavigationItemSelectedListner implements BottomNavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.btm_nav_channels:
                    Log.d("myTag", "onNavigationItemSelected->channels");
                    setFragment(new StationFragment());
                    break;
                case R.id.btm_nav_favorites:
                    Log.d("myTag", "onNavigationItemSelected->favorites");
                    setFragment(new FavoritesFragment());
                    break;
                case R.id.btm_nav_device_tracks:
                    Log.d("myTag", "onNavigationItemSelected->favorites");

                    if(requestStoragePermission())
                        setFragment(new DevicesTracksFragment());
                    else Toast.makeText(MainActivity.this, "Need access to storage",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    }

    private class BottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {

        @Override
        public void onStateChanged(@NonNull View view, int i) {
            switch (i) {
                case BottomSheetBehavior.STATE_COLLAPSED:
                    mBottomsheetPeekBtn.setVisibility(View.VISIBLE);
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    mBottomsheetPeekBtn.setVisibility(View.GONE);
                    break;
                case BottomSheetBehavior.STATE_DRAGGING:
                    mBottomsheetPeekBtn.setVisibility(View.GONE);
                    break;
                case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    mBottomsheetPeekBtn.setVisibility(View.GONE);
                    break;
                case BottomSheetBehavior.STATE_HIDDEN:
                    mBottomsheetPeekBtn.setVisibility(View.VISIBLE);
                    break;
                case BottomSheetBehavior.STATE_SETTLING:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View view, float v) {
            //Log.d("myLog", "onSlide: v -> " + v);

        }
    }

}
