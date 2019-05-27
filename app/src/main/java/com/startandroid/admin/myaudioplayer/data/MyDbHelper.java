package com.startandroid.admin.myaudioplayer.data;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;

import androidx.room.Room;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MyDbHelper {

    private static MyDataBase database;
    private Context mCtx;

    public MyDbHelper(Context ctx){this.mCtx = ctx;}

    private static MyDataBase getDbInstance(Context context){
        int a=6+7;
        if (database == null)
            database = Room.databaseBuilder(context, MyDataBase.class, "my_database")
                    .fallbackToDestructiveMigration()
                    .build();
        return database;
    }

    public Flowable<List<RadioStationModel>> getRadioStationList(){
        return getDbInstance(mCtx).radioStationDao().getRadioStationList()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<RadioStationModel> getRadioStationById(int id) {
        return getDbInstance(mCtx).radioStationDao().getRadioStationById(id)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public Flowable<List<RadioStationModel>> getStationsByFavoriteField(Boolean isFavorite){
        return getDbInstance(mCtx).radioStationDao().getStationsByFavoriteField(isFavorite)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable insert(RadioStationModel radioStation){
        return Completable.fromCallable(() -> {
            getDbInstance(mCtx).radioStationDao().insert(radioStation);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable insert(List<RadioStationModel> radioStationModels){
        return Completable.fromCallable(() -> {
            getDbInstance(mCtx).radioStationDao().insert(radioStationModels);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable update(RadioStationModel radioStation){
        return Completable.fromCallable(() -> {
            RadioStationModel s = radioStation;
            int b = getDbInstance(mCtx).radioStationDao().update(radioStation);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable delete (RadioStationModel radioStation){
        return Completable.fromCallable(() -> {
            getDbInstance(mCtx).radioStationDao().delete(radioStation);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
