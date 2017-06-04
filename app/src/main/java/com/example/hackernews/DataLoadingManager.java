package com.example.hackernews;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

/** Get stories or comments from Restful API.
 * Created by Jean on 2017/5/21.
 */
public class DataLoadingManager {
    final String url_query_top_stories = "https://hacker-news.firebaseio.com/v0/topstories.json";
    final String url_load_item = "https://hacker-news.firebaseio.com/v0/item/";
    final String url_load_item_end = ".json";

    private Context mContext;
    private static DataLoadingManager instance;

    public static DataLoadingManager getInstance(Context context){
        if(instance==null) {
            instance = new DataLoadingManager(context);
        }
        return instance;
    }

    private DataLoadingManager(Context context){
        this.mContext = context;
    }

    public String[] QueryStoriesId(){
        String result = dataLoading(url_query_top_stories,null);
        if (result != null) {
            result = result.substring(1,result.length()-1);
            return result.split(",");
        }
        return null;
    }

    public String dataLoading(String url, String id){
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

    public boolean isNetworkAvailable() {
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
