package com.i550.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

public class PhotoGalleryFragment extends Fragment{
    private RecyclerView mPhotoRecyclerView;
    private static final String TAG = "PhotoGalleryFragment";

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);    // - удерживаем фрагмент;
        new FetchItemsTask().execute();         //-запускаем асинх таск - получение данных...
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container,false);
        mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        return v; // всё что выше - подготовка вью - фрагмента (надо);
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,Void>{ // создает фоновый поток и выполняет doInBackground

        @Override
        protected Void doInBackground(Void... voids) {
            /*
            try {
                String result = new FlickrFetchr().getUrlString("https://www.bignerdranch.com");
                Log.i(TAG, "Fetched contents of URL: " + result);       //запускаем получение данных (фция во FlickrFetchr)
            } catch (IOException ioe){
                Log.e(TAG, "Failed to fetch URL: ", ioe);               //и логгируем получилось или нет
            }
            return null;
        }*/
            new FlickrFetchr().fetchItems();
            return null;
        }
    }
}
