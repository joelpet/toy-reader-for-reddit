package se.joelpet.android.reddit.activities;

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("%s.onCreate(%s); intent=%s", this, savedInstanceState, getIntent());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        if (savedInstanceState == null) {
            WebFragment webFragment = WebFragment.newInstance(getUriArgument());
            getSupportFragmentManager().beginTransaction().add(R.id.container, webFragment)
                    .commit();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Uri getUriArgument() {
        return (Uri) getIntent().getExtras().get("uri");
    }
}
