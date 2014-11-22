package com.kevinschildhorn.gamecompletionist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;
import com.kevinschildhorn.gamecompletionist.DataClasses.Platform;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * SCM Products Inc.
 * Created by Kevin Schildhorn on Nov 03, 2014.
 */
public class SQLiteHelper extends SQLiteOpenHelper{

    private static SQLiteHelper sInstance;

    private static final String DATABASE_NAME = "games.db";
    private static final int DATABASE_VERSION = 1;


    public static final String TABLE_PLATFORM = "Platforms";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "Name";
    public static final String KEY_LOGIN = "Login";
    public static final String KEY_TYPEID = "TypeID";
    public static final String KEY_APIKEY = "APIkey";

    public static final String TABLE_GAMES= "Games";
    public static final String KEY_PLATFORMID = "PlatformID";
    public static final String KEY_LOGOURL = "LogoUrl";
    public static final String KEY_LOGO = "Logo";
    public static final String KEY_MINUTESPLAYED = "minutesPlayed";
    public static final String KEY_RECENTMINUTESPLAYED = "recentMinutesPlayed";
    public static final String KEY_ACHIEVEMENTSFINISHEDCOUNT = "AchievementsFinishedCount";
    public static final String KEY_ACHIEVEMENTTOTALCOUNT = "AchievementTotalCount";
    public static final String KEY_COMPLETIONSTATUS = "CompletionStatus";
    public static final String KEY_CUSTOMSORTINDEX = "CustomSortIndex";

    public static SQLiteHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new SQLiteHelper(context.getApplicationContext());
        }
        return sInstance;
    }


    private SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PLATFORM_TABLE = "CREATE TABLE " + TABLE_PLATFORM + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," +
                KEY_NAME + " TEXT," +
                KEY_LOGIN + " TEXT," +
                KEY_TYPEID + " INT," +
                KEY_APIKEY + " TEXT" +
                ")";

        String CREATE_GAMES_TABLE = "CREATE TABLE " + TABLE_GAMES + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," +
                KEY_NAME + " TEXT," +
                KEY_PLATFORMID + " INT," +
                KEY_LOGOURL + " TEXT," +
                KEY_LOGO + " BLOB," +
                KEY_MINUTESPLAYED + " INT," +
                KEY_RECENTMINUTESPLAYED + " INT," +
                KEY_ACHIEVEMENTSFINISHEDCOUNT + " INT," +
                KEY_ACHIEVEMENTTOTALCOUNT + " INT," +
                KEY_COMPLETIONSTATUS + " INT," +
                KEY_CUSTOMSORTINDEX + " INT" +
                ")";

        db.execSQL(CREATE_PLATFORM_TABLE);
        db.execSQL(CREATE_GAMES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLATFORM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES);
        onCreate(db);
    }


    // Platform

    public void addPlatform(Platform platform) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(KEY_ID, platform.getID());
        values.put(KEY_NAME,platform.getName());
        values.put(KEY_LOGIN, platform.getLogin());
        values.put(KEY_TYPEID, platform.getTypeID());
        values.put(KEY_APIKEY, platform.getAPIkey());

        db.insert(TABLE_PLATFORM,null,values);
    }
    public void removePlatform(Platform platform) {
        SQLiteDatabase db = this.getWritableDatabase();

        // delete games
        db.delete(TABLE_GAMES,
                KEY_PLATFORMID + " =?",
                new String[]{platform.getID() + ""});

        // delete platform
        db.delete(TABLE_PLATFORM,
                KEY_ID + " =?",
                new String[]{platform.getID() + ""});
    }
    public int getPlatformCount(){
        String countQuery = "SELECT  * FROM " + TABLE_PLATFORM;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }
    Cursor getPlatformCursor(int ID){
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                KEY_ID,
                KEY_NAME,
                KEY_LOGIN,
                KEY_TYPEID,
                KEY_APIKEY
        };
        String whereColumn = null;
        if(ID > 0){
            whereColumn = KEY_ID + "=" + ID;
        }
        Cursor curTemp = db.query(
                TABLE_PLATFORM,  // The table to query
                projection,                                 // The columns to return
                whereColumn,                                // The columns for the WHERE clause
                null,                                       // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                null                                         // The sort Type
        );
        return curTemp;
    }
    public ArrayList getPlatforms(){
        Cursor curTemp = getPlatformCursor(-1);


        Platform platformTemp;
        ArrayList platformArray = new ArrayList();
        for (int i = 0; i < curTemp.getCount(); i++) {
            curTemp.moveToFirst();

            ArrayList<Game> gamesTemp = getGames(curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_ID)),-1,1,true);

            platformTemp = new Platform(curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_ID)),
                    curTemp.getString(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_NAME)),
                    curTemp.getString(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_LOGIN)),
                    curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_TYPEID)),
                    curTemp.getString(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_APIKEY)),
                    gamesTemp);

            platformArray.add(platformTemp);

            curTemp.moveToNext();
        }
        return  platformArray;
    }
    public Platform getPlatform(int ID,int sortType,int filterType,boolean sortAscending){
        Cursor curTemp = getPlatformCursor(ID);

        Platform platformTemp;

        if(curTemp.getCount() > 0) {
            curTemp.moveToFirst();
            ArrayList<Game> gamesTemp = getGames(curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_ID)),filterType,sortType,sortAscending);

            platformTemp = new Platform(curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_ID)),
                    curTemp.getString(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_NAME)),
                    curTemp.getString(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_LOGIN)),
                    curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_TYPEID)),
                    curTemp.getString(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_APIKEY)),
                    gamesTemp);

            return platformTemp;
        }
        return null;
    }
    public void setPlatform(Platform platform){
        SQLiteDatabase db = this.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, platform.getName());

        // Which row to update, based on the ID
        String selection = KEY_ID + " LIKE ?";
        String[] selectionArgs = { platform.getID() + "" };

        int count = db.update(
                TABLE_PLATFORM,
                values,
                selection,
                selectionArgs);
    }


    // Games

    public long addGame(Game game) {

        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        //values.put(KEY_ID, game.id);
        values.put(KEY_NAME, game.getName());
        values.put(KEY_PLATFORMID, game.getPlatformID());
        values.put(KEY_LOGOURL, game.getLogoURL());
        values.put(KEY_LOGO, game.getLogoInBytes());
        values.put(KEY_MINUTESPLAYED, game.getMinutesPlayed());
        values.put(KEY_RECENTMINUTESPLAYED, game.getRecentMinutesPlayed());
        values.put(KEY_ACHIEVEMENTSFINISHEDCOUNT, game.getAchievementsFinishedCount());
        values.put(KEY_ACHIEVEMENTTOTALCOUNT, game.getAchievementsTotalCount());
        values.put(KEY_COMPLETIONSTATUS, game.completionStatus);
        values.put(KEY_CUSTOMSORTINDEX, game.customSortTypeIndex);

        return db.insert(TABLE_GAMES,null,values);
    }
    public ArrayList getGames(int platformID,int filterType,int sortType,boolean sortAscending){
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                KEY_ID,
                KEY_NAME,
                KEY_PLATFORMID,
                KEY_LOGOURL,
                KEY_LOGO,
                KEY_MINUTESPLAYED,
                KEY_RECENTMINUTESPLAYED,
                KEY_ACHIEVEMENTSFINISHEDCOUNT,
                KEY_ACHIEVEMENTTOTALCOUNT,
                KEY_COMPLETIONSTATUS,
                KEY_CUSTOMSORTINDEX
        };

        String selection = "";
        if(platformID != -1) {
            selection = KEY_PLATFORMID + "=" + platformID;
        }
        if(filterType != -1) {
            if(selection != ""){
                selection = selection + " AND ";
            }
            selection = selection + KEY_COMPLETIONSTATUS + "=" + (filterType - 1);
        }
        String sortTypeText;
        switch (sortType){
            case 0:
            default:
                sortTypeText = KEY_NAME;
                break;
            case 1:
                sortTypeText = KEY_MINUTESPLAYED;
                break;
            case 2:
                sortTypeText = KEY_RECENTMINUTESPLAYED;
                break;
            case 3:
                sortTypeText = KEY_ACHIEVEMENTSFINISHEDCOUNT;
                break;
            case 4:
                sortTypeText = KEY_NAME;
                break;
        }
        if(sortAscending){
            sortTypeText +=" ASC";
        }
        else{
            sortTypeText +=" DESC";
        }


        Cursor curTemp = db.query(
                TABLE_GAMES,  // The table to query
                columns,                                 // The columns to return
                selection,                               // The columns for the WHERE clause
                null,                           // The values for the WHERE clause
                null,                                    // don't group the rows
                null,                                    // don't filter by row groups
                sortTypeText                                // The sort order
        );


        Game gameTemp;
        ArrayList gameArray = new ArrayList();
        curTemp.moveToFirst();
        for (int i = 0; i < curTemp.getCount(); i++) {


            gameTemp = new Game(curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_ID)),
                                curTemp.getString(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_NAME)),
                                curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_PLATFORMID)),
                                curTemp.getString(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_LOGOURL)),
                                curTemp.getBlob(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_LOGO)),
                                curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_MINUTESPLAYED)),
                                curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_RECENTMINUTESPLAYED)),
                                curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_ACHIEVEMENTSFINISHEDCOUNT)),
                                curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_ACHIEVEMENTTOTALCOUNT)),
                                curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_COMPLETIONSTATUS)),
                                curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_CUSTOMSORTINDEX)));

            gameArray.add(gameTemp);

            curTemp.moveToNext();
        }
        return gameArray;
    }
    public void setGame(Game game){
        SQLiteDatabase db = this.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        //values.put(KEY_ID, game.id);
        values.put(KEY_NAME, "'" + game.getName() + "'");
        values.put(KEY_PLATFORMID, game.getPlatformID());
        values.put(KEY_LOGOURL, "'" + game.getLogoURL() + "'");
        values.put(KEY_LOGO, game.getLogoInBytes());
        values.put(KEY_MINUTESPLAYED, game.getMinutesPlayed());
        values.put(KEY_RECENTMINUTESPLAYED, game.getRecentMinutesPlayed());
        values.put(KEY_ACHIEVEMENTSFINISHEDCOUNT, game.getAchievementsFinishedCount());
        values.put(KEY_ACHIEVEMENTTOTALCOUNT, game.getAchievementsTotalCount());
        values.put(KEY_COMPLETIONSTATUS, game.completionStatus);
        values.put(KEY_CUSTOMSORTINDEX, game.customSortTypeIndex);

        // Which row to update, based on the ID
        String selection = KEY_ID + " LIKE ?";
        String[] selectionArgs = { game.getID() + "" };

        int count = db.update(
                TABLE_GAMES,
                values,
                selection,
                selectionArgs);
    }
    public void setGameAchievements(Game game){
        SQLiteDatabase db = this.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(KEY_ACHIEVEMENTSFINISHEDCOUNT, game.getAchievementsFinishedCount());
        values.put(KEY_ACHIEVEMENTTOTALCOUNT, game.getAchievementsTotalCount());

        // Which row to update, based on the ID
        String selection = KEY_NAME + "=" + '"' + game.getName() + '"' + " AND " + KEY_PLATFORMID + "=" + game.getPlatformID();

        int count = db.update(
                TABLE_GAMES,
                values,
                selection,
                null);

        if(count == 0){
            Log.e("","ERROR SAVING ACHIEVEMENTS");
        }
    }
    public void setGameLogo(Game game){
        SQLiteDatabase db = this.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(KEY_LOGO, game.getLogoInBytes());

        // Which row to update, based on the ID
        String selection = KEY_NAME + "=" + '"' + game.getName() + '"' + " AND " + KEY_PLATFORMID + "=" + game.getPlatformID();

        //TODO
        int count = db.update(
                TABLE_GAMES,
                values,
                selection,
                null);

        if(count == 0){
            Log.e("","ERROR SAVING LOGO");
        }
    }
    public int getGameCount(String platformName, int sortType){
        SQLiteDatabase db;
        String countQuery;
        Cursor cursor;
        int id = 0;
        if(!platformName.equals("All")) {
            countQuery = "SELECT " + KEY_ID + " FROM " + TABLE_PLATFORM + " WHERE " + KEY_NAME + " = '" + platformName + "'";
            db = this.getReadableDatabase();
            cursor = db.rawQuery(countQuery, null);
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.KEY_ID));
            }
        }

        countQuery = "SELECT  * FROM " + TABLE_GAMES;

        if(id != 0){
            countQuery += " WHERE " + KEY_PLATFORMID + " =" + id;
            if(sortType != 0){
                countQuery += " AND " + KEY_COMPLETIONSTATUS + " =" + sortType;
            }
        }
        else if(sortType != 0){
            countQuery += " WHERE " + KEY_COMPLETIONSTATUS + " =" + sortType;
        }
        db = this.getReadableDatabase();
        cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }
    public byte[] getGameLogo(Game game){
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                KEY_LOGO
        };

        String selection = KEY_NAME + "=" + '"' + game.getName() + '"' + " AND " + KEY_PLATFORMID + "=" + '"' + game.getPlatformID() + '"';

        Cursor curTemp = db.query(
                TABLE_GAMES,  // The table to query
                columns,                                 // The columns to return
                selection,                               // The columns for the WHERE clause
                null,                           // The values for the WHERE clause
                null,                                    // don't group the rows
                null,                                    // don't filter by row groups
                null                                // The sort order
        );


        Game gameTemp;
        ArrayList gameArray = new ArrayList();
        curTemp.moveToFirst();
        byte[] logo = curTemp.getBlob(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_LOGO));

        return logo;
    }
}
