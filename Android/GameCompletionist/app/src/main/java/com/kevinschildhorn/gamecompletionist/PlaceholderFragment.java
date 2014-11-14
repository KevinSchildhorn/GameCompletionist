package com.kevinschildhorn.gamecompletionist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;
import com.kevinschildhorn.gamecompletionist.DataClasses.Platform;

import java.util.ArrayList;

/**
 * Created by Kevin on 11/7/2014.
 */

public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PlaceholderCallbacks mCallbacks;

    ListView listview;
    ProgressBar mProgressSpinner;
    TextView mTextInfo;

    GameArrayAdapter adapter;
    Platform currentPlatform;
    boolean editingList = false;
    int platformID;
    int filterType;
    int sortType;
    boolean sortDirectionAscending;

    ActionMode actionMode;

    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        return fragment;
    }
    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // List View
        listview = (ListView) rootView.findViewById(R.id.listView);
        listview.setBackgroundColor(Color.WHITE);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mTextInfo = (TextView) rootView.findViewById(R.id.textInfo);
        mTextInfo.setVisibility(View.INVISIBLE);
        mTextInfo.setBackgroundColor(Color.argb(100,0,0,0));

        mProgressSpinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mProgressSpinner.setVisibility(View.VISIBLE);

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (actionMode != null) {
                    return false;
                }

                // Start the CAB using the ActionMode.Callback defined above
                actionMode = getActivity().startActionMode(mActionModeCallback);

                adapter.SelectItemAtIndex(position);
                view.setSelected(true);
                editingList = true;

                return true;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (editingList == true) {

                    if(view.isSelected()) {
                        adapter.UnselectItemAtIndex(position);
                        view.setSelected(false);
                    }
                    else{
                        adapter.SelectItemAtIndex(position);
                        view.setSelected(true);
                    }
                }
            }
        });


        // Query the database for games
        updatePlatform();

        return rootView;
    }


    // Update GUI

    public void updatePlatform(){
        if(mTextInfo != null) {
            mTextInfo.setVisibility(View.INVISIBLE);
        }
        if(mProgressSpinner != null){
            mProgressSpinner.setVisibility(View.INVISIBLE);
        }
        SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(getActivity());
        platformID =  m_settingsSP.getInt(getString(R.string.platform_id),-1);
        filterType =  m_settingsSP.getInt(getString(R.string.filter_type),-1);
        sortType =  m_settingsSP.getInt(getString(R.string.sort_type),-1);
        sortDirectionAscending = m_settingsSP.getBoolean(getString(R.string.sort_Direction),true);

        if(platformID != -1) {
            Platform platform = mCallbacks.getDatabase().getPlatform(platformID, sortType, filterType,sortDirectionAscending);
            updateListView(platform, sortType, "");
        }
        else{
            Platform platform = new Platform(0,"","",0,"",mCallbacks.getDatabase().getGames(platformID,filterType,sortType,sortDirectionAscending));
            if(platform.getGames().length == 0){
                mTextInfo.setVisibility(View.VISIBLE);
                mTextInfo.setText("You have no Platforms. Please add a platform from the drawer");
            }
            else{
                updateListView(platform, sortType, "");
            }
        }
    }
    public void updateListView(Platform platform,int sortType,String filterString){
        currentPlatform = platform;

        // Get All Games
        Game gameTemp;
        Game [] games = platform.getGames();

        // Filter Based on Sort String
        ArrayList<Game> gameArrayList = new ArrayList<Game>();
        for (int i = 0; i < games.length; ++i) {
            gameTemp = games[i];

            if(gameTemp.getName().toLowerCase().contains(filterString.toLowerCase())){
                gameArrayList.add(gameTemp);
            }
        }

        // Set up the adapter
        Game[] gamesFiltered = gameArrayList.toArray(new Game[gameArrayList.size()]);

        if(sortType == getResources().getInteger(R.integer.customOrder)){
            adapter = new GameArrayAdapter(getActivity().getApplicationContext(),gamesFiltered,true);
        }
        else{
            adapter = new GameArrayAdapter(getActivity().getApplicationContext(),gamesFiltered);
        }


        // add extra information
        adapter.SetSortType(sortType);
        listview.setAdapter(adapter);


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
        try {
            mCallbacks = (PlaceholderCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }


    // Action Mode

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.list_edit, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.selectAll:
                    adapter.SelectAllItems();
                    for ( int i=0; i< listview.getAdapter().getCount(); i++ ) {
                        listview.setItemChecked(i, true);
                    }
                    mode.finish();
                    return true;
                case R.id.unselectAll:
                    adapter.UnselectAllItems();
                    for ( int i=0; i< listview.getAdapter().getCount(); i++ ) {
                        listview.setItemChecked(i, false);
                    }
                    return true;
                case R.id.cancel:
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    modifySelectedGames();
                    mode.finish();
                    return false;
            }
        }


        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mode = null;
            modifySelectedGames();
        }
    };

    public Platform getCurrentPlatform(){
        return currentPlatform;
    }
    public void setCurrentPlatform(Platform platform){
        currentPlatform = platform;
    }
    public void deleteCurrentPlatform(){
        mCallbacks.getDatabase().removePlatform(currentPlatform);
        currentPlatform = null;
        SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(getActivity());
        m_settingsSP.edit().putInt(getString(R.string.platform_id),-1).apply();
        updatePlatform();
    }
    public void modifySelectedGames(){
        // ask user which platform they'd like to move the games to
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Game Status")
                .setNegativeButton("Cancel",null)
                .setItems(R.array.filter_types, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int filterIdx) {

                        // move the selected items to the new category
                        ArrayList<Integer> selectedItems = adapter.GetSelectedItems();
                        Game gameTemp;
                        Integer temp;
                        for (int i=0;i<selectedItems.size();i++){
                            temp = selectedItems.get(i);
                            gameTemp = currentPlatform.getGameAtIndex(temp);
                            gameTemp.completionStatus = filterIdx+1;
                            mCallbacks.getDatabase().setGame(gameTemp);
                        }
                        updatePlatform();
                    }});
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void setLoadingScreen(boolean screenOn){
        if(mProgressSpinner != null && mTextInfo != null) {
            if (screenOn) {
                mProgressSpinner.setVisibility(View.VISIBLE);
                mTextInfo.setVisibility(View.VISIBLE);
                mTextInfo.setText("\n\n\n\n\nDownloading Games for the first time, this may take a minute");
            }
            else{
                mProgressSpinner.setVisibility(View.INVISIBLE);
                mTextInfo.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static interface PlaceholderCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        SQLiteHelper getDatabase();
    }
}
