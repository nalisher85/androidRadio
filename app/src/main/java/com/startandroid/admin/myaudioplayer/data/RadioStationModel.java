package com.startandroid.admin.myaudioplayer.data;

public class RadioStationModel {

    private String stationName;
    private String stationLink;
    private boolean isFavorite;
    private int stationIcon;

    public RadioStationModel (){

    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationLink() {
        return stationLink;
    }

    public void setStationLink(String stationLink) {
        this.stationLink = stationLink;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getStationIcon() {
        return stationIcon;
    }

    public void setStationIcon(int stationIcon) {
        this.stationIcon = stationIcon;
    }


}
