package se.joelpet.android.reddit.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import se.joelpet.android.reddit.R;
import se.joelpet.android.reddit.fragments.WebFragment;
import timber.log.Timber;

public class WebActivity extends ActionBarActivity {

    public static void startActivity(Context context, Uri uri) {
        Timber.d("startActivity(%s, %s)", context, uri);
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra("uri", uri);
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

        if (savedInstanceState == null) {
            WebFragment webFragment = WebFragment.newInstance(getUriArgument());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, webFragment, "web_fragment").commit();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("onOptionsItemSelected(%s)", item);
        switch (item.getItemId()) {
            case android.R.id.home:
                // TODO: Is it possible to animate the back button action too?
                break;
            case R.id.action_settings:
                return true;
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

    private Uri getUriArgument() {
        return (Uri) getIntent().getExtras().get("uri");
    }
}
