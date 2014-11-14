package com.kevinschildhorn.gamecompletionist.DataClasses;

import java.util.Date;

/**
 * SCM Products Inc.
 * Created by Kevin Schildhorn on Nov 03, 2014.
 *
 * Game contains information of video games based on a certain platform, which can be found using the platformID
 */

public class Game {
    private int id;                     // DONE - requestGameInfo
    private String name;                // DONE - requestGameInfo
    private int platformID;             // DONE - parent
    private String logoURL;             // DONE - requestGameInfo
    private int minutesPlayed;        // DONE - requestGameInfo
    private int recentMinutesPlayed;  // DONE - requestGameInfo/requestRecentGames
    private int achievementCount;       // DONE - requestAchievements

    public int completionStatus;
    public int customSortTypeIndex;


    public Game (int id, String name, int platformID,String logoURL, int minutesPlayed, int recentMinutesPlayed,int achievementCount,int completionStatus,int customSortTypeIndex){
        // initialize
        this.id = id;
        this.name = name;
        if(this.name.startsWith("The ")){
            this.name = this.name.substring(4) + ", The";
        }
        this.platformID = platformID;
        if(logoURL.startsWith("http://")){
            this.logoURL = logoURL;
        }
        else {
            this.logoURL = "http://media.steampowered.com/steamcommunity/public/images/apps/" + id + "/" + logoURL + ".jpg";
        }
        this.minutesPlayed = minutesPlayed;
        this.recentMinutesPlayed = recentMinutesPlayed;
        if(this.recentMinutesPlayed == -1) {
            this.recentMinutesPlayed = 0;
        }
        this.achievementCount = achievementCount;
        if(this.achievementCount == -1) {
            this.achievementCount = 0;
        }
        this.completionStatus = completionStatus;
        this.customSortTypeIndex = customSortTypeIndex;
    }


    // Getters

    public int getID(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public int getPlatformID(){
        return this.platformID;
    }
    public String getLogoURL(){
        return this.logoURL;
    }
    public int getMinutesPlayed(){
        return this.minutesPlayed;
    }
    public int getRecentMinutesPlayed(){
        return this.recentMinutesPlayed;
    }
    public int getAchievementCount(){
        return this.achievementCount;
    }

}
