package com.example.group5project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.group5project.databinding.ActivityAppointmentCreationBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AppointmentCreation extends AppCompatActivity {

    private String[] specialtiesArray = {
            "Cardiologist",
            "Dermatologist",
            "Ophthalmologist",
            "Orthopedic Surgeon",
            "Pediatrician",
            "Neurologist"
    };
    private CheckBox[] checkBoxes = new CheckBox[specialtiesArray.length];
    private boolean[] selectedSpecialties = new boolean[specialtiesArray.length];
    ArrayList<Shift> doctorArrayList = new ArrayList<>();
    ActivityAppointmentCreationBinding binding;
    TimeSlotAdapter listAdapter;
    Button searchBtn;
    EditText searchField;
    TextView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        searchBtn = findViewById(R.id.search_button);
        searchField = findViewById(R.id.search_field);
        backBtn = findViewById(R.id.apt_backBtn);

        checkBoxes[0] = findViewById(R.id.cardiologist);
        checkBoxes[1] = findViewById(R.id.dermatologist);
        checkBoxes[2] = findViewById(R.id.ophthalmologist);
        checkBoxes[3] = findViewById(R.id.orthopedic_surgeon);
        checkBoxes[4] = findViewById(R.id.pediatrician);
        checkBoxes[5] = findViewById(R.id.neurologist);

        // Create and set the custom ListAdapter for the ListView
        listAdapter = new TimeSlotAdapter(AppointmentCreation.this, doctorArrayList);
        binding.appointmentList.setAdapter(listAdapter);

        binding.appointmentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String doctorID = doctorArrayList.get(position).getAssignedDoctor();

                // Retrieve the specific doctor using the ID
                FirebaseDatabase.getInstance().getReference().child("Approved").child("Doctor").child(doctorID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Doctor doctor = dataSnapshot.getValue(Doctor.class);
                                if (doctor != null) {
                                    // Doctor object is now available, you can use it as needed
                                    Intent intent =  new Intent(AppointmentCreation.this, AppointmentDetail.class);
                                    intent.putExtra("inboxType", "patientApt");
                                    intent.putExtra("firstName", doctor.getFirstName());
                                    intent.putExtra("lastName", doctor.getLastName());
                                    intent.putExtra("phoneNum", doctor.getPhone());
                                    intent.putExtra("email", doctor.getEmail());
                                    intent.putExtra("startTime", doctorArrayList.get(position).getStartTime().getTime());
                                    intent.putExtra("endTime", doctorArrayList.get(position).getEndTime().getTime());
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Handle the case where the doctor is not found
                                    Toast.makeText(AppointmentCreation.this, "Doctor not found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle database error
                            }
                        });
            }
        });


        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedSpecialties = new boolean[specialtiesArray.length];//Clears
                storeCheckedSpecialties();
                Log.e("specialtiesCheck", "Selected specialties" + Arrays.toString(selectedSpecialties));
                doctorArrayList.clear();
                Log.e("specialtiesCheck", "Search field " + searchField.getText().toString());
                if (!isClear() || !searchField.getText().toString().equals("")){
                    getDoctorsWithSelectedSpecialties();
                }
                else {
                    doctorArrayList.clear();
                    listAdapter.notifyDataSetChanged();
                }

            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(AppointmentCreation.this, InboxActivity.class);
                intent.putExtra("inboxType", "patientApt");
                startActivity(intent);
                finish();
            }
        });
    }
    public boolean isClear(){
        boolean allFalse = true;
        for (boolean element : selectedSpecialties) {
            if (element) {
                allFalse = false;
                break;
            }
        }
        return allFalse;
    }

    private void storeCheckedSpecialties() {
        for (int i = 0; i < checkBoxes.length; i++) {
            selectedSpecialties[i] = checkBoxes[i].isChecked();
        }
    }

    public void getDoctorsWithSelectedSpecialties() {
        FirebaseDatabase.getInstance().getReference().child("Approved").child("Doctor")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        doctorArrayList.clear();
                        List<Doctor> doctorsWithSelectedSpecialties = new ArrayList<>();
                        for (DataSnapshot doctorSnapshot : dataSnapshot.getChildren()) {
                            Doctor doctor = doctorSnapshot.getValue(Doctor.class);
                            if (doctor != null && doctor.getSpecialties() != null) {
                                List<String> doctorSpecialties = doctor.getSpecialties();
                                Log.e("specialtiesCheck", "Doctor's specialties" + String.valueOf(doctorSpecialties));
                                boolean hasAllSelectedSpecialties = true;
                                for (int i = 0; i < selectedSpecialties.length; i++) {
                                    if (selectedSpecialties[i] && !doctorSpecialties.contains(specialtiesArray[i])) {
                                        hasAllSelectedSpecialties = false;
                                        break;
                                    }
                                }
                                Log.e("specialtiesCheck", "name are equal: " + String.valueOf(searchField.getText().toString().equals(doctor.getFirstName() + " " + doctor.getLastName())));
                                Log.e("specialtiesCheck", doctor.getFirstName() + " " + doctor.getLastName());
                                if (isClear()){
                                    if (searchField.getText().toString().equals(doctor.getFirstName() + " " + doctor.getLastName())) {
                                        Log.e("specialtiesCheck", "TRUE");
                                        // This doctor has all the selected specialties
                                        List<Shift> timeSlots = doctor.generateTimeSlots();
                                        doctorArrayList.addAll(timeSlots);
                                    }
                                }
                                else if (hasAllSelectedSpecialties) {
                                    Log.e("specialtiesCheck", "TRUE");
                                    // This doctor has all the selected specialties

                                    List<Shift> timeSlots = doctor.generateTimeSlots();
                                    doctorArrayList.addAll(timeSlots);
                                }
                            } else {
                                Log.e("specialtiesCheck", "doctor or specialties was null");
                            }
                            useDoctorArrayList();
                        }

                        // Notify the adapter that the data has changed
                        listAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle database error
                    }
                });
    }


    private void useDoctorArrayList() {
        // Use aptArrayList here
        Log.e("specialtiesCheck", "specialties size: " + String.valueOf(doctorArrayList.size()));
        if (!doctorArrayList.isEmpty()) {
            Log.e("specialtiesCheck", "Doctor's name: " + String.valueOf(doctorArrayList.get(0).getStartTime()));
        }
        listAdapter.notifyDataSetChanged();
    }
}
