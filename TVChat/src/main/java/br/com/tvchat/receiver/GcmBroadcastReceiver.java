package br.com.tvchat.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import br.com.tvchat.service.MessageHandleService;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    public GcmBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(), MessageHandleService.class.getName());

        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
