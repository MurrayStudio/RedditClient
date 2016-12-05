package com.murraystudio.redditclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

import static com.murraystudio.redditclient.MainActivity.CLIENT_ID;
import static com.murraystudio.redditclient.MainActivity.CLIENT_SECRET;

/**
 * This class shall serve as a utility class that handles network
 * connections.
 */

public class RemoteDataOAuth extends AsyncTask<String, Void, String> {

    HomePage homepage;

    List<Post> postList;

    String after; //for the next set of posts

    private static SyncHttpClient client;
    private SharedPreferences pref;

    private JSONArray children;

    public RemoteDataOAuth(HomePage homepage) {
        this.homepage = homepage;
        init();
    }

    private void init() {
        client = new SyncHttpClient();
        client.setBasicAuth(CLIENT_ID, CLIENT_SECRET);

        pref = homepage.getActivity().getSharedPreferences("AppPref", Context.MODE_PRIVATE);
    }

    @Override
    protected String doInBackground(String... url) {
        Log.i("token", pref.getString("token", ""));
        //  client.addHeader("Authorization", "bearer " + pref.getString("token", ""));
        // client.addHeader("User-Agent", "Redditsavedoffline/0.1 by pratik");

        Header[] headers = new Header[2];
        headers[0] = new BasicHeader("User-Agent", "myRedditapp/0.1 by redditusername");
        headers[1] = new BasicHeader("Authorization", "bearer " + pref.getString("token", ""));

        client.get(homepage.getActivity(), "https://oauth.reddit.com", headers, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("response frontpage", response.toString());

                try {
                    children = response.getJSONObject("data").getJSONArray("children");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("response", errorResponse.toString());
                Log.i("statusCode", "" + statusCode);
            }
        });

        return null;
    }

    protected void onPostExecute(String rawData) {
        if (children != null){
            fetchPostExecute();
        }
    }

    public void fetchPostExecute() {

        postList = new ArrayList<Post>();

        try {
            for (int i = 0; i < children.length(); i++) {
                JSONObject cur = children.getJSONObject(i)
                        .getJSONObject("data");
                Post p = new Post();
                p.title = cur.optString("title");
                p.url = cur.optString("url"); //direct link to media or reddit post.
                p.selfText = cur.optString("selftext"); //direct link to any text in reddit post
                p.numComments = cur.optInt("num_comments");
                p.points = cur.optInt("score");
                p.author = cur.optString("author");
                p.subreddit = cur.optString("subreddit");
                p.permalink = cur.optString("permalink");
                p.domain = cur.optString("domain");
                p.id = cur.optString("id");
                if (p.title != null) {
                    postList.add(p);
                }
            }

            homepage.onPostFetchComplete(postList); //we are done so alert HomePage fragment class of new data

        } catch (Exception e) {
            Log.e("RemoteDataOAuth()", e.toString());
        }
    }
}
