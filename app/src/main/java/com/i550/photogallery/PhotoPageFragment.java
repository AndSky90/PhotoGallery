package com.i550.photogallery;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class PhotoPageFragment extends VisibleFragment {
    private static final String ARG_URI = "photo_page_url";
    private Uri mUri;
    private WebView mWebView;
    private ProgressBar mProgressBar;

    public static PhotoPageFragment newInstance(Uri uri){       //конструктор - при передаче УРЛ создает фрагмент и передает ему УРЛ в бандле
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {     //тут расшифровывает бандл
        super.onCreate(savedInstanceState);
        mUri = getArguments().getParcelable(ARG_URI);
    }

    @Nullable
    @Override @SuppressLint("setJavaScriptEnabled") //подавили варнинг от JS
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_page, container, false);

        mWebView = (WebView) v.findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);      //включаем JS в настройках WebView

        mProgressBar = v.findViewById(R.id.progress_bar);
        mProgressBar.setMax(100);

        mWebView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView webView, int newProgress){            //обновление индикатора прогресса
                if (newProgress==100){
                    mProgressBar.setVisibility(View.GONE);      //если 100% то скрываем прогрессБар
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }
            public void onReceivedTitle(WebView webView,String title){                  //обновление заголовка вебвью (получает от страницы в инете)
                AppCompatActivity activity = (AppCompatActivity)getActivity();
                activity.getSupportActionBar().setSubtitle(title);
            }
        });
        mWebView.setWebViewClient(new WebViewClient());         //устанавливаем у нас
        mWebView.loadUrl(mUri.toString());
        return v;
    }
}
