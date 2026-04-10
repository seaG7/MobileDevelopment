package ru.mirea.danilov.mireaproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WebViewFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        android.webkit.WebView webView = view.findViewById(R.id.webView);
        webView.setWebViewClient(new android.webkit.WebViewClient());
        webView.loadUrl("https://www.google.com");
        return view;
    }
}