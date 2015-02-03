package se.joelpet.android.toyredditreader.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import se.joelpet.android.toyredditreader.R;
import timber.log.Timber;

public class NavigationDrawerFragment extends Fragment implements AdapterView.OnItemClickListener,
        View.OnClickListener {

    @InjectView(R.id.list_view)
    protected ListView mListView;

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

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Timber.i("Clicked on item with position: %d", position);
        if (position < mListView.getHeaderViewsCount()) {
            return;
        }
        int menuItem = position - mListView.getHeaderViewsCount();
        mNavigationItemClickListener.onNavigationItemClick(menuItem);
        // TODO: set item checked
    }

    @Override
    public void onClick(View v) {
        Timber.i("Clicked on view: %s", v);
        switch (v.getId()) {
            case R.id.navigation_drawer_list_footer_settings_button:
                mNavigationItemClickListener
                        .onNavigationItemClick(NavigationItemClickListener.ITEM_SETTINGS);
                break;
        }
    }

    public interface NavigationItemClickListener {

        public static final int ITEM_EVERYTHING = 0;
        public static final int ITEM_SUBSCRIBED = 1;
        public static final int ITEM_SAVED = 2;
        public static final int ITEM_SETTINGS = 3;

        void onNavigationItemClick(int item);
    }
}
