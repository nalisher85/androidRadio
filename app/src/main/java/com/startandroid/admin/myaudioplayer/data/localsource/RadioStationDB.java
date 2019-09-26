package com.startandroid.admin.myaudioplayer.data.localsource;

import android.content.Context;

import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {RadioStation.class}, version = 2)
public abstract class RadioStationDB extends RoomDatabase {

    private static RadioStationDB INSTANCE;

    public abstract RadioStationDao radioStationDao();

    private final static Object mLock = new Object();

    public static final RadioStationDB getInstance(Context context){

        synchronized (mLock){
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        RadioStationDB.class, "my_database")
                        .build();
            }
            return INSTANCE;
        }

    }
}
