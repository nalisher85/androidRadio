package com.startandroid.admin.myaudioplayer.data.remotesource;

import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface StationApi {

    @GET("stations")
    Call<Response> getAllStations();

    @GET("stations/{id}")
    Call<RadioStation> getStationById(@Path("id") int id);

}
