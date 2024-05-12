package com.example.group5project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.group5project.Appointment;
import com.example.group5project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AppointmentDetail extends AppCompatActivity {

    private ImageView userIcon;
    private TextView aptTitle, doctorName, doctorEmail, doctorPhoneNumber, appointmentId, appointmentStartTime, appointmentEndTime, backBtn, startDateText, status;
    private RatingBar ratingBar;
    private Button cancelButton, bookButton;
    private LinearLayout aptDetail, startDate;
    String inboxType;
    private DatabaseReference mDatabase;
    Date startTime, endTime;
    Shift shift;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        // Initialize variables by finding views in the layout
        userIcon = findViewById(R.id.user_icon);
        aptTitle = findViewById(R.id.apt_title);
        doctorName = findViewById(R.id.doctor_name);
        doctorEmail = findViewById(R.id.doctor_email);
        doctorPhoneNumber = findViewById(R.id.doctor_phone_number);
        appointmentId = findViewById(R.id.appointment_id);
        appointmentStartTime = findViewById(R.id.appointment_start_time);
        appointmentEndTime = findViewById(R.id.appointment_end_time);
        status = findViewById(R.id.appointment_status);

        ratingBar = findViewById(R.id.rating_bar);
        cancelButton = findViewById(R.id.cancel_button);
        bookButton = findViewById(R.id.book_button);
        backBtn =findViewById(R.id.detail_backBtn);

        aptDetail = findViewById(R.id.apt_id_text);
        startDate = findViewById(R.id.start_date);
        startDateText = findViewById(R.id.apt_start_date);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        if (intent != null) {
             inboxType= intent.getStringExtra("inboxType");

            // Extract values from intent
            if (intent.getStringExtra("inboxType").equals("patientApt")){
                if (intent.hasExtra("firstName")){
                    aptDetail.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.GONE);
                    startDate.setVisibility(View.VISIBLE);
                    bookButton.setVisibility(View.VISIBLE);
                    String firstName = intent.getStringExtra("firstName");
                    String lastName = intent.getStringExtra("lastName");
                    String phoneName = intent.getStringExtra("phoneNum");
                    String email = intent.getStringExtra("email");

                    shift = getIntent().getParcelableExtra("shift");

                    aptTitle.setText(firstName + " " + lastName);
                    doctorName.setText(firstName + " " + lastName);
                    doctorEmail.setText(email);
                    doctorPhoneNumber.setText(phoneName);
                }
                else{
                    String aptID = intent.getStringExtra("aptID");
                    appointmentId.setText(aptID);
                }
                //getPatient();
                position = intent.getIntExtra("position", 0);
                long startTimeLong = intent.getLongExtra("startTime", 0);
                long endTimeLong = intent.getLongExtra("endTime", 0);

                // Convert and set text for start time
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy HH:mm", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.CANADA);

                startTime = new Date(startTimeLong);

                startDateText.setText(dateFormat.format(startTime));
                appointmentStartTime.setText(timeFormat.format(startTime));

                // Convert and set text for end time
                endTime = new Date(endTimeLong);
                appointmentEndTime.setText(timeFormat.format(endTime));
            }
            else {

            }
        }
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(AppointmentDetail.this, InboxActivity.class);
                if (bookButton.getVisibility() == View.VISIBLE){
                    intent  = new Intent(AppointmentDetail.this, AppointmentCreation.class);
                }
                intent.putExtra("inboxType", inboxType);
                startActivity(intent);
                finish();
            }
        });
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookAppointment();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calculate 60 minutes before startTime
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startTime);
                calendar.add(Calendar.MINUTE, -60);
                Date sixtyMinutesBeforeStartTime = calendar.getTime();

                // Check if the current time is within 60 minutes before startTime
                if ((new Date()).after(sixtyMinutesBeforeStartTime)) {
                    // Display an error message to the user
                    Toast.makeText(AppointmentDetail.this, "Cannot cancel an appointment starting in less than 60 minutes", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void bookAppointment() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User is not signed in
            return;
        }

        String currentUserId = currentUser.getUid();
        DatabaseReference patientRef = mDatabase.child("Approved").child("Patient").child(currentUserId);

        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Patient patient = snapshot.getValue(Patient.class);
                    if (patient != null) {
                        if(patient.setAppointment(startTime, endTime)){
                            Intent intent  = new Intent(AppointmentDetail.this, AppointmentCreation.class);
                            intent.putExtra("inboxType", inboxType);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            // Display an error message to the user
                            Toast.makeText(AppointmentDetail.this, "Appointment conflicts or cannot add a appointment for a past date.", Toast.LENGTH_SHORT).show();
                        }


                        // Remove the ValueEventListener to avoid listening to future changes
                        patientRef.removeEventListener(this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }

    public void getDoctor(String doctorID) {

        DatabaseReference patientRef = mDatabase.child("Approved").child("Doctor").child(doctorID);

        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        aptTitle.setText("Dr. " + doctor.getFirstName() + " " + doctor.getLastName().charAt(0));
                        // Remove the ValueEventListener to avoid listening to future changes
                        patientRef.removeEventListener(this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }
    public void getPatient() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User is not signed in
            return;
        }

        String currentUserId = currentUser.getUid();
        DatabaseReference patientRef = mDatabase.child("Approved").child("Patient").child(currentUserId);

        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Patient patient = snapshot.getValue(Patient.class);
                    if (patient != null) {
                        if (patient.getAppointments().get(position).getAssignedPatient() == null || patient.getAppointments().get(position).getAssignedPatient().equals("")){
                            status.setText("pending");
                        }
                        else{
                            status.setText("approved");
                        }

                        if (status.getText().toString().equals("approved")){
                            getDoctor(patient.getAppointments().get(position).getAssignedDoctor());
                        }



                        // Remove the ValueEventListener to avoid listening to future changes
                        patientRef.removeEventListener(this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }

}
