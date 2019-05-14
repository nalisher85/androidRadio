package com.startandroid.admin.myaudioplayer.data;

import android.content.Context;

import java.util.List;

import androidx.room.Room;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MyDbHelper {

    private static MyDataBase database;
    private Context mCtx;

    public MyDbHelper(Context ctx){this.mCtx = ctx;}

    private static MyDataBase getDbInstance(Context context){
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

    public Single<RadioStationModel> getRadioStationById(long id) {
        return getDbInstance(mCtx).radioStationDao().getRadioStationById(id)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Flowable<List<RadioStationModel>> getRadioStationsByField(String field, String fieldValue){
        return getDbInstance(mCtx).radioStationDao().getRadioStationsByField(field, fieldValue)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void insert(RadioStationModel radioStation){
        getDbInstance(mCtx).radioStationDao().insert(radioStation);
    }

    public void insert(List<RadioStationModel> radioStationModels){
        getDbInstance(mCtx).radioStationDao().insert(radioStationModels);
    }

    public  void update(RadioStationModel radioStation){
        getDbInstance(mCtx).radioStationDao().update(radioStation);
    }

    public void delete (RadioStationModel radioStation){
        getDbInstance(mCtx).radioStationDao().delete(radioStation);
    }

}
