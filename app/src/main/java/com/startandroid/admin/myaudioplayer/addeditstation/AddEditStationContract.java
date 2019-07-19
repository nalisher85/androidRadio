package com.startandroid.admin.myaudioplayer.addeditstation;

import com.startandroid.admin.myaudioplayer.BasePresenter;

public interface AddEditStationContract {

    interface View {

        void showStationName(String name);

        void showStationLink(String link);

        void showAddToolbarTitle();

        void showEditToolbarTitle();

        void setStationNameErr(boolean enabled);

        void setStationLinkErr(boolean enabled);

        void finishView();
    }

    interface Presenter extends BasePresenter {

        void saveStation(String name, String link);

        void validationSucceeded();

        void validationFailed();

    }

}
