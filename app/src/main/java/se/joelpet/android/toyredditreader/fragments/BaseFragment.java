package se.joelpet.android.toyredditreader.fragments;

import android.support.v4.app.Fragment;

import se.joelpet.android.toyredditreader.RedditApp;

public class BaseFragment extends Fragment {

    /**
     * Injects any dependencies into the given fragment.
     */
    protected static void inject(Fragment fragment) {
        ((RedditApp) fragment.getActivity().getApplication()).inject(fragment);
    }
}
