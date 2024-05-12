package com.example.group5project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class RegisterDoctor extends AppCompatActivity {
    private FirebaseAuth mAuth;

    // Initialize Doctor information's
    TextInputEditText editTextFirstName, editTextLastName, editTextPhone, editTextAddress, editTextEmployeeNum, editTextEmail, editTextPassword;

    private DatabaseReference mDatabase;
    TextInputLayout passwordBox;
    Button confirmReg;
    ProgressBar progressBar;
    TextView textView;

    // Specialties variables
    MaterialCardView selectCard;
    TextView tvSpecialties;
    boolean[] selectedSpecialties;
    ArrayList<Integer> specialtiesList = new ArrayList<>();
    String[] specialtiesArray = {
            "Cardiologist",
            "Dermatologist",
            "Ophthalmologist",
            "Orthopedic Surgeon",
            "Pediatrician",
            "Neurologist"
    };
    String[] chosenSpecialties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_doctor);

        // Assign the information's of the Doctor user (by ID)
        editTextFirstName = findViewById(R.id.firstName);
        editTextLastName = findViewById(R.id.lastName);
        editTextPhone = findViewById(R.id.phoneNumber);
        editTextAddress = findViewById(R.id.address);
        editTextEmployeeNum = findViewById(R.id.employee_num);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        passwordBox = findViewById(R.id.password_box);

        // Set UI elements to their respective IDs
        confirmReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        // Initialize specialties variables
        selectCard = findViewById(R.id.selectCard);
        tvSpecialties = findViewById(R.id.specialties);
        selectedSpecialties = new boolean[specialtiesArray.length];

        // Display on-screen box that allows the doctor to choose their specialties
        selectCard.setOnClickListener(v -> {
            showSpecialtiesDialog();
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterDoctor.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        confirmReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password, phoneNumber, employeeNumber;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                phoneNumber = String.valueOf(editTextPhone.getText());
                employeeNumber = String.valueOf(editTextEmployeeNum.getText());
                mAuth = FirebaseAuth.getInstance();
                progressBar.setVisibility(View.VISIBLE);

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterDoctor.this, "Enter an email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterDoctor.this, "Enter a password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isPasswordValid(password)){
                    passwordBox.setError("Invalid password");
                    Toast.makeText(RegisterDoctor.this, "Password is not valid.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (!isEmailValid(email)) {
                    Toast.makeText(RegisterDoctor.this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isPhoneNumberValid(phoneNumber)) {
                    Toast.makeText(RegisterDoctor.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isPasswordValid(password)) {
                    passwordBox.setError("Invalid password");
                    Toast.makeText(RegisterDoctor.this, "Password is not valid.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (!isEmployeeNumberValid(employeeNumber)) {
                    Toast.makeText(RegisterDoctor.this, "Enter a valid employee number", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    assert user != null;
                                    String userUID = user.getUid();

                                    writeNewDoctor(String.valueOf(editTextFirstName.getText()),
                                            String.valueOf(editTextLastName.getText()),
                                            String.valueOf(editTextPhone.getText()),
                                            String.valueOf(editTextAddress.getText()),
                                            String.valueOf(editTextEmployeeNum.getText()),
                                            String.valueOf(editTextEmail.getText()),
                                            chosenSpecialties,
                                            userUID
                                    );

                                    Toast.makeText(RegisterDoctor.this, "Account created",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterDoctor.this, MainActivity.class);
                                    intent.putExtra("role", "Doctor");
                                    intent.putExtra("name", String.valueOf(editTextFirstName.getText()));
                                    intent.putExtra("status", "Pending");
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(RegisterDoctor.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void writeNewDoctor(String firstName, String lastName, String phone, String address, String employeeNum, String email, String[] specialties, String uID) {
        Doctor doctor = new Doctor(firstName, lastName, phone, address, employeeNum, email, Arrays.asList(specialties), new Date(), uID);
        mDatabase.child("Doctor").child(uID).setValue(doctor);
    }

    public void showSpecialtiesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterDoctor.this);
        builder.setTitle("Select specialties");
        builder.setCancelable(false);

        builder.setMultiChoiceItems(specialtiesArray, selectedSpecialties, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    specialtiesList.add(which);
                } else {
                    specialtiesList.remove(which);
                }
            }
        }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chosenSpecialties = new String[specialtiesList.size()];
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < specialtiesList.size(); i++) {
                    stringBuilder.append(specialtiesArray[specialtiesList.get(i)]);
                    chosenSpecialties[i] = specialtiesArray[specialtiesList.get(i)];

                    if (i != specialtiesList.size() - 1) {
                        stringBuilder.append(", ");
                    }
                    tvSpecialties.setText(stringBuilder.toString());
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNeutralButton("Clear all", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < selectedSpecialties.length; i++) {
                    selectedSpecialties[i] = false;
                    specialtiesList.clear();
                    tvSpecialties.setText("");
                }
            }
        });
        builder.show();
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
    public boolean isEmployeeNumberValid(String employeeNumber) {
        // You can customize the employee number validation based on your requirements
        // This example checks for non-empty, alphanumeric, and a maximum length of 8 characters
        return !TextUtils.isEmpty(employeeNumber) && employeeNumber.matches("[A-Za-z0-9]+") && employeeNumber.length() <= 8;
    }

}