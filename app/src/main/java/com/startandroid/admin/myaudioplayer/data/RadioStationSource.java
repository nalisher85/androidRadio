package com.startandroid.admin.myaudioplayer.data;

import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public interface RadioStationSource {

    Flowable<List<RadioStation>> getAllRadioStation();

    Single<RadioStation> getRadioStationById(int id);

    Flowable<List<RadioStation>> getStationsByFavoriteField(Boolean isFavorite);

    Maybe<RadioStation> getRadioStationByLink(String link);

    Completable insert(RadioStation radioStation);

    Completable insert(List<RadioStation> radioStations);

    Completable update(RadioStation radioStation);

    Completable delete (RadioStation radioStation);

    Completable delete(String id);
}
