package com.startandroid.admin.myaudioplayer.data.localsource;

import android.annotation.SuppressLint;
import android.content.Context;

import com.startandroid.admin.myaudioplayer.MyApplication;
import com.startandroid.admin.myaudioplayer.data.RadioStationSource;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RadioStationLocalDataSource implements RadioStationSource {

    private static RadioStationLocalDataSource INSTANCE = null;

    private static RadioStationDB mDatabase;


    private RadioStationLocalDataSource(Context ctx){
        mDatabase = RadioStationDB.getInstance(ctx);
    }

    public static RadioStationLocalDataSource getInstance(){

        if (INSTANCE == null) {
            INSTANCE = new RadioStationLocalDataSource(MyApplication.getContext());
        }
        return INSTANCE;
    }

    @Override
    public Flowable<List<RadioStation>> getAllRadioStation(){
        return mDatabase.radioStationDao().getRadioStationList()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<RadioStation> getRadioStationById(int id) {
        return mDatabase.radioStationDao().getRadioStationById(id)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Flowable<List<RadioStation>> getStationsByFavoriteField(Boolean isFavorite){
        return mDatabase.radioStationDao().getStationsByFavoriteField(isFavorite)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Maybe<RadioStation> getRadioStationByLink(String link) {
        return mDatabase.radioStationDao().getRadioStationByLink(link)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @SuppressLint("CheckResult")
    @Override
    public Completable insert(RadioStation radioStation){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    mDatabase.radioStationDao().insert(radioStation);
                    emitter.onComplete();
                } catch (Exception err) {
                    emitter.onError(err);
                }

            }
        }).subscribeOn(Schedulers.io());
    }

    @SuppressLint("CheckResult")
    @Override
    public Completable insert(List<RadioStation> radioStations){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    mDatabase.radioStationDao().insert(radioStations);
                    emitter.onComplete();
                } catch (Exception err) {
                    emitter.onError(err);
                }

            }
        }).subscribeOn(Schedulers.io());
    }

    @SuppressLint("CheckResult")
    @Override
    public Completable update(RadioStation radioStation){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    mDatabase.radioStationDao().update(radioStation);
                    emitter.onComplete();
                } catch (Exception err) {
                    emitter.onError(err);
                }

            }
        }).subscribeOn(Schedulers.io());
    }

    @SuppressLint("CheckResult")
    @Override
    public Completable delete(RadioStation radioStation) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    mDatabase.radioStationDao().delete(radioStation);
                    emitter.onComplete();
                } catch (Exception err) {
                    emitter.onError(err);
                }

            }
        }).subscribeOn(Schedulers.io());
    }

    @SuppressLint("CheckResult")
    @Override
    public Completable delete (String id){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    mDatabase.radioStationDao().delete(id);
                    emitter.onComplete();
                } catch (Exception err) {
                    emitter.onError(err);
                }

            }
        }).subscribeOn(Schedulers.io());
    }

    @SuppressLint("CheckResult")
    @Override
    public Completable deleteAll (){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    mDatabase.radioStationDao().deleteAll();
                    emitter.onComplete();
                } catch (Exception err) {
                    emitter.onError(err);
                }

            }
        }).subscribeOn(Schedulers.io());
    }

}
