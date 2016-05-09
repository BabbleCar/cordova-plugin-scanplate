package com.tnc.alpr;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import java.io.IOException;
import java.util.List;

import static android.hardware.Camera.*;
import static android.hardware.Camera.CameraInfo.*;

class PreviewOld extends AbstractPreview {

    SurfaceHolder mHolder;
    private Camera mCamera;

    PreviewOld(Context context) {
        super(context);
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int finalCamId = 0;
        releaseCameraAndPreview();
        CameraInfo cameraInfo = new CameraInfo();
        int cameraCount = getNumberOfCameras();

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == CAMERA_FACING_FRONT) {
                finalCamId = camIdx;
            }
        }

        try {
            mCamera = open(finalCamId);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
            releaseCameraAndPreview();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

        Camera.Size previewSize = previewSizes.get(1);

        parameters.setPreviewSize(previewSize.width, previewSize.height);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
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
}
