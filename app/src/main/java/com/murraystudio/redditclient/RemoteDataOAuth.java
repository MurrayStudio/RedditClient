package com.murraystudio.redditclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

import static com.murraystudio.redditclient.MainActivity.CLIENT_ID;
import static com.murraystudio.redditclient.MainActivity.CLIENT_SECRET;

/**
 * This class shall serve as a utility class that handles network
 * connections.
 *
 */

public class RemoteDataOAuth extends AsyncTask<String, Void, String> {

    HomePage homepage;

    List<Post> postList;

    String after; //for the next set of posts

    private static AsyncHttpClient client;
    private SharedPreferences pref;

    public RemoteDataOAuth(HomePage homepage){
        this.homepage = homepage;
        init();
    }

    private void init(){
        client = new AsyncHttpClient();
        client.setBasicAuth(CLIENT_ID,CLIENT_SECRET);

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

        client.get(homepage.getActivity(), "https://oauth.reddit.com/api/v1/me", headers, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("response", response.toString());
                try {
                    String username = response.getString("name").toString();
                    Log.i("username", "username: " + username);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("username", username);
                    edit.commit();
                } catch (JSONException j) {
                    j.printStackTrace();
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
        fetchPostExecute(rawData);
    }

    public void fetchPostExecute(String rawData){

    }
}
