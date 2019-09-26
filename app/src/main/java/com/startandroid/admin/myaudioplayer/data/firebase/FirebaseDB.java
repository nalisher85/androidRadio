package com.startandroid.admin.myaudioplayer.data.firebase;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.startandroid.admin.myaudioplayer.MyApplication;
import com.startandroid.admin.myaudioplayer.data.RadioStationSource;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public class FirebaseDB implements RadioStationSource {

    private  static FirebaseDB INSTANCE = null;
    private DatabaseReference mDbRef;

    private FirebaseDB() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDbRef = database.getReference("stations");
        //fillDataBase(mDbRef);
    }

    public static FirebaseDB getInstance(){
        if (INSTANCE == null) {
            INSTANCE = new FirebaseDB();
        }
        return INSTANCE;
    }

    private void fillDataBase(DatabaseReference dbRef) {
        ArrayList<RadioStation> stations = convertJsonToObj(readJsonFile());

        if (stations == null) {
            Log.e("myLog", "FireBaseDB line 59: No data for fill database");
            return;
        }

        for (RadioStation station : stations) {
            dbRef.push().setValue(station);
        }
    }

    private ArrayList<RadioStation> convertJsonToObj(String jsonStr){

        ArrayList<RadioStation> stations =  new ArrayList<>();
        JSONArray jsonArray = null;

        try {
            jsonArray = new JSONArray(jsonStr);
        } catch (JSONException e) {
            Log.e("myLog", "FireBaseDB line 76. "+ e.getMessage());
            e.printStackTrace();
        }

        if (jsonArray != null){
            for (int i = 0; i <  jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    RadioStation station = new RadioStation();

                    station.setStationName(jsonObject.getString("station_name"));
                    station.setPath(jsonObject.getString("station_link"));
                    station.setCountry(jsonObject.getString("station_country"));
                    station.setLanguage(jsonObject.getString("station_language"));

                    stations.add(station);
                } catch (Exception e) {
                    Log.e("myLog", "FireBaseDB line 92. "+ e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return stations;
    }

    private String readJsonFile(){
        StringBuilder stringBuilder = new StringBuilder();

        try {
            InputStream inputStream = MyApplication.getContext().getAssets().open("stations.json");
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    @Override
    public Flowable<List<RadioStation>> getAllRadioStation() {
        return Flowable.create(
                emitter -> {

                    // Read from the database
                    mDbRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            ArrayList<RadioStation> stations = new ArrayList<>();
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                            for (DataSnapshot child : children) {
                                RadioStation station = new RadioStation();

                                station.setStationName(child.child("station_name").getValue(String.class));
                                station.setPath(Objects.requireNonNull(
                                        child.child("station_link").getValue(String.class)
                                ));
                                station.setLanguage(child.child("station_language").getValue(String.class));
                                station.setCountry(child.child("station_country").getValue(String.class));

                                stations.add(station);
                            }

                            emitter.onNext(stations);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("myLog", error.getMessage());
                            emitter.onError(error.toException());
                        }
                    });

                },
                BackpressureStrategy.MISSING
        );
    }

    @Override
    public Single<RadioStation> getRadioStationById(int id) {
        return null;
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
}
