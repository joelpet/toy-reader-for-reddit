package se.joelpet.android.toyredditreader.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import se.joelpet.android.toyredditreader.RedditApp;

public class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RedditApp) getApplication()).inject(this);
    }

}
