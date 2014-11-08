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
import android.util.Log;
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
import android.widget.SearchView;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;
import com.kevinschildhorn.gamecompletionist.DataClasses.Platform;


import java.util.ArrayList;

import static com.kevinschildhorn.gamecompletionist.R.drawable.selection;


public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    // Fragments
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private PlaceholderFragment placeholderFragment;

    // last screen title
    private CharSequence mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the drawer.
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        SQLiteHelper db = new SQLiteHelper(this);
        ArrayList platformArray = db.getPlatforms();
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                platformArray);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    // Navigation Drawer Callbacks

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        placeholderFragment = PlaceholderFragment.newInstance(position + 1);
        fragmentManager.beginTransaction()
                .replace(R.id.container, placeholderFragment)
                .commit();
    }

    @Override
    public void onSortOrderSelected(int sortType){
        // save the order Type
        SharedPreferences m_settingsSP = this.getSharedPreferences("MyPreferences", this.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = m_settingsSP.edit();
        editor.putInt(getString(R.string.order_type),sortType);
        editor.commit();

        // Alert the fragment to reset the platform
        placeholderFragment.updatePlatform();
    }


    // Action Bar

    public void onSectionAttached(int number) {
        mTitle =  mNavigationDrawerFragment.getListItemName(number);

        SharedPreferences m_settingsSP = this.getSharedPreferences("MyPreferences", this.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = m_settingsSP.edit();
        if (mTitle == "Finished"){
            editor.putInt(getString(R.string.filter_type),1);
        }
        if (mTitle == "Unfinished"){
            editor.putInt(getString(R.string.filter_type),2);
        }
        if (mTitle == "100% Complete"){
            editor.putInt(getString(R.string.filter_type),3);
        }
        editor.commit();

        placeholderFragment.updatePlatform();

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
                    //PlaceholderFragment.updateListView(currentPlatform,orderType,newText);
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





}
