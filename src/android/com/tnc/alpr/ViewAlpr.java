package com.tnc.alpr;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ViewAlpr extends Activity {

    public static int SRV_RET_INT = 0;
    public static int SRV_CHAT_INT = 1;
    public static int SRV_PROFILE_INT = 2;
    public static int SRV_PAY_INT = 3;
    public static String TYPE_SRV = "srv";
    public static String TYPE_PLATE = "plate";

    AbstractPreview mPreview = null;

    protected String Plate = "TAG-N-CAR";
    protected Integer mColor = 0XBB000000;
    protected TextView mtextPlate;
    protected RelativeLayout mRl1;
    protected RelativeLayout mRl2;
    protected FrameLayout mFl1;
    protected Button mBch;
    protected Button mBpay;
    protected Button mBpro;
    protected boolean isSelect = false;
    protected View mBcapture;
    protected Drawable mDrag;
    protected Drawable mDragClose;
    protected ProgressBar mProgress;
    protected Drawable mBorder;
    protected ProgressCustom mCustomProgress;

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
        copyFileOrDir("www/runtime_data", "runtime_data");
        //copyFileOrDir("runtime_data", "runtime_data");
        setContentView(getResources().getIdentifier("ui", "layout", getPackageName()));
        HandlerThread mBackgroundThread = new HandlerThread("background");
        mBackgroundThread.start();
        Handler mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        FrameLayout preview = (FrameLayout) findViewById(getResources().getIdentifier("previewcam", "id", getPackageName()));
        mPreview = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                new Preview(this, mBackgroundHandler, mtextPlate) :
                new PreviewOld(this);
        preview.addView(mPreview);
        mRl1 = (RelativeLayout) findViewById(getResources().getIdentifier("rl1", "id", getPackageName()));
        mRl2 = (RelativeLayout) findViewById(getResources().getIdentifier("rl2", "id", getPackageName()));
        mFl1 = (FrameLayout) findViewById(getResources().getIdentifier("fl1", "id", getPackageName()));
        mtextPlate = (TextView) findViewById(getResources().getIdentifier("viewPlate", "id", getPackageName()));
        mtextPlate.setVisibility(View.VISIBLE);
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "www/fonts/catamaran.ttf");
        mtextPlate.setTypeface(myTypeface);
        mtextPlate.setTextColor(0XFF3F8DD2);
        Button mBret = (Button) findViewById(getResources().getIdentifier("breturn", "id", getPackageName()));
        mBret.setOnClickListener(onRet);
        mBch = (Button) findViewById(getResources().getIdentifier("bchat", "id", getPackageName()));
        mBch.setVisibility(View.INVISIBLE);
        mBch.setOnClickListener(onCh);
        mProgress = (ProgressBar) findViewById(getResources().getIdentifier("progressBar", "id", getPackageName()));
        mBpay = (Button) findViewById(getResources().getIdentifier("bpay", "id", getPackageName()));
        mBpay.setVisibility(View.INVISIBLE);
        mBpay.setOnClickListener(onPay);
        mBpro = (Button) findViewById(getResources().getIdentifier("bprofile", "id", getPackageName()));
        mBpro.setVisibility(View.INVISIBLE);
        mBpro.setOnClickListener(onPro);
        mDrag = getResources().getDrawable(getResources().getIdentifier("selectorcircle", "drawable", getPackageName()));
        mBorder = getResources().getDrawable(getResources().getIdentifier("border", "drawable", getPackageName()));
        mDragClose = getResources().getDrawable(getResources().getIdentifier("close1", "drawable", getPackageName()));
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
                            }
                            if (!hasResult) {
                                unWaitSelect();
                                Toast.makeText(mPreview.getContext(),"Aucun résultat trouvé." , Toast.LENGTH_LONG).show();
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

    public void waitSelect() {
        isSelect = true;
        if(mCustomProgress == null) {
            mCustomProgress = new ProgressCustom();
            mCustomProgress.execute();
        }
        mBcapture.setVisibility(View.INVISIBLE);
        mRl1.setBackgroundColor(mColor);
        mRl2.setBackgroundColor(mColor);
        mFl1.setBackgroundColor(mColor);
    }

    public void unWaitSelect() {
        isSelect = false;
        mPreview.cancelCapture();
        if(mCustomProgress != null) {
            mCustomProgress.cancel(true);
            mCustomProgress = null;
        }
        mBcapture.setBackground(mDrag);
        mBcapture.setVisibility(View.VISIBLE);
        mRl1.setBackgroundColor(mColor);
        mRl2.setBackgroundColor(mColor);
        mFl1.setBackground(mBorder);
        mtextPlate.setText("");
        mBch.setVisibility(View.INVISIBLE);
        mBpay.setVisibility(View.INVISIBLE);
        mBpro.setVisibility(View.INVISIBLE);
    }

    public void select() {
        if(mCustomProgress != null) {
            mCustomProgress.cancel(true);
            mCustomProgress = null;
        }
        mBcapture.setBackground(mDragClose);
        mBcapture.setVisibility(View.VISIBLE);
        mRl1.setBackgroundColor(mColor);
        mRl2.setBackgroundColor(mColor);
        mFl1.setBackgroundColor(mColor);
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

    public void copyFileOrDir(String path, String dest) {
        AssetManager assetManager = this.getAssets();
        String[] assets;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path, dest);
            } else {
                //String fullPath = "/data/data/" + this.getPackageName() + "/" + dest;
                String fullPath = "/data/data/com.tagncar.app/" + dest;
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDir(path + "/" + assets[i], dest + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
        }
    }

    private void copyFile(String filename, String filenamedest) {
        AssetManager assetManager = this.getAssets();

        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(filename);
            //String newFileName = "/data/data/" + this.getPackageName() + "/" + filenamedest;
            String newFileName = "/data/data/com.tagncar.app/" + filenamedest;
            out = new FileOutputStream(newFileName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
        }
    }

    private class ProgressCustom extends AsyncTask<Void, Integer, Void> {
        private boolean sens = true;
        private Integer progress = 0;

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgress.setProgress(progress);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            while (true) {
                doTask();
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

        protected void doTask() {
            if(sens) progress++; else progress--;
            publishProgress(progress);
            if(progress==0 || progress==100) {
                sens = !sens;
                mProgress.setRotation(mProgress.getRotation()==0?180:0);
            }
           for (int i=0;i<1000000;i++);
        }
    }

}