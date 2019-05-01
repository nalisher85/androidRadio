package com.startandroid.admin.myaudioplayer.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;


public class StorageAudioFiles {
    private Context context;

    public StorageAudioFiles(Context ctx) {
        context = ctx.getApplicationContext();
    }

    public List<AudioModel> getStorageAudio(){
        final List<AudioModel> audioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null){
            while (cursor.moveToNext()) {
                String path = cursor.getString(0);
                String name = cursor.getString(1);
                String album = cursor.getString(2);
                String artist = cursor.getString(3);
                AudioModel audioModel = new AudioModel(name, artist, album, path);

                audioList.add(audioModel);
            }
            cursor.close();
        }
        return audioList;
    }
}
