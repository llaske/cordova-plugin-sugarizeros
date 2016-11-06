package org.olpc_france.sugarizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

public class AppsBroadcastReceiver extends BroadcastReceiver {
    private CallbackContext AppsListenerCallBack;

    public void setCallback(CallbackContext callback) {
        AppsListenerCallBack = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getEncodedSchemeSpecificPart();
        Log.i("packageName", packageName);
        if (AppsListenerCallBack != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, packageName);
            result.setKeepCallback(true);
            AppsListenerCallBack.sendPluginResult(result);
        }
    }
}
