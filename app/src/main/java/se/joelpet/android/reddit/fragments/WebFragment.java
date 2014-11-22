package se.joelpet.android.reddit.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import butterknife.ButterKnife;
import butterknife.InjectView;
import se.joelpet.android.reddit.R;
import timber.log.Timber;

public class WebFragment extends Fragment {

    @InjectView(R.id.web_view)
    protected WebView mWebView;

    public static WebFragment newInstance(Uri uri) {
        Timber.d("newInstance(%s)", uri);
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        args.putParcelable("uri", uri);
        fragment.setArguments(args);
        return fragment;
    }

    public WebFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Timber.d("%s.onCreate(%s)", this, savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_web, container, false);
        ButterKnife.inject(this, rootView);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setUseWideViewPort(false);
        return rootView;
    }

    @Override
    public void onStart() {
        Timber.d("%s.onStart()", this);
        super.onStart();
        mWebView.loadUrl(getUriArgument().toString());
    }

    private Uri getUriArgument() {
        return (Uri) getArguments().get("uri");
    }

}
