package com.startandroid.admin.myaudioplayer.devicetrack;

import com.startandroid.admin.myaudioplayer.client.IMediaBrowser;
import com.startandroid.admin.myaudioplayer.data.MusicDataSource;
import com.startandroid.admin.myaudioplayer.data.model.Audio;

import java.util.List;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class DeviceTrackPresenter implements DeviceTrackContract.Presenter {

    private MusicDataSource mRepository;
    private IMediaBrowser mMediaBrowser;
    private DeviceTrackContract.View mView;

    private List<Audio> mMusicList;
    private Disposable mAudioListSubscriber;

    private Audio mRingtoneForSetAfterPermission = null;

    public DeviceTrackPresenter(MusicDataSource repository, IMediaBrowser mediaBrowser,
                                DeviceTrackContract.View view) {
        mRepository = repository;
        mMediaBrowser = mediaBrowser;
        mView = view;

        mView.setPermissionCallBack(new PermissionCallBack());
        mRepository.setOnDataChangedCallback(this::updateMusicList);
    }

    @Override
    public void start() {
        loadMusic();
    }

    @Override
    public void onDestroy() {
        mView = null;
        mAudioListSubscriber.dispose();
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

    @Override
    public void loadMusic() {
        if (mView.checkExternalStoragePermission()) {
            mAudioListSubscriber = mRepository.getAllMusic().subscribe(
                    musicList -> {
                        mMusicList = musicList;
                        mView.showMusicList(musicList);
                    }
            );
        } else {
            mView.requestExternalStoragePermission();
        }
    }

    public void updateMusicList(){
        if (mView.checkExternalStoragePermission()) {
            mAudioListSubscriber = mRepository.getAllMusic().subscribe(
                    musicList -> {
                        mMusicList = musicList;
                        mView.updateMusicList(musicList);
                    }
            );
        } else {
            mView.requestExternalStoragePermission();
        }
    }

    @Override
    public void deleteMusic(String id) {
        Objects.requireNonNull(id);
        mRepository.deleteMusicById(id).subscribe();
    }

    @Override
    public void addAllToQueue(boolean clearOldList) {
        if (clearOldList) mMediaBrowser.clearPlayList();

        for (Audio audio : mMusicList) {
            mMediaBrowser.addQueueItem(audio.convertToMediaDescription());
        }

        mMediaBrowser.prepare();
        if (clearOldList) mMediaBrowser.play();
    }

    @Override
    public void addQueueItem(Audio audio, boolean clearOldList) {

        if (clearOldList) {

            mMediaBrowser.clearPlayList();
            mMediaBrowser.addQueueItem(audio.convertToMediaDescription());
            mMediaBrowser.prepare();
            mMediaBrowser.play();

        } else mMediaBrowser.addQueueItem(audio.convertToMediaDescription());
    }

    @Override
    public void setRingtoneForSetAfterPermission(Audio ringtone) {
        mRingtoneForSetAfterPermission = ringtone;
    }


    class PermissionCallBack implements DeviceTrackContract.PermissionCallBack {

        PermissionCallBack() { }

        @Override
        public void onExternalStoragePermission(boolean granted) {
            if (granted) {
                loadMusic();
            }
        }

        @Override
        public void WriteSettingsPermission(boolean granted) {
            if (granted && mRingtoneForSetAfterPermission != null) {
                mView.setAudioTrackAsRingtone(mRingtoneForSetAfterPermission);
                mRingtoneForSetAfterPermission = null;
            }
        }
    }

    class MediaBrowserConnectionCallBack implements IMediaBrowser.ConnectionCallback {

        @Override
        public void onConnected() {

        }

        @Override
        public void onConnectionSuspended() {

        }

        @Override
        public void onConnectionFailed() {

        }
    }
}
