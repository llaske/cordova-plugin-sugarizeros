package sugarizer.olpc_france.org.sugarizeroslibrary.launcher;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class LauncherCleanerManager {

    private Context mContext;

    public LauncherCleanerManager(Context context) {
        mContext = context;
    }

    public void resetLauncher(boolean launchPicker) {
        PackageManager pm = mContext.getPackageManager();
        ComponentName mockupComponent = new ComponentName(mContext.getPackageName(), FakeLauncherActivity.class.getName());

        pm.setComponentEnabledSetting(mockupComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        if (launchPicker) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(startMain);
        }

        pm.setComponentEnabledSetting(mockupComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

}
