package com.kevinschildhorn.gamecompletionist.DataClasses;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.kevinschildhorn.gamecompletionist.HTTP.HTTPReplyHandler;
import com.kevinschildhorn.gamecompletionist.HTTP.HTTPRequestHandler;
import com.kevinschildhorn.gamecompletionist.R;
import com.kevinschildhorn.gamecompletionist.SQLiteHelper;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * SCM Products Inc.
 * Created by Kevin Schildhorn on Nov 03, 2014.
 *
 * Platform contains information for users platform: such as Steam, Origin, GoG etc
 * It is either created initially using a typeID and login name or pulled from the database
 * Platform also contains an array of the users games in that platform
 */



public class Platform implements HTTPReplyHandler{
    private int id;
    private String name;
    private String login;
    private int typeID;
    private String APIkey;
    private Game[] games;

    //private Context context;
    private Context context;
    private HTTPRequestHandler requestHandler;
    private PlatformCallbacks mCallback;

    // New Platform
    public Platform (PlatformCallbacks callbacks,Context context, int incomingID, int incomingType,String incomingLogin){
        mCallback = callbacks;
        this.context = context;
        // initialize
        this.id = incomingID;
        this.login = incomingLogin;
        this.typeID = incomingType;


        if(incomingID == -1){
            SQLiteHelper db = new SQLiteHelper(context);
            this.id = db.getPlatformCount()+1;
        }
        // initialize based on platform type
        switch (this.typeID){
            case 1:
                this.name = getUniqueName("Steam");
                this.APIkey = "B6D54D6EBCF3A1A320644C485ACD1A6F";

                // fetch login ID
                requestHandler = new HTTPRequestHandler(this);
                requestHandler.requestSteamID(this);
                break;
        }
    }

    // New Platform From Database
    public Platform (int incomingID, String incomingName, String incomingLogin, int incomingType,String incomingAPIkey,ArrayList<Game> games){
        // initialize
        this.id = incomingID;
        this.name = incomingName;
        this.login = incomingLogin;
        this.typeID = incomingType;
        this.APIkey = incomingAPIkey;

        Game[] gameArray = games.toArray(new Game[games.size()]);
        this.games = gameArray;
    }

    // Checks if name already exists in database and if so returns a custom one
    private String getUniqueName(String name){
        int nameCount = 0;
        SQLiteHelper db = new SQLiteHelper(context);
        ArrayList<String> existingNames = db.getPlatformNames();

        for(int i=0;i<existingNames.size();i++){
            if(existingNames.get(i).startsWith(name)){
                nameCount++;
            }
        }
        if(nameCount == 0){
            return name;
        }
        else{
            return name + " " + nameCount+1;
        }
    }
    // Getters


    public Game[] getGames () {
        return this.games;
    }
    public Game getGameAtIndex(int index){
        return this.games[index];
    }
    public String getName () {
        return this.name;
    }
    public int getID(){
        return this.id;
    }
    public String getLogin(){
        return  this.login;
    }
    public int getTypeID(){
        return  this.typeID;
    }
    public String getAPIkey(){
        return  this.APIkey;
    }

    public void setName (String incomingName) {
        this.name = incomingName;
    }
    // Modify Games

    public Platform sortPlatformGames(int sortType,Context cont){
        SQLiteHelper db = new SQLiteHelper(cont);
        ArrayList<Game> gameList = db.getGames(this.id,sortType,1,true);
        this.games = gameList.toArray(new Game[gameList.size()]);
        return this;
    }
    public Platform filterPlatformGames(int completionType,Context cont){
        SQLiteHelper db = new SQLiteHelper(cont);
        ArrayList<Game> gameList = db.getGames(this.id,completionType,1,true);
        this.games = gameList.toArray(new Game[gameList.size()]);
        return this;
    }


    // HTTPReplyHandler Implementation

    public void incomingSteamID(String steamID){
        // we just got the steamID so now get all the games
        this.login = steamID;
        requestGameList();
    }
    public void  requestGameList(){
        requestHandler.requestGameList(this);
    }
    public void incomingSteamGameList(JSONObject gameList) throws JSONException {

        int currentGameCount = this.games.length;
        int gameCount = gameList.getInt("game_count");

        if(currentGameCount != gameCount) {
            Game[] oldGames = this.games;
            ArrayList<Game> updatedGames = new ArrayList<Game>();

            this.games = new Game[gameCount];

            // Get game information
            JSONArray gameInfoArray = gameList.getJSONArray("games");
            JSONObject gameInfoTemp;

            Game gameTemp;
            int recent;
            SQLiteHelper db = new SQLiteHelper(context);

            // Pull data from JSON
            for (int i = 0; i < gameCount; i++) {
                gameInfoTemp = gameInfoArray.getJSONObject(i);
                recent = -1;
                try {
                    recent = gameInfoTemp.getInt("playtime_2weeks");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                gameTemp = new Game(gameInfoTemp.getInt("appid"),               // ID
                        gameInfoTemp.getString("name"),             // Name
                        this.id,                                    // platformID
                        gameInfoTemp.getString("img_logo_url"),     // LogoURL
                        gameInfoTemp.getInt("playtime_forever"),    // HoursPlayed
                        recent,                                     // lastTimePlayed
                        -1,                                         // AchievementCount
                        0,                                          // CompletionStatus
                        -1);                                        // CustomSortIndex

                for (int j=0;j<oldGames.length;j++){
                    if(gameTemp.getName() != oldGames[j].getName()){
                        updatedGames.add(gameTemp);
                    }
                }
                this.games[i] = gameTemp;
                if (db.addGame(gameTemp) == -1) {
                    Toast.makeText(context, "Inserting Game Failed", Toast.LENGTH_SHORT);
                }
            }

            db.addPlatform(this);

            SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor edit = m_settingsSP.edit();
            edit.putInt(SQLiteHelper.KEY_PLATFORMID, this.id);
            edit.commit();

            // Send broadcast if this is a new platform
            if(currentGameCount == 0) {
                mCallback.onNewIncomingPlatform(this);
            }

            // Send Broadcast if this is an updated platform
            if(currentGameCount != 0){
                mCallback.onUpdatedIncomingPlatform(this,updatedGames);
            }
        }
    }
    public static interface PlatformCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNewIncomingPlatform(Platform platform);
        void onUpdatedIncomingPlatform(Platform platform,ArrayList<Game> games);
    }
}

