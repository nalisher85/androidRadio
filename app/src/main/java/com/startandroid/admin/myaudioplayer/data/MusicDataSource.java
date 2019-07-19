package com.startandroid.admin.myaudioplayer.data;

import com.startandroid.admin.myaudioplayer.data.model.Audio;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface MusicDataSource {

    Single<List<Audio>> getAllMusic();

    Single<Audio> getMusicById(String id);

    void setOnDataChangedCallback(OnDataChangedCallback callback);

    Completable deleteMusicById(String id);

    interface OnDataChangedCallback{
        void onChanged();
    }
}
