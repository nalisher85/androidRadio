package com.startandroid.admin.myaudioplayer.util;

import android.net.Uri;
import android.support.v4.media.MediaDescriptionCompat;

import com.startandroid.admin.myaudioplayer.data.model.Audio;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

public class MediaConverter {

    public static MediaDescriptionCompat radioModelToMediaDescription(RadioStation model){
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                .setMediaId("" + model.getId())
                .setTitle(model.getStationName())
                .setMediaUri(UrlConverter.UrlToUri(model.getPath()));
        return builder.build();
    }

    public static MediaDescriptionCompat audioModelToMediaDescription(Audio model){
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                .setMediaId(model.getId())
                .setTitle(model.getName())
                .setSubtitle(model.getArtist())
                .setMediaUri(Uri.parse(model.getPath()));
        return builder.build();
    }

}
