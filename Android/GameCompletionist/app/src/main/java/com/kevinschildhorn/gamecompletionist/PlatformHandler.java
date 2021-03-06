package com.kevinschildhorn.gamecompletionist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;
import com.kevinschildhorn.gamecompletionist.DataClasses.Platform;
import com.kevinschildhorn.gamecompletionist.HTTP.HTTPRequestHandler;

import java.util.ArrayList;

/**
 * Created by kevin on 11/14/2014.
 */
public class PlatformHandler{

    private Context mContext;
    private PlatformGeneratorCallbacks mCallback;
    private HTTPRequestHandler mRequestHandler;

    int mPlatformCounter;

    public PlatformHandler(Context context,PlatformGeneratorCallbacks callbacks){
        this.mContext = context;
        this.mCallback = callbacks;
        this.mRequestHandler = new HTTPRequestHandler();
        SQLiteHelper db = SQLiteHelper.getInstance(mContext);
        mPlatformCounter = db.getPlatformCount()+1;;
    }

    // Requests Platform from server, then saves it to database
    public void RequestNewPlatformFromServer(int platformType,String loginName){
        Platform platform = new Platform(mPlatformCounter,platformType,loginName,"");
        new NewPlatformAsyncTask().execute(platform);
    }

    public void RequestNewPlatformFromServer(int platformType,String loginName, String loginPassword){
        Platform platform = new Platform(mPlatformCounter,platformType,loginName,loginPassword);
        new NewPlatformAsyncTask().execute(platform);
    }

    public void RequestUpdatedGameListFromServer(Platform platform){
        new UpdatedPlatformAsyncTask().execute(platform);
    }

    private class NewPlatformAsyncTask extends AsyncTask<Platform, Void, Platform> {
        @Override
        protected Platform doInBackground(Platform... platforms) {
            Platform platform = platforms[0];
            SQLiteHelper db = SQLiteHelper.getInstance(mContext);
            platform.addInformationFromServer(mRequestHandler,db);

            SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor edit = m_settingsSP.edit();
            edit.putInt(SQLiteHelper.KEY_PLATFORMID, platform.getID());
            edit.commit();

            return platform;
        }
        protected void onPostExecute(Platform result) {
            mCallback.onNewIncomingPlatform(result);
            Toast.makeText(mContext,"Games Loaded, now loading Logos",Toast.LENGTH_LONG).show();
            new getGameLogoAsyncTask().execute(result);
        }
    }


    private class UpdatedPlatformAsyncTask extends AsyncTask<Platform, Void, ArrayList<Game>> {
        @Override
        protected ArrayList<Game> doInBackground(Platform... platforms) {
            Platform platform = platforms[0];
            SQLiteHelper db = SQLiteHelper.getInstance(mContext);
            return platform.updateGamesList(mRequestHandler,db);
        }

        protected void onPostExecute(ArrayList<Game> result) {
            mCallback.onUpdatedIncomingPlatform(result);
        }
    }


    private class getGameLogoAsyncTask extends AsyncTask<Platform, Void, Platform> {

        @Override
        protected Platform doInBackground(Platform... platforms) {
            Platform platform = platforms[0];

            for(int i=0;i<platform.getGames().length;i++){
                SQLiteHelper db = SQLiteHelper.getInstance(mContext);
                platform.updateGameLogoAtIndex(i, mRequestHandler, db);

                Intent intent = new Intent();
                intent.setAction("updateDownloadInfo");
                intent.putExtra("type",1);
                intent.putExtra("finished",i);
                intent.putExtra("total",platform.getGames().length);
                mContext.sendBroadcast(intent);
            }
            return platform;
        }

        protected void onPostExecute(Platform result) {
            mCallback.onNewIncomingPlatform(result);
            Toast.makeText(mContext,"Logos Loaded, now loading Achievements",Toast.LENGTH_LONG).show();
            new getAchievementsAsyncTask().execute(result);
        }
    }
    private class getAchievementsAsyncTask extends AsyncTask<Platform, Void, Platform> {
        @Override
        protected Platform doInBackground(Platform... platforms) {
            Platform platform = platforms[0];
            for(int i=0;i<platform.getGames().length;i++) {
                SQLiteHelper db = SQLiteHelper.getInstance(mContext);
                platform.updateGameAchievementAtIndex(i, mRequestHandler, db);

                Intent intent = new Intent();
                intent.setAction("updateDownloadInfo");
                intent.putExtra("type",2);
                intent.putExtra("finished",i);
                intent.putExtra("total",platform.getGames().length);
                mContext.sendBroadcast(intent);
            }
            return platform;
        }

        protected void onPostExecute(Platform result) {
            mCallback.onNewIncomingPlatform(result);
            Toast.makeText(mContext,"Achievements Loaded",Toast.LENGTH_LONG).show();
            new getControllerSupportAsynctask().execute(result);
        }
    }
    private class getControllerSupportAsynctask extends AsyncTask<Platform, Void, Platform> {
        @Override
        protected Platform doInBackground(Platform... platforms) {
            Platform platform = platforms[0];
            for(int i=0;i<platform.getGames().length;i++) {
                SQLiteHelper db = SQLiteHelper.getInstance(mContext);
                platform.updateGameControllerSupportAtIndex(i, mRequestHandler, db);

                Intent intent = new Intent();
                intent.setAction("updateDownloadInfo");
                intent.putExtra("type",2);
                intent.putExtra("finished",i);
                intent.putExtra("total",platform.getGames().length);
                mContext.sendBroadcast(intent);
            }
            return platform;
        }

        protected void onPostExecute(Platform result) {
            mCallback.onNewIncomingPlatform(result);
            Toast.makeText(mContext,"Controller Support Loaded",Toast.LENGTH_LONG).show();
        }
    }



    public static interface PlatformGeneratorCallbacks {
        void onNewIncomingPlatform(Platform platform);
        void onUpdatedIncomingPlatform(ArrayList<Game> games);
    }
}
