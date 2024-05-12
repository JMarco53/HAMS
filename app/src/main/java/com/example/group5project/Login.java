package com.example.group5project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    TextInputLayout passwordBox, emailBox;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button switchLayout = findViewById(R.id.button_reg);
        Button loginNow = findViewById(R.id.btn_login);

        editTextEmail = findViewById(R.id.email_log);
        editTextPassword = findViewById(R.id.password_log);
        progressBar = findViewById(R.id.progressBar);

        passwordBox = findViewById(R.id.pass_box);
        emailBox = findViewById(R.id.email_box);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        /*
        On the click of the register button, set the activity/layout
        to the user choice page
         */
        switchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, UserChoice.class);
                startActivity(intent);
            }
        });

        loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                mAuth = FirebaseAuth.getInstance();
                emailBox.setError(null);
                passwordBox.setError(null);
                //Send Error message when fields are empty
                if (TextUtils.isEmpty(email)){
                    emailBox.setError("Email is empty");
                    Toast.makeText(Login.this, "Enter an email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)){
                    passwordBox.setError("Password is empty");
                    Toast.makeText(Login.this, "Enter a password", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();//Current user
                                    String userUID = user.getUid();//User ID
                                    Toast.makeText(getApplicationContext(), "Login Successful.",
                                            Toast.LENGTH_SHORT).show();
                                    checkUserRole(user);
                                }
                                else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Login.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
    private void checkUserRole(FirebaseUser user) {
        DatabaseReference status;
        String[] roles = {"Patient", "Doctor", "Administrator"};
        String[] statuses = {"Approved", "Rejected"};

        /*
        Loops over the database to find the name, role, and status of the user that just logged in.
         */
        for (String stat : statuses) {
            status = mDatabase.child(stat);

            for (String role : roles) {
                DatabaseReference roleRef = status.child(role);
                roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user.getUid())) {
                            if (role.equals("Administrator")){
                                Intent intent = new Intent(Login.this, AdminView.class);
                                String email = dataSnapshot.child(user.getUid()).child("email").getValue(String.class);
                                intent.putExtra("name", email);
                                startActivity(intent);
                                finish();
                                return;
                            }
                            String firstName = dataSnapshot.child(user.getUid()).child("firstName").getValue(String.class);
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            intent.putExtra("name", firstName);
                            intent.putExtra("role", role);
                            intent.putExtra("status", stat);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle database errors if necessary
                    }
                });
            }
        }

        // If the user is not found in either "Approved" or "Rejected", check under the role parents
        for (String role : roles) {
            DatabaseReference roleRef = mDatabase.child(role);
            roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(user.getUid())) {
                        String firstName = dataSnapshot.child(user.getUid()).child("firstName").getValue(String.class);
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        intent.putExtra("name", firstName);
                        intent.putExtra("role", role);
                        intent.putExtra("status", "pending");
                        startActivity(intent);
                        finish();
                        return;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle database errors if necessary
                }
            });
        }
    }




}