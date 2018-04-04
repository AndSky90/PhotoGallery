package com.i550.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {       //получатель результата
    private static final String TAG = "NotificationReceiver";       //регистрируем его в манифесте, приоритет-999 (ниже в резерве)
        //интент-фильтр настроаиваем, экспортед-фалс, чтоб с др приложений не видели)

    @Override
    public void onReceive(Context c, Intent i) {
        Log.i(TAG, "received result: " + getResultCode());
        if (getResultCode()!= Activity.RESULT_OK){  //Активность 1 плана отменила рассылку
            return;
        }
        int requestCode = i.getIntExtra(PollService.REQUEST_CODE, 0);
        Notification notification = (Notification) i.getParcelableExtra(PollService.NOTIFICATION);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);
        notificationManager.notify(requestCode,notification);
    }
}
