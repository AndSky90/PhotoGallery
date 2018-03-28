package com.i550.photogallery;

import android.os.HandlerThread;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public class ThumbnailDownloader<T> extends HandlerThread {    // =объект Looper (фоновый поток для загрузки картинок)
          // <T> - обобщенный аргумент - идентификатор загрузки (тип загружаемых данных) (в том классе указано что это тип <PhotoHolder>
    private static final String TAG = "ThubbnailDownloader";
    private boolean mHasQuit = false;

    public ThumbnailDownloader() {
        super(TAG);
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }
    public void queueThumbnail(T target, String url){        //будет вызываться в PhotoAdapter
        Log.i(TAG, "Got a URL: " + url);
    }
}
