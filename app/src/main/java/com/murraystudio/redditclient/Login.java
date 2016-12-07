package com.murraystudio.redditclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.murraystudio.redditclient.MainActivity.CLIENT_ID;
import static com.murraystudio.redditclient.MainActivity.CLIENT_SECRET;
import static com.murraystudio.redditclient.MainActivity.OAUTH_URL2;
import static com.murraystudio.redditclient.MainActivity.REDIRECT_URI;

/**
 * Handles the retrieval of the Reddit access token using a HTTP client POST request.
 */

public class Login {
    SharedPreferences pref;
    String token;
    Context context;

    Login(Context cnt){
        context = cnt;
    }
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        //where we issue POST request to Reddit with the authorization code
        //so we get an access token in return
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return OAUTH_URL2;
    }

    public void getToken(String relativeUrl,String grant_type,String device_id) throws JSONException {
        client.setBasicAuth(CLIENT_ID,CLIENT_SECRET);
        pref = context.getSharedPreferences("AppPref",Context.MODE_PRIVATE);
        String code = pref.getString("Code", "N/A");

        RequestParams requestParams = new RequestParams();
        requestParams.put("code",code);
        requestParams.put("grant_type",grant_type);
        requestParams.put("redirect_uri", REDIRECT_URI);

        post(relativeUrl, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.i("response token",response.toString());
                try {
                    //if successful, our token is in the JSON response variable.
                    token = response.getString("access_token").toString();
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("token",token);
                    edit.commit();
                    Log.i("Access_token",pref.getString("token","N/A"));
                }catch (JSONException j)
                {
                    j.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("statusCode", "" + statusCode);
                Log.i("JSON error", errorResponse.toString());
            }
        });

    }

}
