package org.olpc_france.sugarizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SugarWifiManager {
    static final String WEP = "[WEP";
    static final String WPA2 = "[WPA2";
    static final String WPA = "[WPA";

    private static String getWifiSSID(Context appContext) {
        WifiManager wifiManager = (WifiManager) appContext.getSystemService(appContext.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    public static void resetKeyStore(Context context) {
        SharedPreferencesManager.putString(context, SharedPreferencesManager.KEYSTORE_TAG, "{}");
    }

    public static JSONObject getKeyStore(Context context) {
        String keyStore = SharedPreferencesManager.getString(context, SharedPreferencesManager.KEYSTORE_TAG);
        try {
            return new JSONObject(keyStore);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getKeyStore(CallbackContext callbackContext, Context context) {
        JSONObject keyStore = getKeyStore(context);
        if (keyStore != null)
            callbackContext.success(keyStore);
        else
            callbackContext.error("Invalid JSON KeyStore");
    }

    public static void setKey(CallbackContext callbackContext, Context context, String SSID, String key) {
        try {
            JSONObject keyStore = getKeyStore(context);
            if (keyStore == null) {
                callbackContext.error("Invalid JSON KeyStore");
                return;
            }
            keyStore.put(SSID, key);
            SharedPreferencesManager.putString(context, SharedPreferencesManager.KEYSTORE_TAG, keyStore.toString());
            callbackContext.success();
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error(e.toString());
        }
    }

    public static int isWifiEnabled(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled()) {
            return 1;
        }
        return 0;
    }

    public static void isWifiEnabled(CallbackContext callbackContext, Context appContext) {
        callbackContext.success(isWifiEnabled(appContext));
    }

    public static void disconnect(Context appContext) {
        WifiManager wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        wifi.disconnect();
    }

    public static void joinNetwork(String SSID, String pass, String capabilities, Context context) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + SSID + "\"";
        if (capabilities.contains(WEP)) {
            conf.wepKeys[0] = "\"" + pass + "\"";
            conf.wepTxKeyIndex = 0;
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else if (capabilities.contains(WPA2)) {
            conf.preSharedKey = "\"" + pass + "\"";
        } else if (capabilities.contains(WPA)) {
            conf.preSharedKey = "\"" + pass + "\"";
        } else {
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                break;
            }
        }
    }

    public static void scanWifi(final CallbackContext callbackContext, Context appContext) {
        WifiManager mWifiManager;
        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        final String wifiSSID = getWifiSSID(appContext).replace("\"", "");
        appContext.registerReceiver(new BroadcastReceiver() {
                                        @Override
                                        public void onReceive(Context context, Intent intent) {
                                            JSONArray output = new JSONArray();
                                            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

                                            List<ScanResult> scanResults = mWifiManager.getScanResults();

                                            try {
                                                for (int i = 0; i < scanResults.size(); i++) {
                                                    JSONObject object = new JSONObject();
                                                    ScanResult scanResult = scanResults.get(i);
                                                    object.put("SSID", scanResult.SSID);
                                                    object.put("BSSID", scanResult.BSSID);
                                                    object.put("capabilities", scanResult.capabilities);
                                                    object.put("RSSI", scanResult.level);
                                                    object.put("isConnected", wifiSSID.equals(scanResult.SSID));
                                                    output.put(object);
                                                }
                                                PluginResult result = new PluginResult(PluginResult.Status.OK, output);
                                                result.setKeepCallback(true);
                                                callbackContext.sendPluginResult(result);
                                            } catch (JSONException e) {
                                                callbackContext.error("Android:" + e.toString());
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                , i);

        mWifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        mWifiManager.startScan();
    }

}
