package com.example.group5project;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ShiftListAdapter extends ArrayAdapter<Shift> {

    public ShiftListAdapter(@NonNull Context context, ArrayList<Shift> shiftArrayList) {
        // Constructor for the ShiftListAdapter class, extending ArrayAdapter
        super(context, R.layout.list_item_shift, shiftArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        // Override the getView method to customize the appearance of list items

        // Retrieve the Shift object at the specified position in the shiftArrayList
        Shift shift = getItem(position);

        String formattedDate, formattedTime;

        if (view == null) {
            // If the view is null (not yet created), inflate it from the list_item_shift layout
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_shift, parent, false);
        }

        // Find and bind the TextView elements in the list_item_shift layout
        TextView shiftDate = view.findViewById(R.id.user_date);
        TextView shiftHour = view.findViewById(R.id.user_hour);
        ImageView dayIcon = view.findViewById(R.id.listImage);

        // Set the text of shiftDate TextView to the shift's start time
        if (shift != null) {
            Date startTime = shift.getStartTime();
            Date endTime = shift.getEndTime();

            SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM d", Locale.CANADA);
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.CANADA);

            formattedDate = dateFormat.format(startTime);
            formattedTime = timeFormat.format(startTime) + " - " + timeFormat.format(endTime);

            shiftDate.setText(formattedDate);
            shiftHour.setText(formattedTime);

            //Set the icon to day/night base on the time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime);

            // Set the time to 8:00 PM
            calendar.set(Calendar.HOUR_OF_DAY, 19); // 20 corresponds to 8:00 PM
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 0);

            // Get the Date object for 8:00 PM
            Date eightPm = calendar.getTime();

            if (startTime.after(eightPm)){
                dayIcon.setImageResource(R.drawable.night_icon);
            }
            // You can customize the view further based on the properties of the Shift object
        }

        // Return the customized view for the list item
        return view;
    }
}
