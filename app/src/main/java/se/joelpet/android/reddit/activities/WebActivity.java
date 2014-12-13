package se.joelpet.android.reddit.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import se.joelpet.android.reddit.R;
import se.joelpet.android.reddit.fragments.WebFragment;
import timber.log.Timber;

public class WebActivity extends ActionBarActivity implements WebFragment.WebViewCallback {

    public static final String ARG_URI = "uri";

    public static void startActivity(Context context, Uri uri) {
        Timber.d("startActivity(%s, %s)", context, uri);
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra(ARG_URI, uri);
        context.startActivity(intent);

        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("%s.onCreate(%s); intent=%s", this, savedInstanceState, getIntent());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        updateActionBarTitle(getSupportActionBar(), getUriArgument());

        if (savedInstanceState == null) {
            WebFragment webFragment = WebFragment.newInstance(getUriArgument());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, webFragment, "web_fragment").commit();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("onOptionsItemSelected(%s)", item);
        switch (item.getItemId()) {
            case android.R.id.home:
                // TODO: Is it possible to animate the back button action too?
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        WebFragment webFragment = (WebFragment) getSupportFragmentManager()
                .findFragmentByTag("web_fragment");

        if (!webFragment.onBackPressed()) {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    @Override
    public void onWebViewPageFinished(String url) {
        updateActionBarTitle(getSupportActionBar(), url);
    }

    @Override
    public void onWebViewPageStarted(String url) {
        updateActionBarTitle(getSupportActionBar(), url);
    }

    private static void updateActionBarTitle(ActionBar actionBar, String url) {
        updateActionBarTitle(actionBar, Uri.parse(url));
    }

    private static void updateActionBarTitle(ActionBar actionBar, Uri uri) {
        String title = uri.getHost();
        int subtitleStart = uri.toString().indexOf(title) + title.length();
        String subtitle = uri.toString().substring(subtitleStart);
        actionBar.setTitle(title);
        actionBar.setSubtitle(subtitle);
    }

    private Uri getUriArgument() {
        return (Uri) getIntent().getExtras().get(ARG_URI);
    }
}
