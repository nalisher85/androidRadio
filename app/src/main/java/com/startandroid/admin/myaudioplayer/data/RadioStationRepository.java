package com.startandroid.admin.myaudioplayer.data;


import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public class RadioStationRepository implements RadioStationSource {

    private static RadioStationRepository INSTANCE = null;

    private RadioStationSource mLocalDataSource;
    private RadioStationSource mRemoteDataSource;

    private boolean mLoadDataFromServer = false;

    private RadioStationRepository(RadioStationSource localDataSource, RadioStationSource remoteDataSource) {
        mLocalDataSource = localDataSource;
        mRemoteDataSource = remoteDataSource;
    }

    public static RadioStationRepository getInstance(RadioStationSource localDataSource,
                                                     RadioStationSource remoteDataSource) {
        if (INSTANCE == null){
            INSTANCE = new RadioStationRepository(localDataSource, remoteDataSource);
        }
        return INSTANCE;
    }

    @Override
    public Flowable<List<RadioStation>> getAllRadioStation() {
        Flowable<List<RadioStation>> data;
        if (mLoadDataFromServer) data = mRemoteDataSource.getAllRadioStation();
        else data = mLocalDataSource.getAllRadioStation();
        return data;
    }

    @Override
    public Single<RadioStation> getRadioStationById(int id) {
        return mLocalDataSource.getRadioStationById(id);
    }

    @Override
    public Flowable<List<RadioStation>> getStationsByFavoriteField(Boolean isFavorite) {
        return mLocalDataSource.getStationsByFavoriteField(isFavorite);
    }

    @Override
    public Maybe<RadioStation> getRadioStationByLink(String link) {
        return mLocalDataSource.getRadioStationByLink(link);
    }

    @Override
    public Completable insert(RadioStation radioStation) {
        return mLocalDataSource.insert(radioStation);
    }

    @Override
    public Completable insert(List<RadioStation> radioStations) {
        return mLocalDataSource.insert(radioStations);
    }

    @Override
    public Completable update(RadioStation radioStation) {
        return mLocalDataSource.update(radioStation);
    }

    @Override
    public Completable delete(RadioStation radioStation) {
        return mLocalDataSource.delete(radioStation);
    }

    @Override
    public Completable delete(String id) {
        return mLocalDataSource.delete(id);
    }

    @Override
    public Completable deleteAll() {
        return mLocalDataSource.deleteAll();
    }

    public void loadDataFromServer(boolean loadFromServer){
        mLoadDataFromServer = loadFromServer;
    }

}
