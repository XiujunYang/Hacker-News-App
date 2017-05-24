package com.example.hackernews;

/**
 * Created by Jean on 2017/5/21.
 */

public abstract class Item {
    int id;
    String content;//StoryItem is title, CommentItem is text.
    String author;
    long postTime;
    ItemType type;

    static enum ItemType{job,story,comment,poll,pollopt}

    protected Item(int id, String content, String author, long postTime){
        this.id = id;
        this.content=content;
        this.author = author;
        this.postTime = postTime;
    }
}
