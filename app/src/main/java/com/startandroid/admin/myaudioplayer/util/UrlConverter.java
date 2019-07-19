package com.startandroid.admin.myaudioplayer.util;

import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

public class UrlConverter {

    public static Uri UrlToUri(String urlString){
        URL url = null;
        Uri.Builder builder = new Uri.Builder();
        try {
            urlString = URLDecoder.decode(urlString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (url != null) {
            builder.scheme(url.getProtocol())
                    .encodedAuthority(url.getAuthority())
                    .path(url.getPath());
        }
        return builder.build();
    }

}
