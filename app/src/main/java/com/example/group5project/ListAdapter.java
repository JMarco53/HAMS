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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ListAdapter extends ArrayAdapter<User> {
    public ListAdapter(@NonNull Context context, ArrayList<User> dataArrayList) {
        // Constructor for the ListAdapter class, extending ArrayAdapter
        super(context, R.layout.list_item, dataArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        // Override the getView method to customize the appearance of list items
        Log.e("specialtiesCheck", "Im in ListAdapter");

        // Retrieve the User object at the specified position in the dataArrayList
        User user = getItem(position);

        String formattedDate;

        if (view == null) {
            // If the view is null (not yet created), inflate it from the list_item layout
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        // Find and bind the TextView elements in the list_item layout
        TextView listName = view.findViewById(R.id.listName);
        TextView listStatus = view.findViewById(R.id.apt_status);
        TextView listRole = view.findViewById(R.id.listUserInfo);
        ImageView listImage = view.findViewById(R.id.listImage);

        // Set the text of listName TextView to the user's first name
        listName.setText(user.getFirstName() + " " + user.getLastName().charAt(0));

        // Determine and set the role of the user (Doctor or Patient)
        if (user instanceof Doctor) {
            listRole.setText("Doctor");

        } else if (user instanceof Patient) {
            // Simplify the date format of the user
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            if (user.getRegistrationDate() != null) {
                formattedDate = dateFormat.format(user.getRegistrationDate());
            } else {
                formattedDate = "Unknown";
            }

            // Checks if patient has appointment (only for doctor view)
            if (((Patient) user).getAppointments() != null &&
                    ((Patient) user).getAppointments().size() != 0) {
                formattedDate = dateFormat.format(((Patient) user).getAppointments().get(0).getStartTime());
                listStatus.setVisibility(View.VISIBLE);
                listStatus.setText(((Patient) user).getAppointments().get(0).getStatus());//Need to change in future(possibly)
                listRole.setText(formattedDate);
            } else {
                listRole.setText("Patient");
            }
            listImage.setImageResource(R.drawable.customer);
        }

        // Return the customized view for the list item
        return view;
    }
}
