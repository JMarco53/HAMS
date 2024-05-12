package com.example.group5project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class RegisterPatient extends AppCompatActivity {

    TextInputEditText editTextFirstName, editTextLastName, editTextPhone, editTextAddress, editTextHealthCardNum, editTextEmail, editTextPassword;
    TextInputLayout passwordBox;
    private DatabaseReference mDatabase;
    Button confirmReg;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI elements
        editTextFirstName = findViewById(R.id.firstName);
        editTextLastName = findViewById(R.id.lastName);
        editTextPhone = findViewById(R.id.phoneNumber);
        editTextAddress = findViewById(R.id.address);
        editTextHealthCardNum = findViewById(R.id.healthCard);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        passwordBox = findViewById(R.id.password_box);

        confirmReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        // Handle the "Login Now" link
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterPatient.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Handle user registration
        confirmReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password, phoneNumber, healthNumber;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                phoneNumber = String.valueOf(editTextPhone.getText());
                healthNumber = String.valueOf(editTextHealthCardNum.getText());
                mAuth = FirebaseAuth.getInstance();
                progressBar.setVisibility(View.VISIBLE);

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterPatient.this, "Enter an email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterPatient.this, "Enter a password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isPasswordValid(password)){
                    passwordBox.setError("Invalid password");
                    Toast.makeText(RegisterPatient.this, "Password is not valid.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (!isEmailValid(email)) {
                    Toast.makeText(RegisterPatient.this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isPhoneNumberValid(phoneNumber)) {
                    Toast.makeText(RegisterPatient.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isHealthCardNumberValid(healthNumber)) {
                    Toast.makeText(RegisterPatient.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a new user (Doctor) in the Firebase authentication database
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String userUID = user.getUid();

                                    // Write Patient information into the database
                                    writeNewPatient(String.valueOf(editTextFirstName.getText()),
                                            String.valueOf(editTextLastName.getText()),
                                            String.valueOf(editTextPhone.getText()),
                                            String.valueOf(editTextAddress.getText()),
                                            String.valueOf(editTextHealthCardNum.getText()),
                                            String.valueOf(editTextEmail.getText()),
                                            userUID
                                    );

                                    // Show a success message and start MainActivity
                                    Toast.makeText(RegisterPatient.this, "Account created",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.putExtra("role", "Patient");
                                    intent.putExtra("name", String.valueOf(editTextFirstName.getText()));
                                    intent.putExtra("status", "Pending");
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Show an error message if account registration failed
                                    Toast.makeText(RegisterPatient.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Initialize the Firebase Realtime Database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // Writes registered patient's information into the database
    public void writeNewPatient(String firstName, String lastName, String phone, String address, String healthCard, String email, String uID) {
        Patient patient = new Patient(firstName, lastName, phone, address, healthCard, email, new Date(), uID);
        String name = String.valueOf(firstName + " " + lastName);

        mDatabase.child("Patient").child(uID).setValue(patient);
    }
    public static boolean isPasswordValid(String password) {
        // Minimum length requirement
        if (password.length() < 6) {
            return false;
        }
        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        // If all requirements are met, return true
        return true;
    }

    public boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isPhoneNumberValid(String phoneNumber) {
        // You can customize the phone number validation based on your requirements
        // This is a simple example checking for digits and length
        return phoneNumber.matches("\\d{10}");

    }
    public static boolean isHealthCardNumberValid(String healthCardNumber) {
        // Check for non-empty and alphanumeric
        if (TextUtils.isEmpty(healthCardNumber) || !healthCardNumber.matches("[A-Za-z0-9]+")) {
            return false;
        }

        // Check if the length is exactly 8 characters
        if (healthCardNumber.length() != 8) {
            return false;
        }

        // If all requirements are met, return true
        return true;
    }

}