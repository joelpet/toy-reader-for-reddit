package se.joelpet.android.toyredditreader.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import se.joelpet.android.toyredditreader.R;
import se.joelpet.android.toyredditreader.activities.LoginActivity;
import se.joelpet.android.toyredditreader.domain.Me;
import timber.log.Timber;

public class NavigationDrawerFragment extends Fragment implements AdapterView.OnItemClickListener,
        View.OnClickListener {

    public static final int REQUEST_CODE_LOGIN = 1;

    @InjectView(R.id.list_view)
    protected ListView mListView;

    @Optional
    @InjectView(R.id.user_text_view)
    protected TextView mUserTextView;

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

        View listViewFooter = inflater
                .inflate(R.layout.view_navigation_drawer_list_footer, mListView, false);
        listViewFooter.findViewById(R.id.navigation_drawer_list_footer_settings_button)
                .setOnClickListener(this);
        mListView.addFooterView(listViewFooter);

        ButterKnife.inject(this, mListView);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mUserTextView.setOnClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Timber.d("Clicked on item with position: %d", position);
        if (position < mListView.getHeaderViewsCount()) {
            return;
        }
        int menuItem = position - mListView.getHeaderViewsCount();
        mNavigationItemClickListener.onNavigationItemClick(menuItem);
        // TODO: set item checked
    }

    @Override
    public void onClick(View v) {
        Timber.d("Clicked on view: %s", v);
        switch (v.getId()) {
            case R.id.navigation_drawer_list_footer_settings_button:
                mNavigationItemClickListener
                        .onNavigationItemClick(NavigationItemClickListener.ITEM_SETTINGS);
                break;
            case R.id.user_text_view:
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                // TODO: Replace this with events
                Me me = (Me) data.getSerializableExtra("me");
                mUserTextView.setText(me.getName());
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
