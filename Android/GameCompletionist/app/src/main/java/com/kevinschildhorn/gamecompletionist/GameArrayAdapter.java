package com.kevinschildhorn.gamecompletionist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * SCM Products Inc.
 * Created by Kevin Schildhorn on Nov 05, 2014.
 */

public class GameArrayAdapter extends ArrayAdapter<Game> {
    private final Context context;
    private Game[] values;
    private int sortType = 0;
    private ArrayList<Integer> selectedItemIndexes;
    boolean customSortType;

    HashMap<Game, Integer> mIdMap = new HashMap<Game, Integer>();

    final int INVALID_ID = -1;


    public GameArrayAdapter(Context context, Game[] values) {
        super(context, R.layout.fragment_main_cell, values);

        this.customSortType = false;
        this.context = context;
        this.values = values;
        selectedItemIndexes = new ArrayList<Integer>();
    }

    public GameArrayAdapter(Context context, int layoutID,Game[]  values) {
        super(context, layoutID, values);

        for (int i = 0; i < values.length; ++i) {
            mIdMap.put(values[i], i);
        }

        this.customSortType = customSortType;
        this.context = context;
        this.values = values;
        selectedItemIndexes = new ArrayList<Integer>();
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size()) {
            return INVALID_ID;
        }
        Game item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void SetSortType(int sortType){
        this.sortType = sortType;
    }


    //region Selection

    public Game GetItemAtIndex(int position){
        return values[position];
    }
    public void SelectItemAtIndex(int position){
        selectedItemIndexes.add(position);
    }
    public void UnselectItemAtIndex(int position){
        selectedItemIndexes.remove((Object)position);
    }

    public void SelectAllItems(){
        UnselectAllItems();
        for(int i=0;i<values.length;i++){
            selectedItemIndexes.add(i);
        }
    }
    public void UnselectAllItems(){
        selectedItemIndexes.clear();
    }

    public ArrayList<Integer> GetSelectedItems(){
        return this.selectedItemIndexes;
    }

    //endregion

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.fragment_main_cell, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.gameName);
        TextView secondaryTextView = (TextView) rowView.findViewById(R.id.secondaryInfo);
        //TextView tertiaryTextView = (TextView) rowView.findViewById(R.id.teritaryInfo);
        TextView achievementTextView = (TextView) rowView.findViewById(R.id.achievements);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        Game temp = values[position];
        textView.setText(temp.getName());
        textView.setTextColor(Color.BLACK);
        if(temp.getAchievementsTotalCount() > 0) {
            achievementTextView.setText("A " + temp.getAchievementsFinishedCount() + "/" + temp.getAchievementsTotalCount());
        }
        int hours;
        int minutes;
        switch (sortType){

            default:    // Name
            case 0:
                hours = temp.getMinutesPlayed()/60;
                minutes = temp.getMinutesPlayed()%60;
                secondaryTextView.setText(hours + " Hours " + minutes + " Minutes Played");
                if(temp.getRecentMinutesPlayed() > 0) {
                    hours = temp.getRecentMinutesPlayed() / 60;
                    minutes = temp.getRecentMinutesPlayed() % 60;
                    //tertiaryTextView.setText(hours + " Hours " + minutes + " Minutes Recently Played");
                }
                break;

            case 1:     // Hours Played
                hours = temp.getMinutesPlayed()/60;
                minutes = temp.getMinutesPlayed()%60;
                secondaryTextView.setText(hours + " Hours " + minutes + " Minutes Played");
                if(temp.getRecentMinutesPlayed() > 0) {
                    hours = temp.getRecentMinutesPlayed() / 60;
                    minutes = temp.getRecentMinutesPlayed() % 60;
                   // tertiaryTextView.setText(hours + " Hours " + minutes + " Minutes Recently Played");
                }
                break;

            case 2:     // Recent Hours
                hours = temp.getMinutesPlayed()/60;
                minutes = temp.getMinutesPlayed()%60;
                //tertiaryTextView.setText(hours + " Hours " + minutes + " Minutes Played");
                if(temp.getRecentMinutesPlayed() > 0) {
                    hours = temp.getRecentMinutesPlayed() / 60;
                    minutes = temp.getRecentMinutesPlayed() % 60;
                    secondaryTextView.setText(hours + " Hours " + minutes + " Minutes Recently Played");
                }
                break;

            case 3:     // Achievements Earned
                hours = temp.getMinutesPlayed()/60;
                minutes = temp.getMinutesPlayed()%60;
                secondaryTextView.setText(hours + " Hours " + minutes + " Minutes Played");
                if(temp.getRecentMinutesPlayed() > 0) {
                    hours = temp.getRecentMinutesPlayed() / 60;
                    minutes = temp.getRecentMinutesPlayed() % 60;
                   // tertiaryTextView.setText(hours + " Hours " + minutes + " Minutes Recently Played");
                }
                break;

            case 4:     // Custom Order
                hours = temp.getMinutesPlayed()/60;
                minutes = temp.getMinutesPlayed()%60;
                secondaryTextView.setText(hours + " Hours " + minutes + " Minutes Played");
                if(temp.getRecentMinutesPlayed() > 0) {
                    hours = temp.getRecentMinutesPlayed() / 60;
                    minutes = temp.getRecentMinutesPlayed() % 60;
                    // tertiaryTextView.setText(hours + " Hours " + minutes + " Minutes Recently Played");
                }
                break;

        }

        SQLiteHelper db = SQLiteHelper.getInstance(context);
        byte[] logo = db.getGameLogo(temp);

        if(logo.length > 0){
            Bitmap logoBitmap = BitmapFactory.decodeByteArray(logo, 0, logo.length);

            //if (logoBitmap != null && !logoBitmap.isRecycled()) {
                imageView.setImageBitmap(logoBitmap);
              //  logoBitmap.recycle();
              //  logoBitmap = null;
            //}
        }

        return rowView;
    }


}