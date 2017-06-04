package com.example.hackernews;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jean on 2017/5/21.
 */

public class StoryItem extends Item implements Parcelable {
    String url=null;
    int score=0;
    String[] commentsId= new String[0];

    public StoryItem(int id){
        super(id);
        this.type = ItemType.story;
    }

    public StoryItem(int id, String title, String url, int score, String author, long postTime, String[] comments){
        super(id, title, author,postTime);
        this.url=url;
        this.score=score;
        this.commentsId=comments;
        this.type = ItemType.story;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.content);
        dest.writeString(this.url);
        dest.writeInt(this.score);
        dest.writeString(this.author);
        dest.writeLong(this.postTime);
        if(this.commentsId==null)
        dest.writeInt(0);
        else {
            dest.writeInt(this.commentsId.length);
            dest.writeStringArray(this.commentsId);
        }
    }

    public static final Parcelable.Creator<StoryItem> CREATOR = new Parcelable.Creator<StoryItem>() {
        @Override
        public StoryItem createFromParcel(Parcel source) {
            int id= source.readInt();
            String title = source.readString();
            String url = source.readString();
            int score = source.readInt();
            String author = source.readString();
            long time = source.readLong();
            int commentLen = source.readInt();
            String[] comments;
            if(commentLen==0) comments=null;
            else{
                comments = new String[commentLen];
                source.readStringArray(comments);
            }
            return new StoryItem(id,title,url,score,author,time,comments);
        }
        @Override
        public StoryItem[] newArray(int size) {
            return new StoryItem[size];
        }
    };
}
