package com.startandroid.admin.myaudioplayer.data.remotesource;

import android.graphics.Rect;

import com.startandroid.admin.myaudioplayer.data.RadioStationSource;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import org.reactivestreams.Subscriber;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import retrofit2.Call;
import retrofit2.Callback;

public class RadioStationRemoteDataSource implements RadioStationSource {

    private static RadioStationRemoteDataSource INSTANCE = null;
    private static StationApi api;

    private RadioStationRemoteDataSource() {
        api = Apis.getStationApi();
    }

    public static RadioStationRemoteDataSource getInstance() {

        if (INSTANCE == null){
            INSTANCE = new RadioStationRemoteDataSource();
        }
        return INSTANCE;
    }


    @Override
    public Flowable<List<RadioStation>> getAllRadioStation() {
        return Flowable.create(
                emitter -> api.getAllStations().enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        Response stationResponse = response.body();
                        if (stationResponse != null){
                            emitter.onNext(stationResponse.getResult());
                        }
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        emitter.onError(t);
                    }
                }),
                BackpressureStrategy.MISSING
        );
    }

    @Override
    public Single<RadioStation> getRadioStationById(int id) {
        return Single.create(new SingleOnSubscribe<RadioStation>() {
            @Override
            public void subscribe(SingleEmitter<RadioStation> emitter) throws Exception {

                api.getStationById(id).enqueue(new Callback<RadioStation>() {
                    @Override
                    public void onResponse(Call<RadioStation> call, retrofit2.Response<RadioStation> response) {
                        RadioStation station = response.body();
                        if (station != null){
                            emitter.onSuccess(station);
                        }
                    }

                    @Override
                    public void onFailure(Call<RadioStation> call, Throwable t) {
                        emitter.onError(t);
                    }
                });
            }
        });
    }

    @Override
    public Flowable<List<RadioStation>> getStationsByFavoriteField(Boolean isFavorite) {
        return null;
    }

    @Override
    public Maybe<RadioStation> getRadioStationByLink(String link) {
        return null;
    }

    @Override
    public Completable insert(RadioStation radioStation) {
        return null;
    }

    @Override
    public Completable insert(List<RadioStation> radioStations) {
        return null;
    }

    @Override
    public Completable update(RadioStation radioStation) {
        return null;
    }

    @Override
    public Completable delete(RadioStation radioStation) {
        return null;
    }

    @Override
    public Completable delete(String id) {
        return null;
    }

    @Override
    public Completable deleteAll() {
        return null;
    }
}
