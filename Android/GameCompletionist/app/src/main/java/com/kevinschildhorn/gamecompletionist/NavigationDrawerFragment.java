package com.kevinschildhorn.gamecompletionist;


import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;
import com.kevinschildhorn.gamecompletionist.DataClasses.Platform;

import org.json.JSONException;
import org.json.JSONObject;


public class NavigationDrawerFragment extends Fragment implements Platform.PlatformCallbacks {
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private NavigationDrawerCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    SharedPreferences sp;

    private ArrayList<Platform> platforms = new ArrayList<Platform>();

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            //mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Get the last selected Drawer Item
        mCurrentSelectedPosition = sp.getInt("drawerPosition",0);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView temp = (TextView)view.findViewById(R.id.firstLine);
                selectItem(position);

                // If you are adding a new platform
                if(temp.getText() == getString(R.string.add_platform)){
                    addPlatform();
                }
            }
        });

        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout,ArrayList<Platform> incomingPlatforms) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        platforms = incomingPlatforms;
        refreshDrawer();
        selectItem(mCurrentSelectedPosition);

        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
    }

    private void selectItem(int position) {
        sp.edit().putInt("drawerPosition",position).apply();

        mCurrentSelectedPosition = position;

        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    private void addPlatform(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Your Platform")
                .setItems(R.array.platforms, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int platformIdx) {

                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Enter your Username");
                        builder.setIcon(R.drawable.ic_launcher);

                        // Set an EditText view to get user input
                        final EditText input = new EditText(builder.getContext());
                        builder.setView(input);

                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                // hold as temporary object
                                createNewPlatform(platformIdx+1,input.getText().toString());

                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });

                        AlertDialog dialog2 = builder.create();
                        dialog2.show();


                    }});
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void createNewPlatform(int id,String text){
        new Platform(this,getActivity(),-1,id, text);
        mCallbacks.onNewPlatformEntered();
    }

    public void OnUpdatePlatformReceived(Context context){
        // get new platforms
        platforms = mCallbacks.getDatabase().getPlatforms();

        refreshDrawer();
    }

    public void refreshDrawer(){
        if(mDrawerListView != null && platforms != null) {
            ArrayList<String> drawerStrings = new ArrayList<String>();

            drawerStrings.add(getString(R.string.all));
            drawerStrings.add(getString(R.string.unfinished));
            drawerStrings.add(getString(R.string.finished));
            drawerStrings.add(getString(R.string.complete));

            Platform platformTemp;
            for (int i=0;i<platforms.size();i++){
                platformTemp = platforms.get(i);
                drawerStrings.add(platformTemp.getName());
                drawerStrings.add(getString(R.string.unfinished));
                drawerStrings.add(getString(R.string.finished));
                drawerStrings.add(getString(R.string.complete));
            }
            drawerStrings.add(getString(R.string.add_platform));

            String[] arr = drawerStrings.toArray(new String[drawerStrings.size()]);

            mDrawerListView.setAdapter(new PlatformArrayAdapter(
                    getActionBar().getThemedContext(),
                    arr));

        }

    }

    @Override
    public void onNewIncomingPlatform(Platform platform){
        // get new platforms
        platforms = mCallbacks.getDatabase().getPlatforms();

        // set actionBar title
        ActionBar actionBar = getActivity().getActionBar();
        Platform platformTemp = platforms.get(platforms.size() - 1);
        actionBar.setTitle(platformTemp.getName());

        refreshDrawer();
    }

    @Override
    public void onUpdatedIncomingPlatform(Platform platform,ArrayList<Game> games){
        String gameNames = "";
        for (int i=0;i<games.size();i++){
            gameNames += games.get(i).getName() + "\n";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("New games added to your list")
                .setMessage("New games were added to your platform:\n" + gameNames)
                .setNegativeButton("Ok",null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public String getListItemName(int ListIndex){
        return (String) mDrawerListView.getAdapter().getItem(ListIndex);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.action_edit) {
            return true;
        }
        else if (item.getItemId() == R.id.action_change_direction) {
            reverseSortDirection();
            return true;
        }
        else if (item.getItemId() == R.id.action_sort) {
            selectSortType();
            return true;
        }
        else if (item.getItemId() == R.id.action_rename) {
            mCallbacks.onPlatformRenameSelected();
            platforms = mCallbacks.getDatabase().getPlatforms();
            return true;
        }
        else if (item.getItemId() == R.id.action_delete) {
            mCallbacks.onPlatformDeleteSelected();
            platforms = mCallbacks.getDatabase().getPlatforms();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void selectSortType(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sort By")
                .setItems(R.array.sort_types, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int sortType) {
                        if (mCallbacks != null) {
                            mCallbacks.onSortTypeSelected(sortType);
                        }
                    }});
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void reverseSortDirection(){
        SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean sortDirectionAsc = m_settingsSP.getBoolean(getString(R.string.sort_Direction),true);
        if (mCallbacks != null) {
            mCallbacks.onSortDirectionSelected(!sortDirectionAsc);
        }
    }
    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle("Platforms");
    }

    private ActionBar getActionBar() {
        return getActivity().getActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
        void onSortTypeSelected(int sortType);
        void onSortDirectionSelected(boolean sortAsc);
        void onPlatformDeleteSelected();
        void onPlatformRenameSelected();
        void onNewPlatformEntered();
        SQLiteHelper getDatabase();
    }
}

