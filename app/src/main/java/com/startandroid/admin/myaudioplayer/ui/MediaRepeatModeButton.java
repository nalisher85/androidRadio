package com.startandroid.admin.myaudioplayer.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.startandroid.admin.myaudioplayer.R;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

public class MediaRepeatModeButton extends AppCompatImageButton {

    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ONE = 1;
    public static final int REPEAT_MODE_ALL = 2;
    private int mode;


    public MediaRepeatModeButton(Context context) {
        super(context);
        init();
    }

    public MediaRepeatModeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MediaRepeatModeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public int getRepeatMode() {
        return mode;
    }

    public void setRepeatMode(int state) {
        this.mode = state;
        update();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);

    }

    private void init(){
        mode = REPEAT_MODE_NONE;
        update();
    }

    private void update(){
        int drawable = R.drawable.ic_repeat_off;
        switch (mode) {
            case REPEAT_MODE_NONE:
                drawable = R.drawable.ic_repeat_off;
                break;
            case REPEAT_MODE_ONE:
                drawable = R.drawable.ic_repeat_one;
                break;
            case REPEAT_MODE_ALL:
                drawable = R.drawable.ic_repeat_all;
                break;
        }
        setImageResource(drawable);
    }
}
