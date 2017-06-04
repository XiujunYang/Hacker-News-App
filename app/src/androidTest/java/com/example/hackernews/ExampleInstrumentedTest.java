package com.example.hackernews;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Rule
    public ActivityTestRule<TopStoriesActivity> mActivityStoreRule = new ActivityTestRule(TopStoriesActivity.class);
    public ActivityTestRule<CommentsActivity> mActivityCommentsRule = new ActivityTestRule(CommentsActivity.class);
    private TopStoriesActivity topStoriesActivity;
    Context context = InstrumentationRegistry.getContext();

    @Before
    public void setActivity() {
        topStoriesActivity = mActivityStoreRule.getActivity();
    }

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.example.hackernews", appContext.getPackageName());
    }

    @Test
    public void checkSingleInstance(){
        assertEquals(DataLoadingManager.getInstance(context),DataLoadingManager.getInstance(context));
    }
}
