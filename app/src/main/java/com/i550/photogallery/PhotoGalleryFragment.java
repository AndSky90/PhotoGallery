package com.i550.photogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import java.util.ArrayList;
import java.util.List;


public class PhotoGalleryFragment extends VisibleFragment{
    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);    // - включаем кнопки в шапке(меню)
        setRetainInstance(true);    // - удерживаем фрагмент;
        updateItems();         //-запускаем асинх таск - получение данных...
        Intent i = PollService.newIntent(getActivity());            //запускаем службу интентом
        getActivity().startService(i);                               //запускаем службу интентом
        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);      //Handler присоединяется к Looper-y текущего потока
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(),thumbnail);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Bkgnd thread started");
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container,false);
        mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        setupAdapter();
        return v; // всё что выше - подготовка вью - фрагмента (надо);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Bkgnd thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {     //вдуваем меню))
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery,menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);     //находим сёрчИтем
        final SearchView searchView = (SearchView) searchItem.getActionView();      //получаем у него SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "QueryTextSubmit: " + s);
                QueryPreferences.setStoredQuery(getActivity(),s);   //сохраняем в SharedPreferences
                updateItems();
                searchView.clearFocus();
                return true;
                }
            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query,false);
            }
        });
        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);     //менять надпись на кнопке
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();      // обновление вида RecyclerView для соответствия последнему запросу
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(),shouldStartAlarm);
                getActivity().invalidateOptionsMenu();  // обновляем меню на панели интсрументов чтоб надпись обновилась
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems(){
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    private void setupAdapter() {
        if(isAdded()) {     //если фрагмент был присоединен к активности (изза асинкТаск метод может вызваться не только из активити
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));        //присоединяет адаптер
        }       //проверяет текущее состояние модели, вызывается при любом ее изменении и при создании нового РесайклВью,
        // настраивает адаптер для РесайклВью
    }
//_____________________________________________

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{  //viewHolder  - вывод на экран
        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {     //вместо Text отображает ImageView (из gallery_item)
            super(itemView);
            mItemImageView = itemView.findViewById(R.id.item_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable){
        mItemImageView.setImageDrawable(drawable);      //грузит картинку в ImageView
        }

        public void bindGalleryItem(GalleryItem item){
            mGalleryItem=item;
        }

        @Override       //по клику стартуем активность - интентом просмотра указанного УРЛ
        public void onClick(View v) { Intent i = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoPageUri());
        startActivity(i);

        }


    }

//_____________________________________________

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{   //адаптер, предоставляет необходимой ФотоХолдер на основании списка ГаллериИтем
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems){
            mGalleryItems=galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {/*
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);*/
            LayoutInflater inflater = LayoutInflater.from(getActivity());       //получаем LayoutInflater
            View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);  //вдуваем в вью, родитель - viewGroup
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);  //получаем ГалериИтем по позиции
            holder.bindGalleryItem(galleryItem);    //биндим
            Drawable placeholder = getResources().getDrawable((R.drawable.bill_up_close));
            holder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(holder, galleryItem.getmUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

//_____________________________________________

    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>>{ // создает фоновый поток и выполняет doInBackground
        // 1 параметр - тип входных параметров для execute() которые -> в DoInBkgnd
        // 2 параметр - тип для передачи инфы о ходе выполнения
        // 3 параметр - тип результата АсинхТаска (ретурн бэкграунда и входной параметр онПостЕхе)
        private String mQuery;

        public FetchItemsTask(String query){
            mQuery=query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {

            if (mQuery==null){
            return new FlickrFetchr().fetchRecentPhotos();
        }else{
                return new FlickrFetchr().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {     //выполняется после окончания доп.потока, в основном
            mItems=items;       //после загрузки фоток обновляет итемс (загружает лист ГаллериИтемс)
            setupAdapter();     //обновление источника данных РесайклВью
        }
    }
}
