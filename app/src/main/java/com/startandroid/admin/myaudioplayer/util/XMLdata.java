package com.startandroid.admin.myaudioplayer.util;

import com.startandroid.admin.myaudioplayer.MyApplication;
import com.startandroid.admin.myaudioplayer.R;

import org.xmlpull.v1.XmlPullParser;

public class XMLdata {

    public static XmlPullParser getData(){
        return MyApplication.getContext().getResources().getXml(R.xml.radiostationdata);
    }

}
