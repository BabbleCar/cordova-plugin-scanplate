package com.tnc.alpr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;

import org.openalpr.AlprJNIWrapper;
import org.openalpr.model.Result;
import org.openalpr.model.Results;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static android.hardware.Camera.*;
import static android.hardware.Camera.CameraInfo.*;

class PreviewOld extends AbstractPreview {

    private final FrameLayout mFl1;
    SurfaceHolder mHolder;
    private Camera mCamera;
    private AlprJNIWrapper mAlpr;
    private OnTakeListener mListener;
    private byte[] mBuffer;
    private boolean isTake = false;
    private int mDegrees = 0;
    private byte[] mData;
    private String mPlate;
    private double mPercent;
    private boolean mHasResult;
    private CameraInfo mCameraInfo;

    PreviewOld(Context context) {
        super(context);
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        initAlpr();
        mFl1 = (FrameLayout) ((Activity) context).findViewById(getResources().getIdentifier("fl1", "id", context.getPackageName()));
    }

    @Override
    public void takeCapture(OnTakeListener listener) {
        if (isTake==false) {
            isTake = true;
            mListener = listener;
            PictureCallback callbackJpeg = new PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    mData = data;
                    new Thread(new Runnable() {
                        public void run() {
                            Bitmap picture = BitmapFactory.decodeByteArray(mData, 0, mData.length);
                            int ww = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();
                            int hh = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight();

                            Matrix mat = new Matrix();
                            mat.postRotate(mDegrees);
                            picture = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), mat, true);
                            picture = Bitmap.createBitmap(picture,
                                    ((int) mFl1.getX() * picture.getWidth() / ww),
                                    ((int) mFl1.getY() * picture.getHeight() / hh),
                                    (mFl1.getWidth() * picture.getWidth() / ww),
                                    (mFl1.getHeight() * picture.getHeight() / hh));

                            int size = picture.getRowBytes() * picture.getHeight();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                            picture.copyPixelsToBuffer(byteBuffer);
                            Results res = mAlpr.recognize(byteBuffer.array(), 4, picture.getWidth(), picture.getHeight());
                            mPlate = "";
                            mPercent = 0.0;
                            mHasResult = false;
                            if (!res.getResults().isEmpty()) {
                                Result ret = res.getResults().get(0);
                                mPlate = ret.getPlate();
                                mPercent = ret.getConfidence();
                                mHasResult = true;
                            }

                            ((Activity) getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mListener.onResult(mPlate, mPercent, mHasResult);
                                }
                            });
                            isTake = false;
                        }}).start();
                }
            };
            mCamera.takePicture(null, null, callbackJpeg);
        }
    }

    @Override
    public void cancelCapture() {
        if(mCamera != null) mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        InitCameraDefaults();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        ConfigureCameraDefaults();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCameraAndPreview();
    }

    public void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void initAlpr() {
        if (mAlpr == null) {
            mAlpr = new AlprJNIWrapper();
            mAlpr.setCountry("eu");
            mAlpr.setTopN(1);
            mAlpr.setConfigFile("/data/data/com.tagncar.app/runtime_data/openalpr.conf");
            //mAlpr.setConfigFile("/data/data/com.tnc.alpr/runtime_data/openalpr.conf");
        }
    }

    void InitCameraDefaults() {
        int finalCamId = 0;
        releaseCameraAndPreview();
        mCameraInfo = new CameraInfo();
        int cameraCount = getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            getCameraInfo(camIdx, mCameraInfo);
            if (mCameraInfo.facing == CAMERA_FACING_BACK) {
                finalCamId = camIdx;
                break;
            }
        }

        try {
            mCamera = open(finalCamId);
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Camera.Parameters oParameters=mCamera.getParameters();
        oParameters.setPreviewFormat(ImageFormat.NV21);
        oParameters.setPictureFormat(ImageFormat.JPEG);
        List<Camera.Size> previewSizes = oParameters.getSupportedPreviewSizes();
        Camera.Size previewSize = previewSizes.get(0);
        oParameters.setPreviewSize(previewSize.width, previewSize.height);
        oParameters.setPictureSize(previewSize.width, previewSize.height);
        int bpp = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        int iBufSize = (int) (previewSize.width*previewSize.height*((float)bpp));
        mBuffer = new byte[iBufSize];
        mCamera.addCallbackBuffer(mBuffer);
        mCamera.setParameters(oParameters);
    }
    void ConfigureCameraDefaults() {
        switch (((Activity) this.getContext()).getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                mDegrees = (mCameraInfo.orientation - 0 + 360) % 360;
                break;
            case Surface.ROTATION_90:
                mDegrees = (mCameraInfo.orientation - 90 + 360) % 360;
                break;
            case Surface.ROTATION_180:
                mDegrees = (mCameraInfo.orientation - 180 + 360) % 360;
                break;
            case Surface.ROTATION_270:
                mDegrees = (mCameraInfo.orientation - 270 + 360) % 360;
                break;
        }

        mCamera.setDisplayOrientation(mDegrees);
        mCamera.startPreview();
    }
}
