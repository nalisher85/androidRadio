package com.startandroid.admin.myaudioplayer.stationdataforadd;

import com.startandroid.admin.myaudioplayer.BasePresenter;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import java.util.List;

public interface StationsDataForAddContract {

    interface View {

        void showStations(List<RadioStation> stations);

        void showAddStationsBtn();

        void showMessage(String msg);

        void hideAddStationsBtn();

        void showCountryFilterDialog(List<String> countries);

        void showLanguageFilterDialog(List<String> languages);

        void setCheckAllChbx(boolean checked);

    }

    interface Presenter extends BasePresenter {

        void addSelectedStationsToLocalDb();

        void filterStations();

        void filterWithLanguage();

        void filterWithCountry();

        void setLanguageFilter(String language);

        void setCountryFilter(String country);

        void addToSelectedStation(RadioStation item);

        void removeFromSelectedStation(RadioStation item);

        void setAllStationsSelected();

        void clearSelectedStations();

    }

}
