package com.i550.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class PhotoGalleryFragment extends Fragment{
    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();

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
        setupAdapter();
        return v; // всё что выше - подготовка вью - фрагмента (надо);
    }

    private void setupAdapter() {
        if(isAdded()) {     //если фрагмент был присоединен к активности (изза асинкТаск метод может вызваться не только из активити
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));        //присоединяет адаптер
        }       //проверяет текущее состояние модели, вызывается при любом ее изменении и при создании нового РесайклВью,
        // настраивает адаптер для РесайклВью
    }

    private class PhotoHolder extends RecyclerView.ViewHolder{  //viewHolder  - вывод на экран
        private TextView mTitleTextView;

        public PhotoHolder(View itemView) {
            super(itemView);
            this.mTitleTextView = (TextView)itemView;
        }
        public void bindGalleryItem(GalleryItem item){
            mTitleTextView.setText(item.toString());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{   //адаптер, предоставляет необходимой ФотоХолдер на основании списка ГаллериИтем
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems){
            mGalleryItems=galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);  //получаем ГалериИтем по позиции
            holder.bindGalleryItem(galleryItem);    //биндим

        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>>{ // создает фоновый поток и выполняет doInBackground
        // 1 параметр - тип входных параметров для execute() которые -> в DoInBkgnd
        // 2 параметр - тип для передачи инфы о ходе выполнения
        // 3 параметр - тип результата АсинхТаска (ретурн бэкграунда и входной параметр онПостЕхе)

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            /*
            try {
                String result = new FlickrFetchr().getUrlString("https://www.bignerdranch.com");
                Log.i(TAG, "Fetched contents of URL: " + result);       //запускаем получение данных (фция во FlickrFetchr)
            } catch (IOException ioe){
                Log.e(TAG, "Failed to fetch URL: ", ioe);               //и логгируем получилось или нет
            }
            return null;
        }*/
            return new FlickrFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {     //выполняется после окончания доп.потока, в основном
            mItems=items;       //после загрузки фоток обновляет итемс (загружает лист ГаллериИтемс)
            setupAdapter();     //обновление источника данных РесайклВью
        }
    }
}
