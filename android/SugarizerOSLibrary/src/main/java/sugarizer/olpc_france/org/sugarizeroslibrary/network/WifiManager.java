package sugarizer.olpc_france.org.sugarizeroslibrary.network;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import sugarizer.olpc_france.org.sugarizeroslibrary.R;

public class WifiManager {

    static final String WEP = "[WEP";
    static final String WPA2 = "[WPA2";
    static final String WPA = "[WPA";

    private final android.net.wifi.WifiManager wifiManager;
    private Context mContext;

    public WifiManager(Context context) {
        mContext = context;
        wifiManager = (android.net.wifi.WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    public void startWifi() {
        wifiManager.setWifiEnabled(true);
        wifiManager.disconnect();
    }

    public void disconnect() {
        wifiManager.disconnect();
    }

    public void removeNetwork(final String SSID) {
        Observable.from(wifiManager.getConfiguredNetworks())
                .filter(new Func1<WifiConfiguration, Boolean>() {
                    @Override
                    public Boolean call(WifiConfiguration wifiConfiguration) {
                        return SSID.equals(wifiConfiguration.SSID) || ('"' + SSID + '"').equals(wifiConfiguration.SSID);
                    }
                })
                .subscribe(new Action1<WifiConfiguration>() {
                    @Override
                    public void call(WifiConfiguration wifiConfiguration) {
                        wifiManager.removeNetwork(wifiConfiguration.networkId);
                    }
                });
    }

    public Observable<Boolean> isKnownNetwork(final String SSID) {
        return Observable.from(wifiManager.getConfiguredNetworks())
                .filter(new Func1<WifiConfiguration, Boolean>() {
                    @Override
                    public Boolean call(WifiConfiguration wifiConfiguration) {
                        return SSID.equals(wifiConfiguration.SSID) || ('"' + SSID + '"').equals(wifiConfiguration.SSID);
                    }
                })
                .isEmpty()
                .map(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return !aBoolean;
                    }
                });
    }

    public void saveNetwork(final String SSID, final String password, final boolean join) {
        removeNetwork(SSID);

        getAPs().flatMap(new Func1<List<ScanResult>, Observable<?>>() {
            @Override
            public Observable<?> call(List<ScanResult> scanResults) {
                return Observable.from(scanResults);
            }
        }).map(new Func1<Object, ScanResult>() {
            @Override
            public ScanResult call(Object o) {
                return (ScanResult) o;
            }
        }).filter(new Func1<ScanResult, Boolean>() {
            @Override
            public Boolean call(ScanResult scanResult) {
                return SSID.equals(scanResult.SSID);
            }
        }).subscribe(new Action1<ScanResult>() {
            @Override
            public void call(ScanResult scanResult) {
                saveNetwork(SSID, password, scanResult.capabilities, join);
            }
        });
    }

    private void saveNetwork(String SSID, String password, String capabilities, boolean join) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        wifiConfiguration.SSID = SSID;

        if (capabilities.contains(WEP)) {
            wifiConfiguration.wepKeys[0] = "\"" + password + "\"";
            wifiConfiguration.wepTxKeyIndex = 0;
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else if (capabilities.contains(WPA2)) {
            wifiConfiguration.preSharedKey = "\"" + password + "\"";
        } else if (capabilities.contains(WPA)) {
            wifiConfiguration.preSharedKey = "\"" + password + "\"";
        } else {
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        wifiManager.addNetwork(wifiConfiguration);

        if (join) {
            joinNetwork(SSID);
        }
    }


    public void saveNetwork(String SSID, boolean join) {
        saveNetwork(SSID, null, join);
    }

    public void joinNetwork(String SSID) {
        if (wifiManager.getConfiguredNetworks() != null) {
            for (WifiConfiguration tmp : wifiManager.getConfiguredNetworks()) {
                if (SSID.equals(tmp.SSID) || ('"' + SSID + '"').equals(tmp.SSID)) {
                    wifiManager.enableNetwork(tmp.networkId, true);
                }
            }
        }
    }

    public Boolean isEnabled() {
        return wifiManager.isWifiEnabled();
    }

    public Boolean isConnected() {
        return wifiManager.getConnectionInfo() != null;
    }

    public WifiInfo getCurrentNetwork() {
        return wifiManager.getConnectionInfo();
    }

    private void startScan(final Subscriber<? super List<ScanResult>> subscriber) {
        IntentFilter i = new IntentFilter();
        i.addAction(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mContext.unregisterReceiver(this);

                if (wifiManager.getScanResults().size() != 0) {

                    Observable.from(wifiManager.getScanResults())
                            .distinct(new Func1<ScanResult, Object>() {
                                @Override
                                public Object call(ScanResult scanResult) {
                                    return scanResult.SSID;
                                }
                            })
                            .toList()
                            .subscribe(new Action1<List<ScanResult>>() {
                                @Override
                                public void call(List<ScanResult> scanResults) {
                                    subscriber.onNext(scanResults);
                                }
                            });
                }
            }
        }, i);

        wifiManager.startScan();
    }

    public void stopWifi() {
        wifiManager.setWifiEnabled(false);
    }

    public Observable<List<ScanResult>> getAPs() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
            intent.setData(uri);
            Toast.makeText(mContext, R.string.please_allow_permissions, Toast.LENGTH_SHORT).show();
            mContext.startActivity(intent);

            return Observable.empty();
        }

        return Observable.create(new Observable.OnSubscribe<List<ScanResult>>() {
            @Override
            public void call(Subscriber<? super List<ScanResult>> subscriber) {
                startScan(subscriber);
            }
        });
    }

}
