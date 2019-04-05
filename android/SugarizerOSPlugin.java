package org.olpc_france.sugarizer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;

import com.google.gson.Gson;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import rx.functions.Action1;
import sugarizer.olpc_france.org.sugarizeroslibrary.applications.Application;
import sugarizer.olpc_france.org.sugarizeroslibrary.applications.ApplicationsManager;
import sugarizer.olpc_france.org.sugarizeroslibrary.launcher.LauncherCleanerManager;
import sugarizer.olpc_france.org.sugarizeroslibrary.network.SugarScanResult;
import sugarizer.olpc_france.org.sugarizeroslibrary.network.WifiManager;

public class SugarizerOSPlugin extends CordovaPlugin {
    private PackageManager pm;
    private ApplicationsManager applicationsManager;
    private LauncherCleanerManager launcherCleanerManager;
    private WifiManager wifiManager;

    protected void pluginInitialize() {
        applicationsManager = new ApplicationsManager(cordova.getActivity());
        launcherCleanerManager = new LauncherCleanerManager(cordova.getActivity());
        wifiManager = new WifiManager(cordova.getActivity());
        pm = cordova.getActivity().getPackageManager();
    }

    private void getDefaultLauncherPackageName(CallbackContext callbackContext) {
        callbackContext.success(getDefaultLauncherPackageName(pm));
    }

    public static String getDefaultLauncherPackageName(PackageManager packageManager) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }

    public static boolean isMyAppLauncherDefault(Context appContext, PackageManager packageManager) {
        if (packageManager == null)
            packageManager = appContext.getPackageManager();
        return getDefaultLauncherPackageName(packageManager).equals(appContext.getPackageName());
    }

    private void runSettings(CallbackContext callbackContext) {
        cordova.getActivity().startActivity(
                new Intent(Settings.ACTION_SETTINGS));
        callbackContext.success();
    }

    private void runActivity(CallbackContext callbackContext, String packageName) {
        Intent LaunchIntent = pm.getLaunchIntentForPackage(packageName);
        this.cordova.getActivity().startActivity(LaunchIntent);
        callbackContext.success();
    }

    private void openAppSettings(Context context) {
        String packageName = getDefaultLauncherPackageName(pm);
        if (context == null || packageName == null || packageName.equals("android")) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + packageName));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    private void openChooseLauncherPopup(Context context) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);
    }

    private void isMyAppLauncherDefault(CallbackContext callbackContext, Context appContext) {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        final String myPackageName = appContext.getPackageName();

        filter.addCategory(Intent.CATEGORY_HOME);
        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        List<ComponentName> activities = new ArrayList<ComponentName>();

        pm.getPreferredActivities(filters, activities, null);
        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                callbackContext.success(1);
            }
        }
        callbackContext.success(0);
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("runActivity")) {
            this.runActivity(callbackContext, args.getString(0));
        } else if (action.equals("runSettings")) {
            this.runSettings(callbackContext);
        } else if (action.equals("isAppCacheReady")) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ready", applicationsManager.hasCache());
            callbackContext.success(jsonObject);

            return true;
        } else if (action.equals("apps")) {
            applicationsManager.listApplications()
                    .subscribe(new Action1<List<Application>>() {
                        @Override
                        public void call(List<Application> applications) {
                            callbackContext.success(new Gson().toJson(applications));
                        }
                    });

            return true;
        } else if (action.equals("scanWifi")) {
            if (!wifiManager.isEnabled()) {
                wifiManager.startWifi();
            }

            wifiManager.getAPs()
                    .subscribe(new Action1<List<SugarScanResult>>() {
                        @Override
                        public void call(List<SugarScanResult> scanResults) {
                            callbackContext.success(new Gson().toJson(scanResults));
                        }
                    });
            return true;
        } else if (action.equals("isDefaultLauncher")) {
            this.isMyAppLauncherDefault(callbackContext, cordova.getActivity());
        } else if (action.equals("chooseLauncher")) {
            launcherCleanerManager.resetLauncher(true);
        } else if (action.equals("selectLauncher")) {
            launcherCleanerManager.resetLauncher(false);
        } else if (action.equals("joinNetwork")) {
            wifiManager.joinNetwork(args.getString(0));
        } else if (action.equals("isKnownNetwork")) {
            wifiManager.isKnownNetwork(args.getString(0))
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            callbackContext.success(aBoolean ? 1 : 0);
                        }
                    });
        }

        //fixme
        else if (action.equals("getInt")) {
            callbackContext.success(42);
//            SharedPreferencesManager.getInt(callbackContext, cordova.getActivity(), args.getString(0));
        } else if (action.equals("putInt")) {
//            SharedPreferencesManager.putInt(cordova.getActivity(), args.getString(0), args.getInt(1));
        } else if (action.equals("disconnect")) {
            wifiManager.disconnect();
        } else if (action.equals("getLauncherPackageName")) {
            getDefaultLauncherPackageName(callbackContext);
        } else if (action.equals("isWifiEnabled")) {
            callbackContext.success(wifiManager.isEnabled() ? 1 : 0);
            return true;
        } else if (action.equals("getWifiSSID")) {
            String ssid = wifiManager.getCurrentNetwork().getSSID();
            ssid = ssid.substring(1, ssid.length() - 1);
            callbackContext.success(ssid);
            return true;
        }
        else if (action.equals("forgetNetwork")) {
            wifiManager.removeNetwork(args.getString(0));
        }
        else if (action.equals("setKey")) {
            boolean autoLogin = false;
            if (args.length() == 3) {
                autoLogin = args.getBoolean(2);
            }
            wifiManager.saveNetwork(args.getString(0), args.getString(1), autoLogin);
        }
        return false;
    }
}
