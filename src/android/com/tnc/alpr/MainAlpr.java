package com.tnc.alpr;

import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainAlpr  extends CordovaPlugin {

    public final static int REQUEST_CODE = 123;
    private CallbackContext callback = null;

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("open")) {
            callback = callbackContext;
            Intent intent = new Intent(cordova.getActivity().getApplicationContext(), ViewAlpr.class);
            cordova.setActivityResultCallback(this);
            cordova.getActivity().overridePendingTransition(
                    cordova.getActivity().getResources().getIdentifier("left", "anim", cordova.getActivity().getPackageName()),
                    cordova.getActivity().getResources().getIdentifier("left", "anim", cordova.getActivity().getPackageName())
            );
            cordova.getActivity().startActivityForResult(intent,REQUEST_CODE);
        }
        else {
            callbackContext.error("AlertPlugin." + action + " not found !");
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE) {
            JSONObject j = new JSONObject();
            try {
                j.put("ok", data.getStringExtra("OK"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PluginResult result = new PluginResult(PluginResult.Status.OK, j);
            result.setKeepCallback(true);
            callback.sendPluginResult(result);
        }
    }
}