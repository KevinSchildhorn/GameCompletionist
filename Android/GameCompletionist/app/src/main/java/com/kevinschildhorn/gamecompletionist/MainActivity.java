package com.kevinschildhorn.gamecompletionist;

import android.app.Activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.EditText;
import android.widget.SearchView;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;
import com.kevinschildhorn.gamecompletionist.DataClasses.Platform;


import java.util.ArrayList;


public class MainActivity extends Activity  implements  NavigationDrawerFragment.NavigationDrawerCallbacks, PlatformHandler.PlatformGeneratorCallbacks, ActionBar.TabListener{

    // Fragments
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private TabFragment mCurrentTabFragment;

    // last screen title
    private CharSequence mTitle = "";

    // Platform Generator
    PlatformHandler mPlatformHandler;

    public static final int NOPLATFORMS = 0;
    public static final int DOWNLOADINGPLATFORMS = 1;
    public static final int DOWNLOADINGACHIEVEMENTS = 2;
    public static final int DOWNLOADSFINISHED = 2;

    int platformState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SQLiteHelper db = SQLiteHelper.getInstance(this);
        mPlatformHandler = new PlatformHandler(this,this);

        // GUI

        // Set up the drawer.
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);


        ArrayList platformArray = db.getPlatforms();
        if(platformArray.size() <= 0){
            platformState = NOPLATFORMS;
        }
        else{
            platformState = DOWNLOADSFINISHED;
        }


        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                platformArray);

        // Tabs
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // for each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText(R.string.unfinished)
                .setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.finished)
                .setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.complete)
                .setTabListener(this));

        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,67,36,102)));
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.WHITE));
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.argb(255,67,36,102)));


        for(int i=0;i<platformArray.size();i++) {
            mPlatformHandler.RequestUpdatedGameListFromServer((Platform)platformArray.get(i));
        }

    }
    @Override
    protected void onDestroy(){
        SQLiteHelper db = SQLiteHelper.getInstance(this);
        db.close();
    }

    //region Navigation Drawer Callbacks

    @Override
    public void onNavigationDrawerItemSelected(String platformName) {
        if(mCurrentTabFragment != null) {
            mCurrentTabFragment.updatePlatform();
        }
        mTitle = platformName;
        getActionBar().setTitle(mTitle);
    }

    @Override
    public void onSortTypeSelected(int sortType){
        // save the sort Type
        SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = m_settingsSP.edit();
        editor.putInt(getString(R.string.sort_type),sortType);
        editor.apply();

        // Alert the fragment to reset the platform
        mCurrentTabFragment.updatePlatform();
    }

    @Override
    public void onSortDirectionSelected(boolean sortAscending){
        // save the sort Type
        SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = m_settingsSP.edit();
        editor.putBoolean(getString(R.string.sort_Direction),sortAscending);
        editor.apply();

        // Alert the fragment to reset the platform
        mCurrentTabFragment.updatePlatform();
    }
    @Override
    public  void onPlatformDeleteSelected(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you Sure you want to delete this platform?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int platformIdx) {
                        mCurrentTabFragment.deleteCurrentPlatform();
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
        final Platform platformTemp = mCurrentTabFragment.getCurrentPlatform();
        input.setText(platformTemp.getName());
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                platformTemp.setName(input.getText().toString());
                SQLiteHelper db = SQLiteHelper.getInstance(getApplication());
                db.setPlatform(platformTemp);
                mNavigationDrawerFragment.resetPlatforms();
                mTitle = platformTemp.getName();
                getActionBar().setTitle(mTitle);

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
        platformState = DOWNLOADINGPLATFORMS;
        mCurrentTabFragment.setPlatformState(platformState);
        mPlatformHandler.RequestNewPlatformFromServer(platformType,text);
        mCurrentTabFragment.setLoadingScreen(true);
    }

    @Override
    public void onEditSelected() {
        mCurrentTabFragment.enableEditing();
    }

    //endregion

    //region Tabs

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
        mCurrentTabFragment = new TabFragment();
        Bundle args = new Bundle();
        args.putInt("tabNumber", tab.getPosition()+1);
        args.putInt("state", platformState);
        mCurrentTabFragment.setArguments(args);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, mCurrentTabFragment).commit();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }
    //endregion

    //region Action Bar

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    //region Options

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
                    mCurrentTabFragment.setQueryText(newText);
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
    //endregion
    //endregion

    //region Platform Handler Callbacks

    @Override
    public void onNewIncomingPlatform(Platform platform) {
        platformState = DOWNLOADINGACHIEVEMENTS;
        mCurrentTabFragment.setPlatformState(platformState);
        mNavigationDrawerFragment.updatePlatforms(true);
        mCurrentTabFragment.updatePlatform();
    }

    @Override
    public void onUpdatedIncomingPlatform(ArrayList<Game> games) {
        platformState = DOWNLOADINGPLATFORMS;
        mCurrentTabFragment.setPlatformState(platformState);
        mNavigationDrawerFragment.updatePlatforms(false);
        mCurrentTabFragment.updatePlatform();

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

    //endregion
}
