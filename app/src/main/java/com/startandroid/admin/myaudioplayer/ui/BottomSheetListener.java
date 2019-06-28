package com.startandroid.admin.myaudioplayer.ui;

import android.view.View;


interface BottomSheetListener {
        void onStateChanged(View view, int i);
        void onSlide(View view, float v);
}
