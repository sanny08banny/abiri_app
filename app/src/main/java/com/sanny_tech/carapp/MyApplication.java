package com.sanny_tech.carapp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class MyApplication extends Application {

    private static MyApplication instance;
    private boolean isInBackground = true;
    private OnAppBackgroundListener onAppBackgroundListener;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {}

            @Override
            public void onActivityResumed(Activity activity) {
                isInBackground = false;
                if (onAppBackgroundListener != null) {
                    onAppBackgroundListener.onAppForeground();
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                isInBackground = true;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                if (isInBackground) {
                    if (onAppBackgroundListener != null) {
                        onAppBackgroundListener.onAppBackground();
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {}
        });
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public void setOnAppBackgroundListener(OnAppBackgroundListener listener) {
        this.onAppBackgroundListener = listener;
    }

    public interface OnAppBackgroundListener {
        void onAppBackground();
        void onAppForeground();
    }
}
