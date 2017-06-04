package com.example.hackernews;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TopStoriesActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    Context context;
    ArrayList<StoryItem> list = new ArrayList<StoryItem>();
    RecyclerView recyclerView;
    CustomizedAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    AsyncTask task;
    HashMap<String,AsyncTask> taskMap = new HashMap<String,AsyncTask>();//id, mapping task
    boolean loading = false;
    String[] stories_ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        setContentView(R.layout.activity_top_stories);
        recyclerView = (RecyclerView) findViewById(R.id.stories_recylerview);
        adapter = new CustomizedAdapter((ArrayList<Item>)(ArrayList<?>) list, Item.ItemType.story);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(TopStoriesActivity.this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.story_swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy>0 && !loading && stories_ids.length>list.size()){
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                    if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading=true;
                        loadStories();
                }
            }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list",list);
        outState.putStringArray("stories_ids",stories_ids);
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onSaveInstanceState(inState);
        list.clear();
        ArrayList<StoryItem> story_list = inState.getParcelableArrayList("list");
        list.addAll(story_list);
        stories_ids = inState.getStringArray("stories_ids");
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume(){
        super.onResume();
        queryTopStories();
    }

    private void queryTopStories(){
        if(list.size()==0){
            if(task!=null) task.cancel(true);
            Iterator it = taskMap.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry item = (Map.Entry)it.next();
                TopStoriesQueryAsyncTask value = (TopStoriesQueryAsyncTask)item.getValue();
                value.cancel(true);
                it.remove();
            }
            task = new TopStoriesQueryAsyncTask().execute("");
        }
    }

    @Override
    public void onRefresh() {
        list.clear();
        adapter.notifyDataSetChanged();
        queryTopStories();
    }

    private void loadStories(){
        int size = list.size();
        for(int i=size;i<(stories_ids.length-size<10?size+(stories_ids.length-size):size+10);i++){
            String id=stories_ids[i];
            AsyncTask storyTask;
            StoryItem item = new StoryItem(Integer.parseInt(id));
            list.add(item);
            adapter.notifyDataSetChanged();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                storyTask = new TopStoriesQueryAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,id);
            else storyTask = new TopStoriesQueryAsyncTask().execute(id);
            taskMap.put(id, storyTask);
        }
    }

    public class TopStoriesQueryAsyncTask extends AsyncTask<String, Void, String[]> {
        DataLoadingManager instance = DataLoadingManager.getInstance(context);

        protected String[] doInBackground(String... ids) {
            if(ids==null) return null;
            if(ids[0]==null || ids[0].length()==0){//query id list of sop stories
                list.clear();
                stories_ids = instance.QueryStoriesId();
                return stories_ids;
            }else{//load story
                String result = instance.dataLoading(null,ids[0]);
                if (result != null) {
                    try{
                        JSONObject json = new JSONObject(result);
                        int id = json.getInt("id");
                        String type = json.getString("type");
                        String author = json.has("by")?json.getString("by"):null;
                        long postTime = json.getLong("time");
                        if(type.equals(Item.ItemType.story.name())) {
                            String title = json.getString("title");
                            int score = json.getInt("score");
                            String url = json.has("url")?json.getString("url"):null;
                            String comments= json.has("kids")?json.getString("kids"):null;
                            String[] commentIds = comments!=null?
                                    comments.substring(1,comments.length()-1).split(","):null;
                            for(Iterator it = list.iterator();it.hasNext();){
                                StoryItem item = (StoryItem)it.next();
                                if(item.id==id){
                                    item.content = title;
                                    item.url = url;
                                    item.score = score;
                                    item.author = author;
                                    item.postTime = postTime;
                                    //It might comments is not existed becaue nobody leave a comment yet.
                                    if(commentIds!=null) item.commentsId = commentIds;
                                }
                            }
                            return new String[]{String.valueOf(id)};
                        } else{// remove type is not story.\
                            MyLog.d("id["+id+"] is not story type, remove it");
                            taskMap.remove(String.valueOf(id));
                            for(Iterator it = list.iterator();it.hasNext();){
                                StoryItem item = (StoryItem)it.next();
                                if(item.id==id) it.remove();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        protected void onPostExecute(String[] result) {
            if(result==null) return;
            if(result.length==1){
                if(mSwipeRefreshLayout!=null && mSwipeRefreshLayout.isRefreshing())
                    mSwipeRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
                taskMap.remove(result[0]);
                if(taskMap.size()<=0) loading=false;
            }else{//result.length>0
                loadStories();
            }
        }
    }
}
