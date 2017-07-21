package com.tnc.alpr;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openalpr.AlprJNIWrapper;
import org.openalpr.model.Result;
import org.openalpr.model.Results;

public class MainAlpr  extends CordovaPlugin {

    private static final String PATH = "www";
    protected AlprJNIWrapper mAlpr;
    private Result r;

    @TargetApi(Build.VERSION_CODES.FROYO)
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {

        if ("open".equals(action)) {

            try {
                Log.i("TAGNCAR", args.getString(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject obj = new JSONObject();
            try {
                obj.put("PLATE", "TAGNCAR");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

            try {
            Results res = this.getAlpr().recognize(Base64.decode(args.getString(0), Base64.DEFAULT), 1, 1920, 1080);
                if (!res.getResults().isEmpty()) {
                    r = res.getResults().get(0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } else if ("init".equals(action)) {
            callbackContext.success("init ok!!");
        } else {
            callbackContext.error("AlertPlugin." + action + " not found !");
        }

        return true;
    }

    public AlprJNIWrapper getAlpr() {
        Context ctx = cordova.getActivity().getApplicationContext();
        if (mAlpr == null) {
            mAlpr = AlprJNIWrapper.Factory.create(ctx, "/data/data/"+ctx.getApplicationContext().getPackageName(), PATH);
            mAlpr.setCountry("eu");
            mAlpr.setTopN(1);
        }

        return mAlpr;
    }
}
