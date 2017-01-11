package sugarizer.olpc_france.org.sugarizeroslibrary.network;


import android.net.wifi.ScanResult;

public class SugarScanResult {

    public String SSID;
    public String BSSID;
    public int RSSI;
    public boolean isConnected;
    public String capabilities;

    public SugarScanResult(ScanResult scanResult, boolean isConnected) {
        SSID = scanResult.SSID;
        BSSID = scanResult.BSSID;
        RSSI = scanResult.level;
        this.isConnected = isConnected;
        capabilities = scanResult.capabilities;
    }
}
