package com.startandroid.admin.myaudioplayer.addeditstation;

import android.annotation.SuppressLint;

import com.startandroid.admin.myaudioplayer.data.RadioStationRepository;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

public class AddEditStationPresenter implements AddEditStationContract.Presenter {

    private boolean isDataValid;
    private RadioStationRepository mRepository;
    private String mStationId;
    private AddEditStationContract.View mView;
    private RadioStation mRadioStation = null;

    public AddEditStationPresenter(String stationId, RadioStationRepository repository,
                                   AddEditStationContract.View addEditStationView) {
        mStationId = stationId;
        mRepository = repository;
        mView = addEditStationView;
    }

    /*
     * TODO: Везде создать методы connect/disconnect mediabrowser
     */

    @SuppressLint("CheckResult")
    @Override
    public void start() {
        if (isNew()){
            isDataValid = false;
            mView.showAddToolbarTitle();
        } else {
            isDataValid = true;
            mView.showEditToolbarTitle();

            mRepository.getRadioStationById(Integer.parseInt(mStationId)).subscribe(station -> {
                mRadioStation = station;
                mView.showStationName(station.getStationName());
                mView.showStationLink(station.getPath());
            });
        }
    }

    @Override
    public void onDestroy() {
        mView = null;
    }

    @Override
    public void saveStation(String name, String link) {
        if (!isDataValid) return;

        if(isNew()){
            addNewStation(name, link);
        } else {
            updateStation(name, link);
        }
    }

    @Override
    public void validationSucceeded() {
        isDataValid = true;
        mView.setStationLinkErr(false);
        mView.setStationNameErr(false);
    }

    @Override
    public void validationFailed() {
        isDataValid = false;
    }

    private boolean isNew(){
        return mStationId == null;
    }

    @SuppressLint("CheckResult")
    private void updateStation(String name, String link){
        if (mRadioStation != null) {
            mRadioStation.setStationName(name);
            mRadioStation.setPath(link);

            mRepository.update(mRadioStation).subscribe(
                    () -> mView.finishView(),
                    Throwable::printStackTrace // TODO: Show error message toast
            );
        }
    }

    @SuppressLint("CheckResult")
    private void addNewStation(String name, String link){
        RadioStation station = new RadioStation();
        station.setStationName(name);
        station.setPath(link);
        station.setFavorite(false);

        mRepository.insert(station).subscribe(
                () -> mView.finishView(),
                Throwable::printStackTrace // TODO: Show error message toast
        );
    }

}
