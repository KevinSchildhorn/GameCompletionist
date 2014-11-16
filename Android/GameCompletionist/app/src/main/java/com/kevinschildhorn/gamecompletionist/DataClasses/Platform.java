package com.kevinschildhorn.gamecompletionist.DataClasses;

import android.content.Context;

import com.kevinschildhorn.gamecompletionist.HTTP.HTTPRequestHandler;
import com.kevinschildhorn.gamecompletionist.SQLiteHelper;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * SCM Products Inc.
 * Created by Kevin Schildhorn on Nov 03, 2014.
 *
 * Platform contains information for users platform: such as Steam, Origin, GoG etc
 * It is either created initially using a typeID and login name or pulled from the database
 * Platform also contains an array of the users games in that platform
 */

public class Platform {
    private int id;
    private String name;
    private String login;
    private int typeID;
    private String APIkey;
    private Game[] games;


    // New Platform
    public Platform ( int incomingID, int incomingType,String incomingLogin){
        // initialize
        this.id = incomingID;
        this.login = incomingLogin;
        this.typeID = incomingType;
        this.games = new Game[0];
        // initialize based on platform type
        switch (this.typeID) {
            case 1:
                this.name = "Steam";
                this.APIkey = "B6D54D6EBCF3A1A320644C485ACD1A6F";
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


    public void addInformationFromServer(HTTPRequestHandler requestHandler,SQLiteHelper db){
        try {
            this.name = this.name + "_" + this.login;
            this.login = requestHandler.requestSteamID(this);
            updateGamesList(requestHandler,db);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Game> updateGamesList(HTTPRequestHandler requestHandler,SQLiteHelper db){
        try {
            JSONObject gameListJSON = requestHandler.requestGameList(this);
            return parseAndAddGameList(gameListJSON,db);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ArrayList<Game>();
    }


    public void updateGameAchievementAtIndex(int index,HTTPRequestHandler requestHandler,SQLiteHelper db){
        requestHandler.requestGameAchievements(this,index);
        db.setGame(games[index]);
    }

    // Getters

    // Checks if name already exists in database and if so returns a custom one
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


    // Returns the new games
    public ArrayList<Game> parseAndAddGameList(JSONObject gameList,SQLiteHelper db) throws JSONException {
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

            // Pull data from JSON
            for (int i = 0; i < gameCount; i++) {
                gameInfoTemp = gameInfoArray.getJSONObject(i);
                recent = -1;

                try {
                    if(gameInfoTemp.has("playtime_2weeks")) {
                        recent = gameInfoTemp.getInt("playtime_2weeks");
                    }
                } catch (JSONException e) {
                    //e.printStackTrace();
                }

                gameTemp = new Game(gameInfoTemp.getInt("appid"),               // ID
                        gameInfoTemp.getString("name"),             // Name
                        this.id,                                    // platformID
                        gameInfoTemp.getString("img_logo_url"),     // LogoURL
                        gameInfoTemp.getInt("playtime_forever"),    // HoursPlayed
                        recent,                                     // lastTimePlayed
                        -1,                                         // AchievementsFinishedCount
                        -1,                                         // AchievementsTotalCount
                        0,                                          // CompletionStatus
                        -1);                                        // CustomSortIndex

                for (int j=0;j<oldGames.length;j++){
                    if(gameTemp.getName() != oldGames[j].getName()){
                        updatedGames.add(gameTemp);
                    }
                }
                this.games[i] = gameTemp;
                db.addGame(gameTemp);
            }

            db.addPlatform(this);

            return updatedGames;
        }
        return new ArrayList<Game>();
    }

}

