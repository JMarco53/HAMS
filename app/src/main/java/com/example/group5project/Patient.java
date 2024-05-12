package com.example.group5project;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Patient extends User{

    protected String healthCard;
    protected List<Appointment> appointments;
    public  Patient(){
        appointments = new ArrayList<>();
    }
    public Patient(String firstName, String lastName, String phone, String address, String healthCard, String email, Date registrationDate, String id) {
        super(firstName, lastName, phone, address, email, registrationDate, id);
        this.healthCard = healthCard;
        this.appointments = new ArrayList<>();
    }
    public String getHealthCard() {
        return healthCard;
    }
    public boolean setAppointment(Date startTime, Date endTime) {
        // Get a reference to the patient's appointments in the database
        if (this.getId() != null) {
            DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference("Approved").child("Patient").child(this.getId());

            // Initialize appointments list if not already initialized
            if (this.appointments == null) {
                this.appointments = new ArrayList<>();
            }
            //Log.e("checkingDoctor", assignedDoctor.getFirstName());
            // Check for overlapping appointments
            for (Appointment existingAppointment : this.appointments) {
                if (existingAppointment.conflictsWith(startTime, endTime)) {
                    // Overlapping appointment found, handle accordingly (e.g., show an error message)
                    Log.e("setAppointment", "Appointment overlaps with an existing appointment");
                    return false;
                }
            }

            Log.e("getAppointment", "Before generating key in setAppointment");
            // Generate a unique ID for the new appointment
            String appointmentID = UUID.randomUUID().toString();
            Log.e("getAppointment", "Appointment ID is " + appointmentID);
            // Create an appointment for the current patient
            Appointment newAppointment = new Appointment(startTime, endTime, "pending", appointmentID,this.getId());

            // Add the new appointment to the local list
            this.appointments.add(newAppointment);

            // Check if the appointment ID is not null
            if (appointmentID != null) {
                // Update the patient in the database
                patientRef.setValue(this);
            }
        }
        return true;
    }



    public List<Appointment> getAppointments() {
        if (appointments == null) {
            return new ArrayList<>();
        }
        return appointments;
    }



}