package com.kevinschildhorn.gamecompletionist;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ResourceBundle;


/**
 * Created by kevin on 11/11/2014.
 */
public class PlatformArrayAdapter extends ArrayAdapter<String> {
    Context context;
    private String[] values;

    public PlatformArrayAdapter(Context context, String[] values) {
        super(context, R.layout.fragment_navigation_cell, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.fragment_navigation_cell, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
        TextView subtextView = (TextView) rowView.findViewById(R.id.secondLine);
        ImageView backgroundView = (ImageView) rowView.findViewById(R.id.imageView);

        SQLiteHelper db = SQLiteHelper.getInstance(getContext());

        int gameCount;

        // All
        if(values[position] == getContext().getString(R.string.all)){
            textView.setTextColor(Color.GRAY);
            subtextView.setTextColor(Color.GRAY);
            backgroundView.setBackgroundColor(Color.LTGRAY);
            gameCount = db.getGameCount(values[position], getContext().getResources().getInteger(R.integer.all));
        }
        // Add Platform
        else if(values[position] == getContext().getString(R.string.add_platform)){
            textView.setTextColor(Color.LTGRAY);
            subtextView.setTextColor(Color.LTGRAY);
            backgroundView.setBackgroundColor(Color.GRAY);
            gameCount = -1;
        }
        // Platform Name
        else {
            textView.setTextColor(Color.DKGRAY);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            subtextView.setTextColor(Color.DKGRAY);
            //subtextView.setBackgroundResource(R.drawable.count_background);
            backgroundView.setBackgroundColor(Color.argb(255,171,171,62));
            gameCount = db.getGameCount(values[position],getContext().getResources().getInteger(R.integer.all));
        }
        textView.setText(values[position]);

        if(gameCount != -1) {
            subtextView.setText(gameCount + "");
        }

        return rowView;
    }
}
