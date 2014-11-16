package com.kevinschildhorn.gamecompletionist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;
import com.kevinschildhorn.gamecompletionist.DataClasses.Platform;
import com.kevinschildhorn.gamecompletionist.HTTP.HTTPRequestHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by kevin on 11/14/2014.
 */
public class PlatformHandler{

    private Context mContext;
    private PlatformGeneratorCallbacks mCallback;
    private HTTPRequestHandler mRequestHandler;
    SQLiteHelper db;

    int mPlatformCounter;

    public PlatformHandler(Context context,PlatformGeneratorCallbacks callbacks,SQLiteHelper db){
        this.mContext = context;
        this.mCallback = callbacks;
        this.mRequestHandler = new HTTPRequestHandler();
        this.db = db;
        mPlatformCounter = db.getPlatformCount()+1;
    }

    // Requests Platform from server, then saves it to database
    public void RequestNewPlatformFromServer(int platformType,String loginName){
        Platform platform = new Platform(mPlatformCounter,platformType,loginName);
        new NewPlatformAsyncTask().execute(platform);
    }

    public void RequestUpdatedGameListFromServer(Platform platform){
        new UpdatedPlatformAsyncTask().execute(platform);
    }

    private class NewPlatformAsyncTask extends AsyncTask<Platform, Void, Platform> {
        @Override
        protected Platform doInBackground(Platform... platforms) {
            Platform platform = platforms[0];
            platform.addInformationFromServer(mRequestHandler,db);

            SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor edit = m_settingsSP.edit();
            edit.putInt(SQLiteHelper.KEY_PLATFORMID, platform.getID());
            edit.commit();

            return platform;
        }
        protected void onPostExecute(Platform result) {
            mCallback.onNewIncomingPlatform(result);
            Toast.makeText(mContext,"Games Loaded, now loading Achievements",Toast.LENGTH_LONG).show();
            new getAchievementsAsyncTask().execute(result);
        }
    }
    private class UpdatedPlatformAsyncTask extends AsyncTask<Platform, Void, ArrayList<Game>> {
        @Override
        protected ArrayList<Game> doInBackground(Platform... platforms) {
            Platform platform = platforms[0];
            return platform.updateGamesList(mRequestHandler,db);
        }

        protected void onPostExecute(ArrayList<Game> result) {
            mCallback.onUpdatedIncomingPlatform(result);
        }
    }

    private class getAchievementsAsyncTask extends AsyncTask<Platform, Void, Platform> {
        @Override
        protected Platform doInBackground(Platform... platforms) {
            Platform platform = platforms[0];
            for(int i=0;i<platform.getGames().length;i++) {
                platform.updateGameAchievementAtIndex(i, mRequestHandler, db);

                Intent intent = new Intent();
                intent.setAction("test");
                intent.putExtra("finished",i);
                intent.putExtra("total",platform.getGames().length);
                mContext.sendBroadcast(intent);
                //mCallback.onOnUpdatedAchievements(i, platform.getGames().length);
            }
            return platform;
        }

        protected void onPostExecute(Platform result) {
            mCallback.onNewIncomingPlatform(result);
            Toast.makeText(mContext,"Achievements Loaded",Toast.LENGTH_LONG).show();
        }
    }

    public static interface PlatformGeneratorCallbacks {
        void onNewIncomingPlatform(Platform platform);
        void onUpdatedIncomingPlatform(ArrayList<Game> games);
        void onOnUpdatedAchievements(int finishedCount,int totalCount);
    }
}
