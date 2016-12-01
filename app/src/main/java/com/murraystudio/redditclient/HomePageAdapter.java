package com.murraystudio.redditclient;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by sushi_000 on 11/3/2016.
 */

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.MyViewHolder> {

    private List<Post> postList;
    private Activity activity;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mTitleView;
        public TextView mPostTextView;
        public ImageView mImageView;
        public MyViewHolder(View v) {
            super(v);

            mCardView = (CardView) v.findViewById(R.id.card_view);
            mTitleView = (TextView) v.findViewById(R.id.title);
            mPostTextView = (TextView) v.findViewById(R.id.post_text);
            mImageView = (ImageView) v.findViewById(R.id.media);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HomePageAdapter(List<Post> postList, Activity activity) {
        this.postList = postList;
        this.activity = activity;
    }

    @Override
    public HomePageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_page_card_view, parent, false);
            // set the view's size, margins, paddings and layout parameters
            MyViewHolder vh = new MyViewHolder(v);
            return vh;
    }

    @Override
    public void onBindViewHolder(HomePageAdapter.MyViewHolder holder, int position) {
        holder.mTitleView.setText(postList.get(position).title);

        if (postList.get(position).selfText.isEmpty() == false){
            holder.mPostTextView.setVisibility(View.VISIBLE);
            holder.mPostTextView.setText(postList.get(position).selfText);
        }
        else{
            //need an else so when the recycle happens it doesn't create text in wrong places
            holder.mPostTextView.setVisibility(View.GONE);
            holder.mPostTextView.setText("");
        }

        String mediaURL = postList.get(position).url;

        Glide.with(activity)
                .load(mediaURL)
                .crossFade()
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void refresh(List<Post> postList) {
        this.postList = postList;
        notifyDataSetChanged();
    }
}
