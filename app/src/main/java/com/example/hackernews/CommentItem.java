package com.example.hackernews;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jean on 2017/5/21.
 */

public class CommentItem extends Item implements Parcelable {
    int parent=-1;

    public CommentItem(int id){
        super(id);
        this.type=ItemType.comment;
    }

    public CommentItem(int id, String comment, String author, long postTime, int parent){
        super(id,comment,author,postTime);
        this.type=ItemType.comment;
        this.parent = parent;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.content);
        dest.writeString(this.author);
        dest.writeLong(this.postTime);
        dest.writeInt(this.parent);
    }

    public static final Parcelable.Creator<CommentItem> CREATOR = new Parcelable.Creator<CommentItem>() {
        @Override
        public CommentItem createFromParcel(Parcel source) {
            int id= source.readInt();
            String comment = source.readString();
            String author = source.readString();
            long time = source.readLong();
            int parent = source.readInt();
            return new CommentItem(id,comment,author,time,parent);
        }
        @Override
        public CommentItem[] newArray(int size) {
            return new CommentItem[size];
        }
    };
}
