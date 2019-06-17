package com.startandroid.admin.myaudioplayer.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {RadioStationModel.class}, version = 2)
public abstract class MyDataBase extends RoomDatabase {
    public abstract RadioStationDao radioStationDao();
}
