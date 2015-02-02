package se.joelpet.android.toyredditreader.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import se.joelpet.android.toyredditreader.R;
import se.joelpet.android.toyredditreader.fragments.NavigationDrawerFragment;
import se.joelpet.android.toyredditreader.fragments.SubredditListingFragment;

public class SubredditActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationItemClickListener {

    @InjectView(R.id.toolbar)
    protected Toolbar mToolbar;

    @InjectView(R.id.drawer_layout)
    protected DrawerLayout mDrawerLayout;

    @InjectView(R.id.navigation_drawer_fragment_container)
    protected ViewGroup mNavigationDrawerFragmentContainer;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SubredditListingFragment())
                    .commit();
        }

        onNavigationItemClick(ITEM_EVERYTHING);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.subreddit, menu);
        return false;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNavigationDrawerFragmentContainer)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationItemClick(int item) {
        // TODO: Implement when required fragments and activities have been implemented
        switch (item) {
            case ITEM_EVERYTHING:
                mToolbar.setTitle("Everything");
                mToolbar.setSubtitle("from all subreddits");
                break;
            case ITEM_SUBSCRIBED:
                mToolbar.setTitle("Subscribed");
                mToolbar.setSubtitle("your frontpage");
                break;
            case ITEM_SAVED:
                mToolbar.setTitle("Saved");
                mToolbar.setSubtitle(null);
                break;
            case ITEM_SETTINGS:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            default:
                return;
        }
        mDrawerLayout.closeDrawers();
    }
}
