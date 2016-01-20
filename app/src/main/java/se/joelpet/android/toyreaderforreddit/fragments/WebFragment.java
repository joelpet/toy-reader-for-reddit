package se.joelpet.android.toyreaderforreddit.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import se.joelpet.android.toyreaderforreddit.R;
import timber.log.Timber;

public class WebFragment extends BaseFragment {

    @Bind(R.id.web_view)
    protected WebView mWebView;

    private WebViewCallback mWebViewCallback;

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof WebViewCallback) {
            Timber.d("Setting WebViewCallback to: %s", activity);
            mWebViewCallback = (WebViewCallback) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Timber.d("%s.onCreate(%s)", this, savedInstanceState);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_web, container, false);
        ButterKnife.bind(this, rootView);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mWebViewCallback.onWebViewProgressChanged(view, newProgress);
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (mWebViewCallback != null) {
                    mWebViewCallback.onWebViewPageStarted(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mWebViewCallback != null) {
                    mWebViewCallback.onWebViewPageFinished(url);
                }
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setUseWideViewPort(false);

        if (savedInstanceState == null) {
            mWebView.loadUrl(getUriArgument().toString());
        } else {
            mWebView.restoreState(savedInstanceState);
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(Menu.NONE, R.id.menu_item_copy_address, Menu.NONE, "Copy address");
    }

    /**
     * Called by parent activity when used pressed back button.
     *
     * @return {@code true} if action was taken and the back press consumed, otherwise {@code false}
     */
    public boolean onBackPressed() {
        Timber.d("onBackPressed(); mWebView.canGoBack=%b", mWebView.canGoBack());
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consumed = super.onOptionsItemSelected(item);

        if (R.id.menu_item_copy_address == item.getItemId()) {
            ClipboardManager clipboard = (ClipboardManager) getActivity()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            String address = mWebView.getUrl();
            clipboard.setPrimaryClip(ClipData.newPlainText("address", address));
            Toast.makeText(getActivity(), "Address copied to clipboard", Toast.LENGTH_SHORT).show();
            consumed = true;
            Timber.d("Address copied to clipboard: %s", address);
        }

        return consumed;
    }

    private Uri getUriArgument() {
        return (Uri) getArguments().get("uri");
    }

    public interface WebViewCallback {

        void onWebViewPageFinished(String url);

        void onWebViewPageStarted(String url);

        void onWebViewProgressChanged(WebView view, int newProgress);
    }

}
