package com.startandroid.admin.myaudioplayer.data.remotesource;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Apis {

    private static final String BASE_URL = "http://192.168.43.18:80/myradio/public/";

    private static Retrofit getRetrofitInstance(){
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static StationApi getStationApi(){
        return getRetrofitInstance().create(StationApi.class);
    }

}
