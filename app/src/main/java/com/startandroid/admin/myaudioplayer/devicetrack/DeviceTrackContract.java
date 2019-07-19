package com.startandroid.admin.myaudioplayer.devicetrack;

import com.startandroid.admin.myaudioplayer.BasePresenter;
import com.startandroid.admin.myaudioplayer.data.model.Audio;

import java.util.List;

public interface DeviceTrackContract {

    interface View {

        void showMusicList(List<Audio> musicList);

        void updateMusicList(List<Audio> list);

        void setAudioTrackAsRingtone(Audio audioTrack);

        boolean checkWriteSettingsPermission();

        boolean checkExternalStoragePermission();

        void requestWriteSettingsPermission();

        void requestExternalStoragePermission();

        void setPermissionCallBack(PermissionCallBack callBack);

    }

    interface Presenter extends BasePresenter {

        void connectMediaBrowser();

        void disconnectMediaBrowser();

        void loadMusic();

        void deleteMusic(String id);

        void addAllToQueue(boolean clearOldList);

        void addQueueItem(Audio audio, boolean clearOldList);

        void setRingtoneForSetAfterPermission(Audio ringtone);
    }

    interface PermissionCallBack {

        void onExternalStoragePermission(boolean granted);

        void WriteSettingsPermission(boolean granted);
    }

}
