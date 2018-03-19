package com.tnc.alpr;

import android.content.Context;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.openalpr.AlprJNIWrapper;
import org.openalpr.Util.Utils;
import org.openalpr.model.Results;

public class MainAlpr  extends CordovaPlugin {

    private AlprJNIWrapper mAlpr;

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final Context ctx = cordova.getActivity().getApplicationContext();
        if (action.equals("scan")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray jarr = new JSONArray();
                        Log.i("TAGNCAR", "CONFIGURE");
                        if (mAlpr == null) {
                            mAlpr = AlprJNIWrapper.Factory.create(String.format("/data/data/%s/runtime_data/openalpr.conf", ctx.getApplicationContext().getPackageName()));
                            mAlpr.setCountry("eu");
                            mAlpr.setTopN(1);
                        }
                        Log.i("TAGNCAR", "RECONIZE");
                        Log.i("TAGNCAR", args.getString(0));
                        Results res = mAlpr.recognize(args.getString(0));
                        Log.i("TAGNCAR", "RESULT");
                        if (!res.getResults().isEmpty()) {
                            String r = res.getResults().get(0).getPlate();
                            jarr.put(r);
                            Log.i("TAGNCAR", r);
                        }

                        PluginResult result = new PluginResult(PluginResult.Status.OK, jarr);
                        result.setKeepCallback(true);
                        callbackContext.sendPluginResult(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (action.equals("init")) {
            (new Utils()).copyAssetFolder("www/runtime_data", String.format("/data/data/%s/runtime_data", ctx.getApplicationContext().getPackageName()), ctx.getAssets());
            callbackContext.success();
        } else {
            callbackContext.error("AlertPlugin." + action + " not found !");

            return false;
        }

        return true;
    }
}
