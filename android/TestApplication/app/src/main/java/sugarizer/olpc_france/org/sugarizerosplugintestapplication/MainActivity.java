package sugarizer.olpc_france.org.sugarizerosplugintestapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import sugarizer.olpc_france.org.sugarizeroslibrary.launcher.LauncherCleanerManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        new ApplicationsManager(this).listApplications();

        new WifiManager(this).startWifi();
        new WifiManager(this).stopWifi();
        new WifiManager(this).joinNetwork("toto");
        new WifiManager(this).disconnect();
        new WifiManager(this).getAPs();
        */

        new LauncherCleanerManager(this).resetLauncher(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
