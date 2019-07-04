package com.startandroid.admin.myaudioplayer.ui;

import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
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
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.client.MediaBrowserClient;
import com.startandroid.admin.myaudioplayer.data.AudioModel;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;
import com.startandroid.admin.myaudioplayer.data.StorageAudioFiles;
import com.startandroid.admin.myaudioplayer.service.MediaService;
import com.startandroid.admin.myaudioplayer.util.EditorViewHitArea;
import com.startandroid.admin.myaudioplayer.util.TouchDelegateComposite;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity implements FragmentListener {


    public static final String IS_FAVORITE_FRAGMENT_KEY = "is_favorite_fragment_key";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.bottom_sheet)
    FrameLayout mBottomSheetContainer;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView mBottomNavigationView;
    @BindView(R.id.bnav_container)
    FrameLayout mFragmentContainer;

    private MediaBrowserClient mMediaBrowserClient;
    private BottomSheet mBottomSheet;
    private StorageAudioFiles mStorageData;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("myLog1", "MainActivity -> onCreate");

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationItemSelectedListener());

        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(IS_FAVORITE_FRAGMENT_KEY, false);
            StationFragment stationFragment = new StationFragment();
            stationFragment.setArguments(bundle);
            setFragment(stationFragment);
        }

        mMediaBrowserClient = new MediaBrowserClient(this, MediaService.class);
        mMediaBrowserClient.registerCallback(new MediaBrowserClientCallback());
        mBottomSheet = new BottomSheet(mBottomSheetContainer);
        mStorageData = new StorageAudioFiles(this.getApplicationContext());

        ViewCompat.setElevation(mBottomSheetContainer, 21);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("myLog1", "MainActivity -> onStart");
        mMediaBrowserClient.connect();
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("myLog1", "MainActivity -> onResume");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d("myLog1", "MainActivity -> onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("myLog1", "MainActivity -> onStop");
        mMediaBrowserClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("myLog1", "MainActivity -> onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        return super.onCreateOptionsMenu(menu);
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.bnav_container, fragment).commit();
    }

    @Override
    public void onAddQueueItems(List<AudioModel> queueItems, boolean clearOldQueue) {
        mMediaBrowserClient.addToQueueItems(queueItems, clearOldQueue);
    }

    @Override
    public void onAddQueueItem(AudioModel audioTrack, boolean cleanOldList) {
        mMediaBrowserClient.addToQueueItem(audioTrack, cleanOldList);
    }

    @Override
    public void onAddQueueItem(RadioStationModel radioStationModel) {
        mMediaBrowserClient.addToQueueItem(radioStationModel);
    }

    private void setAudioTrackAsRingtone(@NonNull MediaDescriptionCompat audioTrack){
        String path = audioTrack.getMediaUri().toString();
        Uri uriAbs = new Uri.Builder().scheme("file").path(path).build(); //Absolute uri
        RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, uriAbs);
    }

    private void deleteAudioTrackFromStorage(@NonNull MediaDescriptionCompat audioTrack){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_audio_dialog_title)
                .setIcon(R.drawable.ic_delete)
                .setMessage(R.string.delete_audio_dialog_message)
                .setNegativeButton("Нет",
                        (dialog, which) -> {
                            dialog.cancel();
                        })
                .setPositiveButton("Да" ,
                        ((dialog, which) -> {
                            mMediaBrowserClient.removeQueueItem(audioTrack);
                            mStorageData.deleteAudioById(audioTrack.getMediaId());
                            dialog.cancel();
                        })).show();
    }

    //--------------------------------

    private class BottomNavigationItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(mBottomSheetContainer);
            Bundle bundle;
            StationFragment stationFragment;

            switch (menuItem.getItemId()){
                case R.id.btm_nav_channels:
                    bundle = new Bundle();
                    bundle.putBoolean(IS_FAVORITE_FRAGMENT_KEY, false);
                    stationFragment = new StationFragment();
                    stationFragment.setArguments(bundle);
                    setFragment(stationFragment);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;
                case R.id.btm_nav_favorites:
                    bundle = new Bundle();
                    bundle.putBoolean(IS_FAVORITE_FRAGMENT_KEY, true);
                    stationFragment = new StationFragment();
                    stationFragment.setArguments(bundle);
                    setFragment(stationFragment);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;
                case R.id.btm_nav_device_tracks:
                    setFragment(new DevicesTracksFragment());
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;
            }
            return true;
        }
    }

    private class MediaBrowserClientCallback implements MediaBrowserClient.MediaBrowserCallbacks {

        @Override
        public void onConnected() {
            if (mMediaBrowserClient.getMediaController().getMetadata() != null)
                mBottomSheet.initBottomSheet();
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
            Log.d("myLog", "MainActivity -> MediaBrowserClientCallback -> onPlaybackStateChanged." +
                    "state="+state.getState());
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.d("myLog", "MainActivity->MediaBrowserClientCallback->onMetadataChanged");
            if (!mBottomSheet.isBottomSheetInitialized()) mBottomSheet.initBottomSheet();
        }

        @Override
        public void onSessionReady() {

        }

        @Override
        public void onSessionDestroyed() {
            Log.d("myLog", "MainActivity->MediaBrowserClientCallback->onSessionDestroyed");
        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {

        }

        @Override
        public void onQueueChanged(List<QueueItem> queue) {

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


    class BottomSheet {

        static final int BOTTOM_SHEET_MODE_AUDIO = 1;
        static final int BOTTOM_SHEET_MODE_RADIO = 2;

        private AudioMode audioType;
        private RadioMode radioType;
        private BottomSheetListener mBottomSheetListener = null;
        private ViewGroup mBottomSheetContainer;
        private final BottomSheetBehavior mBSBehavior;
        private int mCurrantBottomSheet;

        @SuppressLint("CheckResult")
        BottomSheet(ViewGroup bottomSheetContainer) {
            mBottomSheetContainer = bottomSheetContainer;

            mBSBehavior = BottomSheetBehavior.from(mBottomSheetContainer);
            mBSBehavior.setBottomSheetCallback(new BottomSheetCallback());

            mMediaBrowserClient.getPlayerStateObservable().getMetadata()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            metadata -> {
                                if(isBottomSheetModeChanged()) initBottomSheet();
                            },
                            Throwable::printStackTrace
                    );

        }

        void initBottomSheet() {
            if (isBottomSheetInitialized() && !isBottomSheetModeChanged()) return;

            if (getBottomSheetMode() == BOTTOM_SHEET_MODE_AUDIO) {
                mCurrantBottomSheet = BOTTOM_SHEET_MODE_AUDIO;

                if (radioType != null) {
                    radioType.onDestroy();
                    radioType = null;
                }
                audioType = new AudioMode(mBottomSheetContainer);

            } else if (getBottomSheetMode() == BOTTOM_SHEET_MODE_RADIO) {
                mCurrantBottomSheet = BOTTOM_SHEET_MODE_RADIO;

                if (audioType != null) {
                    audioType.onDestroy();
                    audioType = null;
                }
                radioType = new RadioMode(mBottomSheetContainer);
            } else {
                return;
            }

            ((ViewGroup.MarginLayoutParams) mFragmentContainer.getLayoutParams()).bottomMargin += 150;
        }

        int getBottomSheetMode() {
            if (mMediaBrowserClient.getMediaController().getMetadata() == null) return -1;
            MediaDescriptionCompat description = mMediaBrowserClient.getMediaController().getMetadata().getDescription();

            if (description.getMediaUri() == null) return -1;
            String uriScheme = description.getMediaUri().getScheme();

            if (Objects.equals(uriScheme, "http")) {
                return BOTTOM_SHEET_MODE_RADIO;
            } else {
                return BOTTOM_SHEET_MODE_AUDIO;
            }
        }

        boolean isBottomSheetModeChanged() {
            return mCurrantBottomSheet != getBottomSheetMode();
        }

        boolean isBottomSheetInitialized() {
            return audioType != null || radioType != null;
        }

        void expandBottomSheet(){
            mBSBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        void collapseBottomSheet(){
            mBSBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        void destroyBottomSheet(){
            if (audioType != null){
                audioType.onDestroy();
                audioType = null;
            } else if (radioType != null) {
                radioType.onDestroy();
                radioType = null;
            }
        }


        //-------------------------------------------------------------------------------------------

        class AudioMode implements MediaSeekBar.MediaSeekBarListener, BottomSheetListener,
                QueueListAdapter.QueueItemClickListener {

            private static final int MIDDLE_CONTAINER_IMG_MODE = 1;
            private static final int MIDDLE_CONTAINER_QUEUELIST_MODE = 2;

            @BindView(R.id.bottomsheet_media_menu)
            ImageButton mMediaMenu;
            @BindView(R.id.peek_title)
            TextView mPeekTitle;
            @BindView(R.id.peek_subtitle)
            TextView mPeekSubtitle;
            @BindView(R.id.peek_play_btn)
            ToggleButton mPeekPlayBtn;


            @BindView(R.id.middle_container)
            FrameLayout mMiddleContainer;
            @BindView(R.id.queuelist_btn)
            ImageButton mQueueListBtn;

            @BindView(R.id.play_btn)
            ToggleButton mPlayBtn;
            @BindView(R.id.bottomsheet_prev_button)
            ImageButton mPrevBtn;
            @BindView(R.id.bottomsheet_next_button)
            ImageButton mNextBtn;
            @BindView(R.id.bottomsheet_repeat_btn)
            MediaRepeatModeButton mRepeatBtn;
            @BindView(R.id.bottomsheet_shuffle_btn)
            ImageButton mShuffleBtn;

            @BindView(R.id.mediaSeekBar)
            MediaSeekBar mMediaSeekBar;
            @BindView(R.id.currant_time_progress)
            TextView mCurrantTimeProgress;
            @BindView(R.id.progress_duration_time)
            TextView mProgressDurationTime;

            View mAudioBottomSheet;
            CompositeDisposable audioBottomSheetDisposable = new CompositeDisposable();
            CompositeDisposable queueListDisposable = new CompositeDisposable();
            EditorViewHitArea editorViewHitArea;
            RecyclerView mQueueList;
            RelativeLayout mMiddleContainerWithImg;
            int middleContainerMode;
            private int mQueueIndex = -1;


            AudioMode(@NonNull ViewGroup parentView) {
                mAudioBottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet_audio_mode, parentView, false);
                ButterKnife.bind(this, mAudioBottomSheet);
                init();
                parentView.addView(mAudioBottomSheet);
                mBottomSheetListener = this;

                editorViewHitArea = new EditorViewHitArea(new TouchDelegateComposite(getApplicationContext()));
                editorViewHitArea.increaseViewHitArea(mRepeatBtn, 10, 10, 10, 10);
                editorViewHitArea.increaseViewHitArea(mShuffleBtn, 10, 10, 10, 10);

            }

            void init() {

                mPrevBtn.setOnClickListener(v -> mMediaBrowserClient.onMediaButtonClicked(v));
                mNextBtn.setOnClickListener(v -> mMediaBrowserClient.onMediaButtonClicked(v));

                mPlayBtn.setOnClickListener(v -> {
                    mMediaBrowserClient.onMediaButtonClicked(v);
                });

                mPeekPlayBtn.setOnClickListener(v -> {
                    mMediaBrowserClient.onMediaButtonClicked(v);
                });

                audioBottomSheetDisposable.add(
                        mMediaBrowserClient.getPlayerStateObservable().getMetadata().subscribe(
                                metadata -> {
                                    mPeekTitle.setText(metadata.getDescription().getTitle());
                                    mPeekSubtitle.setText(metadata.getDescription().getSubtitle());

                                    long mediaDuration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                                    String durationMMSS = new SimpleDateFormat("mm:ss")
                                            .format(new Date(mediaDuration));
                                    mProgressDurationTime.setText(durationMMSS);
                                },
                                Throwable::printStackTrace)
                );
                audioBottomSheetDisposable.add(
                        mMediaBrowserClient.getPlayerStateObservable().getPlaybackState().subscribe(
                                state -> {
                                    boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
                                    mPlayBtn.setChecked(isPlaying);
                                    mPeekPlayBtn.setChecked(isPlaying);
                                },
                                Throwable::printStackTrace
                        )
                );

                audioBottomSheetDisposable.add(
                        mMediaBrowserClient.getPlayerStateObservable().getQueueIndex().subscribe(
                                index -> {
                                    mQueueIndex = index;
                                    if (index == -1) {
                                        collapseBottomSheet();
                                        destroyBottomSheet();
                                    }
                                }
                        )
                );

                setMiddleContainer();
                ViewCompat.setElevation(mQueueListBtn, 20);
                mQueueListBtn.setOnClickListener(v -> {
                    Log.d("myLog", "mQueueListBtn clicked");
                    setMiddleContainer();
                });

                mRepeatBtn.setOnClickListener(v -> {

                    int mode = ((MediaRepeatModeButton)v).getRepeatMode();
                    switch (mode) {
                        case MediaRepeatModeButton.REPEAT_MODE_NONE:
                            ((MediaRepeatModeButton)v).setRepeatMode(MediaRepeatModeButton.REPEAT_MODE_ALL);
                            break;
                        case MediaRepeatModeButton.REPEAT_MODE_ALL:
                            ((MediaRepeatModeButton)v).setRepeatMode(MediaRepeatModeButton.REPEAT_MODE_ONE);
                            break;
                        case MediaRepeatModeButton.REPEAT_MODE_ONE:
                            ((MediaRepeatModeButton)v).setRepeatMode(MediaRepeatModeButton.REPEAT_MODE_NONE);
                            break;
                    }

                    mMediaBrowserClient.onMediaButtonClicked(v);

                });

                mShuffleBtn.setActivated(false);
                mShuffleBtn.setOnClickListener(v -> {
                    v.setActivated(!v.isActivated());
                    mMediaBrowserClient.onMediaButtonClicked(v);
                });

                mMediaSeekBar.initializeMediaSeekBar(mMediaBrowserClient, this);
                mBottomSheetContainer.setVisibility(View.VISIBLE);
            }

            @OnClick({R.id.peek_title, R.id.peek_subtitle})
            void expandBottomSheet(){
                BottomSheet.this.expandBottomSheet();
            }

            @OnClick(R.id.bottomsheet_media_menu)
            void showQueueItemMenu(View view){

                PopupMenu menu = new PopupMenu(MainActivity.this, view);
                menu.inflate(R.menu.queue_item_menu);

                menu.setOnMenuItemClickListener(item -> {

                   //need to extract queue item
                    MediaMetadataCompat metadata = mMediaBrowserClient.getMediaController().getMetadata();
                    List<QueueItem> queueItems = mMediaBrowserClient.getMediaController().getQueue();

                    for (QueueItem qItem : queueItems){

                        assert qItem.getDescription().getMediaId() != null;
                        if (qItem.getDescription().getMediaId().equals(metadata.getDescription().getMediaId())) {
                            onItemClickListener(qItem, item.getItemId());
                        }
                    }
                    return true;
                });

                menu.show();
            }

            void setMiddleContainer() {

                if (middleContainerMode == MIDDLE_CONTAINER_IMG_MODE){
                    middleContainerMode = MIDDLE_CONTAINER_QUEUELIST_MODE;
                    mMiddleContainer.removeAllViews();
                    mMiddleContainer.addView(mQueueListBtn);
                    queueListDisposable.clear();
                    setImg();

                } else if(middleContainerMode == MIDDLE_CONTAINER_QUEUELIST_MODE) {
                    middleContainerMode = MIDDLE_CONTAINER_IMG_MODE;
                    mMiddleContainer.removeAllViews();
                    mMiddleContainer.addView(mQueueListBtn);
                    setQueueList();

                } else {
                    middleContainerMode = MIDDLE_CONTAINER_QUEUELIST_MODE;
                    mMiddleContainer.removeAllViews();
                    mMiddleContainer.addView(mQueueListBtn);
                    setImg();
                }
            }

            void setImg(){
             mMiddleContainerWithImg = (RelativeLayout) getLayoutInflater()
                     .inflate(R.layout.bottomsheet_middle_img, mMiddleContainer, false);
             mMiddleContainer.addView(mMiddleContainerWithImg);
            }

            void setQueueList() {
                mQueueList = new RecyclerView(MainActivity.this);
                mQueueList.addItemDecoration(new DividerItemDecoration(MainActivity.this, LinearLayoutManager.VERTICAL));
                mMiddleContainer.addView(mQueueList);
                mQueueList.setHasFixedSize(true);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                mQueueList.setLayoutManager(linearLayoutManager);
                QueueListAdapter adapter = new QueueListAdapter(this);

                queueListDisposable.add(
                        mMediaBrowserClient.getPlayerStateObservable().getQueueItems().subscribe(
                                queueItems -> {
                                    adapter.setQueueList(queueItems);
                                    mQueueList.setAdapter(adapter);
                                },
                                Throwable::printStackTrace
                        )
                );

                queueListDisposable.add(
                        mMediaBrowserClient.getPlayerStateObservable().getQueueIndex()
                                .subscribe(index -> {
                                    adapter.setPlayingPosition(index);
                                    adapter.notifyDataSetChanged();
                                })
                );

                queueListDisposable.add(
                        mMediaBrowserClient.getPlayerStateObservable().getIsPlaying().subscribe(
                                isPlaying -> {
                                    adapter.setPlaying(isPlaying);
                                    adapter.notifyDataSetChanged();
                                }
                        )
                );

            }

            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        middleContainerMode = MIDDLE_CONTAINER_IMG_MODE;
                        setMiddleContainer();
                        mPeekPlayBtn.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        mPeekPlayBtn.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        mPeekPlayBtn.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        mPeekPlayBtn.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        mPeekPlayBtn.setVisibility(View.VISIBLE);
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

            @Override
            public void onProgressChanged(int progress) {
                String currentProgressTime = new SimpleDateFormat("mm:ss").format(new Date(progress));
                mCurrantTimeProgress.setText(currentProgressTime);
            }

            @Override
            public void onItemClickListener(QueueItem itemData, int viewId) {

                switch (viewId) {

                    case R.id.track_item:
                        long id = itemData.getQueueId();
                        mMediaBrowserClient.getMediaController().getTransportControls().skipToQueueItem(id);
                        break;

                    case R.id.set_as_ring_menu:
                        setAudioTrackAsRingtone(itemData.getDescription());
                        break;

                    case R.id.remove_from_queue:
                        mMediaBrowserClient.removeQueueItem(itemData.getDescription());
                        break;

                    case R.id.track_delete_menu:
                        deleteAudioTrackFromStorage(itemData.getDescription());
                        break;
                }
            }

            void onDestroy(){
                mMediaSeekBar.disconnectController();
                mBottomSheetContainer.setVisibility(View.GONE);
                ((ViewGroup.MarginLayoutParams) mFragmentContainer.getLayoutParams()).bottomMargin -= 150;
                audioBottomSheetDisposable.dispose();
                queueListDisposable.dispose();
            }
        }

        class RadioMode implements BottomSheetListener {

            @BindView(R.id.bottomsheet_media_menu)
            ImageButton mMediaMenu;
            @BindView(R.id.peek_title)
            TextView mPeekTitle;
            @BindView(R.id.peek_play_btn)
            PlayButton mPeekPlayBtn;

            @BindView(R.id.middle_container)
            FrameLayout mMiddleContainer;

            @BindView(R.id.play_btn)
            PlayButton mPlayBtn;
            @BindView(R.id.bottomsheet_favorite_button)
            ToggleButton mFavoriteBtn;

            View mRadioBottomSheet;
            CompositeDisposable mDisposable = new CompositeDisposable();

            RadioMode(ViewGroup parent) {
                mRadioBottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet_radio_mode, parent, false);
                ButterKnife.bind(this, mRadioBottomSheet);
                init();
                parent.addView(mRadioBottomSheet);
                mBottomSheetListener = this;
            }

            void init() {

                mMediaMenu.setOnClickListener(v -> showMediaMenu());

                mDisposable.add(
                        mMediaBrowserClient.getPlayerStateObservable().getMetadata()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        metadata -> {
                                            mPeekTitle.setText(metadata.getDescription().getTitle());
                                        },
                                        Throwable::printStackTrace
                                )
                );

                mDisposable.add(
                        mMediaBrowserClient.getPlayerStateObservable().getPlaybackState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                state -> {
                                    Log.d("mediaPlayer", "getPlayerStateObservable changed="+state);
                                    boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;

                                    if (isPlaying) {
                                        mPeekPlayBtn.setMode(PlayButton.PAUSE_MODE);
                                        mPlayBtn.setMode(PlayButton.PAUSE_MODE);
                                    } else {
                                        mPlayBtn.setMode(PlayButton.PLAY_MODE);
                                        mPeekPlayBtn.setMode(PlayButton.PLAY_MODE);
                                    }

                                },
                                Throwable::printStackTrace
                        )
                );

                mFavoriteBtn.setOnClickListener(v -> {
                });

                mBottomSheetContainer.setVisibility(View.VISIBLE);
            }

            @OnClick(R.id.peek_title)
            void collapseBottomSheet(){
                mBSBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            @OnClick({R.id.peek_play_btn, R.id.play_btn})
            void onPlayBtnClick(View v){

                PlayButton playBtn = (PlayButton) v;
                if (playBtn.getMode() == PlayButton.BUFFERING_MODE) {

                    mPlayBtn.setMode(PlayButton.PLAY_MODE);
                    mPeekPlayBtn.setMode(PlayButton.PLAY_MODE);

                } else if (playBtn.getMode() == PlayButton.PLAY_MODE) {

                    mPlayBtn.setMode(PlayButton.BUFFERING_MODE);
                    mPeekPlayBtn.setMode(PlayButton.BUFFERING_MODE);
                }

                mMediaBrowserClient.onMediaButtonClicked(v);
            }

            void showMediaMenu(){
            }

            @Override
            public void onStateChanged(View view, int i) {
                switch (i) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mPeekPlayBtn.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        mPeekPlayBtn.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        mPeekPlayBtn.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        mPeekPlayBtn.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onSlide(View view, float v) {

            }

            void onDestroy(){
                mBottomSheetContainer.setVisibility(View.GONE);
                ((ViewGroup.MarginLayoutParams) mFragmentContainer.getLayoutParams()).bottomMargin -= 150;
                mDisposable.dispose();
            }
        }

        class BottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {

            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (mBottomSheetListener != null) mBottomSheetListener.onStateChanged(view, i);
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                if (mBottomSheetListener != null) mBottomSheetListener.onSlide(view, v);
            }
        }
    }

}
