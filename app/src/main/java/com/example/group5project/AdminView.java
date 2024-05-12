package com.example.group5project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AdminView extends AppCompatActivity {
    Button inbox, logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view);

        // Initialize UI elements
        inbox = findViewById(R.id.current_inbox_btn);
        logout = findViewById(R.id.btn_logout);

        // Set click listeners for the buttons
        inbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the CurrentInbox activity
                Intent intent = new Intent(AdminView.this, InboxActivity.class);
                intent.putExtra("inboxType", "currentInbox");
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log the user out and navigate to the Login activity
                Intent intent = new Intent(AdminView.this, Login.class);
                Toast.makeText(AdminView.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });

    }
}


