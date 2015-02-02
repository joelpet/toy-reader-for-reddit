package se.joelpet.android.toyredditreader.fragments;

import android.app.Activity;
import android.app.Fragment;

import se.joelpet.android.toyredditreader.RedditApp;

public class BaseFragment extends Fragment {

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((RedditApp) activity.getApplication()).inject(this);
    }
}
