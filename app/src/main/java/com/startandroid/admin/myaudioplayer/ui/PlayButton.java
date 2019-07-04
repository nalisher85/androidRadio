package com.startandroid.admin.myaudioplayer.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.startandroid.admin.myaudioplayer.R;

import pl.droidsonroids.gif.GifImageButton;

public class PlayButton extends GifImageButton {

    public static final int PLAY_MODE = 0;
    public static final int PAUSE_MODE = 1;
    public static final int BUFFERING_MODE = 2;

    private int mMode;

    public PlayButton(Context context) {
        super(context);
        init();
    }

    public PlayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        mMode = PLAY_MODE;
        update();
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        mMode = mode;
        update();
    }

    private void update(){
        int drawable = R.drawable.ic_play2;

        switch (mMode) {
            case PLAY_MODE:
                drawable = R.drawable.ic_play2;
                break;
            case PAUSE_MODE:
                drawable = R.drawable.ic_pause2;
                break;
            case BUFFERING_MODE:
                drawable = R.drawable.ic_play_buffering;
                break;
        }
        setImageResource(drawable);
    }
}
