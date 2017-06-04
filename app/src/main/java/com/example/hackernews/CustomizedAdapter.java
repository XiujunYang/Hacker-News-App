package com.example.hackernews;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Jean on 2017/5/23.
 */

public class CustomizedAdapter extends RecyclerView.Adapter<CustomizedAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Item> itemList;
    private Item.ItemType type;

    public CustomizedAdapter(ArrayList<Item> list, Item.ItemType type){
        this.itemList = list;
        this.type = type;
    }

    @Override
    public CustomizedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        CustomizedAdapter.ViewHolder holder;
        if(type == Item.ItemType.story)
            holder = new CustomizedAdapter.ViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.story_item_layout, null));
        else holder = new CustomizedAdapter.ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.comment_item_layout, null));
        return holder;
    }

    @Override
    public void onBindViewHolder(CustomizedAdapter.ViewHolder holder, final int position) {
        if(type == Item.ItemType.story) holder.content.setText(itemList.get(position).content);
        else{
            if(itemList.get(position).content!=null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                holder.content.setText(Html.fromHtml(itemList.get(position).content, Html.FROM_HTML_MODE_COMPACT));
            else holder.content.setText(Html.fromHtml(itemList.get(position).content));
        }
        }
        holder.author.setText(itemList.get(position).author==null?"[null]":itemList.get(position).author);
        Date now = new Date(System.currentTimeMillis());
        Date postTime = new java.util.Date(itemList.get(position).postTime*1000);
        long day = TimeUnit.MILLISECONDS.toDays(now.getTime() - postTime.getTime());
        long hour = TimeUnit.MILLISECONDS.toHours(now.getTime() - postTime.getTime());
        long minute = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - postTime.getTime());
        if(day>0) holder.postTime.setText(day+ (day>1?" days ago":" day ago"));
        else if(hour>0) holder.postTime.setText(hour+ (hour>1?" hours ago":" hour ago"));
        else holder.postTime.setText(minute+ (minute>1?" minutes ago":" minute ago"));

        if(type== Item.ItemType.story) {
            holder.score.setText(Integer.toString(((StoryItem) itemList.get(position)).score));
            if (((StoryItem) itemList.get(position)).commentsId != null)
                holder.commentCount.setText(
                        Integer.toString(((StoryItem) itemList.get(position)).commentsId.length) + " comments");
            holder.commentCount.setPaintFlags(holder.commentCount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.commentCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StoryItem item = (StoryItem) itemList.get(position);
                    if (item != null && item.commentsId != null && item.commentsId.length > 0) {
                        Intent intent = new Intent(context, CommentsActivity.class);
                        intent.putExtra("parentId", item.id);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {return itemList.size();}

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView content;
        TextView author;
        TextView postTime;
        TextView score;
        TextView commentCount;
        public ViewHolder(View itemView) {
            super(itemView);
            if(type== Item.ItemType.story) {
                content = (TextView) itemView.findViewById(R.id.story_title);
                score = (TextView) itemView.findViewById(R.id.story_score);
                author = (TextView) itemView.findViewById(R.id.story_author);
                postTime = (TextView) itemView.findViewById(R.id.story_postTime);
                commentCount = (TextView) itemView.findViewById(R.id.story_comments);
            } else{ //ItemType.comment
                content = (TextView) itemView.findViewById(R.id.comment_text);
                author = (TextView) itemView.findViewById(R.id.comment_author);
                postTime = (TextView) itemView.findViewById(R.id.comment_postTime);
            }
        }
    }
}
