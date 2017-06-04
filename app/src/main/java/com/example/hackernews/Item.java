package com.example.hackernews;

/**
 * Created by Jean on 2017/5/21.
 */

public abstract class Item {
    int id=-1;
    String content=null;//StoryItem is title, CommentItem is text.
    String author=null;
    long postTime=System.currentTimeMillis()/1000;
    ItemType type;

    static enum ItemType{job,story,comment,poll,pollopt}

    protected Item(int id){
        this.id = id;
    }

    protected Item(int id, String content, String author, long postTime){
        this.id = id;
        this.content=content;
        this.author = author;
        this.postTime = postTime;
    }
}
