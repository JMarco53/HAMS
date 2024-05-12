package com.example.group5project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AppointmentListAdapter extends ArrayAdapter<Appointment> {

    public AppointmentListAdapter(@NonNull Context context, ArrayList<Appointment> aptArrayList) {
        super(context, R.layout.list_item_shift, aptArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        Appointment appointment = getItem(position);

        String formattedDate, formattedTime;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_shift, parent, false);
        }

        TextView appointmentDate = view.findViewById(R.id.user_date);
        TextView appointmentHour = view.findViewById(R.id.user_hour);

        if (appointment != null) {
            Date startTime = appointment.getStartTime();
            Date endTime = appointment.getEndTime();

            SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM d", Locale.CANADA);
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.CANADA);

            formattedDate = dateFormat.format(startTime);
            formattedTime = timeFormat.format(startTime) + " - " + timeFormat.format(endTime);

            appointmentDate.setText(formattedDate);
            appointmentHour.setText(formattedTime);
        }

        return view;
    }
}
