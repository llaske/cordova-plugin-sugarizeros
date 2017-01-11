package sugarizer.olpc_france.org.sugarizeroslibrary.applications;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackageAddedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionStr = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(actionStr) ||
                Intent.ACTION_PACKAGE_CHANGED.equals(actionStr) ||
                Intent.ACTION_PACKAGE_REMOVED.equals(actionStr)) {

            new ApplicationCacher().invalidateCache();
        }
    }
}
