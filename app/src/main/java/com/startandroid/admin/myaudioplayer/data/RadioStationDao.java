package com.startandroid.admin.myaudioplayer.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface RadioStationDao {

    @Query("SELECT * FROM radio_station")
    Flowable<List<RadioStationModel>> getRadioStationList();

    @Query("SELECT * FROM radio_station WHERE id = :id")
    Single<RadioStationModel> getRadioStationById(int id);

    @Query("SELECT * FROM radio_station WHERE :field = :fieldValue")
    Flowable<List<RadioStationModel>> getRadioStationsByField(String field, Boolean fieldValue);

    @Query("SELECT * FROM radio_station WHERE is_favorite = :isFavorite")
    Flowable<List<RadioStationModel>> getStationsByFavoriteField(Boolean isFavorite);


    @Insert
    public void insert(RadioStationModel radioStation);

    @Insert
    public long[] insert(List<RadioStationModel> radioStationModels);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public int update(RadioStationModel radioStation);

    @Delete
    public void delete (RadioStationModel radioStation);

}
