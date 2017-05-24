package com.example.hackernews;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

import static com.example.hackernews.CommentsActivity.Request_loadComments;
import static com.example.hackernews.CommentsActivity.Response_loadComments;
import static com.example.hackernews.TopStoriesActivity.Request_loadStoryDetail;
import static com.example.hackernews.TopStoriesActivity.Request_queryTopStories;
import static com.example.hackernews.TopStoriesActivity.Response_loadstoryDetail;


/** Get stories or comments from Restful API.
 * Created by Jean on 2017/5/21.
 */

public class DataLoadingManager {
    final String url_query_top_stories = "https://hacker-news.firebaseio.com/v0/topstories.json";
    final String url_load_item = "https://hacker-news.firebaseio.com/v0/item/";
    final String url_load_item_end = ".json";

    private Context mContext;
    private static DataLoadingManager instance;
    private HandlerThread handlerThread;
    private Handler bgdHander;
    private List<StoryItem> storyLists = new ArrayList<StoryItem>();
    private List<CommentItem> commentList = new ArrayList<CommentItem>();

    private int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final int KEEP_ALIVE_TIME = 1;
    private final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    BlockingQueue<Runnable> mWorkQueue = new LinkedBlockingQueue<Runnable>();
    ThreadPoolExecutor mThreadPool;

    public static DataLoadingManager getInstance(Context context){
        if(instance==null) {
            instance = new DataLoadingManager(context);
        }
        return instance;
    }

    private DataLoadingManager(Context context){
        this.mContext = context;
        initalBgdThread();
    }

    public Handler getHandler(){
        if(handlerThread!=null && !handlerThread.isAlive()) {
            MyLog.d("Restart handlerThread.");
            initalBgdThread();
        }
        return bgdHander;
    }

    //Release resource
    public void quit(){
        if(handlerThread!=null){
            MyLog.d("End handlerThread.");
            //Todo: Espresso test will fail while multiple test, because here kill handler.
            handlerThread.quit();
        }
        commentList.clear();
        storyLists.clear();
    }

    private void initalBgdThread(){
        handlerThread = new HandlerThread("DataLoader");
        handlerThread.start();
        bgdHander = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg){
                MyLog.i("msg.what="+msg.what);
                Handler caller;
                switch (msg.what){
                    case Request_queryTopStories:
                        terminatedOtherRquestsOrAliveThreads();
                        storyLists.clear();
                        caller = (Handler)msg.obj;
                        String[] result = QueryStoriesId();
                        Object[] obj = new Object[]{(Object)caller,(Object)result};
                        if(result !=null) obtainMessage(Request_loadStoryDetail,obj).sendToTarget();
                        break;
                    case Request_loadComments:
                    case Request_loadStoryDetail:
                        terminatedOtherRquestsOrAliveThreads();
                        if(msg.what==Request_loadComments) commentList.clear();
                        caller = (Handler)((Object[])msg.obj)[0];
                        String[] targetId = (String[])((Object[])msg.obj)[1];
                        loadStoryItemOrCommentByMultiThreads(targetId,caller);
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
            }
        };
    }

    private boolean isMultipleThreadAlive(){
        return mThreadPool!=null && !mThreadPool.isTerminating() && !mThreadPool.isTerminated();
    }

    private void terminatedOtherRquestsOrAliveThreads(){
        if(isMultipleThreadAlive())mThreadPool.shutdownNow();
        getHandler().removeMessages(Request_loadStoryDetail);
        getHandler().removeMessages(Request_loadComments);
    }

    private String[] QueryStoriesId(){
        String result = dataLoading(url_query_top_stories,null);
        if (result != null) {
            result = result.substring(1,result.length()-1);
            return result.split(",");
        }
        return null;
    }

    private String dataLoading(String url, String id){
        if(isNetworkAvailable()) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet;
            try {
                if(url!=null) httpGet= new HttpGet(url);
                else httpGet= new HttpGet(url_load_item.concat(id).concat(url_load_item_end));
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    InputStream inputStream = httpResponse.getEntity().getContent();
                    if(inputStream == null) return null;
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = "", result = "";
                    while((line = bufferedReader.readLine()) != null)
                        result += line;
                    inputStream.close();
                    bufferedReader.close();
                    return result;
                } else MyLog.e("Get error statusCode:" + statusCode);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
        return null;
    }

    private void loadStoryItemOrCommentItem(String itemId, Handler callback){
        String result = dataLoading(null, itemId);
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
                    StoryItem storyItem = new StoryItem(id,title,null,score,author,postTime,commentIds);
                    storyLists.add(storyItem);
                    if(callback!=null)
                        callback.obtainMessage(Response_loadstoryDetail,storyLists).sendToTarget();
                } else if(type.equals(Item.ItemType.comment.name())){
                    String comment = "[This comment was deleted]";
                    if(!json.has("deleted") || json.getBoolean("deleted")==false)
                        comment = json.getString("text");
                    int parent = json.getInt("parent");
                    CommentItem commentItem = new CommentItem(id,comment,author,postTime,parent);
                    if(commentList!=null && commentList.size()>0 && commentList.get(0)!=null &&
                            commentList.get(0).parent!=commentItem.parent)
                        commentList.clear();
                    commentList.add(commentItem);
                    if(callback!=null)
                        callback.obtainMessage(Response_loadComments,commentList).sendToTarget();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean loadStoryItemOrCommentByMultiThreads(String[] Ids, Handler callback){
        if(Ids==null) {
            MyLog.e("Id array is null");
            return false;
        }
        if(mThreadPool==null || mThreadPool.isShutdown())
            mThreadPool = new ThreadPoolExecutor(
                    NUMBER_OF_CORES,       // Initial pool size
                    NUMBER_OF_CORES,       // Max pool size
                    KEEP_ALIVE_TIME,
                    KEEP_ALIVE_TIME_UNIT,
                     mWorkQueue);
        for(String id:Ids){//Todo: swipe down to the end of list, and then load a unit amount news.
            mThreadPool.execute(new loadItemRunnable(id, callback));
        }
        return true;
    }

    class loadItemRunnable implements Runnable {
        String itemId;
        Handler callback;
        public loadItemRunnable(String id, Handler callback){
            this.itemId = id;
            this.callback = callback;
        }
        @Override
        public void run() {
            loadStoryItemOrCommentItem(itemId, callback);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()) return true;
        else {
            Toast.makeText(mContext, mContext.getResources().getText(R.string.notify_network_unavailable),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
