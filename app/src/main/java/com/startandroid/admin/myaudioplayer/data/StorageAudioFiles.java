package com.startandroid.admin.myaudioplayer.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class StorageAudioFiles {

    private final static Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private final static String[] COLUMNS = {MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media._ID};
    private WeakReference<Context> mCtx;
    private ContentResolver mContentResolver;

    public StorageAudioFiles(@NonNull Context ctx) {
        mCtx = new WeakReference<>(ctx);
        mContentResolver = mCtx.get().getContentResolver();
    }

    public void registerContentObserver(ContentObserver observer) {
        mContentResolver.registerContentObserver(URI, true, observer);
    }

    public List<AudioModel> getStorageAudios(String selection, String[] selectionArgs){
        List<AudioModel> audioModels = new ArrayList<>();
        Cursor cursor = mContentResolver.query(URI, COLUMNS, selection, selectionArgs, null);

        if (cursor != null){
            while (cursor.moveToNext()) {

                String path = cursor.getString(0);
                String title = cursor.getString(1);
                String album = cursor.getString(2);
                String artist = cursor.getString(3);
                long duration = cursor.getLong(4);
                String id = cursor.getString(5);

                AudioModel audioModel = new AudioModel(id, title, artist, album, path, duration);
                audioModels.add(audioModel);
            }
            cursor.close();
        }
        return audioModels;
    }

    public AudioModel getAudioById(String audioId) {
        String selection = MediaStore.Audio.Media._ID + " = ?";
        Cursor cursor = mContentResolver.query(URI, COLUMNS, selection,
                new String[]{audioId}, null);
        AudioModel audioModel = new AudioModel();
        if (cursor != null && cursor.moveToNext()) {
            audioModel.setPath(cursor.getString(0));
            audioModel.setName(cursor.getString(1));
            audioModel.setAlbum(cursor.getString(2));
            audioModel.setArtist(cursor.getString(3));
            audioModel.setDuration((int)cursor.getLong(4));
            audioModel.setId(cursor.getString(5));
            cursor.close();
        }
        return audioModel;
    }

    public Maybe<AudioModel> getAudioByIdAsync(String id){

        return Maybe.fromCallable(() -> getAudioById(id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Flowable<List<AudioModel>> getAudiosAsync (String selection, String[] selectionArgs) {
        Flowable<List<AudioModel>> flowable= Flowable.fromCallable(
                () -> getStorageAudios(selection, selectionArgs))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        return flowable;
    }

    public Flowable<List<MediaMetadataCompat>> getAudioMetadatasAsync (String selection, String[] selectionArgs) {
        //noinspection Convert2MethodRef
        return getAudiosAsync(selection, selectionArgs)
                .map(audioModels -> makeMetadataFromAudioModel(audioModels));
    }

    public Maybe<MediaMetadataCompat> getAudioMetadataByIdAsync (String id) {
        //noinspection Convert2MethodRef
        return getAudioByIdAsync(id).map(audioModel -> makeMetadataFromAudioModel(audioModel));
    }

    public void deleteAudioById(String id) {
        String selection = MediaStore.Audio.Media._ID + " = ?";
        mContentResolver.delete(URI, selection, new String[]{id});
        mContentResolver.notifyChange(URI, null);
    }

    @NonNull
    private MediaMetadataCompat makeMetadataFromAudioModel(@NonNull AudioModel audio) {
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder().
                putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, audio.getId()).
                putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, audio.getPath()).
                putString(MediaMetadataCompat.METADATA_KEY_TITLE, audio.getName()).
                putString(MediaMetadataCompat.METADATA_KEY_ALBUM, audio.getAlbum()).
                putString(MediaMetadataCompat.METADATA_KEY_ARTIST, audio.getArtist()).
                putLong(MediaMetadataCompat.METADATA_KEY_DURATION, audio.getDuration());

        return builder.build();
    }

    @NonNull
    private List<MediaMetadataCompat> makeMetadataFromAudioModel(@NonNull List<AudioModel> audios) {
        List<MediaMetadataCompat> metadatas = new ArrayList<>();
        for (AudioModel audio : audios) {
            metadatas.add(makeMetadataFromAudioModel(audio));
        }
        return metadatas;
    }

    //----------------
    public List<MediaMetadataCompat> getAudioMetadatas(String selection, String[] selectionArgs) {
        return makeMetadataFromAudioModel(getStorageAudios(selection, selectionArgs));
    }

    public MediaMetadataCompat getAudioMetadataById (String id) {
        return makeMetadataFromAudioModel(getAudioById(id));
    }
    //-----------

}
