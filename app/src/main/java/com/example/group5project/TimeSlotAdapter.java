package com.example.group5project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeSlotAdapter extends ArrayAdapter<Shift> {
    public TimeSlotAdapter(@NonNull Context context, ArrayList<Shift> timeSlotList) {
        // Constructor for the TimeSlotAdapter class, extending ArrayAdapter
        super(context, R.layout.list_item_shift, timeSlotList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        SimpleDateFormat timeFormat, dateFormat;
        // Override the getView method to customize the appearance of time slot items

        // Retrieve the Shift object representing the time slot at the specified position
        Shift timeSlot = getItem(position);

        String formattedTime, formattedDate;

        if (view == null) {
            // If the view is null (not yet created), inflate it from the list_item_time_slot layout
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_shift, parent, false);
        }

        // Find and bind the TextView elements in the list_item_time_slot layout
        TextView shiftDate = view.findViewById(R.id.user_date);
        TextView timeSlotText = view.findViewById(R.id.user_hour);
        ImageView dayIcon = view.findViewById(R.id.listImage);

        // Set the text of timeSlotText TextView to the time slot's formatted time
        if (timeSlot != null) {
            timeFormat = new SimpleDateFormat("h:mm a", Locale.CANADA);
            dateFormat = new SimpleDateFormat("E MMM d", Locale.CANADA);

            formattedTime = timeFormat.format(timeSlot.getStartTime()) + " - " + timeFormat.format(timeSlot.getEndTime());

            formattedDate = dateFormat.format(timeSlot.getStartTime());

            timeSlotText.setText(formattedTime);
            shiftDate.setText(formattedDate);

            // Set the icon based on the time (you can customize this further)
            // In this example, if the time slot is after 8:00 PM, display a night icon
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeSlot.getStartTime());

            // Set the time to 8:00 PM
            calendar.set(Calendar.HOUR_OF_DAY, 20); // 20 corresponds to 8:00 PM
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // Get the Date object for 8:00 PM
            Date eightPm = calendar.getTime();

            if (timeSlot.getStartTime().after(eightPm)){
                dayIcon.setImageResource(R.drawable.night_icon);
            }
        }

        // Return the customized view for the time slot item
        return view;
    }
}
