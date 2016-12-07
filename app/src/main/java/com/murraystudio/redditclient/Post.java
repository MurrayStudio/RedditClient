package com.murraystudio.redditclient;

/**
 * This is a class that holds the data of the JSON objects
 * returned by the Reddit API.
 */

public class Post {
    String subreddit;
    String title;
    String author;
    int points;
    int numComments;
    String permalink;
    String url;
    String selfText;
    String domain;
    String id;

    String getTitle(){
        return title;
    }

    String getSubreddit(){
        return subreddit;
    }

    String getSelfText(){
        return selfText;
    }

    String getScore(){
        return Integer.toString(points);
    }
}
