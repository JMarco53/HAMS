package com.example.group5project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class UserChoice extends AppCompatActivity {
    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;
    TextView back_option;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_choice);

        Button confirmBtn = findViewById(R.id.confirmRole);
        TextView back_option = findViewById(R.id.backBtn);

        spinner = findViewById(R.id.spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.dropdown_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        back_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserChoice.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedOption = spinner.getSelectedItem().toString();//selected user registration type

                //Switch activity to register a doctor (if user choose "Doctor" in drop down list)
                if (selectedOption.equals("Doctor")){
                    Intent intent = new Intent(UserChoice.this, RegisterDoctor.class);
                    startActivity(intent);
                    finish();
                }
                //Switch activity to register a patient (if user choose "Patient" in drop down list)
                else{
                    Intent intent = new Intent(UserChoice.this, RegisterPatient.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}