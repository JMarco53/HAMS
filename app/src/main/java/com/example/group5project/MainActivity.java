package com.example.group5project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private Button button;
    private TextView userName, userStatus;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = auth.getCurrentUser();

        extras = getIntent().getExtras();

        // Initialize UI elements
        button = findViewById(R.id.btn_logout);
        userName = findViewById(R.id.user_details);
        userStatus = findViewById(R.id.user_role);

        if (user == null) {
            // If the user is not authenticated, redirect to the Login activity
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        } else {
            loadUserData();
        }

        // Logout button click listener
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
    // Load user data and update the userName TextView
    private void loadUserData() {
        if (extras != null){
            String name = extras.getString("name");
            String status = extras.getString("status");
            String role = extras.getString("role");
            userName.setText("Welcome " + name);
            if (role.equals("Doctor") && status.equals("Approved")){
                Intent intent = new Intent(MainActivity.this, DoctorView.class);
                intent.putExtra("name", name);
                startActivity(intent);
                finish();
            }
            if (role.equals("Patient") && status.equals("Approved")){
                Intent intent = new Intent(MainActivity.this, PatientView.class);
                intent.putExtra("name", name);
                startActivity(intent);
                finish();
            }

            if (status.equals("Approved")){
                userStatus.setText("You're application has been approved.\nYou are signed in as a " + role);
            }
            else if (status.equals("Rejected")){
                userStatus.setText("You're application has been rejected.\nPlease contact the administrator by phone: 345-645-2323");
            }
            else{
                userStatus.setText("Your application is " + status + ".\nThe administrator hasn't made a decision yet. ");
            }

        }

    }


}