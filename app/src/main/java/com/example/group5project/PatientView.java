package com.example.group5project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PatientView extends AppCompatActivity {
    Button appointmentBtn, logout;
    TextView userDetail;
    Bundle extras;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_view);

        appointmentBtn = findViewById(R.id.patientApt_btn);
        logout = findViewById(R.id.logout_btn);
        userDetail = findViewById(R.id.patient_user_details);

        extras =getIntent().getExtras();

        userDetail.setText("Welcome!");

        appointmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientView.this, InboxActivity.class);
                intent.putExtra("inboxType", "patientApt");
                intent.putExtra("name", extras.getString("name"));
                startActivity(intent);
                finish();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log the user out and navigate to the Login activity
                Intent intent = new Intent(PatientView.this, Login.class);
                Toast.makeText(PatientView.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });
    }
}