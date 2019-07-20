package com.startandroid.admin.myaudioplayer.main;

import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
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
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.client.IMediaBrowser;
import com.startandroid.admin.myaudioplayer.client.MediaBrowserHelper;
import com.startandroid.admin.myaudioplayer.data.MusicDataSource;
import com.startandroid.admin.myaudioplayer.data.RadioStationRepository;
import com.startandroid.admin.myaudioplayer.data.RadioStationSource;
import com.startandroid.admin.myaudioplayer.data.localsource.RadioStationLocalDataSource;
import com.startandroid.admin.myaudioplayer.data.storageaudiosource.MusicStorageDataSource;
import com.startandroid.admin.myaudioplayer.devicetrack.DeviceTrackFragment;
import com.startandroid.admin.myaudioplayer.radiostation.StationFragment;
import com.startandroid.admin.myaudioplayer.service.MediaService;
import com.startandroid.admin.myaudioplayer.util.DimensionConverter;
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

public class MainActivity extends AppCompatActivity implements MainActivityContract.View {


    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.bottom_sheet)
    FrameLayout mBottomSheetContainer;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView mBottomNavigationView;
    @BindView(R.id.fragment_container)
    FrameLayout mFragmentContainer;


    //Audio Bottom Sheet
    ImageButton mMediaMenuABSh;
    TextView mPeekTitleABSh;
    TextView mPeekSubtitleABSh;
    ToggleButton mPeekPlayBtnABSh;

    FrameLayout mMiddleContainerABSh;
    ImageButton mQueueListBtnABSh;

    ToggleButton mPlayBtnABSh;
    ImageButton mPrevBtnABSh;
    ImageButton mNextBtnABSh;
    MediaRepeatModeButton mRepeatBtnABSh;
    ImageButton mShuffleBtnABSh;

    MediaSeekBar mMediaSeekBar;
    TextView mCurrantTimeProgress;
    TextView mProgressDurationTime;
    View mAudioBottomSheet;


    //Radio Bottom Sheet
    ImageButton mMediaMenuRBSh;
    TextView mPeekTitleRBSh;
    PlayButton mPeekPlayBtnRBSh;

    FrameLayout mMiddleContainerRBSh;

    PlayButton mPlayBtnRBSh;
    ToggleButton mFavoriteBtnRBSh;
    View mRadioBottomSheet;


    MainActivityContract.Presenter mPresenter;

    private BottomSheetBehavior mBSBehavior;
    private RecyclerView mQueueList;
    private RelativeLayout mMiddleContainerWithImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("myLog1", "MainActivity -> onCreate");

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationItemSelectedListener());

        //set presenter
        RadioStationSource radioStationRepository =
                RadioStationRepository.getInstance(RadioStationLocalDataSource.getInstance(), null);
        MusicDataSource musicRepository = MusicStorageDataSource.getInstance();
        IMediaBrowser mediaBrowser = new MediaBrowserHelper(MediaService.class);
        mPresenter = new MainActivityPresenter(mediaBrowser, radioStationRepository, musicRepository, this);

        //set fragment
        if (savedInstanceState == null) {
            setFragment(StationFragment.newInstance(false));
        }

        //set BottomSheet
        mBSBehavior = BottomSheetBehavior.from(mBottomSheetContainer);
        mBSBehavior.setBottomSheetCallback(new BottomSheetCallBack());
        ViewCompat.setElevation(mBottomSheetContainer, 21);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("myLog1", "MainActivity -> onStart");
        mPresenter.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("myLog1", "MainActivity -> onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("myLog1", "MainActivity -> onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("myLog1", "MainActivity -> onStop");
        mPresenter.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("myLog1", "MainActivity -> onDestroy");
        mPresenter.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        return super.onCreateOptionsMenu(menu);
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    public void setAudioTrackAsRingtone(@NonNull MediaDescriptionCompat audioTrack) {
        String path = audioTrack.getMediaUri().toString();
        Uri uriAbs = new Uri.Builder().scheme("file").path(path).build(); //Absolute uri
        RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, uriAbs);
    }

    private void deleteAudioTrackFromStorage(@NonNull MediaDescriptionCompat audioTrack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_audio_dialog_title)
                .setIcon(R.drawable.ic_delete)
                .setMessage(R.string.delete_audio_dialog_message)
                .setNegativeButton("Нет",
                        (dialog, which) -> {
                            dialog.cancel();
                        })
                .setPositiveButton("Да",
                        ((dialog, which) -> {
                            mPresenter.removeQueueItem(audioTrack);
                            mPresenter.deleteMusic(audioTrack.getMediaId());
                            dialog.cancel();
                        })).show();
    }

    void showQueueItemMenu(View view){

        PopupMenu menu = new PopupMenu(MainActivity.this, view);
        menu.inflate(R.menu.queue_item_menu);

        menu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()) {

                case R.id.set_as_ring_menu:
                    mPresenter.setCurrantMusicAsRingTone();
                    break;
                case R.id.remove_from_queue:
                    mPresenter.deleteCurrantMediaFromQueue();
                    break;
                case R.id.track_delete_menu:
                    mPresenter.deleteCurrantMediaFromQueue();
                    mPresenter.deleteCurrantMusic();
                    break;
            }

            return true;
        });

        menu.show();
    }


    //----------------------------------------------------------------------


    @Override
    public void showBottomSheet() {

        if (mBottomSheetContainer.getVisibility() == View.GONE) {

            mBottomSheetContainer.setVisibility(View.VISIBLE);
            float bottomMargin = DimensionConverter.convertDpToPixel(50, this.getApplicationContext());
            ((ViewGroup.MarginLayoutParams) mFragmentContainer.getLayoutParams()).bottomMargin += bottomMargin;
        }
    }

    @Override
    public void destroyBottomSheet() {
        Log.d("myLog", "MainActivity->destroyBottomSheet");
        if (mBottomSheetContainer.getVisibility() == View.VISIBLE){
            collapseBottomSheet();
            mBottomSheetContainer.setVisibility(View.GONE);
            float bottomMargin = DimensionConverter.convertDpToPixel(50, this.getApplicationContext());
            ((ViewGroup.MarginLayoutParams) mFragmentContainer.getLayoutParams()).bottomMargin -= bottomMargin;
            mBottomSheetContainer.removeAllViews();
        }
    }

    @Override
    public void showAudioBottomSheet() {
        mAudioBottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet_audio_mode,
                mBottomSheetContainer, false);

        initAudioBottomSheet(mAudioBottomSheet);
        mBottomSheetContainer.addView(mAudioBottomSheet);
        mPresenter.subscribeAudioBottomSheet();
    }

    @SuppressLint("CheckResult")
    private void initAudioBottomSheet(View audioBottomSheet) {
        if (mAudioBottomSheet == null) return;

        Log.d("myLog", "MainActivity->initAudioBottomSheet");
        mMediaMenuABSh = audioBottomSheet.findViewById(R.id.a_bottomsheet_media_menu);
        mPeekTitleABSh = audioBottomSheet.findViewById(R.id.a_b_sh_peek_title);
        mPeekSubtitleABSh = audioBottomSheet.findViewById(R.id.a_b_sh_peek_subtitle);
        mPeekPlayBtnABSh = audioBottomSheet.findViewById(R.id.a_b_sh_peek_play_btn);
        mMiddleContainerABSh = audioBottomSheet.findViewById(R.id.a_b_sh_middle_container);
        mQueueListBtnABSh = audioBottomSheet.findViewById(R.id.queuelist_btn);
        mPlayBtnABSh = audioBottomSheet.findViewById(R.id.a_b_sh_play_btn);
        mPrevBtnABSh = audioBottomSheet.findViewById(R.id.bottomsheet_prev_button);
        mNextBtnABSh = audioBottomSheet.findViewById(R.id.bottomsheet_next_button);
        mRepeatBtnABSh = audioBottomSheet.findViewById(R.id.bottomsheet_repeat_btn);
        mShuffleBtnABSh = audioBottomSheet.findViewById(R.id.bottomsheet_shuffle_btn);
        mMediaSeekBar = audioBottomSheet.findViewById(R.id.mediaSeekBar);
        mCurrantTimeProgress = audioBottomSheet.findViewById(R.id.currant_time_progress);
        mProgressDurationTime = audioBottomSheet.findViewById(R.id.progress_duration_time);

        if (mBSBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            mPeekPlayBtnABSh.setVisibility(View.VISIBLE);
        } else {
            mPeekPlayBtnABSh.setVisibility(View.GONE);
        }

        mMediaMenuABSh.setOnClickListener(this::showQueueItemMenu);

        mPeekTitleABSh.setOnClickListener(v -> expandBottomSheet());
        mPeekSubtitleABSh.setOnClickListener(v -> expandBottomSheet());

        //touch delegate for audio bottom sheet
        TouchDelegateComposite touchDelegateForABSh = new TouchDelegateComposite(audioBottomSheet.getContext());
        EditorViewHitArea.increaseViewHitAreaPost(mRepeatBtnABSh, touchDelegateForABSh,
                10, 10, 10, 10);
        EditorViewHitArea.increaseViewHitAreaPost(mShuffleBtnABSh, touchDelegateForABSh,
                10, 10, 10, 10);

        mPrevBtnABSh.setOnClickListener(v -> mPresenter.skipToPrevious());
        mNextBtnABSh.setOnClickListener(v -> mPresenter.skipToNext());

        mPlayBtnABSh.setOnClickListener(v -> mPresenter.playPause());
        mPeekPlayBtnABSh.setOnClickListener(v -> mPresenter.playPause());

        mRepeatBtnABSh.setOnClickListener(v -> {

            MediaRepeatModeButton button = ((MediaRepeatModeButton) v);
            int mode = button.getRepeatMode();
            switch (mode) {
                case MediaRepeatModeButton.REPEAT_MODE_NONE:
                    button.setRepeatMode(MediaRepeatModeButton.REPEAT_MODE_ALL);
                    break;
                case MediaRepeatModeButton.REPEAT_MODE_ALL:
                    button.setRepeatMode(MediaRepeatModeButton.REPEAT_MODE_ONE);
                    break;
                case MediaRepeatModeButton.REPEAT_MODE_ONE:
                    button.setRepeatMode(MediaRepeatModeButton.REPEAT_MODE_NONE);
                    break;
            }
            mPresenter.setRepeatMode(button.getRepeatMode());
        });

        mShuffleBtnABSh.setActivated(false);
        mShuffleBtnABSh.setOnClickListener(v -> {
            v.setActivated(!v.isActivated());
            int mode = v.isActivated()
                    ? PlaybackStateCompat.SHUFFLE_MODE_ALL
                    : PlaybackStateCompat.SHUFFLE_MODE_NONE;
            mPresenter.setShuffleMode(mode);
        });

        //set MediaSeekBar
        mPresenter.subscribeMediaSeekBar();
        mMediaSeekBar.subscribe(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String currentProgressTime = new SimpleDateFormat("mm:ss")
                        .format(new Date(progress));
                mCurrantTimeProgress.setText(currentProgressTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPresenter.seekMusicTo(seekBar.getProgress());
            }
        });

        ViewCompat.setElevation(mQueueListBtnABSh, 20);
        mQueueListBtnABSh.setOnClickListener(v -> {
            Log.d("myLog", "mQueueListBtn clicked");
            mPresenter.switchAudioBottomSheetMiddle();
        });

        mPresenter.switchOffAudioBottomSheetPlayList();
    }

    @Override
    public void showAudioBShQueueList() {
        if (mAudioBottomSheet == null) return;

        mQueueList = new RecyclerView(MainActivity.this);
        mQueueList.addItemDecoration(new DividerItemDecoration(MainActivity.this, LinearLayoutManager.VERTICAL));
        mQueueList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mQueueList.setLayoutManager(linearLayoutManager);
        QueueListAdapter adapter = new QueueListAdapter(mQueueItemClickListener);
        mQueueList.setAdapter(adapter);

        mMiddleContainerABSh.addView(mQueueList);
        showQueueListButton();
    }

    @Override
    public void showBShMiddleContainerWithImg() {
        if (mAudioBottomSheet == null) return;

        mMiddleContainerWithImg = (RelativeLayout) getLayoutInflater()
                .inflate(R.layout.bottomsheet_middle_img, mMiddleContainerABSh, false);
        clearABShMiddleContainer();
        showQueueListButton();
        mMiddleContainerABSh.addView(mMiddleContainerWithImg);
    }

    @Override
    public void clearABShMiddleContainer() {
        if (mAudioBottomSheet == null) return;
        mMiddleContainerABSh.removeAllViews();
    }

    @Override
    public void showQueueListButton() {
        if (mAudioBottomSheet == null) return;
        mMiddleContainerABSh.addView(mQueueListBtnABSh);
    }

    @Override
    public void showRadioBottomSheet() {
        mRadioBottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet_radio_mode,
                mBottomSheetContainer, false);
        initRadioBottomSheet(mRadioBottomSheet);
        mBottomSheetContainer.addView(mRadioBottomSheet);
        mPresenter.subscribeRadioBottomSheet();
    }

    private void initRadioBottomSheet(View radioBottomSheet){

        mMediaMenuRBSh = radioBottomSheet.findViewById(R.id.r_bottomsheet_media_menu);
        mPeekTitleRBSh = radioBottomSheet.findViewById(R.id.r_b_sh_peek_title);
        mPeekPlayBtnRBSh = radioBottomSheet.findViewById(R.id.r_b_sh_peek_play_btn);
        mMiddleContainerRBSh = radioBottomSheet.findViewById(R.id.r_b_sh_middle_container);
        mPlayBtnRBSh = radioBottomSheet.findViewById(R.id.r_b_sh_play_btn);
        mFavoriteBtnRBSh = radioBottomSheet.findViewById(R.id.bottomsheet_favorite_button);

        mPeekTitleRBSh.setOnClickListener(v -> expandBottomSheet());
        mPeekPlayBtnRBSh.setOnClickListener(this::onRadioPlayBtnClick);
        mPlayBtnRBSh.setOnClickListener(this::onRadioPlayBtnClick);
        mFavoriteBtnRBSh.setOnClickListener(v -> {
        });
    }

    @Override
    public void clearBottomSheet() {
        mBottomSheetContainer.removeAllViews();
    }

    public void onRadioPlayBtnClick(View v) {
        PlayButton playBtn = (PlayButton) v;
        if (playBtn.getMode() == PlayMode.BUFFERING) {

            mPlayBtnRBSh.setMode(PlayMode.PLAY);
            mPeekPlayBtnRBSh.setMode(PlayMode.PLAY);

        } else if (playBtn.getMode() == PlayMode.PLAY) {

            mPlayBtnRBSh.setMode(PlayMode.BUFFERING);
            mPeekPlayBtnRBSh.setMode(PlayMode.BUFFERING);
        }

        mPresenter.playPause();
    }

    QueueListAdapter.QueueItemClickListener mQueueItemClickListener = (item, position, viewId) -> {
        switch (viewId) {

            case R.id.track_item:
                mPresenter.skipToQueueItem(position);
                break;

            case R.id.set_as_ring_menu:
                setAudioTrackAsRingtone(item);
                break;

            case R.id.remove_from_queue:
                mPresenter.removeQueueItem(item);
                break;

            case R.id.track_delete_menu:
                deleteAudioTrackFromStorage(item);
                break;
        }
    };

    @Override
    public void changeQueueAdapterData(List<QueueItem> list) {
        QueueListAdapter adapter = (QueueListAdapter) mQueueList.getAdapter();
        if (adapter != null) {
            adapter.updateQueueList(list);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void changeQueueAdapterData(int position) {
        QueueListAdapter adapter = (QueueListAdapter) mQueueList.getAdapter();
        if (adapter != null) {
            adapter.setPlayingPosition(position);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void changeQueueAdapterData(boolean isPlaying) {
        QueueListAdapter adapter = (QueueListAdapter) mQueueList.getAdapter();
        if (adapter != null) {
            adapter.setPlaying(isPlaying);
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void showAudioBShPeekTitle(String title) {
        mPeekTitleABSh.setText(title);
    }

    @Override
    public void showAudioBShPeekSubtitle(String subtitle) {
        mPeekSubtitleABSh.setText(subtitle);
    }

    @Override
    public void showRadioBShPeekTitle(String title) {
        mPeekTitleRBSh.setText(title);
    }

    @Override
    public void showAudioBShProgressDurationTime(String duration) {
        mProgressDurationTime.setText(duration);
    }

    @Override
    public void setAudioPlayBtnStatus(boolean isPlaying) {
        mPlayBtnABSh.setChecked(isPlaying);
        mPeekPlayBtnABSh.setChecked(isPlaying);
    }

    @Override
    public void setRadioPlayBtnMode(PlayMode mode) {
        mPeekPlayBtnRBSh.setMode(mode);
        mPlayBtnRBSh.setMode(mode);
    }

    @Override
    public void setShuffleBtnMode(int mode) {
        if(mode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            mShuffleBtnABSh.setActivated(true);
        } else mShuffleBtnABSh.setActivated(false);
    }

    @Override
    public void setRepeatBtnMode(int mode) {
        mRepeatBtnABSh.setRepeatMode(mode);
    }

    @Override
    public boolean isBottomSheetInitialized() {
        return mBottomSheetContainer.getChildCount() > 0;
    }

    @Override
    public void expandBottomSheet() {
        mBSBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void collapseBottomSheet() {
        Log.d("myLog", "MainActivity->collapseBottomSheet");
        mBSBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void setMediaSeekBarNewPlaybackState(PlaybackStateCompat state) {
        if (mMediaSeekBar == null) return;
        mMediaSeekBar.updatePlaybackState(state);
    }

    @Override
    public void setMediaSeekBarNewMetadata(MediaMetadataCompat metadata) {
        if (mMediaSeekBar == null) return;
        mMediaSeekBar.updateMetadata(metadata);
    }

    void setVisibilityPeekPlayBtn(int visibility) {
        if (mPeekPlayBtnRBSh != null) mPeekPlayBtnRBSh.setVisibility(visibility);
        else if (mPeekPlayBtnABSh != null) mPeekPlayBtnABSh.setVisibility(visibility);
    }


    //---------------------------------------------------------------------------

    private class BottomNavigationItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(mBottomSheetContainer);

            switch (menuItem.getItemId()) {
                case R.id.btm_nav_channels:
                    setFragment(StationFragment.newInstance(false));
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;
                case R.id.btm_nav_favorites:
                    setFragment(StationFragment.newInstance(true));
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;
                case R.id.btm_nav_device_tracks:
                    setFragment(DeviceTrackFragment.newInstance());
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;
            }
            return true;
        }
    }

    private class BottomSheetCallBack extends BottomSheetBehavior.BottomSheetCallback {

        @Override
        public void onStateChanged(@NonNull View view, int i) {

            if (!isBottomSheetInitialized()) return;
            switch (i) {
                case BottomSheetBehavior.STATE_COLLAPSED:
                    Log.d("myLog", "MainActivity->BottomSheetCallBack->STATE_COLLAPSED");
                    mPresenter.switchOffAudioBottomSheetPlayList();
                    setVisibilityPeekPlayBtn(View.VISIBLE);
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    setVisibilityPeekPlayBtn(View.GONE);
                    break;
                case BottomSheetBehavior.STATE_DRAGGING:
                    setVisibilityPeekPlayBtn(View.GONE);
                    break;
                case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    setVisibilityPeekPlayBtn(View.GONE);
                    break;
                case BottomSheetBehavior.STATE_HIDDEN:
                    setVisibilityPeekPlayBtn(View.VISIBLE);
                    break;
                case BottomSheetBehavior.STATE_SETTLING:
                    break;
                default:
                    break;
            }

        }

        @Override
        public void onSlide(@NonNull View view, float v) {
        }
    }
}
