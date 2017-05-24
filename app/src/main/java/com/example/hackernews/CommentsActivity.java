package com.example.hackernews;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class CommentsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    static final int Request_loadComments = 2000;
    static final int Response_loadComments = 2001;
    static final int Request_refreshComments = 2002;

    ArrayList<CommentItem> list = new ArrayList<CommentItem>();
    UIHandler uiHandler;
    CustomizedAdapter adapter;
    String[] commentIds;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Intent intent = getIntent();
        commentIds = intent.getStringArrayExtra("commentIds");
        //for(String str:commentIds) MyLog.i("onCreate: "+str+"/"+commentIds.length);
        uiHandler = new UIHandler();
        recyclerView = (RecyclerView) findViewById(R.id.comments_recylerview);
        adapter = new CustomizedAdapter((ArrayList<Item>)(ArrayList<?>) list, Item.ItemType.comment);
        recyclerView.setLayoutManager(new LinearLayoutManager(CommentsActivity.this));
        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.comments_swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }

    @Override
    public void onRefresh() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Object[] obj = new Object[]{(Object)uiHandler,(Object)commentIds};
                Message msg = Message.obtain(DataLoadingManager.getInstance(getApplicationContext()).getHandler(),
                        Request_loadComments, obj);
                msg.sendToTarget();
                list.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list",(ArrayList<CommentItem>)list);
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onSaveInstanceState(inState);
        list.clear();
        ArrayList<CommentItem> comment_list = inState.getParcelableArrayList("list");
        list.addAll(comment_list);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume(){
        super.onResume();
        queryComments();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        DataLoadingManager.getInstance(getApplicationContext()).quit();
    }

    private void queryComments(){
        if(list.size()==0 && commentIds!=null && commentIds.length>0){
            Object[] obj = new Object[]{(Object)uiHandler,(Object)commentIds};
            DataLoadingManager instance = DataLoadingManager.getInstance(getApplicationContext());
            Message msg = Message.obtain(instance.getHandler(), Request_loadComments, obj);
            msg.sendToTarget();
        }
    }

    class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            MyLog.i("msg.what="+msg.what);
            switch (msg.what){
                case Response_loadComments:
                    if(msg.obj==null) return;
                    list.clear();
                    list.addAll((ArrayList<CommentItem>) msg.obj);
                    adapter.notifyDataSetChanged();
                    if(mSwipeRefreshLayout!=null && mSwipeRefreshLayout.isRefreshing())
                        mSwipeRefreshLayout.setRefreshing(false);
                    break;
                default: super.handleMessage(msg);
            }
        }
    }
}
