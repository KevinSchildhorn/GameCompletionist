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
    public int customOrderIndex;


    public Game (int id, String name, int platformID,String logoURL, int minutesPlayed, int recentMinutesPlayed,int achievementCount,int completionStatus,int customOrderIndex){
        // initialize
        this.id = id;
        this.name = name;
        this.platformID = platformID;
        if(logoURL.startsWith("http://")){
            this.logoURL = logoURL;
        }
        else {
            this.logoURL = "http://media.steampowered.com/steamcommunity/public/images/apps/" + id + "/" + logoURL + ".jpg";
        }
        this.minutesPlayed = minutesPlayed;
        this.recentMinutesPlayed = recentMinutesPlayed;
        this.achievementCount = achievementCount;
        this.completionStatus = completionStatus;
        this.customOrderIndex = customOrderIndex;
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
    public int getHoursPlayed(){
        return this.minutesPlayed;
    }
    public int getRecentMinutesPlayed(){
        return this.recentMinutesPlayed;
    }
    public int getAchievementCount(){
        return this.achievementCount;
    }

}
