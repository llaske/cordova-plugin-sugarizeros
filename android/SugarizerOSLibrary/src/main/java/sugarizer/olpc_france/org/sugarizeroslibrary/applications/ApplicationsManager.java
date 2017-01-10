package sugarizer.olpc_france.org.sugarizeroslibrary.applications;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.HashSet;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import sugarizer.olpc_france.org.sugarizeroslibrary.applications.icons.ApplicationIconFetcher;

public class ApplicationsManager {

    private Context mContext;
    private final String appPackageName;
    private ApplicationCacher applicationCacher;

    public ApplicationsManager(Context context) {
        mContext = context;
        appPackageName = mContext.getPackageName();
        applicationCacher = new ApplicationCacher();
    }

    public boolean hasCache() {
        return applicationCacher.hasCache();
    }

    public Observable<List<Application>> listApplications() {
        if (applicationCacher.hasCache()) {
            return Observable.from(applicationCacher.getApplications()).toList();
        }

        final PackageManager packageManager = mContext.getPackageManager();

        return Observable.from(getLauncherApps(packageManager))
                .observeOn(Schedulers.newThread())
                .filter(new Func1<Application, Boolean>() {
                    @Override
                    public Boolean call(Application application) {
                        return !application.packageName.equals(appPackageName);
                    }
                })
                .flatMap(new Func1<Application, Observable<Application>>() {
                    @Override
                    public Observable<Application> call(Application application) {
                        try {
                            PackageInfo packageInfo = packageManager.getPackageInfo(application.packageName, 0);
                            application.name = packageManager.getApplicationLabel(packageManager.getApplicationInfo(application.packageName, 0)).toString();
                            application.icon = new ApplicationIconFetcher(mContext, packageManager).getIcon(application.packageName);
                            application.version = packageInfo.versionName;

                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                        return Observable.just(application);
                    }
                })
                .toList()
                .flatMap(new Func1<List<Application>, Observable<List<Application>>>() {
                    @Override
                    public Observable<List<Application>> call(List<Application> applications) {
                        applicationCacher.setApplications(applications);
                        return Observable.just(applications);
                    }
                });
    }

    private HashSet<Application> getLauncherApps(PackageManager packageManager) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resInfos = packageManager.queryIntentActivities(intent, 0);
        HashSet<Application> applications = new HashSet<>();

        for (ResolveInfo resolveInfo : resInfos) {
            applications.add(Application.Builder.create().setPackageName(resolveInfo.activityInfo.packageName).build());
        }

        return applications;
    }


}
