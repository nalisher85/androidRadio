package com.startandroid.admin.myaudioplayer.data.localsource;

import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface RadioStationDao {

    @Query("SELECT * FROM radio_station")
    Flowable<List<RadioStation>> getRadioStationList();

    @Query("SELECT * FROM radio_station WHERE id = :id")
    Single<RadioStation> getRadioStationById(int id);

    @Query("SELECT * FROM radio_station WHERE :field = :fieldValue")
    Flowable<List<RadioStation>> getRadioStationsByField(String field, Boolean fieldValue);

    @Query("SELECT * FROM radio_station WHERE is_favorite = :isFavorite")
    Flowable<List<RadioStation>> getStationsByFavoriteField(Boolean isFavorite);

    @Query("SELECT * FROM radio_station WHERE path = :link")
    Maybe<RadioStation> getRadioStationByLink(String link);

    @Insert
    void insert(RadioStation radioStation);

    @Insert
    long[] insert(List<RadioStation> radioStations);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(RadioStation radioStation);

    @Delete
    void delete (RadioStation radioStation);

    @Query("DELETE FROM radio_station WHERE id = :id")
    void delete (String id);

    @Query("DELETE FROM radio_station")
    void deleteAll ();
}
