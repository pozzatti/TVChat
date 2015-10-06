package br.com.tvchat;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

public class MyLifecycleHandler implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        Log.w("LIFECICLY", "ACTIVITY CREATED " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.w("LIFECICLY", "ACTIVITY STARTED " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.w("LIFECICLY", "ACTIVITY RESUMED " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.w("LIFECICLY", "ACTIVITY PAUSED " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.w("LIFECICLY", "ACTIVITY STOPPED " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        Log.w("LIFECICLY", "ACTIVITY SAVE INSTANCE STATE " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.w("LIFECICLY", "ACTIVITY DESTROYED " + activity.getClass().getSimpleName());
    }
}
