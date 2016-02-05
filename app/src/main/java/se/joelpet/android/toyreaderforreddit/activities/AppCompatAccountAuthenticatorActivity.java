package se.joelpet.android.toyreaderforreddit.activities;

import android.accounts.AccountAuthenticatorActivity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;
import se.joelpet.android.toyreaderforreddit.RedditApplication;
import se.joelpet.android.toyreaderforreddit.dagger.ActivityModule;

public class AppCompatAccountAuthenticatorActivity extends AccountAuthenticatorActivity
        implements AppCompatCallback {

    private ObjectGraph mActivityGraph;

    @Inject
    protected AppCompatDelegate mAppCompatDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mActivityGraph = ((RedditApplication) getApplication()).getApplicationGraph()
                .plus(getModules().toArray());
        mActivityGraph.inject(this);

        getDelegate().installViewFactory();
        super.onCreate(savedInstanceState);
        getDelegate().onCreate(savedInstanceState);
    }

    protected List<Object> getModules() {
        return Arrays.<Object>asList(new ActivityModule(this, this));
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    @NonNull
    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(View v) {
        getDelegate().setContentView(v);
    }

    @Override
    public void setContentView(int resId) {
        getDelegate().setContentView(resId);
    }

    @Override
    public void setContentView(View v, ViewGroup.LayoutParams lp) {
        getDelegate().setContentView(v, lp);
    }

    @Override
    public void addContentView(View v, ViewGroup.LayoutParams lp) {
        getDelegate().addContentView(v, lp);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    public void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    @Override
    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    public AppCompatDelegate getDelegate() {
        if (mAppCompatDelegate == null) mAppCompatDelegate = AppCompatDelegate.create(this, this);
        return mAppCompatDelegate;
    }
}
