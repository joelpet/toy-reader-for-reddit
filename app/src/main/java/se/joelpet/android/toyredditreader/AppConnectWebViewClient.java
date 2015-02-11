package se.joelpet.android.toyredditreader;

import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import timber.log.Timber;

public class AppConnectWebViewClient extends WebViewClient {

    public interface OnAppConnectListener {

        void onAllowConnect(String token, String state);

        // See https://github.com/reddit/reddit/wiki/oauth2#token-retrieval-code-flow
        void onError(String error);

    }

    private OnAppConnectListener mOnAppConnectListener;

    public void setOnAppConnectListener(OnAppConnectListener onAppConnectListener) {
        mOnAppConnectListener = onAppConnectListener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("shouldOverrideUrlLoading(%s, %s)", view, url);
        Uri uri = Uri.parse(url);

        if ("toyredditreader".equals(uri.getScheme())) {
            String error = uri.getQueryParameter("error");

            if (error != null) {
                mOnAppConnectListener.onError(error);
                return true;
            }

            String state = uri.getQueryParameter("state");
            String code = uri.getQueryParameter("code");
            Timber.d("Acquired auth token: %s", code);
            mOnAppConnectListener.onAllowConnect(code, state);
            return true;
        }

        return super.shouldOverrideUrlLoading(view, url);
    }
}
