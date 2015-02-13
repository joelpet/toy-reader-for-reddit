package se.joelpet.android.toyredditreader.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import se.joelpet.android.toyredditreader.RedditApp;
import timber.log.Timber;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("%s###onCreate(%s)", this, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.v("%s###onStart()", this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.v("%s###onStop()", this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.v("%s###onDestroy()", this);
    }

    /**
     * Injects any dependencies into the given fragment.
     */
    protected static void inject(Fragment fragment) {
        ((RedditApp) fragment.getActivity().getApplication()).inject(fragment);
    }
}
