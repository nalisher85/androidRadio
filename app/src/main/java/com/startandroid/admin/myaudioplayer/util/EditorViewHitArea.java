package com.startandroid.admin.myaudioplayer.util;

import android.graphics.Rect;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;

import androidx.annotation.NonNull;

public class EditorViewHitArea {


    public EditorViewHitArea() {
    }

    public static void increaseViewHitArea(@NonNull final View v, TouchDelegateComposite touchDelegate,
                                           int top, int left, int bottom, int right) {
        View parent = (View) v.getParent();
        final Rect rect = new Rect();
        v.getHitRect(rect);

        Log.d("myLog", "increaseViewHitAreaPost Before-> top=" + rect.top + ", bottom=" + rect.bottom
                + ", left=" + rect.left + ", right=" + rect.right);

        rect.top -= DimensionConverter.convertDpToPixel(top, v.getContext());
        rect.left -= DimensionConverter.convertDpToPixel(left, v.getContext());
        rect.bottom += DimensionConverter.convertDpToPixel(bottom, v.getContext());
        rect.right += DimensionConverter.convertDpToPixel(right, v.getContext());
        touchDelegate.addDelegate(new TouchDelegate(rect, v));
        parent.setTouchDelegate(touchDelegate);

        Log.d("myLog", "increaseViewHitAreaPost After-> top=" + rect.top + ", bottom=" + rect.bottom
                + ", left=" + rect.left + ", right=" + rect.right);

    }

    public static void increaseViewHitAreaPost(@NonNull final View v, TouchDelegateComposite touchDelegate,
                                               int top, int left, int bottom, int right) {
        View parent = (View) v.getParent();

        parent.post(() -> {
            final Rect rect = new Rect();
            v.getHitRect(rect);

            rect.top -= DimensionConverter.convertDpToPixel(top, v.getContext());
            rect.left -= DimensionConverter.convertDpToPixel(left, v.getContext());
            rect.bottom += DimensionConverter.convertDpToPixel(bottom, v.getContext());
            rect.right += DimensionConverter.convertDpToPixel(right, v.getContext());
            touchDelegate.addDelegate(new TouchDelegate(rect, v));
            parent.setTouchDelegate(touchDelegate);

        });
    }

}
