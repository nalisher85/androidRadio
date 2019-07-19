package com.startandroid.admin.myaudioplayer.stationdataforadd;

import com.startandroid.admin.myaudioplayer.BasePresenter;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import java.util.List;

public interface StationsDataForAddContract {

    interface View {

        void showStations(List<RadioStation> stations);

        void showAddStationsBtn();

        void hideAddStationsBtn();

        void showCountryFilterDialog(String[] countries);

        void showLanguageFilterDialog(String[] languages);

    }

    interface Presenter extends BasePresenter {

        void addSelectedStationsToDb();

        void filterStations();

        void filterWithLanguage();

        void filterWithCountry();

        void setLanguageFilter(String language);

        void setCountryFilter(String country);

        void addToSelectedStation(RadioStation item);

        void removeFromSelectedStation(RadioStation item);

        void setAllStationsSelected();

        void clearSelectedStations();

        void onDestroy();

    }

}
