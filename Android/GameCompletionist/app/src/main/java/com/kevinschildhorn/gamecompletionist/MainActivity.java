package com.kevinschildhorn.gamecompletionist;

import android.app.Activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;
import com.kevinschildhorn.gamecompletionist.DataClasses.Platform;


import java.util.ArrayList;


public class MainActivity extends Activity  implements  NavigationDrawerFragment.NavigationDrawerCallbacks,
                                                        PlaceholderFragment.PlaceholderCallbacks,
                                                        PlatformHandler.PlatformGeneratorCallbacks{

    // Fragments
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private PlaceholderFragment mPlaceholderFragment;

    // last screen title
    private CharSequence mTitle;

    // Platform Generator
    PlatformHandler mPlatformHandler;

    SQLiteHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the drawer.
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        db = new SQLiteHelper(this);
        ArrayList platformArray = db.getPlatforms();
        db.close();
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                platformArray);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mPlatformHandler = new PlatformHandler(this,this,db);
        // Pull any new games from platform
        Platform platformTemp;
        for(int i=0;i<platformArray.size();i++){
            platformTemp = (Platform)platformArray.get(i);
            mPlatformHandler.RequestUpdatedGameListFromServer(platformTemp);
        }
    }

    @Override
    public SQLiteHelper getDatabase(){
        return db;
    }
    // Navigation Drawer Callbacks

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        if(mPlaceholderFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            mPlaceholderFragment = PlaceholderFragment.newInstance(position + 1);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mPlaceholderFragment)
                    .commit();
        }
        else {
            onSectionAttached(position + 1);
            mPlaceholderFragment.updatePlatform();
        }
    }

    /*private BroadcastReceiver newPlatformReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mPlaceholderFragment.updatePlatform();
        }
    };

    private BroadcastReceiver updatePlatformReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mNavigationDrawerFragment.OnUpdatePlatformReceived(getApplication());
            mPlaceholderFragment.updatePlatform();
            Toast.makeText(context,"New Games Found",Toast.LENGTH_LONG).show();
        }
    };*/

    @Override
    public void onSortTypeSelected(int sortType){
        // save the sort Type
        SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = m_settingsSP.edit();
        editor.putInt(getString(R.string.sort_type),sortType);
        editor.apply();

        // Alert the fragment to reset the platform
        mPlaceholderFragment.updatePlatform();
    }

    @Override
    public void onSortDirectionSelected(boolean sortAscending){
        // save the sort Type
        SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = m_settingsSP.edit();
        editor.putBoolean(getString(R.string.sort_Direction),sortAscending);
        editor.apply();

        // Alert the fragment to reset the platform
        mPlaceholderFragment.updatePlatform();
    }
    @Override
   public  void onPlatformDeleteSelected(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you Sure you want to delete this platform?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int platformIdx) {
                        mPlaceholderFragment.deleteCurrentPlatform();
                        mNavigationDrawerFragment.refreshDrawer();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int platformIdx) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onPlatformRenameSelected(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Your Platform");
        builder.setIcon(R.drawable.ic_launcher);

        // Set an EditText view to get user input
        final EditText input = new EditText(builder.getContext());
        final Platform platformTemp = mPlaceholderFragment.getCurrentPlatform();
        input.setText(platformTemp.getName());
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                platformTemp.setName(input.getText().toString());
                SQLiteHelper db = new SQLiteHelper(getApplication());
                db.setPlatform(platformTemp);
                db.close();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void requestNewPlatform(int platformType, String text) {
        mPlatformHandler.RequestNewPlatformFromServer(platformType,text);
        mPlaceholderFragment.setLoadingScreen(true);
    }


    // Action Bar

    public void onSectionAttached(int number) {
        number = number-1;
        mTitle =  mNavigationDrawerFragment.getListItemName(number);

        SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = m_settingsSP.edit();
        if (mTitle == getString(R.string.unfinished)){
            mTitle = mNavigationDrawerFragment.getListItemName(number-getResources().getInteger(R.integer.unfinished)) + " - " + mTitle;
            editor.putInt(getString(R.string.filter_type),getResources().getInteger(R.integer.unfinished));
        }
        else if (mTitle == getString(R.string.finished)){
            mTitle = mNavigationDrawerFragment.getListItemName(number-getResources().getInteger(R.integer.finished)) + " - " + mTitle;
            editor.putInt(getString(R.string.filter_type),getResources().getInteger(R.integer.finished));
        }
        else if (mTitle == getString(R.string.complete)){
            mTitle = mNavigationDrawerFragment.getListItemName(number-getResources().getInteger(R.integer.complete)) + " - " + mTitle;
            editor.putInt(getString(R.string.filter_type),getResources().getInteger(R.integer.complete));
        }
        editor.commit();

    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    // Options

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();

            // Associate searchable configuration with the SearchView
            SearchView temp = (SearchView) menu.findItem(R.id.search).getActionView();

            temp.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    //PlaceholderFragment.updateListView(currentPlatform,sortType,newText);
                    return false;
                }
            });


            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // Platform Handler Callbacks

    @Override
    public void onNewIncomingPlatform(Platform platform){
        mNavigationDrawerFragment.updatePlatforms(true);
        mPlaceholderFragment.updatePlatform();
    }


    @Override
    public void onUpdatedIncomingPlatform(ArrayList<Game> games){
        mNavigationDrawerFragment.updatePlatforms(false);
        mPlaceholderFragment.updatePlatform();

        if(games.size() > 0) {
            String gamesString = "";
            for (int i = 0; i < games.size(); i++) {
                gamesString += "\n  -" + games.get(i).getName();
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("New Games Found")
                    .setMessage("New Games were found for this platform" + gamesString)
                    .setNegativeButton("Ok", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }



    @Override
    public void onOnUpdatedAchievements(int finishedCount, int totalCount) {
        String loadingInfo = null;
        if(finishedCount != totalCount) {
            loadingInfo = finishedCount + "/" + totalCount + " achievements loaded";
        }
        mPlaceholderFragment.setLoadingInfo(loadingInfo);
    }
}
