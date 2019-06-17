package com.startandroid.admin.myaudioplayer.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;


public class StorageAudioFiles {
    private final static Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private final static String[] COLUMNS = {MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media._ID};
    private WeakReference<Context> mCtx;

    public StorageAudioFiles(@NonNull Context ctx) {
        mCtx = new WeakReference<>(ctx);
    }

    public List<AudioModel> getStorageAudios(String selection, String[] selectionArgs){
        List<AudioModel> audioModels = new ArrayList<>();
        Cursor cursor = mCtx.get().getContentResolver().query(URI, COLUMNS, selection, selectionArgs, null);

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
        Cursor cursor = mCtx.get().getContentResolver().query(URI, COLUMNS, selection,
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

    public Observable<AudioModel> getAudioByIdAsync(String id){
        return Observable.create((ObservableOnSubscribe<AudioModel>)emitter -> {
            try {
                AudioModel audioModel = getAudioById(id);
                if (!emitter.isDisposed()) emitter.onNext(audioModel);
            } catch (Throwable e) {
                emitter.onError(e);
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<AudioModel>> getAudiosAsync (String selection, String[] selectionArgs) {
        return Observable.create((ObservableOnSubscribe<List<AudioModel>>) emitter -> {
            List<AudioModel> audios = getStorageAudios(selection, selectionArgs);
            if(!emitter.isDisposed()) emitter.onNext(audios);
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<MediaMetadataCompat>> getAudioMetadatasAsync (String selection, String[] selectionArgs) {
        //noinspection Convert2MethodRef
        return getAudiosAsync(selection, selectionArgs).map(audioModels ->
                makeMetadataFromAudioModel(audioModels));
    }

    public Observable<MediaMetadataCompat> getAudioMetadataByIdAsync (String id) {
        //noinspection Convert2MethodRef
        return getAudioByIdAsync(id).map(audioModel -> makeMetadataFromAudioModel(audioModel));
    }

    public void deleteAudioById(String id) {
        String selection = MediaStore.Audio.Media._ID + " = ?";
        mCtx.get().getContentResolver().delete(URI, selection, new String[]{id});
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
