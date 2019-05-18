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
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.client.MediaBrowserClient;
import com.startandroid.admin.myaudioplayer.data.AudioModel;
import com.startandroid.admin.myaudioplayer.data.MyDbHelper;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;
import com.startandroid.admin.myaudioplayer.service.MediaService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements FragmentListener, MediaSeekBar.MediaSeekBarListener {


    public static final int MEDIA_PANEL_RADIO_TYPE = 1;
    public static final int MEDIA_PANEL_AUDIO_TYPE = 2;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.bottom_sheet)
    ConstraintLayout mBottomSheet;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView mBottomNavigationView;
    @BindView(R.id.bnav_container)
    FrameLayout mFragmentContainer;

    @BindView(R.id.bottomsheet_peek_logo)
    ImageView mBottomsheetPeekLogo;
    @BindView(R.id.bottomsheet_peek_title)
    TextView mBottomsheetPeekTitle;
    @BindView(R.id.bottomsheet_peek_subtitle)
    TextView mBottomsheetPeekSubtitle;
    @BindView(R.id.bottomsheet_peek_button)
    ToggleButton mBottomsheetPeekBtn;
    @BindView(R.id.media_panel)
    FrameLayout mMediaPanel;
                            //------------
    @BindView(R.id.radio_media_panel)
    ConstraintLayout mRadioMediaPanel;
    @BindView(R.id.bottomsheet_rec_button)
    ImageButton mBottomsheetRecBtn;
    @BindView(R.id.radio_mediapanel_play_btn)
    ToggleButton mRadioMediaPanelPlayBtn;
    @BindView(R.id.bottomsheet_favorite_button)
    ToggleButton mBottomsheetFavoriteBtn;
    @BindView(R.id.rec_time)
    TextView mRecTime;
    @BindView(R.id.media_panel_save_btn)
    ImageButton mMediaSaveBtn;

    @BindView(R.id.audio_media_panel)
    ConstraintLayout mAudioMediaPanel;
    @BindView(R.id.audio_mediapanel_play_btn)
    ToggleButton mAudioMediaPanelPlayBtn;
    @BindView(R.id.bottomsheet_prev_button)
    ImageButton mBottomsheetPrevBtn;
    @BindView(R.id.bottomsheet_next_button)
    ImageButton mBottomsheetNextBtn;
    @BindView(R.id.mediaSeekBar)
    MediaSeekBar mMediaSeekBar;
    @BindView(R.id.currant_time_progress)
    TextView mCurrantTimeProgress;
    @BindView(R.id.progress_duration_time)
    TextView mProgressDurationTime;

    private MediaBrowserClient mMediaBrowserClient;

    private static String[][] testData = {
            {"Record radio", "http://air2.radiorecord.ru:9003/rr_320", "false"},
            {"Record bighits", "http://air2.radiorecord.ru:9003/bighits_320", "false"},
            {"Record gold", "http://air2.radiorecord.ru:9003/gold_320", "false"},
            {"Record rap", "http://air2.radiorecord.ru:9003/rap_320 ", "false"},
            {"Record russianhits", "http://air2.radiorecord.ru:9003/russianhits_320 ", "false"},
            {"Record darkside", "http://air2.radiorecord.ru:9003/darkside_320", "false"},
            {"Record маятник фуко", "http://air2.radiorecord.ru:9003/mf_320", "false"},
            {"Record tecktonik", "http://air2.radiorecord.ru:9003/tecktonik_320", "false"},
            {"Record 2step", "http://air2.radiorecord.ru:9003/2step_320", "false"},
            {"Record discofunk", "http://air2.radiorecord.ru:9003/discofunk_320", "false"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationItemSelectedListener());
        if (savedInstanceState == null)
            setFragment(new StationFragment());

        mMediaBrowserClient = new MediaBrowserClient(this, MediaService.class);
        mMediaBrowserClient.registerCallback(new MediaBrowserClientCallback());

        final BottomSheetBehavior bsBehavior = BottomSheetBehavior.from(mBottomSheet);
        bsBehavior.setBottomSheetCallback(new BottomSheetCallback());

        ViewCompat.setElevation(mBottomSheet, 21);

        //test data
        MyDbHelper db = new MyDbHelper(this.getApplicationContext());
        List<RadioStationModel> stationModels = new ArrayList<>();
        Disposable disposable = db.getRadioStationList().subscribe(stations -> {
            if (stations.isEmpty()) {
                for (String[] aData : testData) {
                    RadioStationModel station = new RadioStationModel(aData[0], aData[1],
                            Boolean.parseBoolean(aData[2]));
                    stationModels.add(station);
                }
                db.insert(stationModels);
            }
        }, e -> e.printStackTrace());
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("myLog", "MainActivity -> onStart");

        mMediaBrowserClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("myLog", "MainActivity -> onStop");

        mMediaSeekBar.disconnectController();
        mMediaBrowserClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("myLog", "MainActivity -> onCreateOptionsMenu");
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onAddQueueItems(List<AudioModel> queueItems, boolean clearOldQueue) {
        mMediaBrowserClient.addToQueueItems(queueItems, clearOldQueue);
    }

    @Override
    public void onAddQueueItems(RadioStationModel radioStationModel) {
        mMediaBrowserClient.addToQueueItems(radioStationModel);
    }

    private void updateBottomSheet(){
        PlayerState playerState = mMediaBrowserClient.getPlayerState();
        MediaDescriptionCompat desc = playerState.getMetadata().getDescription();
        long mediaDuration = playerState.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        String durationMMSS = new SimpleDateFormat("mm:ss").format(new Date(mediaDuration));

        mProgressDurationTime.setText(durationMMSS);
        mBottomsheetPeekTitle.setText(desc.getTitle());
        mBottomsheetPeekSubtitle.setText(desc.getSubtitle());

        mBottomsheetPeekBtn.setChecked(playerState.isPlaying());
        mRadioMediaPanelPlayBtn.setChecked(playerState.isPlaying());
        mAudioMediaPanelPlayBtn.setChecked(playerState.isPlaying());
    }

    private void setBottomSheet(){
        PlayerState playerState = mMediaBrowserClient.getPlayerState();
        if (mBottomSheet.getVisibility() == View.VISIBLE &&
                playerState.getMetadata() != null) {
            updateBottomSheet();
        } else if(playerState.getMetadata() != null) {
            updateBottomSheet();
            mBottomsheetPrevBtn.setOnClickListener(v -> mMediaBrowserClient.onMediaButtonClicked(v.getId()));
            mBottomsheetNextBtn.setOnClickListener(v -> mMediaBrowserClient.onMediaButtonClicked(v.getId()));
            mRadioMediaPanelPlayBtn.setOnClickListener(v -> {
                mMediaBrowserClient.onMediaButtonClicked(v.getId());
                mRadioMediaPanelPlayBtn.setChecked(playerState.isPlaying());
            });
            mAudioMediaPanelPlayBtn.setOnClickListener(v -> {
                mMediaBrowserClient.onMediaButtonClicked(v.getId());
                mAudioMediaPanelPlayBtn.setChecked(playerState.isPlaying());
            });
            mBottomsheetPeekBtn.setOnClickListener(v -> {
                mMediaBrowserClient.onMediaButtonClicked(v.getId());
                mBottomsheetPeekBtn.setChecked(playerState.isPlaying());
            });

            ((ViewGroup.MarginLayoutParams) mFragmentContainer.getLayoutParams()).bottomMargin += 150;
            mBottomSheet.setVisibility(View.VISIBLE);
        }
    }

    private void changeMediaPanel(@NonNull MediaMetadataCompat metadata) {
        String uriScheme = Objects.requireNonNull(metadata.getDescription().getMediaUri()).getScheme();
        if(uriScheme != null && uriScheme.equals("http")){
            setMediaPanel(MEDIA_PANEL_RADIO_TYPE);
        } else {
            setMediaPanel(MEDIA_PANEL_AUDIO_TYPE);
        }
    }

    private void setMediaPanel(int type) {

        if (type == MEDIA_PANEL_RADIO_TYPE){
            mAudioMediaPanel.setVisibility(View.GONE);
            mRadioMediaPanel.setVisibility(View.VISIBLE);

        } else if (type == MEDIA_PANEL_AUDIO_TYPE){
            mRadioMediaPanel.setVisibility(View.GONE);
            mAudioMediaPanel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onProgressChanged(int progress) {
        String currentProgressTime = new SimpleDateFormat("mm:ss").format(new Date(progress));
        mCurrantTimeProgress.setText(currentProgressTime);
    }

    //--------------------------------

    private class BottomNavigationItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {

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
                    mBottomsheetPeekBtn.setChecked(mMediaBrowserClient.getPlayerState().isPlaying());
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
                    mBottomsheetPeekBtn.setChecked(mMediaBrowserClient.getPlayerState().isPlaying());
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

    private class MediaBrowserClientCallback implements MediaBrowserClient.MediaBrowserCallbacks {

        @Override
        public void onConnected() {
            Log.d("myLog", "MainActivity->MediaBrowserClientCallback->onConnected");
            mMediaSeekBar.initializeMediaSeekBar(mMediaBrowserClient, MainActivity.this);
            setBottomSheet();
        }

        @Override
        public void onConnectionSuspended() {
            Log.d("myLog", "MainActivity->MediaBrowserClientCallback->onConnectionSuspended");
        }

        @Override
        public void onConnectionFailed() {
            Log.d("myLog", "MainActivity->MediaBrowserClientCallback->onConnectionFailed");
        }

        @Override
        public void onChildrenLoaded(@NonNull List<MediaBrowserCompat.MediaItem> children) {
            Log.d("myLog", "MainActivity->MediaBrowserClientCallback->onChildrenLoaded");
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d("myLog", "MainActivity -> onPlaybackStateChanged." +
                    "state="+state.getState());
            updateBottomSheet();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.d("myLog", "MainActivity->MediaBrowserClientCallback->onMetadataChanged");
            changeMediaPanel(metadata);
            setBottomSheet();
        }

        @Override
        public void onSessionReady() {

        }

        @Override
        public void onSessionDestroyed() {

        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {

        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {

        }

        @Override
        public void onQueueTitleChanged(CharSequence title) {

        }

        @Override
        public void onExtrasChanged(Bundle extras) {

        }

        @Override
        public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info) {

        }

        @Override
        public void onCaptioningEnabledChanged(boolean enabled) {

        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeChanged(int shuffleMode) {

        }
    }

}
