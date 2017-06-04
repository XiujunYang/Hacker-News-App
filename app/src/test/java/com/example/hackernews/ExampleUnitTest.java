package com.example.hackernews;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(RobolectricTestRunner.class)
public class ExampleUnitTest{
    DataLoadingManager instance;
    final CountDownLatch signal = new CountDownLatch(1);
    @Before
    public void setup(){
        Context context = RuntimeEnvironment.application.getApplicationContext();
        instance = DataLoadingManager.getInstance(context);
    }

    @Test
    public void testTopStoriesLoading(){
        String[] result = instance.QueryStoriesId();
        if(instance.isNetworkAvailable()) assertTrue(result.length > 0);
        else assertEquals(result,null);
    }

    @Test
    public void testCommentType(){
        String[] result = instance.QueryStoriesId();
        if(result!=null && result.length>0){
            for(int i=0;i<5;i++) {
                int pos = getRandomNumberInRange(0,result.length-1);
                String response = instance.dataLoading(null, result[pos]);
                try {
                    JSONObject json = new JSONObject(response);
                    int id = json.getInt("id");
                    String type = json.getString("type");
                    if (type.equals(Item.ItemType.story.name())) {
                        String comments = json.has("kids") ? json.getString("kids") : null;
                        String[] commentIds = comments != null ?
                                comments.substring(1, comments.length() - 1).split(",") : null;
                        if (commentIds != null && commentIds.length > 0) {
                            int index = getRandomNumberInRange(0,commentIds.length-1);
                            response = instance.dataLoading(null, commentIds[index]);
                            try {
                                json = new JSONObject(response);
                                id = json.getInt("id");
                                type = json.getString("type");
                                assertEquals(type, Item.ItemType.comment.name());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testInitialItem(){
        StoryItem item = new StoryItem(14475791);
        assertEquals(item.content,null);
        assertEquals(item.commentsId.length,0);
        assertEquals(item.score,0);
        assertEquals(item.url,null);
        assertEquals(item.author,null);
        assertEquals(item.type, Item.ItemType.story);
    }

    @Test
    public void verifyWithoutUrlStoryItem(){
        String result = instance.dataLoading(null,"14478104");
        if (result != null) {
            try{
                JSONObject json = new JSONObject(result);
                int id = json.getInt("id");
                String type = json.getString("type");
                if(type.equals(Item.ItemType.story.name())) {
                    String url = json.has("url")?json.getString("url"):null;
                    assertEquals(url, null);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private int getRandomNumberInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}