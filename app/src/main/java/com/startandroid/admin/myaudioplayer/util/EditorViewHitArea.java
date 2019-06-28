package com.startandroid.admin.myaudioplayer.util;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;

import androidx.annotation.NonNull;

public class EditorViewHitArea {

    private TouchDelegateComposite mTouchDelegate;

    public EditorViewHitArea(TouchDelegateComposite touchDelegate) {
        mTouchDelegate = touchDelegate;
    }

    public void increaseViewHitArea(@NonNull final View v, int top, int left, int bottom, int right) {
        View parent = (View) v.getParent();

        parent.post(() -> {
            final Rect rect = new Rect();
            v.getHitRect(rect);
            rect.top -= DimensionConverter.convertDpToPixel(top, v.getContext());
            rect.left -= DimensionConverter.convertDpToPixel(left, v.getContext());
            rect.bottom += DimensionConverter.convertDpToPixel(bottom, v.getContext());
            rect.right += DimensionConverter.convertDpToPixel(right, v.getContext());
            mTouchDelegate.addDelegate(new TouchDelegate(rect, v));
            parent.setTouchDelegate(mTouchDelegate);
        });
    }

}
