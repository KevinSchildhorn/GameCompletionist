package com.kevinschildhorn.gamecompletionist.DataClasses;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * SCM Products Inc.
 * Created by Kevin Schildhorn on Nov 03, 2014.
 *
 * Game contains information of video games based on a certain platform, which can be found using the platformID
 */

public class Game {
    private int id;                     // DONE - requestGameInfo
    private String idString;
    private String name;                // DONE - requestGameInfo
    private int platformID;             // DONE - parent
    private String logoURL;             // DONE - requestGameInfo
    private Bitmap logo;
    private int minutesPlayed;        // DONE - requestGameInfo
    private int recentMinutesPlayed;  // DONE - requestGameInfo/requestRecentGames
    private int achievementsFinishedCount;       // DONE - requestAchievements
    private int achievementsTotalCount;       // DONE - requestAchievements
    private int controllerSupport;

    public int completionStatus;
    public int customSortTypeIndex;


    public Game (int id, String idString, String name, int platformID,String logoURL, byte[] logo,int minutesPlayed,
                 int recentMinutesPlayed,int achievementsFinishedCount,int achievementsTotalCount,
                 int completionStatus,int customSortTypeIndex,int controllerSupport){
        // initialize
        this.id = id;
        this.idString = idString;
        this.name = name;
        if(this.name.startsWith("The ")){
            this.name = this.name.substring(4) + ", The";
        }
        this.platformID = platformID;
        if(logoURL == null) {
            logoURL = "";
        }

        if (logoURL.startsWith("http://")) {
            this.logoURL = logoURL;
        } else if (!logoURL.isEmpty()) {
            this.logoURL = "http://media.steampowered.com/steamcommunity/public/images/apps/" + id + "/" + logoURL + ".jpg";
        }

        if(logo != null) {
            this.logo = BitmapFactory.decodeByteArray(logo, 0, logo.length);
        }
        this.minutesPlayed = minutesPlayed;
        this.recentMinutesPlayed = recentMinutesPlayed;
        if(this.recentMinutesPlayed == -1) {
            this.recentMinutesPlayed = 0;
        }
        this.achievementsFinishedCount = achievementsFinishedCount;
        if(this.achievementsFinishedCount == -1) {
            this.achievementsFinishedCount = 0;
        }
        this.achievementsTotalCount = achievementsTotalCount;
        if(this.achievementsTotalCount == -1) {
            this.achievementsTotalCount = 0;
        }
        this.completionStatus = completionStatus;
        this.customSortTypeIndex = customSortTypeIndex;
        this.controllerSupport = controllerSupport;
    }

    public Game(String id,String title,String logoURL){
        this.idString = id;
        this.name = title;
        this.logoURL = logoURL;
    }
    // Getters

    public int getID(){
        return this.id;
    }
    public String getIdString(){ return this.idString;}
    public String getName(){
        return this.name;
    }
    public int getPlatformID(){
        return this.platformID;
    }
    public String getLogoURL(){
        return this.logoURL;
    }
    public Bitmap getLogo(){return  this.logo;}
    public byte[] getLogoInBytes(){
        if(this.logo != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            this.logo.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }
        else{
            return new byte[0];
        }
    }
    public int getMinutesPlayed(){
        return this.minutesPlayed;
    }
    public int getRecentMinutesPlayed(){
        return this.recentMinutesPlayed;
    }
    public int getAchievementsFinishedCount(){
        return this.achievementsFinishedCount;
    }
    public int getAchievementsTotalCount(){
        return this.achievementsTotalCount;
    }
    public int getControllerSupport(){
        return this.controllerSupport;
    }

    public void setAchievements(JSONArray achievements) throws JSONException {
        this.achievementsFinishedCount = 0;
        this.achievementsTotalCount = 0;

        JSONObject achievement;
        for(int i=0;i<achievements.length();i++){
            achievement = (JSONObject) achievements.get(i);
            if(achievement.getInt("achieved") == 1){
                this.achievementsFinishedCount++;
            }

            this.achievementsTotalCount++;
        }

        //Log.e("", this.achievementsFinishedCount + "/" + this.achievementsTotalCount + " Achievements");
    }
    public void setLogo(Bitmap logo){
        this.logo = logo;
    }
    public void setControllerSupport(int controllerSupport){
        this.controllerSupport = controllerSupport;
    }
}
