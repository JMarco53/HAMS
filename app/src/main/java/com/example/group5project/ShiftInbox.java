package com.example.group5project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ShiftInbox extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{
    CalendarView calendar;
    TextView dateView, startTimeText, endTimeText, backBtn;
    TimePickerDialog timePickerDialog;
    Button setStartTimeBtn, setEndTimeBtn, confirmBtn, removeBtn;
    Calendar selectedStartDateTime, selectedEndDateTime;
    String  currentStartDate, currentEndDate, formattedTime;
    Date selectedStartDate, selectedEndDate;
    SimpleDateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_inbox);


        //Initialize calendar
        calendar = findViewById(R.id.calendar);
        dateView = findViewById(R.id.date_view);

        //Initialize Buttons
        setStartTimeBtn = findViewById(R.id.start_time_dialog);
        setEndTimeBtn = findViewById(R.id.end_time_dialog);
        confirmBtn = findViewById(R.id.confirm_shift);
        backBtn = findViewById(R.id.shift_inbox_backBtn);
        removeBtn = findViewById(R.id.remove_shift);

        //Initialize TextView to set shift (start-end) time
        startTimeText = findViewById(R.id.start_time_text);
        endTimeText = findViewById(R.id.end_time_text);

        //Will store the value to the shift after date and times has been selected
        selectedStartDateTime = Calendar.getInstance();
        selectedEndDateTime = Calendar.getInstance();

        timeFormat = new SimpleDateFormat("h:mm a", Locale.CANADA);
        Intent intent = this.getIntent();

        if (intent != null){
            if (intent.hasExtra("startDate")){
                removeBtn.setVisibility(View.VISIBLE);
                confirmBtn.setVisibility(View.GONE);
                Date startTime = new Date(intent.getLongExtra("startDate", -1));
                Date endTime = new Date(intent.getLongExtra("endDate", -1));
                String formattedTime = timeFormat.format(startTime);
                startTimeText.setText(formattedTime);
                formattedTime = timeFormat.format(endTime);
                endTimeText.setText(formattedTime);

                // Set the selected date and time
                Date shiftDate = new Date(intent.getLongExtra("shiftDate", -1)) ;
                calendar.setDate(startTime.getTime());
                selectedStartDateTime.setTime(startTime);
                selectedEndDateTime.setTime(endTime);

                selectedStartDate = startTime;
                selectedEndDate = endTime;

                calendar.setEnabled(false);
                calendar.setClickable(false);
                setStartTimeBtn.setVisibility(View.GONE);
                setEndTimeBtn.setVisibility(View.GONE);
            }
            else {
                removeBtn.setVisibility(View.GONE);
                confirmBtn.setVisibility(View.VISIBLE);
                calendar.setEnabled(true);
                calendar.setClickable(true);
                //calendar.setDate(new Date().getTime());
            }


        }

        // Add Listener in calendar
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedStartDateTime.set(year, month, dayOfMonth);
                selectedEndDateTime.set(year, month, dayOfMonth);
                String Date = dayOfMonth + "-" + (month + 1) + "-" + year;
                selectedStartDate = selectedStartDateTime.getTime();
                selectedEndDate = selectedEndDateTime.getTime();
                Log.e("Adding", "Start date: " + String.valueOf(selectedStartDate) + " end date: " + String.valueOf(selectedEndDate));
                dateView.setText(Date);
                Log.e("CurrentText", "Current start text: " + String.valueOf(currentStartDate));
                Log.e("CurrentText", "Current end text: " + String.valueOf(currentEndDate));
                if (currentStartDate != null) {
                    if (!currentStartDate.equals("Not set") || !startTimeText.getText().toString().equals("Not set")) {
                        formattedTime = timeFormat.format(selectedStartDate);
                        startTimeText.setText(formattedTime);
                    }
                }
                if (currentEndDate != null) {
                    if (!currentEndDate.equals("Not set") || !endTimeText.getText().toString().equals("Not set")) {
                        formattedTime = timeFormat.format(selectedEndDate);
                        endTimeText.setText(formattedTime);
                    }
                }
            }
        });

        setStartTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker(true);
            }
        });

        setEndTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker(false);
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addingNewShift();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShiftInbox.this, InboxActivity.class);
                intent.putExtra("inboxType", "shifts");
                startActivity(intent);
                finish();
            }
        });
        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removingShift();
            }
        });

    }
    private void openTimePicker(boolean startTime) {
        Calendar now = Calendar.getInstance();
        currentStartDate = startTimeText.getText().toString();
        currentEndDate = endTimeText.getText().toString();

        timePickerDialog = TimePickerDialog.newInstance(ShiftInbox.this, now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE), false);
        if(startTime){
            timePickerDialog.setTitle("Set start time");
            startTimeText.setText("Not set");
        }
        else {
            timePickerDialog.setTitle("Set end time");
            endTimeText.setText("Not set");
        }
        timePickerDialog.setTimeInterval(1, 30, 60);
        timePickerDialog.show(getSupportFragmentManager(), "Time picker dialog");
        timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (startTime){
                    startTimeText.setText(currentStartDate);
                }
                else {
                    endTimeText.setText(currentEndDate);
                }
            }
        });
    }

    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        if (startTimeText.getText().equals("Not set")){
            selectedStartDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedStartDateTime.set(Calendar.MINUTE, minute);
            selectedStartDateTime.set(Calendar.SECOND, second);
            selectedStartDate = selectedStartDateTime.getTime();

            formattedTime = timeFormat.format(selectedStartDate);
            startTimeText.setText(formattedTime);
            Log.e("TimeSetStart", "Time has been set to " + selectedStartDate.toString());
        }
        else {
            selectedEndDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedEndDateTime.set(Calendar.MINUTE, minute);
            selectedEndDateTime.set(Calendar.SECOND, second);
            selectedEndDate = selectedEndDateTime.getTime();
            formattedTime = timeFormat.format(selectedEndDate);
            endTimeText.setText(formattedTime);
            Log.e("TimeSetEnd", "Time has been set to " + selectedEndDate.toString());
        }




    }

    public void addingNewShift() {
        // Check if start time and end time have been selected
        if (selectedStartDate == null || selectedEndDate == null) {
            // Display an error message to the user
            Toast.makeText(this, "Please select both start and end times", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            // Assuming your database reference is like this
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Assuming your "Approved/Doctor/" path is like this
            DatabaseReference doctorReference = databaseReference.child("Approved").child("Doctor").child(uid);

            doctorReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // dataSnapshot will contain your Doctor object
                    if (dataSnapshot.exists()) {
                        Doctor doctor = dataSnapshot.getValue(Doctor.class);
                        Log.e("Adding", "Start date for adding: " + String.valueOf(selectedStartDate) + " end date: " + String.valueOf(selectedEndDate));
                        if (doctor.addShift(selectedStartDate, selectedEndDate)) {
                            Log.e("Adding", "Start date for first shift: " + String.valueOf(doctor.getShifts().get(0).getStartTime()) + " end date: " + String.valueOf(doctor.getShifts().get(0).getStartTime()));
                            Toast.makeText(ShiftInbox.this, "Shift added successfully.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ShiftInbox.this, InboxActivity.class);
                            intent.putExtra("inboxType", "shifts");
                            startActivity(intent);
                            finish();
                        } else {
                            // Display an error message to the user
                            Toast.makeText(ShiftInbox.this, "Shift conflicts or cannot add a shift for a past date.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle the case where the Doctor data does not exist
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors here
                }
            });
        } else {
            Log.e("Adding Shift in Shift Inbox", "User not logged in.");
        }
    }

    public void removingShift(){
        // Check if start time and end time have been selected
        if (selectedStartDate == null || selectedEndDate == null) {
            // Display an error message to the user
            Toast.makeText(ShiftInbox.this, "Please select both start and end times", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            // Assuming your database reference is like this
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Assuming your "Approved/Doctor/" path is like this
            DatabaseReference doctorReference = databaseReference.child("Approved").child("Doctor").child(uid);

            doctorReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // dataSnapshot will contain your Doctor object
                    if (dataSnapshot.exists()) {
                        Doctor doctor = dataSnapshot.getValue(Doctor.class);
                        Log.e("Removing", "Start date: " + String.valueOf(selectedStartDate) + " end date: " + String.valueOf(selectedEndDate));
                        if (doctor.deleteShift(selectedStartDate, selectedEndDate)){
                            Intent intent = new Intent(ShiftInbox.this, InboxActivity.class);
                            intent.putExtra("inboxType", "shifts");
                            startActivity(intent);
                            finish();
                        }
                        else{
                            // Display an error message to the user
                            Toast.makeText(ShiftInbox.this, "Shift conflicts or cannot delete a shift. Current Shift could be related to a patient's appointment", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        // Handle the case where the Doctor data does not exist
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors here
                }
            });
        } else {
            Log.e("Removing Shift in Shift Inbox", "User not logged in.");
        }
    }

}
