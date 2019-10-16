package com.startandroid.admin.myaudioplayer.radiostation;

import com.startandroid.admin.myaudioplayer.client.IMediaBrowser;
import com.startandroid.admin.myaudioplayer.data.RadioStationSource;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class RadioStationPresenter implements RadioStationContract.Presenter {

    private RadioStationSource mRepository;
    private IMediaBrowser mMediaBrowser;
    private RadioStationContract.View mView;
    private boolean mIsStationFavorite;
    private boolean isMediaBrowserConnected = false;

    private Disposable mDisposable;

    public RadioStationPresenter(RadioStationSource repository, IMediaBrowser mediaBrowser,
                                 RadioStationContract.View view) {
        mRepository = repository;
        mMediaBrowser = mediaBrowser;
        mView = view;
    }

    @Override
    public void start() {
        if(mIsStationFavorite){
            mDisposable = mRepository.getStationsByFavoriteField(true).subscribe(
                    stations -> {
                        mView.setAddButtonVisibility(false);
                        mView.showStationList(stations);
                    }
            );
        } else {
            mDisposable = mRepository.getAllRadioStation().subscribe(
                    stations -> {
                        if (stations.isEmpty()) mView.setAddButtonVisibility(true);
                        else mView.setAddButtonVisibility(false);
                        mView.showStationList(stations);
                    }
            );
        }
    }

    @Override
    public void onDestroy() {
        mView = null;
        mDisposable.dispose();
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
    public void isStationFavorite(boolean isFavorite) {
        mIsStationFavorite = isFavorite;
    }

    @Override
    public void openAddEditStation(String stationId) {
        mView.showAddEditStation(stationId);
    }

    @Override
    public void openStationDataForAdd() {
        mView.showStationsDataForAdd();
    }

    @Override
    public void updateStation(RadioStation station) {
        mRepository.update(Objects.requireNonNull(station)).subscribe();
    }

    @Override
    public void deleteStation(RadioStation station) {
        mRepository.delete(Objects.requireNonNull(station)).subscribe();
    }

    @Override
    public void deleteAllStation() {
        mRepository.deleteAll().subscribe();
    }

    @Override
    public void addToQueueItem(RadioStation station) {
        if (!isMediaBrowserConnected) return;

        mMediaBrowser.clearPlayList();
        mMediaBrowser.addQueueItem(station.convertToMediaDescription());
        mMediaBrowser.prepare();
        mMediaBrowser.play();
    }


    class MediaBrowserConnectionCallBack implements IMediaBrowser.ConnectionCallback {

        @Override
        public void onConnected() {
            isMediaBrowserConnected = true;
        }

        @Override
        public void onConnectionSuspended() {

        }

        @Override
        public void onConnectionFailed() {

        }
    }
}
