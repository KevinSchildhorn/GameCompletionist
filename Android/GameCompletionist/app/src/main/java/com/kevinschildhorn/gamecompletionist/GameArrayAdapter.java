package com.kevinschildhorn.gamecompletionist;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kevinschildhorn.gamecompletionist.DataClasses.Game;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

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

    public GameArrayAdapter(Context context, Game[] values) {
        super(context, R.layout.fragment_main_cell, values);

        this.customSortType = false;
        this.context = context;
        this.values = values;
        selectedItemIndexes = new ArrayList<Integer>();
    }

    public GameArrayAdapter(Context context, Game[] values,boolean customSortType) {
        super(context, R.layout.fragment_main_sorted_cell, values);

        this.customSortType = customSortType;
        this.context = context;
        this.values = values;
        selectedItemIndexes = new ArrayList<Integer>();
    }


    public void SetSortType(int sortType){
        this.sortType = sortType;
    }


    // Selection

    public boolean SelectedAtIndex(int position){
        if (selectedItemIndexes.contains(position)) {
            return true;
        }
        else {
            return false;
        }
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.fragment_main_cell, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.gameName);
        TextView secondaryTextView = (TextView) rowView.findViewById(R.id.secondaryInfo);
        TextView tertiaryTextView = (TextView) rowView.findViewById(R.id.teritaryInfo);
        TextView achievementTextView = (TextView) rowView.findViewById(R.id.achievements);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        Game temp = values[position];
        textView.setText(temp.getName());
        textView.setTextColor(Color.BLACK);

        achievementTextView.setText(temp.getAchievementCount() + " achievements");
        int hours;
        int minutes;
        switch (sortType){

            default:    // Name
            case 0:
            case 1:
                hours = temp.getMinutesPlayed()/60;
                minutes = temp.getMinutesPlayed()%60;
                secondaryTextView.setText(hours + " Hours " + minutes + " Minutes Played");
                hours = temp.getRecentMinutesPlayed()/60;
                minutes = temp.getRecentMinutesPlayed()%60;
                tertiaryTextView.setText(hours + " Hours " + minutes + " Minutes Recently Played");
                break;

            case 2:     // Hours Played
            case 3:
                hours = temp.getMinutesPlayed()/60;
                minutes = temp.getMinutesPlayed()%60;
                secondaryTextView.setText(hours + " Hours " + minutes + " Minutes Played");
                hours = temp.getRecentMinutesPlayed()/60;
                minutes = temp.getRecentMinutesPlayed()%60;
                tertiaryTextView.setText(hours + " Hours " + minutes + " Minutes Recently Played");
                break;

            case 4:     // Recent Hours
            case 5:
                hours = temp.getMinutesPlayed()/60;
                minutes = temp.getMinutesPlayed()%60;
                tertiaryTextView.setText(hours + " Hours " + minutes + " Minutes Played");
                hours = temp.getRecentMinutesPlayed()/60;
                minutes = temp.getRecentMinutesPlayed()%60;
                secondaryTextView.setText(hours + " Hours " + minutes + " Minutes Recently Played");
                break;

            case 6:     // Date Purchased
            case 7:
                hours = temp.getMinutesPlayed()/60;
                minutes = temp.getMinutesPlayed()%60;
                secondaryTextView.setText(hours + " Hours " + minutes + " Minutes Played");
                hours = temp.getRecentMinutesPlayed()/60;
                minutes = temp.getRecentMinutesPlayed()%60;
                tertiaryTextView.setText(hours + " Hours " + minutes + " Minutes Recently Played");
                break;

        }

        new DownloadImageTask(imageView).execute(temp.getLogoURL());

        return rowView;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}