package com.startandroid.admin.myaudioplayer.main;

import android.view.View;


interface BottomSheetListener {
        void onStateChanged(View view, int i);
        void onSlide(View view, float v);
}
