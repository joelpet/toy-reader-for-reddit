package se.joelpet.android.toyredditreader;

import android.test.ActivityInstrumentationTestCase2;

import se.joelpet.android.toyredditreader.activities.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // TODO: Configure DAG so that MockedRedditApi is used instead of ProductionRedditApi.
        getActivity(); // Create the activity under test.
    }

    public void testLoadMoreOnScroll() {
        // TODO: Scroll to last visible item and check that MockedRedditApi receives a call.
    }
}
