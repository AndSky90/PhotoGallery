package com.i550.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollService extends IntentService {
    private static final String TAG = "PollService";
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);  //1 minute

    public static Intent newIntent(Context context){
        return new Intent(context, PollService.class);
    }

    public PollService(){
        super(TAG);
    }

    private boolean isNetworkAvailableAndConnected() {      //проверка доступности сети
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo()!=null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(!isNetworkAvailableAndConnected()){
            return;
        }
        String query = QueryPreferences.getStoredQuery(this);   //получаем из хранилища последний запрос
        String lastResultId = QueryPreferences.getLastResultId(this);   //получаем из хранилища последний ИД
        List<GalleryItem> items;

        if(query==null){
            items=new FlickrFetchr().fetchRecentPhotos();
        } else {
            items = new FlickrFetchr().searchPhotos(query);
        }

        if (items.size()==0) return;            //проверка новых результатов
        String resultId = items.get(0).getmId();
        if (resultId.equals(lastResultId)){
            Log.i(TAG, "Got an old result: " + resultId);
        } else {
            Log.i(TAG, "Got an new result: " + resultId);
        //_______________________________нотификация_________________________
            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this,0,i,0);
            Notification notification = new Notification.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))        //текст бегущей строки
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))  //заголовок
                    .setContentText(resources.getString(R.string.new_pictures_text))    //текст
                    .setContentIntent(pi)       //запуск при нажатии
                    .setAutoCancel(true)        //удаляется уведомление после клика по нему
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0,notification);
        }
        QueryPreferences.setLastResultId(this,resultId);
    }

    public static void setServiceAlarm(Context context, boolean isOn){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i,0);     //пожелание запуска интента (интент запускает сервис)
        // (контекст для отправки интента, код, интент, флаги)
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); //AM - системная служба, отправляет интенты, ALARM_SERVICE - отправляет уведомления
        if (isOn) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),POLL_INTERVAL_MS, pi);   //повторяющийся аларм
            // 	setRepeating(int type, время запуска, период повторения, PendingIntent)  -- неточное повторение
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i,PendingIntent.FLAG_NO_CREATE);
        return pi!=null;
    }

}