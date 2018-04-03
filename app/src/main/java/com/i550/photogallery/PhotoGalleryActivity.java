package com.i550.photogallery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {

        return PhotoGalleryFragment.newInstance();  //надо
    }

    public static Intent newIntent(Context context){
        return new Intent(context, PhotoGalleryActivity.class);     //интент для запуска этой активити
    }
}

