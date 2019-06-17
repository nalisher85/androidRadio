package com.startandroid.admin.myaudioplayer.data;

import android.support.v4.media.MediaMetadataCompat;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity(tableName = "radio_station")
public class RadioStationModel implements Serializable {

    @PrimaryKey(autoGenerate = true)
     private int id;

    @ColumnInfo(name = "station_name")
    private String stationName;

    @ColumnInfo(name = "path")
    private String path;

    @ColumnInfo(name = "country")
    private String country;

    @ColumnInfo(name = "language")
    private String language;

    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;

    @Ignore
    private int stationIcon;

    public RadioStationModel(){}

    public RadioStationModel (String stationName,
                              @NonNull String path, boolean isFavorite){
        this.stationName = stationName;
        this.path = path;
        this.isFavorite = isFavorite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String uri) {
        this.path = uri;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getStationIcon() {
        return stationIcon;
    }

    public void setStationIcon(int stationIcon) {
        this.stationIcon = stationIcon;
    }

    public MediaMetadataCompat convertToMetadata(){
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, ""+id)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, path)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, stationName)
                .build();
    }

}
