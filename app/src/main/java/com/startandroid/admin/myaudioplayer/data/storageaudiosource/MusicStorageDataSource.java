package com.startandroid.admin.myaudioplayer.data.storageaudiosource;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

import com.startandroid.admin.myaudioplayer.MyApplication;
import com.startandroid.admin.myaudioplayer.data.MusicDataSource;
import com.startandroid.admin.myaudioplayer.data.model.Audio;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public class MusicStorageDataSource implements MusicDataSource {

    private static MusicStorageDataSource INSTANCE;

    private final static Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private final static String[] COLUMNS = {MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media._ID};
    private ContentResolver mContentResolver;
    private MusicDataSource.OnDataChangedCallback mOnDataChangedCallback;

    private MusicStorageDataSource(){
        mContentResolver = MyApplication.getContext().getContentResolver();
        registerContentObserver();
    }

    public static MusicStorageDataSource getInstance(){

        if (INSTANCE == null){
            INSTANCE = new MusicStorageDataSource();
        }
        return INSTANCE;
    }

    private void registerContentObserver(){
        ContentObserver contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                if (mOnDataChangedCallback != null){
                    mOnDataChangedCallback.onChanged();
                }
            }
        };
        mContentResolver.registerContentObserver(URI, true, contentObserver);
    }

    @Override
    public Single<List<Audio>> getAllMusic() {

        return Single.create(
                emitter ->  {

                    List<Audio> audioModels = new ArrayList<>();
                    Cursor cursor = mContentResolver.query(URI, COLUMNS, null, null, null);

                    if (cursor != null){
                        while (cursor.moveToNext()) {

                            String path = cursor.getString(0);
                            String title = cursor.getString(1);
                            String album = cursor.getString(2);
                            String artist = cursor.getString(3);
                            long duration = cursor.getLong(4);
                            String id = cursor.getString(5);

                            Audio audioModel = new Audio(id, title, artist, album, path, duration);
                            audioModels.add(audioModel);
                        }
                        cursor.close();
                    }

                    emitter.onSuccess(audioModels);
                }
        );

    }

    @Override
    public Single<Audio> getMusicById(String id) {

        return Single.create(
                emitter -> {

                    String selection = MediaStore.Audio.Media._ID + " = ?";
                    Cursor cursor = mContentResolver.query(URI, COLUMNS, selection,
                            new String[]{id}, null);

                    Audio audioModel = new Audio();
                    if (cursor != null && cursor.moveToNext()) {
                        audioModel.setPath(cursor.getString(0));
                        audioModel.setName(cursor.getString(1));
                        audioModel.setAlbum(cursor.getString(2));
                        audioModel.setArtist(cursor.getString(3));
                        audioModel.setDuration((int) cursor.getLong(4));
                        audioModel.setId(cursor.getString(5));
                        cursor.close();
                    }
                    emitter.onSuccess(audioModel);
                }
        );

    }

    @Override
    public void setOnDataChangedCallback(OnDataChangedCallback callback) {
        mOnDataChangedCallback = callback;
    }

    @SuppressLint("CheckResult")
    @Override
    public Completable deleteMusicById(String id) {
        return Completable.create(
                emitter -> {
                    String selection = MediaStore.Audio.Media._ID + " = ?";
                    mContentResolver.delete(URI, selection, new String[]{id});
                    mContentResolver.notifyChange(URI, null);
                    emitter.onComplete();
                });
    }
}
