package com.example.group5project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.group5project.databinding.ActivityInboxBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import nl.bryanderidder.themedtogglebuttongroup.ThemedButton;
import nl.bryanderidder.themedtogglebuttongroup.ThemedToggleButtonGroup;

public class InboxActivity extends AppCompatActivity {
    ActivityInboxBinding binding;
    ArrayList<User> dataArrayList = new ArrayList<>();
    ArrayList<Shift> shiftArrayList = new ArrayList<>();
    ArrayList<Appointment> aptArrayList = new ArrayList<>();
    ListAdapter listAdapter;
    ShiftListAdapter shiftListAdapter;
    AppointmentListAdapter aptListAdapter;
    private DatabaseReference mDatabase;
    String inboxType;

    TextView backBtn, inboxTypeText;
    Button newShiftBtn, newAptBtn;
    ThemedToggleButtonGroup themedButtonGroup;
    ThemedButton approveAllBtn, pastAptBtn, rejectedBtn, upcomingBtn;

    SimpleDateFormat timeFormat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInboxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mDatabase = FirebaseDatabase.getInstance().getReference();
        backBtn = findViewById(R.id.inbox_backBtn);
        inboxTypeText = findViewById(R.id.inbox_type);

        themedButtonGroup = findViewById(R.id.option_bar);
        approveAllBtn = findViewById(R.id.approve_all);
        pastAptBtn = findViewById(R.id.past_apt);
        rejectedBtn = findViewById(R.id.rejected_apt_btn);
        upcomingBtn = findViewById(R.id.upcoming_apt_btn);

        newShiftBtn = findViewById(R.id.new_shift_btn);
        newAptBtn = findViewById(R.id.new_apt);

        // Create a SimpleDateFormat with the desired date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        timeFormat = new SimpleDateFormat("h:mm a", Locale.CANADA);

        // Gets the type of inbox to display
        Intent intent = this.getIntent();
        inboxType = intent.getStringExtra("inboxType");


        if (!inboxType.equals("upcomingApt") && inboxType != null){
            approveAllBtn.setVisibility(View.GONE);
            pastAptBtn.setVisibility(View.GONE);
        }
        dataToArrayList();



        themedButtonGroup.setOnSelectListener((ThemedButton btn) -> {
            // handle selected button
            if (btn.getId() == R.id.rejected_apt_btn){
                dataArrayList.clear();
                if (inboxType.equals("upcomingApt")){
                    handleUpcomingApt("rejected", false);
                }
                else {
                    inboxType = "rejectedInbox";
                    // Populate the dataArrayList with user data
                    dataToArrayList();
                }

            }
            else if (btn.getId() == R.id.upcoming_apt_btn){
                dataArrayList.clear();
                if (inboxType.equals("upcomingApt")){
                    handleUpcomingApt("pending", false);
                } else if (inboxType.equals("patientApt")) {
                    dataArrayList.clear();
                    handlePatientApt("upcoming");
                } else {
                    inboxType = "currentInbox";
                    // Populate the dataArrayList with user datay
                    dataToArrayList();
                }
            }
            else if (btn.getId() == R.id.approve_all){
                dataArrayList.clear();
                if (inboxType.equals("upcomingApt")){
                    handleUpcomingApt("pending", true);
                }
                if (dataArrayList.size() != 0){
                    Toast.makeText(InboxActivity.this, "Approved all appointments successfully", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                dataArrayList.clear();
                if (inboxType.equals("upcomingApt")){
                    handleUpcomingApt("past", false);
                } else if (inboxType.equals("patientApt")) {
                    handlePatientApt("past");
                }
            }
            return kotlin.Unit.INSTANCE;
        });

        // Create and set the custom ListAdapter for the ListView
        listAdapter = new ListAdapter(InboxActivity.this, dataArrayList);
        shiftListAdapter = new ShiftListAdapter(InboxActivity.this, shiftArrayList);
        aptListAdapter = new AppointmentListAdapter(InboxActivity.this, aptArrayList);
        if (inboxType.equals("shifts")){
            binding.listview.setAdapter(shiftListAdapter);
        }
        else if (inboxType.equals("patientApt")){
            binding.listview.setAdapter(aptListAdapter);
        }
        else {
            binding.listview.setAdapter(listAdapter);
        }
        binding.listview.setClickable(true);

        // Define the item click listener for the ListView
        binding.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create an intent to navigate to the UserDetail activity
                Intent intent;
                Log.e("inboxtype", inboxType);

                if (!inboxType.equals("shifts")){
                    intent = new Intent(InboxActivity.this, UserDetail.class);
                }
                else {
                    intent = new Intent(InboxActivity.this, ShiftInbox.class);
                }
                if (inboxType.equals("patientApt")) {
                    Log.e("inboxtype", "True");
                    intent = new Intent(InboxActivity.this, AppointmentDetail.class);
                    if (inboxType.equals("patientApt")){
                        Appointment apt = aptArrayList.get(position);
                        String aptID = apt.getAppointmentID();

                        /*
                        Doctor assignedDoctor = apt.getAssignedDoctor();

                        String firstName = assignedDoctor.getFirstName();
                        String lastName = assignedDoctor.getLastName();
                        String number = assignedDoctor.getPhone();



                        intent.putExtra("firstName", firstName);
                        intent.putExtra("lastName", lastName);
                        intent.putExtra("phoneNum", number);

                         */



                        intent.putExtra("aptID", aptID);
                        intent.putExtra("startTime", apt.getStartTime().getTime());
                        intent.putExtra("endTime", apt.getEndTime().getTime());
                        intent.putExtra("inboxType", inboxType);
                    }
                    else {
                        intent.putExtra("firstName", dataArrayList.get(position).getFirstName());
                        intent.putExtra("lastName", dataArrayList.get(position).getLastName());
                        intent.putExtra("phone", dataArrayList.get(position).getPhone());
                    }

                    intent.putExtra("name", intent.getStringExtra("name"));
                    intent.putExtra("position", position);
                    startActivity(intent);
                    finish();

                }

                else if (!inboxType.equals("shifts") && !inboxType.equals("patientApt")){
                    // Format the registration date and pass user data as extras in the intent
                    String formattedDate;

                    if (dataArrayList.get(position).getRegistrationDate() != null){
                        formattedDate = dateFormat.format(dataArrayList.get(position).getRegistrationDate());
                    }
                    else{
                        formattedDate = "Unknown";
                    }
                    intent.putExtra("firstName", dataArrayList.get(position).getFirstName());
                    intent.putExtra("lastName", dataArrayList.get(position).getLastName());
                    intent.putExtra("phone", dataArrayList.get(position).getPhone());
                    intent.putExtra("address", dataArrayList.get(position).getAddress());
                    intent.putExtra("email", dataArrayList.get(position).getEmail());
                    intent.putExtra("registration", formattedDate);
                    intent.putExtra("id", dataArrayList.get(position).getId());
                    intent.putExtra("inboxType",inboxType);
                    intent.putExtra("position",position);
                    if (dataArrayList.get(position).getClass() == Doctor.class) {
                        Doctor docFromList = (Doctor) dataArrayList.get(position);

                        // Convert the list of specialties into a comma-separated string
                        StringBuilder result = new StringBuilder();
                        for (int i = 0; i < docFromList.getSpecialties().size(); i++) {
                            result.append(docFromList.getSpecialties().get(i));
                            if (i < docFromList.getSpecialties().size() - 1) {
                                result.append(", ");
                            }
                        }
                        String concatenatedString = result.toString();

                        intent.putExtra("employeeNum", docFromList.getEmployeeNum());
                        intent.putExtra("specialities", concatenatedString);
                        intent.putExtra("role", "Doctor");
                    } else {
                        Patient docFromList = (Patient) dataArrayList.get(position);
                        intent.putExtra("healthCard", docFromList.getHealthCard());
                        intent.putExtra("role", "Patient");
                    }
                }
                else{
                    intent = new Intent(InboxActivity.this, ShiftInbox.class);

                    intent.putExtra("startDate",shiftArrayList.get(position).getStartTime().getTime());
                    intent.putExtra("endDate",shiftArrayList.get(position).getEndTime().getTime());
                    intent.putExtra("shiftDate", shiftArrayList.get(position).getStartTime().getTime());

                }

                startActivity(intent);
                finish(); // Finish the current activity
            }
        });

        // Define the click listener for the back button
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = intent.getStringExtra("name");
                // Navigate back to the AdminView activity
                Intent intent = new Intent(InboxActivity.this, AdminView.class);
                if (inboxType.equals("upcomingApt")){
                    intent = new Intent(InboxActivity.this, DoctorView.class);
                }
                else if (inboxType.equals("shifts")){
                    intent = new Intent(InboxActivity.this, DoctorView.class);
                }
                else if (inboxType.equals("patientApt")){
                    intent = new Intent(InboxActivity.this, PatientView.class);
                }
                intent.putExtra("name", name);
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });
        newShiftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InboxActivity.this, ShiftInbox.class);
                startActivity(intent);
                finish();

            }
        });

        newAptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InboxActivity.this, AppointmentCreation.class);
                startActivity(intent);
                finish();
            }
        });

    }


    private void dataToArrayList(){
        if (inboxType.equals("currentInbox")){
            approveAllBtn.setVisibility(View.GONE);
            pastAptBtn.setVisibility(View.GONE);
            inboxTypeText.setText("Current Inbox");
            handleCurrentInbox();
        } else if (inboxType.equals("rejectedInbox")) {
            approveAllBtn.setVisibility(View.GONE);
            pastAptBtn.setVisibility(View.GONE);
            inboxTypeText.setText("Rejected Inbox");
            handleRejectedInbox();
        } else if (inboxType.equals("upcomingApt")) {
            inboxTypeText.setText("Upcoming Appointments");
            handleUpcomingApt("", false);
        } else if (inboxType.equals("shifts")) {
            inboxTypeText.setText("Shifts Inbox");
            themedButtonGroup.setVisibility(View.GONE);
            newShiftBtn.setVisibility(View.VISIBLE);
            handleShifts();
        }
        else if (inboxType.equals("patientApt")) {
            dataArrayList.clear();
            inboxTypeText.setText("Appointments");
            newShiftBtn.setVisibility(View.GONE);
            pastAptBtn.setVisibility(View.VISIBLE);
            rejectedBtn.setVisibility(View.GONE);
            newAptBtn.setVisibility(View.VISIBLE);
            handlePatientApt("upcoming");

        }
    }

    private void handleCurrentInbox(){
        DatabaseReference doctorRef = mDatabase.child("Doctor");
        doctorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Doctor doctor = userSnapshot.getValue(Doctor.class);
                    dataArrayList.add(doctor);
                }
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors
            }
        });
        Log.e("dataToArray", "Pass the doctor part in data to array");
        DatabaseReference patientRef = mDatabase.child("Patient");
        patientRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Log.e("dataToArray", "Getting user");
                    Patient patient = userSnapshot.getValue(Patient.class);
                    Log.e("dataToArray", "setted user");
                    dataArrayList.add(patient);
                    Log.e("dataToArray", "added user");
                }
                listAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors
            }
        });
        Log.e("dataToArray", "Pass the patient part in data to array");
    }

    private void handleRejectedInbox(){
        //Set a reference to the rejected users from the database
        DatabaseReference doctorRef = mDatabase.child("Rejected").child("Doctor");
        doctorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot: snapshot.getChildren()) {
                    Doctor doctor =  userSnapshot.getValue(Doctor.class);
                    dataArrayList.add(doctor);
                }
                listAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference patientRef = mDatabase.child("Rejected").child("Patient");
        patientRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot: snapshot.getChildren()) {
                    Patient patient =  userSnapshot.getValue(Patient.class);
                    dataArrayList.add(patient);
                }
                listAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void handleUpcomingApt(String type, boolean approveAll) {
        Log.e("patientInbox", type);
        // Set a reference to the rejected users from the database
        DatabaseReference patientRef = mDatabase.child("Approved").child("Patient");
        patientRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataArrayList.clear(); // Clear the list before populating

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.hasChild("appointments")) {
                        Patient patient = userSnapshot.getValue(Patient.class);

                        for (int i = 0; i < patient.getAppointments().size(); i++) {
                            Appointment appointment = patient.getAppointments().get(i);
                            // Check the appointment status based on the provided type
                            boolean isPending = appointment.getStatus().equals("pending");
                            boolean isApproved = appointment.getStatus().equals("approved");
                            boolean isRejected = appointment.getStatus().equals("rejected");
                            boolean isPast = appointment.getEndTime().before(new Date());

                            if ((type.equals("pending") && (isPending || isApproved)) ||
                                    (type.equals("rejected") && isRejected) ||
                                    (type.equals("past") && isPast) ||
                                    (type.equals("") && (isApproved || isPending))) {
                                dataArrayList.add(patient);
                            }

                            if (approveAll && isPending) {
                                // Update the status of the appointment in the database
                                DatabaseReference appointmentRef = userSnapshot.child("appointments").getRef().child(String.valueOf(i));
                                appointmentRef.child("status").setValue("approved");
                                appointment.setStatus("approved");
                            }
                        }
                    }
                }

                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }


    private void handleShifts() {
        // Get the currently signed-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User is not signed in
            return;
        }
        // Get the UID of the currently signed-in doctor
        String currentUserId = currentUser.getUid();

        // Set a reference to the shifts of the current doctor in the database
        DatabaseReference shiftsRef = mDatabase.child("Approved").child("Doctor").child(currentUserId).child("shifts");
        shiftsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shiftArrayList.clear(); // Clear the list before adding shifts

                if (snapshot.exists()) {
                    for (DataSnapshot shiftSnapshot : snapshot.getChildren()) {
                        Shift shift = shiftSnapshot.getValue(Shift.class);
                        if (shift != null) {
                            // Add each shift to the list
                            shiftArrayList.add(shift);
                        }
                    }
                }

                shiftListAdapter.notifyDataSetChanged();
                Log.e("DocShift", "Added shifts for the current doctor");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }

    public void handlePatientApt(String type) {
        Log.e("patientInbox", type);
        // Get the currently signed-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User is not signed in
            return;
        }
        // Get the UID of the currently signed-in patient
        String currentUserId = currentUser.getUid();
        DatabaseReference patientRef = mDatabase.child("Approved").child("Patient").child(currentUserId).child("appointments");
        patientRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    aptArrayList.clear();

                    for (DataSnapshot aptsSnapshot : snapshot.getChildren()) {
                        Appointment appointment = aptsSnapshot.getValue(Appointment.class);

                        if (appointment != null) {
                            Log.e("AptArray", "appointment start date: " + appointment.getStartTime());
                            // Check if the appointment is past
                            boolean isPast = appointment.getEndTime().before(new Date());

                            if (type.equals("past") && isPast) {
                                aptArrayList.add(appointment);
                            } else if (!type.equals("past") && !isPast) {
                                // If the type is not "past" and the appointment is not past, add to the list
                                aptArrayList.add(appointment);
                            }
                        } else {
                            Log.e("AptArray", "appointment was null");
                        }
                    }
                    // Call the method that uses aptArrayList here
                    useAptArrayList();
                }
                aptListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void useAptArrayList() {
        // Use aptArrayList here
        Log.e("AptArray", "appointments size: " + String.valueOf(aptArrayList.size()));
        if (!aptArrayList.isEmpty()) {
            Log.e("AptArray", "appointment start date after handling: " + String.valueOf(aptArrayList.get(0).getStartTime()));
        }
        aptListAdapter.notifyDataSetChanged();
    }






}