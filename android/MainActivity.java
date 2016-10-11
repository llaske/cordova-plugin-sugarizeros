/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.olpcfrance.sugarizer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.apache.cordova.CordovaActivity;

import java.util.Locale;

public class MainActivity extends CordovaActivity {
    private Context mContext;
    private WindowManager.LayoutParams overAllLayoutParams;
    private WindowManager windowManager;
    private CustomViewGroup customViewGroup;
    private static boolean isHackOn = false;
    private boolean is_default_launcher = false;
    public final static int REQUEST_CODE = -1010101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        //Incrementing the number of launches
        int launches = SharedPreferencesManager.getInt(this, SharedPreferencesManager.LAUNCHES_TAG);
        int is_setup = SharedPreferencesManager.getInt(this, SharedPreferencesManager.IS_SETUP_TAG);
        if (launches >= 0 && is_setup > 0)
            launches++;
        SharedPreferencesManager.putInt(this, SharedPreferencesManager.LAUNCHES_TAG, launches);
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && SugarizerOSPlugin.isMyAppLauncherDefault(this, null)) {
            checkDrawOverlayPermission(this);
            onCreateInitAppView(Settings.canDrawOverlays(mContext));
        } else {
            onCreateInitAppView(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission(Context context) {
        if (!Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            onCreateInitAppView(true);
        }
    }

    private void onCreateInitAppView(boolean notificationBarHacks) {
        is_default_launcher = SugarizerOSPlugin.isMyAppLauncherDefault(mContext, null);
        if (is_default_launcher && notificationBarHacks) {
            //Hiding Notification Bar Hacks
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            preventStatusBarExpansion(mContext);
        }

        if (appView == null)
            customInit(is_default_launcher, notificationBarHacks);
        this.keepRunning = preferences.getBoolean("KeepRunning", true);
        appView.loadUrlIntoView(launchUrl, true);
        setContentView(appView.getView());
    }

    @Override
    protected void onStop() {
        super.onStop();
    /*        is_default_launcher = SugarizerOSPlugin.isMyAppLauncherDefault(mContext, null);

    if (is_default_launcher && isHackOn)
    ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).removeView(customViewGroup);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
    /*
    if (is_default_launcher) {
    preventStatusBarExpansion(this);
    isHackOn = true;
  }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
    }

    public void customInit(boolean is_default_launcher, boolean notificationBarHacks) {
        appView = makeWebView();
        initAppView();

        if (!appView.isInitialized()) {
            appView.init(cordovaInterface, pluginEntries, preferences);
        }
        cordovaInterface.onCordovaInit(appView.getPluginManager());
        // Wire the hardware volume controls to control media if desired.
        String volumePref = preferences.getString("DefaultVolumeStream", "");
        if ("media".equals(volumePref.toLowerCase(Locale.ENGLISH))) {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
        //
        if (is_default_launcher) {
            appView.getView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    public void initAppView() {
        appView.getView().setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        if (preferences.contains("BackgroundColor")) {
            int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
            appView.getView().setBackgroundColor(backgroundColor);
        }
        appView.getView().requestFocusFromTouch();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // ATTENTION: GET THE X,Y OF EVENT FROM THE PARAMETER
        // THEN CHECK IF THAT IS INSIDE YOUR DESIRED AREA


        return false;
    }

    public void preventStatusBarExpansion(Context context) {
        windowManager = ((WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));

        Activity activity = (Activity) context;
        overAllLayoutParams = new WindowManager.LayoutParams();
        overAllLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        overAllLayoutParams.gravity = Gravity.TOP;
        overAllLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        overAllLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;

        int resId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int result = 0;
        if (resId > 0) {
            result = activity.getResources().getDimensionPixelSize(resId);
        }

        overAllLayoutParams.height = result;
        overAllLayoutParams.format = PixelFormat.TRANSPARENT;
        customViewGroup = new CustomViewGroup(context);
        customViewGroup.setBackgroundColor(Color.BLACK);
        windowManager.addView(customViewGroup, overAllLayoutParams);
    }

    public static class CustomViewGroup extends ViewGroup {

        public CustomViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return false;
        }
    }
}
