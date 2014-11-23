package com.kevinschildhorn.gamecompletionist.HTTP;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;
import com.kevinschildhorn.gamecompletionist.DataClasses.Platform;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

/**
 * SCM Products Inc.
 * Created by Kevin Schildhorn on Nov 03, 2014.
 *
 * HTTPRequestHandler is used to request information over an HTTP protocol
 * It is called from a platform, and uses an AsyncTask to retrieve data from the protocol API
 * When finished it calls HTTPReplyHandler functions
 */

public class HTTPRequestHandler{


    public HTTPRequestHandler (){
    }

    // Requests

    public JSONObject requestGameList(Platform platform) throws JSONException, ExecutionException, InterruptedException {
        String requestURI = "";

        switch (platform.getTypeID()){
            case 1: //Steam
                requestURI = String.format("http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=%s&steamid=%s&include_appinfo=1&include_played_free_games=1&format=json",platform.getAPIkey(),platform.getLogin());
                break;
        }
        //if(isConnected()) {
        JSONObject gameListJSON = sendRequest(requestURI);
        //JSONObject gameListJSON = new HttpAsyncTask().execute(requestURI).get();
        return gameListJSON.getJSONObject("response");
        //}
    }
    public String requestSteamID(Platform platform) throws JSONException, ExecutionException, InterruptedException {

        String requestURI = String.format("http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=%s&vanityurl=%s",platform.getAPIkey(),platform.getLogin());
        //if(isConnected()) {
        JSONObject steamIDJSON = sendRequest(requestURI);
        //JSONObject steamIDJSON = new HttpAsyncTask().execute(requestURI).get();
        steamIDJSON = steamIDJSON.getJSONObject("response");
        String steamID = steamIDJSON.getString("steamid");
        return steamID;
        //}
    }

    public void requestGameAchievements(Platform platform,int index){
        String requestURI = String.format(  "http://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v0001/?appid=%s&key=%s&steamid=%s",
                platform.getGameAtIndex(index).getID(),
                platform.getAPIkey(),
                platform.getLogin());
        //if(isConnected()) {
        JSONObject steamIDJSON = sendRequest(requestURI);
        if(steamIDJSON != null) {
            try {
                if (steamIDJSON.has("playerstats")) {
                    steamIDJSON = steamIDJSON.getJSONObject("playerstats");
                    if (steamIDJSON.has("achievements")) {
                        JSONArray achievements = steamIDJSON.getJSONArray("achievements");
                        if (achievements != null) {
                            platform.getGameAtIndex(index).setAchievements(achievements);

                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void requestGameLogo(Platform platform,int index){
        if(platform.getGameAtIndex(index).getLogoURL()!= null ) {
            Bitmap logoBitmap = null;
            try {
                InputStream in = new java.net.URL(platform.getGameAtIndex(index).getLogoURL()).openStream();
                logoBitmap = BitmapFactory.decodeStream(in);
                in.close();
            } catch (Exception e) {
                //Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            platform.getGameAtIndex(index).setLogo(logoBitmap);
        }
    }



    // Processing

    private class HttpAsyncTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {
            JSONObject jsonTemp = sendRequest(urls[0]);

            // if the request isn't null, determine the type of incoming message
            if(jsonTemp != null) {
                return jsonTemp;
            }
            return null;
        }
    }

    public boolean isConnected(Context cont){
        ConnectivityManager connMgr = (ConnectivityManager) cont.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
    // Sends HTTP Request to requestURI
    JSONObject sendRequest(String requestURI){
        InputStream inputStream = null;
        String result = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(requestURI));
            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null){
                // parse text into JSON
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                result = sb.toString();
                return new JSONObject(sb.toString());

            }
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
            return null;
        }
        return null;
    }
}


