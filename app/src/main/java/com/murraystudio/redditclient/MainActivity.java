package com.murraystudio.redditclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Post> postList;

    String after; //for the next set of posts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

        }

        postList = new ArrayList<Post>();
        fetchPosts();
        Intent myIntent = new Intent(this, Login.class);
        this.startActivity(myIntent);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void fetchPosts(){
        //String raw=RemoteData.readContents("https://www.reddit.com/r/planetcoaster/.json?after=AFTER");
        RemoteData remoteData = new RemoteData(this);
        remoteData.execute("https://www.reddit.com/r/planetcoaster/.json?after=AFTER");
    }

    public void fetchPostExecute(String rawData){
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
                if(p.title!=null)
                    postList.add(p);
            }

            // Create a new Fragment to be placed in the activity layout
            HomePage homePageFragment = new HomePage();
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, homePageFragment).addToBackStack(null).commit();

        }catch(Exception e){
            Log.e("fetchPosts()",e.toString());
        }
    }

    public List<Post> getPosts(){
        return postList;
    }
}
