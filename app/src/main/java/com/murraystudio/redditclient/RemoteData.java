package com.murraystudio.redditclient;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class shall serve as a utility class that handles network
 * connections.
 *
 */

public class RemoteData extends AsyncTask<String, Void, String> {

    HomePage homepage;

    List<Post> postList;

    String after; //for the next set of posts

    public RemoteData(HomePage homepage){
        this.homepage = homepage;
    }

    /**
     * This methods returns a Connection to the specified URL,
     * with necessary properties like timeout and user-agent
     * set to your requirements.
     *
     * @param url
     * @return
     */
    public static HttpURLConnection getConnection(String url){
        System.out.println("URL: "+url);
        HttpURLConnection hcon = null;
        try {
            hcon=(HttpURLConnection)new URL(url).openConnection();
            hcon.setReadTimeout(30000); // Timeout at 30 seconds
            hcon.setRequestProperty("User-Agent", "Alien V1.0");
        } catch (MalformedURLException e) {
            Log.e("getConnection()",
                    "Invalid URL: "+e.toString());
        } catch (IOException e) {
            Log.e("getConnection()",
                    "Could not connect: "+e.toString());
        }
        return hcon;
    }

    @Override
    protected String doInBackground(String... url) {
        HttpURLConnection hcon = getConnection(url[0]);
        Log.i("Remote Data", "URL: " + hcon.getURL());
        if(hcon==null){
            Log.e("Remote Data", "hcon was null");
            return null;
        }
        try{
            StringBuffer sb=new StringBuffer(8192);
            String tmp="";
            BufferedReader br=new BufferedReader(
                    new InputStreamReader(
                            hcon.getInputStream()
                    )
            );
            while((tmp=br.readLine())!=null)
                sb.append(tmp).append("\n");
            br.close();
            return sb.toString();
        }catch(IOException e){
            Log.d("READ FAILED", e.toString());
            return null;
        }
    }

    protected void onPostExecute(String rawData) {
        fetchPostExecute(rawData);
    }

    public void fetchPostExecute(String rawData){
        postList = new ArrayList<Post>();
        String raw = rawData;
        try{
            JSONObject data=new JSONObject(raw)
                    .getJSONObject("data");
            JSONArray children=data.getJSONArray("children");

            //Using this property we can fetch the next set of
            //posts from the same subreddit
            after = data.getString("after");

            for(int i=0;i<children.length();i++){
                JSONObject cur=children.getJSONObject(i)
                        .getJSONObject("data");
                Post p=new Post();
                p.title=cur.optString("title");
                p.url=cur.optString("url");
                p.numComments=cur.optInt("num_comments");
                p.points=cur.optInt("score");
                p.author=cur.optString("author");
                p.subreddit=cur.optString("subreddit");
                p.permalink=cur.optString("permalink");
                p.domain=cur.optString("domain");
                p.id=cur.optString("id");
                if(p.title!=null) {
                    postList.add(p);
                }
            }

            homepage.onPostFetchComplete(postList); //we are done so alert HomePage fragment class of new data

        }catch(Exception e){
            Log.e("fetchPosts()",e.toString());
        }
    }
}
