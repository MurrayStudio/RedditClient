package com.murraystudio.redditclient;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Main entry point of the application. Handles the UI,
 * fragment view transitions, and part of the log in process.
 */
public class MainActivity extends AppCompatActivity {

    //vars related to oAuth authorization on the Reddit server.
    public static String CLIENT_ID = "LZJx8vb6JeAJLQ";
    public static String REDIRECT_URI = "http://localhost";
    public static String GRANT_TYPE2 = "authorization_code";
    public static String TOKEN_URL = "access_token";
    public static String OAUTH_URL = "https://www.reddit.com/api/v1/authorize";
    public static String OAUTH_URL2 = "https://www.reddit.com/api/v1/access_token";
    public static String OAUTH_SCOPE = "identity,read,mysubreddits";
    public static String CLIENT_SECRET ="";

    private HomePage homePageFragment;
    private Login login;

    private WebView web;
    private SharedPreferences pref;
    private Dialog auth_view;
    private String DEVICE_ID;
    private String authCode;
    private Intent resultIntent = new Intent();

    private List<Post> postList; //where we keep a list of the posts we get from the reddit server

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DEVICE_ID = UUID.randomUUID().toString(); //for getting token

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


        // Create a new Fragment to be placed in the activity layout
        homePageFragment = new HomePage();
        // Replace whatever is in the fragment_container view with this fragment
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, homePageFragment).commit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subredditSearch(); //start a search for a new subreddit to populate posts from
            }
        });
    }

    private void subredditSearch(){
        //show popup dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);
        edittext.setHint("askreddit");
        alert.setTitle("Navigate to a Subreddit");
        alert.setView(edittext);

        alert.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String subredditString = edittext.getText().toString();
                homePageFragment.fetchPosts(subredditString); //get posts from new subreddit
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //cancel search
            }
        });

        alert.show();
    }

    public void startSignIn() {
        //start a web view so we can sign in to Reddit servers and get authorization code
        //so we can then request an access token to retrieve the info of the signed in user.
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        // TODO Auto-generated method stub
        auth_view = new Dialog(MainActivity.this);
        auth_view.setContentView(R.layout.auth_view);
        web = (WebView) auth_view.findViewById(R.id.webv);
        web.getSettings().setJavaScriptEnabled(true);
        String url = OAUTH_URL + "?client_id=" + CLIENT_ID + "&response_type=code&state=TEST&redirect_uri=" + REDIRECT_URI + "&scope=" + OAUTH_SCOPE;
        web.loadUrl(url);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url); //load page
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                //when page finishes, we should have a code that we can use to get access token from reddit.
                if (url.contains("?code=") || url.contains("&code=")) {

                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    Log.i("authCode", "CODE : " + authCode);
                    resultIntent.putExtra("code", authCode);
                    MainActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("Code", authCode); //store auth code for use throughout application
                    edit.commit();
                    auth_view.dismiss();
                    try {
                        login = new Login(getApplicationContext());

                        //now we can request the access token in the Login class with our auth code.
                        login.getToken(TOKEN_URL, GRANT_TYPE2, DEVICE_ID);

                        //login.getUsername();

                        //after we have access token, let's get the user's frontpage
                        homePageFragment.fetchPostsOAuth();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (url.contains("error=access_denied")) {
                    Log.i("", "ACCESS_DENIED_HERE");
                    resultIntent.putExtra("code", authCode);
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();

                    auth_view.dismiss();
                }
            }
        });
        auth_view.show();
        auth_view.setTitle("Authorize");
        auth_view.setCancelable(true);


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
        if (id == R.id.log_in) {
            startSignIn(); //hitting the login menu button starts the sign in process.
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
