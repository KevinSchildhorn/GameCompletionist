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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kevinschildhorn.gamecompletionist.DynamicListView;
import com.kevinschildhorn.gamecompletionist.DataClasses.Game;
import com.kevinschildhorn.gamecompletionist.DataClasses.Platform;
import com.kevinschildhorn.gamecompletionist.StableArrayAdapter;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Kevin on 11/7/2014.
 */

public class TabFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    DynamicListView listview;   // DYNAMICTEST
    TextView mInformationTextView;
    ProgressBar mLoadingSpinner;
    TextView mAchievementLoadingInfo;

    GameArrayAdapter adapter;
    StableArrayAdapter customSortAdapter;
    Platform currentPlatform;

    boolean editingList = false;
    int platformID;
    int sortType;
    int filterType;
    boolean sortDirectionAscending;
    String queryText = "";

    ActionMode actionMode;

    int platformState;

    ArrayList<Map<String, Object>> items = new ArrayList<Map<String, Object>>();

    public TabFragment(){


    }

    //region Initialization

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        this.filterType = getArguments().getInt("tabNumber", 0);
        this.platformState = getArguments().getInt("state", 0);
        // List View
        listview = (DynamicListView ) rootView.findViewById(R.id.listView); // DYNAMICTEST
        listview.setBackgroundColor(Color.WHITE);
        listview.setOnItemClickListener(itemClickListener);
        listview.setOnItemLongClickListener(longClickListener);


        mInformationTextView = (TextView) rootView.findViewById(R.id.textInfo);
        mInformationTextView.setBackgroundColor(Color.argb(100,0,0,0));
        mAchievementLoadingInfo = (TextView) rootView.findViewById(R.id.loadingInfo);
        mLoadingSpinner = (ProgressBar) rootView.findViewById(R.id.progressBar);

        setPlatformState(this.platformState);


        mInformationTextView.setVisibility(View.INVISIBLE);
        mAchievementLoadingInfo.setVisibility(View.INVISIBLE);
        mLoadingSpinner.setVisibility(View.VISIBLE);

        mAchievementLoadingInfo.setBackgroundColor(Color.argb(255,255,255,255));

        if (newPlatformReceiver != null) {
            IntentFilter intentFilter = new IntentFilter("updateDownloadInfo");
            getActivity().registerReceiver(newPlatformReceiver, intentFilter);
        }
        // Query the database for games
        updatePlatform();

        return rootView;
    }

    //endregion

    //region Update List

    public void updatePlatform(){
        if(mInformationTextView != null) {
            mInformationTextView.setVisibility(View.INVISIBLE);
        }
        if(mLoadingSpinner != null){
            mLoadingSpinner.setVisibility(View.INVISIBLE);
        }

        SharedPreferences m_settingsSP = PreferenceManager.getDefaultSharedPreferences(getActivity());
        platformID =  m_settingsSP.getInt(getString(R.string.platform_id),-1);
        sortType =  m_settingsSP.getInt(getString(R.string.sort_type),-1);
        sortDirectionAscending = m_settingsSP.getBoolean(getString(R.string.sort_Direction),true);

        new UpdatePlatformAsyncTask().execute();

    }
    public void updateListView(Platform platform,int sortType,String filterString){

        ArrayList<Game> gameArrayList = new ArrayList<Game>();
        ArrayList<String> gameNameArrayList = new ArrayList<String>();
        if(platform != null) {
            currentPlatform = platform;

            // Get All Games
            Game gameTemp;
            Game[] games = platform.getGames();

            // Filter Based on Sort String

            for (int i = 0; i < games.length; ++i) {
                gameTemp = games[i];

                if (gameTemp.getName().toLowerCase().contains(filterString.toLowerCase())) {
                    gameArrayList.add(gameTemp);
                    gameNameArrayList.add(gameTemp.getName());
                }
            }
        }
        // Set up the adapter
        //Game[] gamesFiltered = gameArrayList.toArray(new Game[gameArrayList.size()]);


        if (sortType == getResources().getInteger(R.integer.customOrder)) {
            listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            adapter = new GameArrayAdapter(getActivity().getApplicationContext(),R.layout.fragment_main_sorted_cell , gameArrayList);
        } else {
            listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            adapter = new GameArrayAdapter(getActivity().getApplicationContext(),R.layout.fragment_main_cell , gameArrayList);
        }
        adapter.SetSortType(sortType);
        listview.setAdapter(adapter);
    }

    //endregion

    //region Click Listeners
    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {

        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (sortType == getResources().getInteger(R.integer.customOrder)) {
                listview.onItemLongClick();
            }
            else{
                if (editingList == false) {
                    enableEditing();
                    adapter.SelectItemAtIndex(position);
                    view.setSelected(true);
                }
            }
            return true;
        }
    };

    AdapterView.OnItemClickListener itemClickListener =  new AdapterView.OnItemClickListener() {

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
            else{
                listview.setItemChecked(position, false);
            }
        }
    };

    void enableEditing(){
        // Start the CAB using the ActionMode.Callback defined above
        actionMode = getActivity().startActionMode(mActionModeCallback);
        editingList = true;
    }
    //endregion

    //region Current Platform
    public Platform getCurrentPlatform(){
        return currentPlatform;
    }
    public void setCurrentPlatform(Platform platform){
        currentPlatform = platform;
        updatePlatform();
    }
    public void deleteCurrentPlatform(){
        SQLiteHelper db = SQLiteHelper.getInstance(getActivity());
        db.removePlatform(currentPlatform);
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
                            gameTemp = adapter.GetItemAtIndex(temp);
                            gameTemp.completionStatus = filterIdx;
                            SQLiteHelper db = SQLiteHelper.getInstance(getActivity());
                            db.setGame(gameTemp);
                        }
                        updatePlatform();
                    }});
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //endregion

    //region Loading Screen
    public void setLoadingScreen(boolean screenOn){
        if(mLoadingSpinner != null && mLoadingSpinner != null) {
            if (screenOn) {
                mLoadingSpinner.setVisibility(View.VISIBLE);
                mInformationTextView.setVisibility(View.VISIBLE);
                mInformationTextView.setText("\n\n\n\n\nDownloading Games for the first time, this may take a minute." +
                        "\nAchievements will be loaded afterwards in the background");
            }
            else{
                mLoadingSpinner.setVisibility(View.INVISIBLE);
                mInformationTextView.setVisibility(View.INVISIBLE);
            }
        }
    }
    public void setLoadingInfo(String loadingInfo){

    }
    public void setPlatformState(int state){
        this.platformState = state;

        int infoVisibility = View.INVISIBLE;
        if(this.platformState == MainActivity.NOPLATFORMS){
            infoVisibility = View.VISIBLE;
            setLoadingScreen(false);
        }
        else if(this.platformState == MainActivity.DOWNLOADINGPLATFORMS){
            infoVisibility = View.INVISIBLE;
            setLoadingScreen(true);
        }
        else if(this.platformState == MainActivity.DOWNLOADINGACHIEVEMENTS){
            infoVisibility = View.INVISIBLE;
            setLoadingScreen(false);
        }
        else if(this.platformState == MainActivity.DOWNLOADSFINISHED){
            infoVisibility = View.INVISIBLE;
            setLoadingScreen(false);
        }

        if(mInformationTextView != null) {
            mInformationTextView.setVisibility(infoVisibility);
        }
    }


    //endregion

    public void setQueryText(String queryText){
        this.queryText = queryText;
        updatePlatform();

    }

    private class UpdatePlatformAsyncTask extends AsyncTask<Integer, Void, Platform> {
        @Override
        protected Platform doInBackground(Integer... text) {
            Platform platform;
            if(platformID != -1) {
                SQLiteHelper db = SQLiteHelper.getInstance(getActivity());
                platform = db.getPlatform(platformID, sortType, filterType,sortDirectionAscending);
            }
            else{
                SQLiteHelper db = SQLiteHelper.getInstance(getActivity());
                platform = new Platform(0,"","",0,"",db.getGames(platformID,filterType,sortType,sortDirectionAscending));
            }
            return platform;
        }
        protected void onPostExecute(Platform result) {
            if(result.getGames().length == 0){
                mInformationTextView.setVisibility(View.VISIBLE);
                mInformationTextView.setText("You don't have any platforms! Please add a platform from the drawer");
            }
            else{
                updateListView(result, sortType, queryText);
            }
        }
    }


    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        boolean actionSelected = false;
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

                    return true;

                case R.id.unselectAll:
                    adapter.UnselectAllItems();
                    for ( int i=0; i< listview.getAdapter().getCount(); i++ ) {
                        listview.setItemChecked(i, false);
                    }
                    return true;

                case R.id.cancel:
                    adapter.UnselectAllItems();
                    for ( int i=0; i< listview.getAdapter().getCount(); i++ ) {
                        listview.setItemChecked(i, false);
                    }
                    actionSelected = true;
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

            if(!actionSelected){
                modifySelectedGames();
            }
            editingList = false;
        }
    };

   BroadcastReceiver newPlatformReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int fin,total;
            fin = intent.getIntExtra("finished",0);
            total = intent.getIntExtra("total",0);
            int type = intent.getIntExtra("type",0);

            if(fin == total-1){
                mAchievementLoadingInfo.setVisibility(View.INVISIBLE);
            }
            else{
                if(type == 1){
                    mAchievementLoadingInfo.setVisibility(View.VISIBLE);
                    mAchievementLoadingInfo.setText(fin + "/" + total + " logos loaded");
                }
                else if(type == 2) {
                    mAchievementLoadingInfo.setVisibility(View.VISIBLE);
                    mAchievementLoadingInfo.setText(fin + "/" + total + " achievements loaded");
                }
            }
        }

    };
}
