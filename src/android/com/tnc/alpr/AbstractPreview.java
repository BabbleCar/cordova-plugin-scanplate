package com.tnc.alpr;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.openalpr.AlprJNIWrapper;

abstract public class AbstractPreview extends SurfaceView implements SurfaceHolder.Callback {

    protected String mPath;
    protected AlprJNIWrapper mAlpr;
    public AbstractPreview(Context context, String path) {
        super(context);
        mPath = path;
        getAlpr();
    }
    public interface OnTakeListener {
        void onResult(String plate, Double percent, boolean hasResult);
    }
    abstract public void takeCapture(OnTakeListener listener);
    abstract public void updateFocus(float x, float y);
    abstract public void updateZoom(float value);
    abstract public  void cancelCapture() ;

    public AlprJNIWrapper getAlpr() {
        if (mAlpr == null) {
            mAlpr = AlprJNIWrapper.Factory.create(getContext(), "/data/data/"+getContext().getApplicationContext().getPackageName(), mPath);
            mAlpr.setCountry("eu");
            mAlpr.setTopN(1);
        }

        return mAlpr;
    }
}
