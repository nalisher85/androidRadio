package com.startandroid.admin.myaudioplayer.main;

import android.annotation.SuppressLint;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.startandroid.admin.myaudioplayer.MyApplication;
import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.model.MediaType;
import com.startandroid.admin.myaudioplayer.util.EditorViewHitArea;
import com.startandroid.admin.myaudioplayer.util.TouchDelegateComposite;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class BottomSheet {

/*
    static final int BOTTOM_SHEET_MODE_AUDIO = 1;
    static final int BOTTOM_SHEET_MODE_RADIO = 2;

    private AudioMode audioType;
    private RadioMode radioType;
    private BottomSheetListener mBottomSheetListener = null;
    private ViewGroup mBottomSheetContainer;
    private final BottomSheetBehavior mBSBehavior;
    private MediaType mCurrantMediaType;

    @SuppressLint("CheckResult")
    public BottomSheet(ViewGroup bottomSheetContainer, AppCompatActivity activity,
                       BottomSheetInitCallBack initCallBack) {
        mBottomSheetContainer = bottomSheetContainer;
        mActivity = activity;
        mInitCallBack = initCallBack;

        mBSBehavior = BottomSheetBehavior.from(mBottomSheetContainer);
        mBSBehavior.setBottomSheetCallback(new BottomSheetCallback());

        mMediaBrowserClient.getMediaControllerSubscription().getMetadata()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        metadata -> {
                            if(isBottomSheetModeChanged()) showBottomSheet();
                        },
                        Throwable::printStackTrace
                );

    }


    void showBottomSheet() {
        if (isBottomSheetInitialized() && !isBottomSheetModeChanged()) return;

        if (getBottomSheetMode() == BOTTOM_SHEET_MODE_AUDIO) {
            mCurrantMediaType = BOTTOM_SHEET_MODE_AUDIO;

            if (radioType != null) {
                radioType.onDestroy();
                radioType = null;
            }
            audioType = new AudioMode(mBottomSheetContainer);

        } else if (getBottomSheetMode() == BOTTOM_SHEET_MODE_RADIO) {
            mCurrantMediaType = BOTTOM_SHEET_MODE_RADIO;

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
        return mCurrantMediaType != getBottomSheetMode();
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

        @BindView(R.id.a_bottomsheet_media_menu)
        ImageButton mMediaMenuABSh;
        @BindView(R.id.a_b_sh_peek_title)
        TextView mPeekTitleABSh;
        @BindView(R.id.a_b_sh_peek_subtitle)
        TextView mPeekSubtitleABSh;
        @BindView(R.id.a_b_sh_peek_play_btn)
        ToggleButton mPeekPlayBtnABSh;


        @BindView(R.id.a_b_sh_middle_container)
        FrameLayout mMiddleContainerABSh;
        @BindView(R.id.queuelist_btn)
        ImageButton mQueueListBtnABSh;

        @BindView(R.id.a_b_sh_play_btn)
        ToggleButton mPlayBtnABSh;
        @BindView(R.id.bottomsheet_prev_button)
        ImageButton mPrevBtnABSh;
        @BindView(R.id.bottomsheet_next_button)
        ImageButton mNextBtnABSh;
        @BindView(R.id.bottomsheet_repeat_btn)
        MediaRepeatModeButton mRepeatBtnABSh;
        @BindView(R.id.bottomsheet_shuffle_btn)
        ImageButton mShuffleBtnABSh;

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

            editorViewHitArea = new EditorViewHitArea(new TouchDelegateComposite(MyApplication.getContext()));
            editorViewHitArea.increaseViewHitAreaPost(mRepeatBtn, 10, 10, 10, 10);
            editorViewHitArea.increaseViewHitAreaPost(mShuffleBtn, 10, 10, 10, 10);

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
                    mMediaBrowserClient.getMediaControllerSubscription().getMetadata().subscribe(
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
                    mMediaBrowserClient.getMediaControllerSubscription().getPlaybackState().subscribe(
                            state -> {
                                boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
                                mPlayBtn.setChecked(isPlaying);
                                mPeekPlayBtn.setChecked(isPlaying);
                            },
                            Throwable::printStackTrace
                    )
            );

            audioBottomSheetDisposable.add(
                    mMediaBrowserClient.getMediaControllerSubscription().getQueueIndex().subscribe(
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

        @OnClick({R.id.a_b_sh_peek_title, R.id.a_b_sh_peek_subtitle})
        void expandBottomSheet(){
            MainActivity.BottomSheet.this.expandBottomSheet();
        }

        @OnClick(R.id.a_bottomsheet_media_menu)
        void showQueueItemMenu(View view){

            PopupMenu menu = new PopupMenu(MainActivity.this, view);
            menu.inflate(R.menu.queue_item_menu);

            menu.setOnMenuItemClickListener(item -> {

                //need to extract queue item
                MediaMetadataCompat metadata = mMediaBrowserClient.getMediaController().getMetadata();
                List<MediaSessionCompat.QueueItem> queueItems = mMediaBrowserClient.getMediaController().getQueue();

                for (MediaSessionCompat.QueueItem qItem : queueItems){

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
                    mMediaBrowserClient.getMediaControllerSubscription().getQueueItems().subscribe(
                            queueItems -> {
                                adapter.updateQueueList(queueItems);
                                mQueueList.setAdapter(adapter);
                            },
                            Throwable::printStackTrace
                    )
            );

            queueListDisposable.add(
                    mMediaBrowserClient.getMediaControllerSubscription().getQueueIndex()
                            .subscribe(index -> {
                                adapter.setPlayingPosition(index);
                                adapter.notifyDataSetChanged();
                            })
            );

            queueListDisposable.add(
                    mMediaBrowserClient.getMediaControllerSubscription().getIsPlaying().subscribe(
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
        public void onItemClickListener(MediaDescriptionCompat item, int pos, int viewId) {

            switch (viewId) {

                case R.id.track_item:
                    mMediaBrowserClient.getMediaController().getTransportControls().skipToQueueItem(pos);
                    break;

                case R.id.set_as_ring_menu:
                    setAudioTrackAsRingtone(item);
                    break;

                case R.id.remove_from_queue:
                    mMediaBrowserClient.removeQueueItem(item);
                    break;

                case R.id.track_delete_menu:
                    deleteAudioTrackFromStorage(item);
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

        @BindView(R.id.r_bottomsheet_media_menu)
        ImageButton mMediaMenuRBSh;
        @BindView(R.id.r_b_sh_peek_title)
        TextView mPeekTitleRBSh;
        @BindView(R.id.r_b_sh_peek_play_btn)
        PlayButton mPeekPlayBtnRBSh;

        @BindView(R.id.r_b_sh_middle_container)
        FrameLayout mMiddleContainerRBSh;

        @BindView(R.id.r_b_sh_play_btn)
        PlayButton mPlayBtnRBSh;
        @BindView(R.id.bottomsheet_favorite_button)
        ToggleButton mFavoriteBtnRBSh;

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
                    mMediaBrowserClient.getMediaControllerSubscription().getMetadata()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    metadata -> {
                                        mPeekTitle.setText(metadata.getDescription().getTitle());
                                    },
                                    Throwable::printStackTrace
                            )
            );

            mDisposable.add(
                    mMediaBrowserClient.getMediaControllerSubscription().getPlaybackState()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    state -> {
                                        Log.d("mediaPlayer", "getMediaControllerSubscription changed="+state);
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

        @OnClick(R.id.a_b_sh_peek_title)
        void collapseBottomSheet(){
            mBSBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        @OnClick({R.id.a_b_sh_peek_play_btn, R.id.a_b_sh_play_btn})
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
    */
}


