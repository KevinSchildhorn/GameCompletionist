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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;
import com.kevinschildhorn.gamecompletionist.DataClasses.Platform;

import java.util.ArrayList;

/**
 * Created by Kevin on 11/7/2014.
 */

public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    SQLiteHelper db = new SQLiteHelper(getActivity());
    GameArrayAdapter adapter;
    Platform currentPlatform;
    boolean editingList = false;
    int platformID;
    int filterType;
    int orderType;


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
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Button temp = (Button) rootView.findViewById(R.id.editButton);
        temp.setVisibility(View.INVISIBLE);

        // List View
        final ListView listview = (ListView) rootView.findViewById(R.id.listView);

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                editingList = true;
                Button temp = (Button) view.findViewById(R.id.editButton);
                temp.setVisibility(View.VISIBLE);
                view.setBackground(getResources().getDrawable(R.drawable.selection));
                return true;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (editingList == true) {

                    if(adapter.selectedAtIndex(position)) {
                        view.setBackground(null);
                        adapter.UnselectItemAtIndex(position);
                    }
                    else{
                        view.setBackground(getResources().getDrawable(R.drawable.selection));
                        adapter.SelectItemAtIndex(position);
                    }
                }
            }
        });


        if (newPlatformReceiver != null) {
            IntentFilter intentFilter = new IntentFilter("updateListView");
            getActivity().registerReceiver(newPlatformReceiver, intentFilter);
        }

        // Query the database for games
        updatePlatform();

        return rootView;
    }

    public void updatePlatform(){
        SharedPreferences m_settingsSP = getActivity().getSharedPreferences("MyPreferences", getActivity().MODE_MULTI_PROCESS);
        platformID =  m_settingsSP.getInt(getString(R.string.platform_id),-1);
        filterType =  m_settingsSP.getInt(getString(R.string.filter_type),-1);
        orderType =  m_settingsSP.getInt(getString(R.string.order_type),-1);

        if(platformID != -1) {
            Platform platform = db.getPlatform(platformID, orderType, filterType);
            updateListView(platform, orderType, "");
        }
    }
    public void updateListView(Platform platform,int orderType,String sortString){
        final ListView listview = (ListView) getActivity().findViewById(R.id.listView);

        // Get All Games
        Game gameTemp;
        Game [] games = platform.getGames();

        // Filter Based on Sort String
        ArrayList<Game> gameArrayList = new ArrayList<Game>();
        for (int i = 0; i < games.length; ++i) {
            gameTemp = games[i];

            if(gameTemp.getName().toLowerCase().contains(sortString.toLowerCase())){
                gameArrayList.add(gameTemp);
            }
        }

        // Set up the adapter
        Game[] gamesFiltered = gameArrayList.toArray(new Game[gameArrayList.size()]);
        adapter = new GameArrayAdapter(getActivity().getApplicationContext(),gamesFiltered);

        // add extra information
        adapter.setOrderType(orderType);
        listview.setAdapter(adapter);
    }


    public void onEditButtonPressed(View view){
        Button temp = (Button) view.findViewById(R.id.editButton);
        temp.setVisibility(View.INVISIBLE);


        // ask user which platform they'd like to move the games to
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Your Platform")
                .setNegativeButton("Cancel",null)
                .setItems(R.array.filter_types, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int filterIdx) {

                        // move the selected items to the new category
                        ArrayList<Integer> selectedItems = adapter.GetSelectedItems();
                        Game gameTemp;
                        for (int i=0;i<selectedItems.size();i++){
                            gameTemp = currentPlatform.getGameAtIndex(selectedItems.get(i));
                            gameTemp.completionStatus = filterIdx;
                            db.setGame(gameTemp);
                        }
                        updatePlatform();
                    }});
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private BroadcastReceiver newPlatformReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences m_settingsSP = context.getSharedPreferences("MyPreferences", context.MODE_MULTI_PROCESS);
            platformID =  m_settingsSP.getInt(getString(R.string.platform_id),-1);
            updatePlatform();
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}
