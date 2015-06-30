package se.joelpet.android.toyredditreader.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Optional;
import rx.functions.Action1;
import se.joelpet.android.toyredditreader.R;
import se.joelpet.android.toyredditreader.activities.LoginActivity;
import se.joelpet.android.toyredditreader.domain.Me;
import se.joelpet.android.toyredditreader.storage.LocalDataStore;
import timber.log.Timber;

public class NavigationDrawerFragment extends BaseFragment {

    public static final int REQUEST_CODE_LOGIN = 1;

    @InjectView(R.id.list_view)
    protected ListView mListView;

    @Optional
    @InjectView(R.id.user_text_view)
    protected TextView mUserTextView;

    @Inject
    protected LocalDataStore mLocalDataStore;

    private ArrayAdapter<String> mAdapter;

    private NavigationItemClickListener mNavigationItemClickListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof NavigationItemClickListener) {
            mNavigationItemClickListener = (NavigationItemClickListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject(this);
        mAdapter = new ArrayAdapter<>(getActivity(), R.layout.view_navigation_drawer_list_item,
                R.id.text_view,
                getResources().getStringArray(R.array.navigation_drawer_list_items));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        ButterKnife.inject(this, view);

        View listViewHeader = inflater
                .inflate(R.layout.view_navigation_drawer_list_header, mListView, false);
        mListView.addHeaderView(listViewHeader);
        mListView.addHeaderView(
                inflater.inflate(R.layout.view_navigation_drawer_list_header_space, mListView,
                        false));

        View listViewFooter = inflater.inflate(R.layout.view_navigation_drawer_list_footer,
                mListView, false);
        mListView.addFooterView(listViewFooter);
        mListView.setAdapter(mAdapter);

        ButterKnife.inject(this, mListView);

        addSubscription(bind(mLocalDataStore.observeMe()).subscribe(new Action1<Me>() {
            @Override
            public void call(Me me) {
                mUserTextView.setText(me.getName());
            }
        }));

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unsubscribeFromAll();
    }

    @Optional
    @OnClick(R.id.user_text_view)
    public void onUserTextViewClick() {
        Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
        startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
    }

    @OnItemClick(R.id.list_view)
    public void onListViewItemClick(View v, int position) {
        Timber.d("Clicked on list view item (position=%d): %s", position, v);
        if (position < mListView.getHeaderViewsCount()) {
            return;
        }
        if (v.getId() == R.id.navigation_drawer_list_footer_settings_button) {
            mNavigationItemClickListener
                    .onNavigationItemClick(NavigationItemClickListener.ITEM_SETTINGS);
            return;
        }
        int menuItem = position - mListView.getHeaderViewsCount();
        mNavigationItemClickListener.onNavigationItemClick(menuItem);
        // TODO: set item checked
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                Me me = (Me) data.getSerializableExtra("me");
                mLocalDataStore.putMe(me);
            }
        }
    }

    public interface NavigationItemClickListener {

        int ITEM_EVERYTHING = 0;
        int ITEM_SUBSCRIBED = 1;
        int ITEM_SAVED = 2;
        int ITEM_SETTINGS = 3;

        void onNavigationItemClick(int item);
    }
}
