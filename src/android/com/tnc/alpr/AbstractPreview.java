package com.tnc.alpr;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

abstract public class AbstractPreview extends SurfaceView implements SurfaceHolder.Callback {
    public AbstractPreview(Context context) {
        super(context);
    }

    public interface OnTakeListener {
        void onResult(String plate, Double percent, boolean hasResult);
    }

    abstract public void takeCapture(OnTakeListener listener);
    abstract public  void cancelCapture() ;
}