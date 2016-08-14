package com.tnc.alpr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ViewAlpr extends Activity {

    AbstractPreview mPreview = null;

    public static int SRV_RET_INT = 0;
    public static int SRV_CHAT_INT = 1;
    public static int SRV_PROFILE_INT = 2;
    public static int SRV_PAY_INT = 3;
    public static String TYPE_SRV = "srv";
    public static String TYPE_PLATE = "plate";
    public static String PATH = "www";

    protected String Plate = "TAG-N-CAR";
    protected EditText mtextPlate;
    protected Button mBret;
    protected Button mBch;
    protected Button mBpay;
    protected Button mBpro;
    protected boolean isSelect = false;
    protected View mBcapture;
    protected Drawable mDrag;
    protected Drawable mDragClose;
    protected ProgressBar mProgress;
    protected ProgressCustom mCustomProgress;

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private DrawView drawcam;

    View.OnClickListener onRet = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            valid(SRV_RET_INT);
        }
    };
    View.OnClickListener onCh = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            valid(SRV_CHAT_INT);
        }
    };
    View.OnClickListener onPay = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            valid(SRV_PAY_INT);
        }
    };
    View.OnClickListener onPro = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            valid(SRV_PROFILE_INT);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResources().getIdentifier("ui", "layout", getPackageName()));
        drawcam = (DrawView) findViewById(getResources().getIdentifier("drawcam", "id", getPackageName()));
        FrameLayout preview = (FrameLayout) findViewById(getResources().getIdentifier("previewcam", "id", getPackageName()));
        mPreview = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                new Preview(this, PATH) :
                new PreviewOld(this, PATH);
        preview.addView(mPreview,0);

        mtextPlate = (EditText) findViewById(getResources().getIdentifier("viewPlate", "id", getPackageName()));
        mtextPlate.setTypeface(Typeface.createFromAsset(getAssets(), PATH + "/fonts/catamaran.ttf"));
        mtextPlate.setTextColor(0XFF3F8DD2);

        mBret = (Button) findViewById(getResources().getIdentifier("breturn", "id", getPackageName()));
        mBret.setOnClickListener(onRet);

        mBch = (Button) findViewById(getResources().getIdentifier("bchat", "id", getPackageName()));
        mBch.setOnClickListener(onCh);

        mBpay = (Button) findViewById(getResources().getIdentifier("bpay", "id", getPackageName()));
        mBpay.setOnClickListener(onPay);

        mBpro = (Button) findViewById(getResources().getIdentifier("bprofile", "id", getPackageName()));
        mBpro.setOnClickListener(onPro);

        mProgress = (ProgressBar) findViewById(getResources().getIdentifier("progressBar", "id", getPackageName()));
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        gestureDetector = new GestureDetector(this, new TouchListener());
        mDrag = getResources().getDrawable(getResources().getIdentifier("circle", "drawable", getPackageName()));
        mDragClose = getResources().getDrawable(getResources().getIdentifier("close_clip", "drawable", getPackageName()));
        mBcapture = findViewById(getResources().getIdentifier("btake", "id", getPackageName()));
        mBcapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelect == false) {
                    waitSelect();
                    mPreview.takeCapture(new AbstractPreview.OnTakeListener() {
                        @Override
                        public void onResult(String plate, Double percent, boolean hasResult) {
                            if (isSelect == true && hasResult) {
                                select();
                                Plate = plate;
                                mtextPlate.setText(plate);
                                mtextPlate.setVisibility(View.VISIBLE);
                            }
                            if (!hasResult) {
                                unWaitSelect();
                                Toast toast = Toast.makeText(mPreview.getContext(),"Aucun résultat trouvé." , Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, getWindow().getWindowManager().getDefaultDisplay().getHeight()/5);
                                toast.show();
                            }
                        }
                    });
                } else {
                    unWaitSelect();
                }
            }
        });
        unWaitSelect();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        return true;
    }

    public void unWaitSelect() {
        isSelect = false;
        drawcam.isSelect(true);
        mPreview.cancelCapture();
        if(mCustomProgress != null) {
            mCustomProgress.cancel(true);
            mCustomProgress = null;
        }
        mtextPlate.setVisibility(View.INVISIBLE);
        mtextPlate.setText("");
        mBcapture.setBackground(mDrag);
        mBcapture.setVisibility(View.VISIBLE);
        mBch.setVisibility(View.INVISIBLE);
        mBpay.setVisibility(View.INVISIBLE);
        mBpro.setVisibility(View.INVISIBLE);
    }

    public void waitSelect() {
        isSelect = true;
        drawcam.isSelect(false);
        if(mCustomProgress == null) {
            mCustomProgress = new ProgressCustom();
            mCustomProgress.execute();
        }

        mBcapture.setVisibility(View.INVISIBLE);
    }

    public void select() {
        if(mCustomProgress != null) {
            mCustomProgress.cancel(true);
            mCustomProgress = null;
        }
        mBcapture.setBackground(mDragClose);
        mBcapture.setVisibility(View.VISIBLE);
        mBch.setVisibility(View.VISIBLE);
        mBpay.setVisibility(View.VISIBLE);
        mBpro.setVisibility(View.VISIBLE);
    }

    protected void valid(int idserv) {
        Intent result = new Intent();
        result.putExtra(TYPE_SRV, idserv);
        result.putExtra(TYPE_PLATE, Plate);
        setResult(RESULT_OK, result);
        finish();
    }

    private class ProgressCustom extends AsyncTask<Void, Integer, Void> {
        private boolean sens = true;
        private Integer progress = 0;
        private long starttime = 0;
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgress.setProgress(progress);
            if(progress==0 || progress==100) {
                sens = !sens;
                mProgress.setRotation(mProgress.getRotation()==0?180:0);
            }
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            starttime = System.currentTimeMillis();
            while (true) {
                long millis = System.currentTimeMillis() - starttime;
                if (millis >= 3) {
                    if(sens) progress++; else progress--;
                    publishProgress(progress);
                    starttime = System.currentTimeMillis();
                }
                if(isCancelled()) break;
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
            mProgress.setRotation(0);
            mProgress.setProgress(0);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgress.setVisibility(View.INVISIBLE);
        }
    }

    private class TouchListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if(mtextPlate.hasFocus()) {
                mtextPlate.onEditorAction(EditorInfo.IME_ACTION_DONE);
            } else if(!isSelect) {
                drawcam.isTouch(e.getX(), e.getY());
                mPreview.updateFocus(e.getX(), e.getY());
            }

            return super.onSingleTapUp(e);
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private boolean isZoomActive = false;
        float initSize;
        private float mV = 0;
        private int w;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if(!isZoomActive) {
                isZoomActive = true;
                float diff = ((detector.getCurrentSpan()-initSize));
                int v = ((int)(diff-mV)*100)/w*2;
                mPreview.updateZoom(v);
                mV=diff;
            }

            isZoomActive = false;

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            mV = -1;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            super.onScaleBegin(detector);
            initSize = detector.getCurrentSpan();
            mV = 0;
            w = getWindow().getWindowManager().getDefaultDisplay().getWidth();

            return true;
        }
    }

}