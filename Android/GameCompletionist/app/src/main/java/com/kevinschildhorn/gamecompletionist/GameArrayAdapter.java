package com.kevinschildhorn.gamecompletionist;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private  int orderType = 0;

    private ArrayList<Integer> selectedItemIndexes;

    public GameArrayAdapter(Context context, Game[] values) {
        super(context, R.layout.celllayout, values);
        this.context = context;
        this.values = values;
        selectedItemIndexes = new ArrayList<Integer>();
    }


    public void setOrderType(int orderType){
        this.orderType = orderType;
    }


    // Selection

    public boolean selectedAtIndex(int position){
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

    public ArrayList<Integer> GetSelectedItems(){
        return this.selectedItemIndexes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.celllayout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
        TextView subtextView = (TextView) rowView.findViewById(R.id.secondLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        Game temp = values[position];
        textView.setText(temp.getName());

        int hours;
        int minutes;
        switch (orderType){

            default:    // Name
            case 0:
            case 1:
                hours = temp.getHoursPlayed()/60;
                minutes = temp.getHoursPlayed()%60;
                subtextView.setText(hours + " Hours " + minutes + " Minutes Played");
                break;

            case 2:     // Hours Played
            case 3:
                hours = temp.getHoursPlayed()/60;
                minutes = temp.getHoursPlayed()%60;
                subtextView.setText(hours + " Hours " + minutes + " Minutes Played");
                break;

            case 4:     // Recent Hours
            case 5:
                hours = temp.getRecentMinutesPlayed()/60;
                minutes = temp.getRecentMinutesPlayed()%60;
                subtextView.setText(hours + " Hours " + minutes + " Minutes Recently Played");
                break;

            case 6:     // Date Purchased
            case 7:
                hours = temp.getHoursPlayed()/60;
                minutes = temp.getHoursPlayed()%60;
                subtextView.setText(hours + " Hours " + minutes + " Minutes Played");
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