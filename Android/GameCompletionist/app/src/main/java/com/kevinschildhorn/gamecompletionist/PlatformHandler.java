package com.kevinschildhorn.gamecompletionist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

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

    public PlatformHandler(Context context,PlatformGeneratorCallbacks callbacks){
        this.mContext = context;
        this.mCallback = callbacks;
        this.mRequestHandler = new HTTPRequestHandler();

        db = new SQLiteHelper(context);
        mPlatformCounter = db.getPlatformCount()+1;
    }

    // Requests Platform from server, then saves it to database
    public void RequestNewPlatformFromServer(int platformType,String loginName){
        Platform platform = new Platform(mPlatformCounter,platformType,loginName);

        try {
            platform = new NewPlatformAsyncTask().execute(platform).get();

            SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor edit = m_settingsSP.edit();
            edit.putInt(SQLiteHelper.KEY_PLATFORMID, platform.getID());
            edit.commit();

            mCallback.onNewIncomingPlatform(platform);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void RequestUpdatedGameListFromServer(Platform platform){
        new NewPlatformAsyncTask().execute(platform);
    }

    private class NewPlatformAsyncTask extends AsyncTask<Platform, Void, Platform> {
        @Override
        protected Platform doInBackground(Platform... platforms) {
            Platform platform = platforms[0];
            platform.setUniqueName(db);
            platform.addInformationFromServer(mRequestHandler,db);

            return platform;

        }
    }
    private class UpdatedPlatformAsyncTask extends AsyncTask<Platform, Void, Void> {
        @Override
        protected Void doInBackground(Platform... platforms) {
            Platform platform = platforms[0];
            ArrayList<Game> newGamesList = platform.updateGamesList(mRequestHandler,db);
            mCallback.onUpdatedIncomingPlatform(platform,newGamesList);
            return null;
        }
    }

    public static interface PlatformGeneratorCallbacks {
        void onNewIncomingPlatform(Platform platform);
        void onUpdatedIncomingPlatform(Platform platform,ArrayList<Game> games);
    }
}
