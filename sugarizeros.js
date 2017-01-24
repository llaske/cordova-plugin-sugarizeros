var sugarizerOS = {};
var exec = require('cordova/exec');

sugarizerOS.minRSSI = -20
sugarizerOS.applicationsLoaded = false;
sugarizerOS.isDefaultLauncher = false;
sugarizerOS.networks = [];
sugarizerOS.networkIconsCache = [];
sugarizerOS.launches = -1;
sugarizerOS.launcherPackageName = null;
sugarizerOS.packageName = "org.olpc_france.sugarizer";
sugarizerOS.appsCache = null;
sugarizerOS.isSetup = false;

sugarizerOS.addApplicationToJournal = function(callback, application, datastore) {
    var mimetype = "text/plain";
    var type = mimetype.split("/")[0];
    var metadata = {
        mimetype: mimetype,
        title: application.name,
        activity: application.id,
        timestamp: new Date().getTime(),
        creation_time: new Date().getTime(),
        file_size: 0,
        icon: application.icon
    };
    datastore.create(metadata, callback, null);
}

sugarizerOS.addNetworkIconToCache = function(icon) {
    if (!sugarizerOS.getNetworkIconFromCache(icon.BSSID)) {
        sugarizerOS.networkIconsCache.push(icon);
    }
}

sugarizerOS.clearNetworkIconCache = function() {
    sugarizerOS.networkIconsCache = [];
}

sugarizerOS.disconnectWifi = function() {
    exec(null, null, "SugarizerOSPlugin", "disconnect", []);
}

sugarizerOS.isKnownNetwork = function(ssid, onSuccess) {
    exec(onSuccess, null, "SugarizerOSPlugin", "isKnownNetwork", [ssid]);
}

sugarizerOS.getWifiSSID = function(onSuccess) {
    exec(onSuccess, null, "SugarizerOSPlugin", "getWifiSSID", []);
}

sugarizerOS.isWifiEnabled = function(onSuccess, onFailure) {
    exec(onSuccess, onFailure, "SugarizerOSPlugin", "isWifiEnabled", []);
}

sugarizerOS.getLauncherPackageName = function(onSuccess, onFailure) {
    exec(onSuccess, onFailure, "SugarizerOSPlugin", "getLauncherPackageName", []);
}

sugarizerOS.getNetworkIconFromCache = function(BSSID) {
    for (var i = 0; i < sugarizerOS.networkIconsCache.length; i++) {
        if (sugarizerOS.networkIconsCache[i].BSSID == BSSID)
            return sugarizerOS.networkIconsCache[i];
    }
}

sugarizerOS.setApplicationsLoaded = function(value) {
    sugarizerOS.applicationsLoaded = true;
}

sugarizerOS.getEncryptionString = function(capabilities) {
    var WEP = "[WEP"
    var WPA2 = "[WPA2"
    var WPA = "[WPA"
    if (capabilities.indexOf(WEP) != -1)
        return "WEP";
    else if (capabilities.indexOf(WPA2) != -1)
        return "WPA2";
    else if (capabilities.indexOf(WPA) != -1)
        return "WPA";
    return "OPEN";
}

sugarizerOS.joinNetwork = function(SSID, onSuccess, onFailure) {
    exec(onSuccess, onFailure, "SugarizerOSPlugin", "joinNetwork", [SSID]);
}

sugarizerOS.init = function() {
    if (window) {
        window.sugarizerOS = sugarizerOS;
        sugarizerOS.checkIfDefaultLauncher();
        console.log("SugarizerOS initialized");
        sugarizerOS.getLauncherPackageName(function(value) {
            sugarizerOS.launcherPackageName = value;
        });
        sugarizerOS.getInt(function(value) {
            sugarizerOS.isSetup == (value == 1);
        }, null, "IS_SETUP");
    } else {
        console.log("No window to initialize sugarizerOS");
    }
}

sugarizerOS.setKey = function(SSID, key, autologin, onSuccess, onFailure) {
    exec(onSuccess, onFailure, "SugarizerOSPlugin", "setKey", [SSID, key, autologin]);
}

sugarizerOS.forgetNetwork = function(SSID) {
    exec(null, null, "SugarizerOSPlugin", "forgetNetwork", [SSID]);
}

sugarizerOS.selectLauncher = function() {
    exec(null, null, "SugarizerOSPlugin", "selectLauncher", []);
}

sugarizerOS.getInt = function(onSuccess, onFailure, tag) {
    exec(onSuccess, onFailure, "SugarizerOSPlugin", "getInt", [tag])
}

sugarizerOS.resetLaunchesLauncher = function() {
    sugarizerOS.putInt("LAUNCHES", 0);
}

sugarizerOS.putInt = function(tag, value, onSuccess, onFailure) {
    exec(onSuccess, onFailure, "SugarizerOSPlugin", "putInt", [tag, value])
}

sugarizerOS.chooseLauncher = function() {
    exec(null, null, "SugarizerOSPlugin", "chooseLauncher", []);
}

sugarizerOS.checkIfDefaultLauncher = function() {
    exec(sugarizerOS.setIsDefaultLauncher, null, "SugarizerOSPlugin", "isDefaultLauncher", []);
    sugarizerOS.getInt(function(value) {
        sugarizerOS.launches = value;
    }, null, "LAUNCHES");
}

sugarizerOS.getIsDefaultLauncher = function(onSuccess, onFailure) {
    exec(onSuccess, onFailure, "SugarizerOSPlugin", "isDefaultLauncher", []);
}

sugarizerOS.setIsDefaultLauncher = function(value) {
    if (value)
        sugarizerOS.isDefaultLauncher = true;
    else
        sugarizerOS.isDefaultLauncher = false;
}

sugarizerOS.echo = function(onSuccess, onFailure, string) {
    exec(onSuccess, onFailure, "SugarizerOSPlugin", "echo", string);
}

sugarizerOS.getAndroidApplications = function(onSuccess, onFailure, flags) {
    exec(onSuccess, onFailure, "SugarizerOSPlugin", "apps", flags);
}

sugarizerOS.scanWifi = function() {
    exec(function(networks) {
        sugarizerOS.networks = JSON.parse(networks);
    }, null, "SugarizerOSPlugin", "scanWifi", []);
}

sugarizerOS.runActivity = function(packageName) {
    exec(null, null, "SugarizerOSPlugin", "runActivity", [packageName])
}

sugarizerOS.runSettings = function() {
    exec(null, null, "SugarizerOSPlugin", "runSettings", []);
}

sugarizerOS.isAppCacheReady = function(onSuccess) {
    exec(onSuccess, null, "SugarizerOSPlugin", "isAppCacheReady", []);
}

sugarizerOS.applicationsToActivities = function(applications) {
    var activities = [];
    for (i = 0; i < applications.length; i++) {
        activities.push({
            activityId: null,
            directory: null,
            favorite: false,
            icon: applications[i].icon,
            id: applications[i].packageName,
            instances: [],
            name: applications[i].name,
            version: applications[i].version,
            type: "native"
        });
    }
    return activities;
}

sugarizerOS.getAppIndex = function(item, list) {
    for (var i = 0; i < list.length; i++) {
        if (list[i].id == item.id)
            return i;
    }
    return -1;
}

sugarizerOS.initActivitiesPreferences = function(callback) {
    sugarizerOS.getAndroidApplications(function(applications) {
        applications = JSON.parse(applications);
        if (applications != sugarizerOS.appsCache) {
            sugarizerOS.appsCache = applications;
            applications = sugarizerOS.applicationsToActivities(applications);
            var activities = preferences.getActivities();

            for (var i = 0; i < applications.length; i++) {
                var index = sugarizerOS.getAppIndex(applications[i], activities);
                if (index == -1)
                    activities.push(applications[i])
            }
            var apps = [];
            for (var i = 0; i < activities.length; i++) {
                if (activities[i].type && activities[i].type == "native") {
                    if (sugarizerOS.getAppIndex(activities[i], applications) == -1)
                        continue;
                    apps.push(activities[i]);
                } else
                    apps.push(activities[i]);
            }

            apps.sort(function(a, b) {
                return a.name.localeCompare(b.name);
            });
            
            preferences.setActivities(apps);
            if (callback)
                callback();
        }
    }, null, [0]);

}

sugarizerOS.log = function(m) {
    console.log(m);
}

sugarizerOS.init()
