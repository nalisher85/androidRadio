package com.startandroid.admin.myaudioplayer.main;

import android.content.Context;
import android.util.AttributeSet;

import com.startandroid.admin.myaudioplayer.R;

import pl.droidsonroids.gif.GifImageButton;

public class PlayButton extends GifImageButton {


    private PlayMode mPlayMode;

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
        mPlayMode = PlayMode.PLAY;
        update();
    }

    public PlayMode getMode() {
        return mPlayMode;
    }

    public void setMode(PlayMode mode) {
        mPlayMode = mode;
        update();
    }

    private void update(){
        int drawable = R.drawable.ic_play2;

        switch (mPlayMode) {
            case PLAY:
                drawable = R.drawable.ic_play2;
                break;
            case PAUSE:
                drawable = R.drawable.ic_pause2;
                break;
            case BUFFERING:
                drawable = R.drawable.ic_play_buffering;
                break;
        }
        setImageResource(drawable);
    }
}
