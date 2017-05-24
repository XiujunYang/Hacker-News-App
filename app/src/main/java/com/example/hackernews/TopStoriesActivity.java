package com.example.hackernews;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class TopStoriesActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    static final int Request_queryTopStories = 1000;
    static final int Request_loadStoryDetail = 1002;
    static final int Response_loadstoryDetail = 1003;

    Context context;
    ArrayList<StoryItem> list = new ArrayList<StoryItem>();
    UIHandler uiHandler;
    RecyclerView recyclerView;
    CustomizedAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        setContentView(R.layout.activity_top_stories);
        if (uiHandler == null) uiHandler = new UIHandler();
        recyclerView = (RecyclerView) findViewById(R.id.stories_recylerview);
        adapter = new CustomizedAdapter((ArrayList<Item>)(ArrayList<?>) list, Item.ItemType.story);
        recyclerView.setLayoutManager(new LinearLayoutManager(TopStoriesActivity.this));
        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.story_swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        //Todo: swipe down to the end of list, and then load a unit amount news.
        /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(!recyclerView.getLayoutManager().canScrollVertically() && dy>0){
                }
            }
        });*/
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list",list);
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onSaveInstanceState(inState);
        list.clear();
        ArrayList<StoryItem> story_list = inState.getParcelableArrayList("list");
        list.addAll(story_list);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume(){
        super.onResume();
        queryTopStories();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        DataLoadingManager.getInstance(getApplicationContext()).quit();
    }

    private void queryTopStories(){
        if(list.size()==0){
            DataLoadingManager instance = DataLoadingManager.getInstance(
                    getApplicationContext());
            Message msg = Message.obtain(instance.getHandler(), Request_queryTopStories, uiHandler);
            msg.sendToTarget();
        }
    }

    @Override
    public void onRefresh() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                list.clear();
                adapter.notifyDataSetChanged();
                queryTopStories();
            }
        });
    }

    class UIHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            MyLog.i("msg.what="+msg.what);
            switch (msg.what){
                case Response_loadstoryDetail:
                    if(msg.obj==null) return;
                    list.clear();
                    list.addAll((ArrayList<StoryItem>) msg.obj);
                    if(mSwipeRefreshLayout!=null && mSwipeRefreshLayout.isRefreshing())
                        mSwipeRefreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();
                    break;
                default: super.handleMessage(msg);
            }
        }
    }
}
