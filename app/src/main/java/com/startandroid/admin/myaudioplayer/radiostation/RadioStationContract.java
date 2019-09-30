package com.startandroid.admin.myaudioplayer.radiostation;

import com.startandroid.admin.myaudioplayer.BasePresenter;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import java.util.List;

public interface RadioStationContract {

    interface Presenter extends BasePresenter {

        void connectMediaBrowser();

        void disconnectMediaBrowser();

        void isStationFavorite(boolean isFavorite);

        void openAddEditStation(String stationId);

        void openStationDataForAdd();

        void updateStation(RadioStation station);

        void deleteStation(RadioStation station);

        void addToQueueItem(RadioStation station);

        void onDestroy();

    }

    interface View {

        void showStationList(List<RadioStation> list);

        void showAddEditStation(String stationId);

        void showStationsDataForAdd();

        void setAddButtonVisibility(boolean isVisible);
    }

}
