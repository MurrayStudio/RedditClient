package com.murraystudio.redditclient;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class shall serve as a utility class that handles network
 * connections.
 *
 */

public class RemoteData extends AsyncTask<String, Void, String> {
    /**
     * This methods returns a Connection to the specified URL,
     * with necessary properties like timeout and user-agent
     * set to your requirements.
     *
     * @param url
     * @return
     */

    MainActivity mainActivity;

    public RemoteData(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

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


/*    *//**
     * A very handy utility method that reads the contents of a URL
     * and returns them as a String.
     *
     * @param url
     * @return
     *//*
    public static String readContents(String url){
        HttpURLConnection hcon=getConnection(url);
        if(hcon==null) return null;
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
    }*/

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
        mainActivity.fetchPostExecute(rawData);
    }
}
