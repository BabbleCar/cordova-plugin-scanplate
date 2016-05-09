package com.tnc.alpr;


import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

abstract public class AbstractPreview extends SurfaceView implements SurfaceHolder.Callback {
    public AbstractPreview(Context context) {
        super(context);
    }
}

