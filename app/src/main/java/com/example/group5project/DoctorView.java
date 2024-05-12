package com.example.group5project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DoctorView extends AppCompatActivity {
    Button appointment, shifts, logout;
    TextView userName;

    Bundle extras;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_view);

        appointment = findViewById(R.id.appointment_btn);
        shifts = findViewById(R.id.shift_btn);
        logout = findViewById(R.id.doc_logout_btn);
        userName = findViewById(R.id.doc_user_details);

        extras =getIntent().getExtras();

        userName.setText("Welcome " + extras.getString("name"));

        appointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorView.this, InboxActivity.class);
                intent.putExtra("inboxType", "upcomingApt");
                startActivity(intent);
                finish();
            }
        });
        shifts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorView.this, InboxActivity.class);
                intent.putExtra("inboxType", "shifts");
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log the user out and navigate to the Login activity
                Intent intent = new Intent(DoctorView.this, Login.class);
                Toast.makeText(DoctorView.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });
    }
}