package com.example.hackernews;

import android.content.Intent;
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
import java.util.Iterator;

public class CommentsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    static final String refresh_from_parentId = "parentId";
    static final int query_parentId_commentsList = 3000;
    static final int load_comment_content = 3001;

    ArrayList<CommentItem> list = new ArrayList<CommentItem>();
    CustomizedAdapter adapter;
    int parentId;
    String[] commentIds;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Intent intent = getIntent();
        parentId = intent.getIntExtra("parentId",-1);
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
        list.clear();
        adapter.notifyDataSetChanged();
        queryComments();
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

    private void queryComments(){
        if(list.size()==0 && parentId!=-1){//commentIds!=null && commentIds.length>0
            new CommentsLoadingAsyncTask().execute(refresh_from_parentId,String.valueOf(parentId));
        }
    }

    public class CommentsLoadingAsyncTask extends AsyncTask<String, Void, Integer> {
        DataLoadingManager instance = DataLoadingManager.getInstance(getApplicationContext());

        protected Integer doInBackground(String... ids) {
            if(ids==null) return -1;
            if(ids[0].equals(refresh_from_parentId)){
                String result = instance.dataLoading(null, ids[1]);
                if (result != null) {
                    try{
                        JSONObject json = new JSONObject(result);
                        int id = json.getInt("id");
                        String type = json.getString("type");
                        if(type.equals(Item.ItemType.story.name())) {
                            String comments= json.has("kids")?json.getString("kids"):null;
                            String[] commentList = comments!=null?
                                    comments.substring(1,comments.length()-1).split(","):null;
                            commentIds = commentList;
                            if(commentIds!=null) return query_parentId_commentsList;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return -1;
            }else {
                String result = instance.dataLoading(null, ids[0]);
                if (result != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        int id = json.getInt("id");
                        String type = json.getString("type");
                        String author = json.has("by") ? json.getString("by") : null;
                        long postTime = json.getLong("time");
                        if (type.equals(Item.ItemType.comment.name())) {
                            String comment = "[This comment was deleted]";
                            if (!json.has("deleted") || json.getBoolean("deleted") == false)
                                comment = json.getString("text");
                            int parent = json.getInt("parent");
                            for (Iterator it = list.iterator(); it.hasNext(); ) {
                                CommentItem item = (CommentItem) it.next();
                                if (item.id == id) {
                                    item.content = comment;
                                    item.author = author;
                                    item.postTime = postTime;
                                    item.parent = parent;
                                }
                            }
                        } else {
                            for (Iterator it = list.iterator(); it.hasNext(); ) {
                                CommentItem item = (CommentItem) it.next();
                                if (item.id == id) it.remove();
                            }
                        }
                        return load_comment_content;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return -1;
            }
        }
        protected void onPostExecute(Integer result) {
            if(result==-1) return;
            if(result==query_parentId_commentsList){
                for(String id:commentIds){
                    CommentItem item = new CommentItem(Integer.parseInt(id));
                    list.add(item);
                    adapter.notifyDataSetChanged();
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        new CommentsLoadingAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,id);
                    else new CommentsLoadingAsyncTask().execute(id);
                }
            }else {
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing())
                    mSwipeRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
