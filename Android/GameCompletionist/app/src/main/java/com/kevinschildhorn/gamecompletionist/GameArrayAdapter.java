package com.kevinschildhorn.gamecompletionist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;
import com.kevinschildhorn.gamecompletionist.DataClasses.Platform;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * SCM Products Inc.
 * Created by Kevin Schildhorn on Nov 05, 2014.
 */

public class GameArrayAdapter extends ArrayAdapter<Game> {
    private final Context context;
    private ArrayList<Game> values;
    private int sortType = 0;
    private ArrayList<Integer> selectedItemIndexes;
    boolean customSortType;
    int layoutID;
    String hoverGameName;
    HashMap<Game, Integer> mIdMap = new HashMap<Game, Integer>();

    final int INVALID_ID = -1;

    public GameArrayAdapter(Context context, int layoutID,ArrayList<Game>  values) {
        super(context, layoutID, values);

        for (int i = 0; i < values.size(); ++i) {
            mIdMap.put(values.get(i), i);
        }

        this.layoutID = layoutID;
        this.customSortType = false;
        this.context = context;
        this.values = values;
        this.hoverGameName = "";
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
        return values.get(position);
    }
    public void SelectItemAtIndex(int position){
        selectedItemIndexes.add(position);
    }
    public void UnselectItemAtIndex(int position){
        selectedItemIndexes.remove((Object)position);
    }

    public void SelectAllItems(){
        UnselectAllItems();
        for(int i=0;i<values.size();i++){
            selectedItemIndexes.add(i);
        }
    }
    public void UnselectAllItems(){
        selectedItemIndexes.clear();
    }

    public ArrayList<Integer> GetSelectedItems(){
        return this.selectedItemIndexes;
    }

    public void setHoverItem(int position){
        if(position != -1) {
            this.hoverGameName = values.get(position).getName();
        }
        else{
            this.hoverGameName = "";
        }
    }
    //endregion

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(this.layoutID, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.gameName);
        TextView secondaryTextView = (TextView) rowView.findViewById(R.id.secondaryInfo);
        ImageView teritaryInfo = (ImageView) rowView.findViewById(R.id.teritaryInfo);
        TextView achievementTextView = (TextView) rowView.findViewById(R.id.achievements);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView orderNumberView = (TextView) rowView.findViewById(R.id.orderNumber);

        // hide selected game CHECK
        Game temp = values.get(position);
        if(temp.getName() == hoverGameName){
            Log.e("",hoverGameName);
            rowView.setVisibility(View.INVISIBLE);
        }

        // set order number
        if(orderNumberView != null){
            orderNumberView.setText(temp.customSortTypeIndex+1 + "");
        }

        if(this.layoutID == R.layout.fragment_main_sorted_cell){
            if(values.get(position).customSortTypeIndex != position) {
                new updateCustomOrderAsyncTask().execute(position);

            }
        }


        if(temp.getControllerSupport() == 1){
            teritaryInfo.setImageDrawable(getContext().getResources().getDrawable(R.drawable.partialcontrollersupport));
        }
        else if(temp.getControllerSupport() == 2){
            teritaryInfo.setImageDrawable(getContext().getResources().getDrawable(R.drawable.fullcontrollersupport));
        }

        // fill in row
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

        if(logo.length > 0) {
            Bitmap logoBitmap = BitmapFactory.decodeByteArray(logo, 0, logo.length);
            imageView.setImageBitmap(logoBitmap);
        }

        return rowView;
    }

    public void setArrayList(ArrayList<Game> games){
        this.values = games;
    }
    public ArrayList<Game> getArrayList(){
        return this.values;
    }


    private class updateCustomOrderAsyncTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... positions) {
            int position = positions[0];
            values.get(position).customSortTypeIndex = position;
            SQLiteHelper.getInstance(context).setGameCustomSortTypeIndex(values.get(position));
            return null;

        }
    }
}