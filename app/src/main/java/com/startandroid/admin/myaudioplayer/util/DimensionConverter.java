package com.startandroid.admin.myaudioplayer.util;

import android.content.Context;
import android.util.DisplayMetrics;

import com.startandroid.admin.myaudioplayer.MyApplication;

import androidx.fragment.app.Fragment;

public class DimensionConverter {

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

}
