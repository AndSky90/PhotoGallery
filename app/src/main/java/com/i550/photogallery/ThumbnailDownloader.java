package com.i550.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread {    // =объект Looper (фоновый поток для загрузки картинок)
          // <T> - обобщенный аргумент - идентификатор загрузки (тип загружаемых данных) (в том классе указано что это тип <PhotoHolder>
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;  //ид запроса на загрузку (what)
    private boolean mHasQuit = false;
    private Handler mRequestHandler; // отвечает за постановку очереди запросов на загрузку в фоновом потоке ThumbnailDownloader
    private Handler mResponseHandler;   //
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();
  //  final int cacheSize = 4 * 1024 * 1024;

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
        protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==MESSAGE_DOWNLOAD){     //проверяем тип сообщения
                    T target = (T) msg.obj;     //читаем ид запроса
                    Log.i(TAG, "Got a rqst 4 URL: " + mRequestMap.get(target));
                    handleRequest(target);      //отдаем ид запроса
                }
            }
        };
    }

    private void handleRequest(final T target) {        //загрузка
        try {
            final String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);       //отдаем урл

            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target)!=url||mHasQuit){return;}    //проверка на совпадение урл с изображением
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target,bitmap);
                }
            });
        }catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }
    public void queueThumbnail(T target, String url){        //будет вызываться в PhotoAdapter
        Log.i(TAG, "Got a URL: " + url);
        if (url==null) {mRequestMap.remove(target);}
        else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget(); //-в очередь
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }


}
