package com.murraystudio.redditclient;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static String CLIENT_ID = "LZJx8vb6JeAJLQ";
    private static String REDIRECT_URI = "http://www.example.com/my_redirect";
    private static String GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";
    private static String GRANT_TYPE2 = "authorization_code";
    private static String TOKEN_URL = "access_token";
    private static String OAUTH_URL = "https://www.reddit.com/api/v1/authorize";
    private static String OAUTH_SCOPE = "identity,read,mysubreddits";
    private static String DURATION = "permanent";

    private HomePage homePageFragment;

    WebView web;
    Button auth;
    SharedPreferences pref;
    TextView Access;
    Dialog auth_view;
    String DEVICE_ID = UUID.randomUUID().toString();
    String authCode;
    boolean authComplete = false;

    Intent resultIntent = new Intent();

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
        //Intent myIntent = new Intent(this, Login.class);
        //this.startActivity(myIntent);

        // Create a new Fragment to be placed in the activity layout
        homePageFragment = new HomePage();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, homePageFragment).addToBackStack(null).commit();

        //startSignIn();
    }

    public void startSignIn() {
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        // TODO Auto-generated method stub
        auth_view = new Dialog(MainActivity.this);
        auth_view.setContentView(R.layout.auth_view);
        web = (WebView) auth_view.findViewById(R.id.webv);
        web.getSettings().setJavaScriptEnabled(true);
        String url = OAUTH_URL + "?client_id=" + CLIENT_ID + "&response_type=code&state=TEST&redirect_uri=" + REDIRECT_URI + "&scope=" + OAUTH_SCOPE;
        web.loadUrl(url);
        Toast.makeText(getApplicationContext(), "" + url, Toast.LENGTH_LONG).show();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.contains("?code=") || url.contains("&code=")) {

                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    Log.i("", "CODE : " + authCode);
                    authComplete = true;
                    resultIntent.putExtra("code", authCode);
                    MainActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("Code", authCode);
                    edit.commit();
                    auth_view.dismiss();
                    Toast.makeText(getApplicationContext(), "Authorization Code is: " + pref.getString("Code", ""), Toast.LENGTH_SHORT).show();

                    try {
                        new Login(getApplicationContext()).getToken(TOKEN_URL, GRANT_TYPE2, DEVICE_ID);
                        Toast.makeText(getApplicationContext(), "Access Token: " + pref.getString("token", ""), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (url.contains("error=access_denied")) {
                    Log.i("", "ACCESS_DENIED_HERE");
                    resultIntent.putExtra("code", authCode);
                    authComplete = true;
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();

                    auth_view.dismiss();
                }
            }
        });
        auth_view.show();
        auth_view.setTitle("Authorize");
        auth_view.setCancelable(true);



        homePageFragment.fetchPosts2();
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
            startSignIn();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
