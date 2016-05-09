package com.tnc.alpr;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ViewAlpr extends Activity {

    protected String Plate = "";
    private static int SRV_RET_INT = 0;
    private static int SRV_CHAT_INT = 1;
    private static int SRV_PROFILE_INT = 2;
    private static int SRV_PAY_INT = 3;

    private static String TYPE_SRV = "srv";
    private static String TYPE_PLATE = "plate";

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
        setContentView(getResources().getIdentifier("ui","layout", getPackageName()));

        HandlerThread mBackgroundThread = new HandlerThread("background");
        mBackgroundThread.start();
        Handler mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

        FrameLayout preview = (FrameLayout) findViewById(getResources().getIdentifier("previewcam", "id", getPackageName()));
        TextView textview = (TextView) findViewById(getResources().getIdentifier("viewPlate", "id", getPackageName()));
        textview.setVisibility(View.VISIBLE);

        AbstractPreview mPreview = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPreview = new Preview(this, mBackgroundHandler, textview);
        } else {
            mPreview = new PreviewOld(this);
        }

        preview.addView(mPreview);

        Button mBret = (Button) findViewById(getResources().getIdentifier("breturn", "id", getPackageName()));
        mBret.setOnClickListener(onRet);

        Button mBch = (Button) findViewById(getResources().getIdentifier("bchat", "id", getPackageName()));
        mBch.setOnClickListener(onCh);

        Button mBpay = (Button) findViewById(getResources().getIdentifier("bpay", "id", getPackageName()));
        mBpay.setOnClickListener(onPay);

        Button mBpro = (Button) findViewById(getResources().getIdentifier("bprofile", "id", getPackageName()));
        mBpro.setOnClickListener(onPro);
    }

    protected void valid(int idserv)
    {
        Intent result = new Intent();
        result.putExtra(TYPE_SRV, idserv);
        result.putExtra(TYPE_PLATE, Plate.toString());
        setResult(RESULT_OK, result);
        finish();
    }

}
