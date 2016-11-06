package org.olpc_france.sugarizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.cordova.CallbackContext;

public class SharedPreferencesManager {
    final static String LAUNCHES_TAG = "LAUNCHES";
    final static String IS_SETUP_TAG = "IS_SETUP";
    final static String KEYSTORE_TAG = "KEYSTORE";

    public static void putInt(Context context, String tag, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(tag, value);
        editor.apply();
    }

    public static void putString(Context context, String tag, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(tag, value);
        editor.apply();
    }

    public static void getString(CallbackContext callbackContext, Context context, String tag) {
        callbackContext.success(getString(context, tag));
    }

    public static void getInt(CallbackContext callbackContext, Context context, String tag) {
        callbackContext.success(getInt(context, tag));
    }

    public static int getInt(Context context, String tag) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(tag, -1);
    }

    public static String getString(Context context, String tag) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(tag, "");
    }
}
