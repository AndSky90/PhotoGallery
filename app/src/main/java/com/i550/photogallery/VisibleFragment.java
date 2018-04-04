package com.i550.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

public abstract class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";

    @Override
    public void onStart() {     //создаем динамический ресивер (не в манифесте а в коде)
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);   //фильтр тут а не в манифесте
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);    //приемник срабатывает только на интент с моим разрешением
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);      //обязательно в онСтопе останавливаем что запустили
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          //  Toast.makeText(getActivity(), "Got a broadcast: " + intent.getAction(), Toast.LENGTH_SHORT).show();       --при получении высвечивался тост
            Log.i(TAG, "canceling notification");
            setResultCode(Activity.RESULT_CANCELED);        //отменяем оповещение, тк если получено, то пользователь щас в приложении

        }
    };
}
