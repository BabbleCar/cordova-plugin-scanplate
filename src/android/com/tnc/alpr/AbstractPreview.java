package com.tnc.alpr;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.openalpr.AlprJNIWrapper;

abstract public class AbstractPreview extends SurfaceView implements SurfaceHolder.Callback {

    protected AlprJNIWrapper mAlpr;
    public AbstractPreview(Context context) {
        super(context);
        initAlpr();
    }
    public interface OnTakeListener {
        void onResult(String plate, Double percent, boolean hasResult);
    }

    abstract public void takeCapture(OnTakeListener listener);
    abstract public void updateFocus(float x, float y);
    abstract public void updateZoom(float value);
    abstract public  void cancelCapture() ;

    public void initAlpr() {
        if (mAlpr == null) {
            mAlpr = new AlprJNIWrapper();
            mAlpr.setCountry("eu");
            mAlpr.setTopN(1);
            mAlpr.setConfigFile("/data/data/com.tagncar.app/runtime_data/openalpr.conf");
            //mAlpr.setConfigFile("/data/data/com.tnc.alpr/runtime_data/openalpr.conf");
        }
    }
}
