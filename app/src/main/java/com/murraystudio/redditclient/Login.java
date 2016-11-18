package com.murraystudio.redditclient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.murraystudio.redditclient.MainActivity.ACCESS_TOKEN_URL;
import static com.murraystudio.redditclient.MainActivity.CLIENT_ID;
import static com.murraystudio.redditclient.MainActivity.REDIRECT_URI;
import static com.murraystudio.redditclient.MainActivity.STATE;

/**
 * Created by yoyoma207 on 11/17/2016.
 */

public class Login extends AppCompatActivity {
    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginlayout);
        //startSignIn(null);

    }


    @Override
    protected void onResume() {
        super.onResume();

        Intent redditIntent = getIntent();

        if(redditIntent != null) {
            if (redditIntent.getAction().equals(Intent.ACTION_VIEW)) {
                Uri uri = redditIntent.getData();
                if (uri.getQueryParameter("error") != null) {
                    String error = uri.getQueryParameter("error");
                } else {
                    String state = uri.getQueryParameter("state");
                    if (state.equals(STATE)) {
                        String code = uri.getQueryParameter("code");
                        getAccessToken(code);
                    }
                }
            }
        }
    }

    private void getAccessToken(String code) {
        client = new OkHttpClient();
        String authString = CLIENT_ID + ":";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=authorization_code&code=" + code +
                                "&redirect_uri=" + REDIRECT_URI))
                .build();

        // GET request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Login", "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    String accessToken = data.optString("access_token");
                    String refreshToken = data.optString("refresh_token");

                    Log.d("Login", "Access Token = " + accessToken);
                    Log.d("Login", "Refresh Token = " + refreshToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        });
    }






}
