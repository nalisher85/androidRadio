package com.startandroid.admin.myaudioplayer.main;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.startandroid.admin.myaudioplayer.client.IMediaBrowser;
import com.startandroid.admin.myaudioplayer.data.MusicDataSource;
import com.startandroid.admin.myaudioplayer.data.RadioStationSource;
import com.startandroid.admin.myaudioplayer.data.model.MediaType;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.disposables.CompositeDisposable;

public class MainActivityPresenter implements MainActivityContract.Presenter {

    private IMediaBrowser mMediaBrowser;
    private RadioStationSource mRadioStationRepository;
    private MusicDataSource mMusicDataSourceRepository;
    private MainActivityContract.View mView;

    /*
     BSh - bottom sheet
     ABSh - audio bottom sheet
     RBSh - radio bottom sheet
    */
    private CompositeDisposable mBShDisposable = new CompositeDisposable();
    private CompositeDisposable mABShDisposable = new CompositeDisposable();
    private CompositeDisposable mQueueListDisposable = new CompositeDisposable();
    private CompositeDisposable mRBShDisposable = new CompositeDisposable();

    private MediaType mCurrantMediaType;
    private boolean mAudioBottomSheetMiddleSwitcher = false;

    public MainActivityPresenter(IMediaBrowser mediaBrowser, RadioStationSource radioRepository,
                                 MusicDataSource musicRepository, MainActivityContract.View view) {
        mMediaBrowser = mediaBrowser;
        mRadioStationRepository = radioRepository;
        mMusicDataSourceRepository = musicRepository;
        mView = view;
        Log.d("myLog", "MainActivityPresenter->created");
    }

    @Override
    public void start() {
        connectMediaBrowser();
    }

    @Override
    public void stop() {
        Log.d("myLog", "MainActivityPresenter->stop");
        mView.destroyBottomSheet();
        mBShDisposable.clear();
        mABShDisposable.clear();
        mQueueListDisposable.clear();
        mRBShDisposable.clear();

        disconnectMediaBrowser();
        Log.d("myLog", "MainActivityPresenter->stop END");
    }

    @Override
    public void onDestroy() {
        Log.d("myLog", "MainActivityPresenter->onDestroy");
        mView = null;
    }


    @Override
    public void connectMediaBrowser() {
        mMediaBrowser.registerConnectionCallback(new MediaBrowserConnectionCallBack());
        mMediaBrowser.connect();
    }

    @Override
    public void disconnectMediaBrowser() {
        mMediaBrowser.disconnect();
    }


    private void subscribeBottomSheet(){

        mBShDisposable.add(
                mMediaBrowser.getMediaControllerSubscription().getQueueItems().subscribe(
                        queueItems -> {
                            if (queueItems.isEmpty()) mView.destroyBottomSheet();
                        }
                )
        );

        mBShDisposable.add(
                mMediaBrowser.getMediaControllerSubscription().getMetadata().subscribe(
                        metadata -> {
                            mView.showBottomSheet();

                            if (mView.isBottomSheetInitialized()){
                                if (isMediaTypeChanged()){
                                    switchBottomSheet();
                                }
                            } else {
                                switchBottomSheet();
                            }

                        }
                )
        );

    }

    private void switchBottomSheet(){
        mView.clearBottomSheet();
        mCurrantMediaType = mMediaBrowser.currantMediaType();

        if (mCurrantMediaType == MediaType.RADIO){
            mView.showRadioBottomSheet();
        } else if (mCurrantMediaType == MediaType.AUDIO){
            mView.showAudioBottomSheet();
        }
    }

    @Override
    public void subscribeAudioBottomSheet() {
        mABShDisposable.add(
                mMediaBrowser.getMediaControllerSubscription().getMetadata().subscribe(
                        metadata -> {
                            mView.showAudioBShPeekTitle((String) metadata.getDescription().getTitle());
                            mView.showAudioBShPeekSubtitle((String)metadata.getDescription().getSubtitle());

                            long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                            String durationMMSS = new SimpleDateFormat("mm:ss")
                                    .format(new Date(duration));
                            mView.showAudioBShProgressDurationTime(durationMMSS);
                        }
                )
        );

        mABShDisposable.add(
                mMediaBrowser.getMediaControllerSubscription().getIsPlaying().subscribe(
                        isPlaying -> mView.setAudioPlayBtnStatus(isPlaying)
                )
        );

        mABShDisposable.add(
                mMediaBrowser.getMediaControllerSubscription().getQueueIndex().subscribe(
                        index -> {
                            if ( index == -1 ){
                                mView.collapseBottomSheet();
                                mView.destroyBottomSheet();
                            }
                        }
                )
        );

        mABShDisposable.add(
                mMediaBrowser.getMediaControllerSubscription().getRepeatMode().subscribe(
                        mode -> mView.setRepeatBtnMode(mode)
                )
        );

        mABShDisposable.add(
                mMediaBrowser.getMediaControllerSubscription().getShuffleMode().subscribe(
                        mode -> mView.setShuffleBtnMode(mode)
                )
        );
    }

    @Override
    public void subscribeRadioBottomSheet() {

        mRBShDisposable.add(
                mMediaBrowser.getMediaControllerSubscription().getMetadata().subscribe(
                        metadata -> {
                            String title = (String) metadata.getDescription().getTitle();
                            mView.showRadioBShPeekTitle(title);
                        }
                )
        );

        mRBShDisposable.add(
                mMediaBrowser.getMediaControllerSubscription().getIsPlaying().subscribe(
                        isPlaying -> {
                            PlayMode mode = isPlaying
                                    ? PlayMode.PAUSE
                                    : PlayMode.PLAY;
                            mView.setRadioPlayBtnMode(mode);
                        }
                )
        );

        mRBShDisposable.add(
                mMediaBrowser.getMediaControllerSubscription().getQueueIndex().subscribe(
                        index -> {
                            if (index == -1) {
                                mView.collapseBottomSheet();
                                mView.destroyBottomSheet();
                            }
                        }
                )
        );
    }

    @Override
    public void subscribeMediaSeekBar() {
        mABShDisposable.add(
                mMediaBrowser.getMediaControllerSubscription().getMetadata().subscribe(
                        metadata -> mView.setMediaSeekBarNewMetadata(metadata)
                )
        );

        mABShDisposable.add(
                mMediaBrowser.getMediaControllerSubscription().getPlaybackState().subscribe(
                        state -> mView.setMediaSeekBarNewPlaybackState(state)
                )
        );
    }


    //mAudioBottomSheetMiddleSwitcher == true ? playlist will show, else will show Image
    @Override
    public void switchAudioBottomSheetMiddle() {
        //if (mView == null) return;

        if (mAudioBottomSheetMiddleSwitcher) {
            mAudioBottomSheetMiddleSwitcher = false;
            mView.clearABShMiddleContainer();
            mView.showAudioBShQueueList();

            mQueueListDisposable.add(
                    mMediaBrowser.getMediaControllerSubscription().getQueueItems().subscribe(
                            list -> mView.changeQueueAdapterData(list)
                    )
            );

            mQueueListDisposable.add(
                    mMediaBrowser.getMediaControllerSubscription().getQueueIndex().subscribe(
                            index -> mView.changeQueueAdapterData(index)
                    )
            );

            mQueueListDisposable.add(
                    mMediaBrowser.getMediaControllerSubscription().getIsPlaying().subscribe(
                            isPlaying -> mView.changeQueueAdapterData((isPlaying))
                    )
            );

        } else {
            Log.d("myLog", "MainActivityPresenter->switchAudioBottomSheetMiddle = false");
            mAudioBottomSheetMiddleSwitcher = true;
            mQueueListDisposable.clear();
            mView.clearABShMiddleContainer();
            mView.showBShMiddleContainerWithImg();
        }
    }

    @Override
    public void switchOffAudioBottomSheetPlayList() {
        Log.d("myLog", "MainActivityPresenter->switchOffAudioBottomSheetPlayList");
        mAudioBottomSheetMiddleSwitcher = false;
        switchAudioBottomSheetMiddle();
    }



    @Override
    public boolean isMediaTypeChanged() {
        return mMediaBrowser.currantMediaType() != mCurrantMediaType;
    }

    @Override
    public void playPause() {
        mMediaBrowser.playPause();
    }

    @Override
    public void skipToPrevious() {
        mMediaBrowser.skipToPrevious();
    }

    @Override
    public void skipToNext() {
        mMediaBrowser.skipToNext();
    }

    @Override
    public void skipToQueueItem(int position) {
        mMediaBrowser.skipToQueueItem(position);
    }

    @Override
    public void setRepeatMode(int repeatMode) {
        mMediaBrowser.setRepeatMode(repeatMode);
    }

    @Override
    public void setShuffleMode(int shuffleMode) {
        mMediaBrowser.setShuffleMode(shuffleMode);
    }

    @Override
    public void removeQueueItem(MediaDescriptionCompat item) {
        mMediaBrowser.removeQueueItem(item);
    }

    @Override
    public void seekMusicTo(int position) {
        mMediaBrowser.seekTo(position);
    }

    @Override
    public void deleteRadioStation(String id) {
        mRadioStationRepository.delete(id);
    }

    @Override
    public void deleteCurrantRadioStation() {
        String id = mMediaBrowser.getCurrantMetadata().getDescription().getMediaId();
        mRadioStationRepository.delete(id);
    }

    @Override
    public void deleteMusic(String id) {
        mMusicDataSourceRepository.deleteMusicById(id);
    }

    @Override
    public void deleteCurrantMusic() {
        String id = mMediaBrowser.getCurrantMetadata().getDescription().getMediaId();
        mMusicDataSourceRepository.deleteMusicById(id);
    }

    @Override
    public void deleteCurrantMediaFromQueue() {
        mMediaBrowser.removeQueueItem(mMediaBrowser.getCurrantMetadata().getDescription());
    }

    @Override
    public void setCurrantMusicAsRingTone() {
        if (mMediaBrowser.currantMediaType() == MediaType.AUDIO){
            MediaDescriptionCompat music = mMediaBrowser.getCurrantMetadata().getDescription();
            mView.setAudioTrackAsRingtone(music);
        }
    }

    class MediaBrowserConnectionCallBack implements IMediaBrowser.ConnectionCallback{

        @Override
        public void onConnected() {
            subscribeBottomSheet();
        }

        @Override
        public void onConnectionSuspended() {

        }

        @Override
        public void onConnectionFailed() {

        }
    }
}
