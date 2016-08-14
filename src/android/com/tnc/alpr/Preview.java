package com.tnc.alpr;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.TextView;

/*
import org.openalpr.AlprJNIWrapper;
import org.openalpr.model.Result;
import org.openalpr.model.Results;
*/
import java.util.Arrays;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Preview extends AbstractPreview {

    static final String TAG = "ALPRPSURFACE";
    private static final int STATE_OK = 0;
    private static final int STATE_NO = 1;
    private final Handler mBackgroundHandler;
    private int mState = STATE_OK;
    //private AlprJNIWrapper alpr;
    private ImageReader mCaptureBuffer;
    private String mCameraId;
    private TextView mTextView;
    private SurfaceHolder mHolder;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mCaptureRaw;
    private CaptureRequest.Builder mCaptureRequestBuilderPreview;
    private CameraCaptureSession mSession;
   /* final ImageReader.OnImageAvailableListener mImageCaptureListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image img = reader.acquireNextImage();
                    if (img != null) {
                        mBackgroundHandler.post(new CapturedImageSaver(img, getAlpr()));
                    }
                }
            };
   */
    private CameraDevice.StateCallback mCameraDeviceStateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    mCameraDevice = camera;
                    try {
                        mCameraDevice.createCaptureSession(Arrays.asList(mHolder.getSurface(), mCaptureBuffer.getSurface()), mCaptureSessionPreviewListener, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    mCameraDevice = null;
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    mCameraDevice = null;
                }
            };
    //private Result r;
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            //if (mState == STATE_OK)
                //captureStillPicture();
        }
    };
    private CameraCaptureSession.StateCallback mCaptureSessionPreviewListener =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        Log.i(TAG, "CAMERA CAPTURE SESSION");
                        mSession = session;
                        mCaptureRequestBuilderPreview = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        mCaptureRequestBuilderPreview.addTarget(mHolder.getSurface());
                        mSession.setRepeatingRequest(mCaptureRequestBuilderPreview.build(), mCaptureCallback, null);
                    } catch (CameraAccessException e) {
                        Log.i(TAG, "EXCEPTION configure : " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.i(TAG, "CAMERA CAPTURE SESSION FAILURE");
                }
            };

    private Activity mActivity;

    public Preview(Context context, String path, Handler BackgroundHandler, TextView textview) {
        super(context.getApplicationContext(), path);
        mBackgroundHandler = BackgroundHandler;
        mTextView = textview;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mActivity = (Activity) context;
    }

    /*public AlprJNIWrapper getAlpr() {
        if (alpr == null) {
            alpr = new AlprJNIWrapper();
            alpr.setCountry("eu");
            alpr.setTopN(1);
            alpr.setConfigFile("/data/data/com.tnc.org.openalprapp/runtime_data/openalpr.conf");
        }
        return alpr;
    }*/

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "Surface created");
        mCameraId = null;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "Surface destroyed");
        holder.removeCallback(this);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        initCameraId(w, h);
        openCamera();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public String initCameraId(int w, int h) {
        mHolder.setFixedSize(w, h);
        try {
            String[] listCameraId = getCameraManager().getCameraIdList();
            if (listCameraId.length > 1) for (String cameraId : listCameraId) {
                CameraCharacteristics mCameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
                if (mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    mCameraId = cameraId;
                    break;
                }
            }
            if (mCameraId == null && listCameraId.length > 0) {
                mCameraId = listCameraId[0];
            }
            /*CameraCharacteristics mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size largestRaw = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888)),
                    new Comparator<Size>() {
                        @Override
                        public int compare(Size lhs, Size rhs) {
                            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
                        }
                    });
            mCaptureBuffer = ImageReader.newInstance(largestRaw.getWidth(), largestRaw.getHeight(), ImageFormat.YUV_420_888, 1);
            mCaptureBuffer.setOnImageAvailableListener(mImageCaptureListener, mBackgroundHandler);
             */
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "NUM CAMERA : " + mCameraId);
        return mCameraId;
    }

    public CameraManager getCameraManager() {
        if (mCameraManager == null) {
            mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        }

        return mCameraManager;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, null);
        } catch (CameraAccessException e) {
            Log.i(TAG, "EXCEPTION onCamera : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void takeCapture(OnTakeListener listener) {

    }

    @Override
    public void updateFocus(float x, float y) {

    }

    @Override
    public void updateZoom(float value) {

    }

    @Override
    public void cancelCapture() {

    }

  /*  private void captureStillPicture() {
        mState = STATE_NO;
        try {
            mCaptureRaw = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureRaw.addTarget(mCaptureBuffer.getSurface());
            mCaptureRaw.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mCaptureRaw.set(CaptureRequest.CONTROL_AWB_REGIONS, null);
            mSession.capture(mCaptureRaw.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (r != null) {
                            mTextView.setText(r.getPlate() + " - " + r.getConfidence().intValue() + "%");
                        }
                        r = null;
                    }
                    });
                    mState = STATE_OK;
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
*/
/*
    class CapturedImageSaver implements Runnable {
        private AlprJNIWrapper mAlpr;
        private Image mCapture;

        public CapturedImageSaver(Image capture, AlprJNIWrapper alpr) {
            mCapture = capture;
            mAlpr = alpr;
        }

        @Override
        public void run() {
            Image.Plane[] mPlanes = mCapture.getPlanes();
            ByteBuffer buffer = mPlanes[0].getBuffer();
            buffer.rewind();
            byte[] data = new byte[buffer.capacity()];
            buffer.get(data);
            Results res = mAlpr.recognize(data, 1, mCapture.getWidth(), mCapture.getHeight());
            if (!res.getResults().isEmpty()) {
                r = res.getResults().get(0);
            }
            mCapture.close();
            buffer = null;
            mPlanes = null;
            res = null;
        }
    }
*/
}