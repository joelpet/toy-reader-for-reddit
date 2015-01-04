package se.joelpet.android.reddit;

import android.test.ActivityInstrumentationTestCase2;

import se.joelpet.android.reddit.activities.SubredditActivity;

public class SubredditActivityTest extends ActivityInstrumentationTestCase2<SubredditActivity> {

    public SubredditActivityTest(Class<SubredditActivity> activityClass) {
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
