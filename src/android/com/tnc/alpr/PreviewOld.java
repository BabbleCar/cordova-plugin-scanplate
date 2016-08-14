package com.tnc.alpr;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;

import org.openalpr.model.Result;
import org.openalpr.model.Results;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.hardware.Camera.*;
import static android.hardware.Camera.CameraInfo.*;

class PreviewOld extends AbstractPreview {
    SurfaceHolder mHolder;
    private Camera mCamera;
    private OnTakeListener mListener;
    private byte[] mBuffer;
    private boolean isTake = false;
    private int mDegrees = 0;
    private byte[] mData;
    private String mPlate;
    private double mPercent;
    private boolean mHasResult;
    private CameraInfo mCameraInfo;
    private int mZ = 0;
    private int mRectCaptX;
    private int mRectCaptY;
    private int mRectCaptH;
    private int mRectCaptW;
    private int ww;
    private int hh;

    PreviewOld(Context context, String path) {
        super(context, path);
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public static float convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);

        return Math.round(px);
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
                            Matrix mat = new Matrix();
                            mat.postRotate(mDegrees);
                            picture = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), mat, true);

                            int pw = picture.getWidth();
                            int ph = picture.getHeight();
                            picture = Bitmap.createBitmap(picture,
                                    (mRectCaptX * pw / ww),
                                    (mRectCaptY * ph / hh),
                                    (mRectCaptW * pw / ww),
                                    (mRectCaptH * ph / hh));
                            //picture
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
                        }
                    }).start();
                }
            };
            mCamera.takePicture(null, null, callbackJpeg);
        }
    }

    @Override
    public void updateZoom(float value){
        Camera.Parameters parameters = mCamera.getParameters();
        if (mCamera != null) {
            int z = (int) ((value*parameters.getMaxZoom())/100);
            mZ=mZ+z;
            mZ = (mZ > parameters.getMaxZoom()) ? parameters.getMaxZoom() : ((mZ < 0) ? 0 : mZ);
            if(parameters.isSmoothZoomSupported()) {
                mCamera.startSmoothZoom(mZ);
            } else if(parameters.isZoomSupported()) {
                parameters.setZoom(mZ);
                mCamera.setParameters(parameters);
            }
        }
    }

    @Override
    public void updateFocus(float x, float y) {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            if (parameters.getMaxNumFocusAreas() > 0) {
                Rect focusRect = calculateTapArea(x, y);
                List<Camera.Area> listFocus = new ArrayList<>();
                listFocus.add(new Camera.Area(focusRect, 1000));
                parameters.setFocusAreas(listFocus);
            }

            if (parameters.getMaxNumMeteringAreas() > 0) {
                Rect meteringRect = calculateTapArea(x, y);
                List<Camera.Area> listMetering = new ArrayList<>();
                listMetering.add(new Camera.Area(meteringRect, 1000));
                parameters.setMeteringAreas(listMetering);
            }
            mCamera.setParameters(parameters);
        }
    }

    private Rect calculateTapArea(float x, float y) {
        float areaSize = 50;
        //Report position x:y / -1000:1000
        float rx = ((x*2000/getWidth())-1000);
        float ry = ((y*2000/getHeight())-1000);
        //Calc Area
        float d = rx-(areaSize/2);
        float h = ry-(areaSize/2);
        float g = rx+(areaSize/2);
        float b = ry+(areaSize/2);
        //Decalage Area
        float dd = (d < -1000) ? (-1000 - d) : 0;
        float dg = (g > 1000) ? (g - 1000) : 0;
        float dh = (h < -1000) ? (-1000 - h) : 0;
        float db = (b > 1000) ? (b - 1000) : 0;

        return new Rect((int)(d + dd - dg), (int)(h + dh - db), (int)(g - dg + dd), (int)(b - db + dh));
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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRectCaptH = (int)convertDpToPixel(150);
        mRectCaptW = getWidth();
        mRectCaptY = (getHeight()/2)-(mRectCaptH/2);
        mRectCaptX = 0;
        ww = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();
        hh = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight();
    }
}
