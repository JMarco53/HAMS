package com.example.group5project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.group5project.databinding.ActivityUserDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.List;

public class UserDetail extends AppCompatActivity {
    ActivityUserDetailBinding binding;
    // Define member variables to store user information
    private String firstName, lastName, phone, address, email, employeeNum, specialties, registration, healthCard, id, role, inboxType;
    private int position;
    // Define UI elements
    TextView backBtn, employeeLabel, healthCardLabel, employeeText, healthText;
    Button approveBtn;

    Button rejectBtn;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Bind UI elements
        backBtn = findViewById(R.id.user_backBtn);
        approveBtn = findViewById(R.id.approve_btn);
        rejectBtn = findViewById(R.id.reject_btn);
        employeeLabel = findViewById(R.id.user_emp_label);
        healthCardLabel = findViewById(R.id.user_health_label);
        employeeText = findViewById(R.id.user_employee_num);
        healthText = findViewById(R.id.user_health_num);

        // Retrieve user details from the intent
        Intent intent = this.getIntent();
        if (intent != null) {
            firstName = intent.getStringExtra("firstName");
            lastName = intent.getStringExtra("lastName");
            phone = intent.getStringExtra("phone");
            address = intent.getStringExtra("address");
            email = intent.getStringExtra("email");
            registration = intent.getStringExtra("registration");
            id = intent.getStringExtra("id");
            role = intent.getStringExtra("role");
            inboxType = intent.getStringExtra("inboxType");
            position = intent.getIntExtra("position", 0);
            Log.e("inboxType", inboxType);
            if (inboxType.equals("rejectedInbox")) {
                rejectBtn.setVisibility(View.GONE);
            }

            // Check if the user is a doctor or patient and set corresponding fields
            else if (intent.hasExtra("employeeNum")) {
                employeeNum = intent.getStringExtra("employeeNum");
                specialties = intent.getStringExtra("specialities");

                // Display employee details
                employeeLabel.setVisibility(View.VISIBLE);
                employeeText.setVisibility(View.VISIBLE);

                // Bind user details to UI elements
                binding.userEmployeeNum.setText(employeeNum);
                binding.userSpecialities.setText(specialties);
            } else {
                // Display health card details
                healthCardLabel.setVisibility(View.VISIBLE);
                healthText.setVisibility(View.VISIBLE);

                healthCard = intent.getStringExtra("healthCard");
                // Bind health card details to UI elements
                binding.userHealthNum.setText(healthCard);
            }

            // Bind common user details to UI elements
            binding.userNameDetail.setText(firstName);
            binding.userSurnameDetail.setText(lastName);
            binding.userPhoneDetail.setText(phone);
            binding.userAddressDetail.setText(address);
            binding.userEmailDetail.setText(email);
            binding.userRegistration.setText(registration);
            binding.userId.setText(id);
        }

        // Define actions for reject button


        // Define actions for approve button
        approveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                approveUser(); // Call the approveUser method
                Toast.makeText(UserDetail.this, "User approved successfully", Toast.LENGTH_SHORT).show();
                // Navigate to Inbox activity
                Intent intent = new Intent(UserDetail.this, InboxActivity.class);
                intent.putExtra("inboxType", inboxType);
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });
        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectUser(); // Call the approveUser method
                Toast.makeText(UserDetail.this, "User rejected successfully", Toast.LENGTH_SHORT).show();
                // Navigate to Inbox activity
                Intent intent = new Intent(UserDetail.this, InboxActivity.class);
                intent.putExtra("inboxType", inboxType);
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetail.this, InboxActivity.class);
                intent.putExtra("inboxType", inboxType);
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });
    }

     //Method to reject a user
     public void rejectUser() {
         DatabaseReference roleRef;
         if (!inboxType.equals("upcomingApt")) {
             roleRef = mDatabase.child(role);
         } else {
             roleRef = mDatabase.child("Approved").child(role);
         }

         roleRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if (role.equals("Doctor")) {
                     Doctor user = dataSnapshot.getValue(Doctor.class);
                     mDatabase.child("Rejected").child(role).child(id).setValue(user);
                 } else {
                     Patient user = dataSnapshot.getValue(Patient.class);
                     if (inboxType.equals("upcomingApt")) {
                         Appointment appointment = user.getAppointments().get(position);
                         appointment.setStatus("rejected");

                         // Remove the appointment from the shift
                         removeAppointmentFromShift(appointment.getAssignedDoctor(), user.getId(), appointment.getAppointmentID());

                         roleRef.child(id).setValue(user);
                     } else {
                         mDatabase.child("Rejected").child(role).child(id).setValue(user);
                     }
                 }

                 // Remove the user from their current role after they've been added to "Rejected"
                 if (!inboxType.equals("upcomingApt")) {
                     roleRef.child(id).removeValue();
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {
                 // Handle any errors
             }
         });
     }

    private void removeAppointmentFromShift(String assignedDoctor, String patientId, String appointmentID) {
        DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("Approved").child("Doctor").child(assignedDoctor);

        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Doctor doctor = dataSnapshot.getValue(Doctor.class);

                if (doctor != null) {
                    for (Shift shift : doctor.getShifts()) {
                        if (shift.getAppointmentIDS().contains(appointmentID)) {
                            // Remove the appointment ID from the shift
                            shift.removeAppointmentID(appointmentID);

                            // Update the doctor in the database
                            doctorRef.setValue(doctor);
                            removeAppointmentFromPatient(patientId, appointmentID);

                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }

    private void removeAppointmentFromPatient(String patientId, String appointmentID) {
        DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference("Approved").child("Patient").child(patientId);

        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Patient patient = dataSnapshot.getValue(Patient.class);

                if (patient != null) {
                    List<Appointment> appointments = patient.getAppointments();
                    for (Appointment appointment : appointments) {
                        if (appointment.getAppointmentID().equals(appointmentID)) {
                            // Set the status of the appointment to "rejected"
                            appointment.setStatus("rejected");

                            // Remove the assignedDoctor (id) from the appointment
                            appointment.setAssignedDoctor(null);

                            // Update the patient in the database
                            patientRef.setValue(patient);

                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }


    public void approveUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User is not signed in
            return;
        }

        String currentUserId = currentUser.getUid();
        DatabaseReference roleRef = mDatabase.child(role);

        if (inboxType.equals("rejectedInbox")){
            roleRef = mDatabase.child("Rejected").child(role);
        }
        else if (inboxType.equals("upcomingApt")){
            roleRef = mDatabase.child("Approved").child("Patient");
        }

        DatabaseReference finalRoleRef = roleRef;
        roleRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check the user's role and move them to "Approved" in the appropriate category
                if (role.equals("Doctor")) {
                    Doctor user = dataSnapshot.getValue(Doctor.class);
                    mDatabase.child("Approved").child(role).child(id).setValue(user);
                } else {
                    Patient user = dataSnapshot.getValue(Patient.class);

                    if (inboxType.equals("upcomingApt")){
                        user.getAppointments().get(position).setStatus("approved");
                        user.getAppointments().get(position).setAssignedDoctor(currentUserId);
                        finalRoleRef.child(id).setValue(user);

                        // Add appointment ID to the list in Doctor class
                        addAppointmentIDToDoctor(currentUserId, user.getAppointments().get(position).getAppointmentID());
                    }

                    else{
                        mDatabase.child("Approved").child(role).child(id).setValue(user);
                    }
                    assert user != null;
                    if (user.getAppointments().size() == 0){
                        // Get the current time
                        Date currentTime = new Date();
                        // Calculate the time 4 hours later
                        long fourHoursLaterMillis = currentTime.getTime() + (4 * 60 * 60 * 1000);

                        // Create a new Date object for 4 hours later
                        Date fourHoursLater = new Date(fourHoursLaterMillis);

                        //user.setAppointment(currentTime, fourHoursLater,);//Test purposes
                    }
                }

                // Remove the user from their current role after they've been added to "Approved"
                if (!inboxType.equals("upcomingApt")){
                    finalRoleRef.child(id).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }

    private void addAppointmentIDToDoctor(String doctorId, String appointmentID) {
        DatabaseReference doctorRef = mDatabase.child("Approved").child("Doctor").child(doctorId);
        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Doctor doctor = dataSnapshot.getValue(Doctor.class);
                if (doctor != null) {
                    for (Shift shift : doctor.getShifts()) {
                        shift.addAppointmentID(appointmentID);
                    }
                    doctorRef.setValue(doctor);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }


}
