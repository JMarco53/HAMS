package com.example.group5project;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Doctor extends User{

    private String employeeNum;

    private List<String> specialties;
    private List<Shift> shifts;

    public Doctor(){
        shifts = new ArrayList<>();
    }

    public Doctor(String firstName, String lastName, String phone, String address, String employeeNum, String email, List<String> specialties, Date registrationDate,String id) {
        super(firstName, lastName, phone, address, email, registrationDate, id);
        this.employeeNum = employeeNum;
        this.specialties = specialties;
    }

    public String getEmployeeNum() {

        return employeeNum;
    }

    public List<String> getSpecialties() {
        return specialties;
    }
    public List<Shift> getShifts() {
        return shifts;
    }

    public boolean addShift(Date startTime, Date endTime) {
        DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("Approved").child("Doctor").child(this.getId());

        // Check if the specified date has already passed
        if (startTime.before(new Date()) || endTime.before(new Date())) {
            Log.e("AddingShift", "Cannot add a shift for a past date.");

            return false;
        }

        if (endTime.before(startTime)){
            Log.e("AddingShift", "End time cannot be before start time");
            return false;
        }

        // Check for conflicts with existing shifts
        for (Shift existingShift : shifts) {
            Log.e("Adding", "Start date: " + String.valueOf(existingShift.getStartTime()) + " end date: " + String.valueOf(existingShift.getEndTime()));
            if (existingShift.conflictsWith(startTime, endTime)) {
                Log.e("AddingShift", "Shift conflicts with an existing one.");
                return false;
            }
        }
        String shiftID = doctorRef.push().getKey();
        // Add the new shift
        Shift newShift = new Shift(startTime, endTime, this.getId());
        shifts.add(newShift);

        // Check if the appointment ID is not null
        if (shiftID != null) {
            // Update the patient in the database
            doctorRef.setValue(this);
        }
        return true;
    }

    public boolean deleteShift(Date startTime, Date endTime) {
        Shift shiftToRemove = null;
        Log.e("Removing", "Starting to remove");

        // Find the shift to remove based on start and end times
        for (Shift existingShift : shifts) {
            Log.e("Removing", "Start date: " + String.valueOf(existingShift.getStartTime()) + " end date: " + String.valueOf(existingShift.getEndTime()));
            if (existingShift.getStartTime().equals(startTime) && existingShift.getEndTime().equals(endTime)) {
                shiftToRemove = existingShift;
                break;
            }
        }
        Log.e("Removing", "Done finding shift to remove");

        if (shiftToRemove != null) {
            // Check if the shift has associated appointments
            if (!shiftToRemove.getAppointmentIDS().isEmpty()) {
                Log.e("Removing", "Cannot delete a shift with associated appointments.");
                return false;
            }

            // Remove the shift from the list
            shifts.remove(shiftToRemove);

            // Update the database
            DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("Approved").child("Doctor").child(this.getId());
            doctorRef.setValue(this);
            Log.e("Removing", "Removed Successfully");
            return true;

        } else {
            Log.e("Removing", "start time and end time did not match");
            return false;
        }
    }


    public List<Shift> generateTimeSlots() {
        List<Shift> timeSlots = new ArrayList<>();

        // Iterate through each existing shift
        for (Shift existingShift : shifts) {
            if (hasAvailableSlots(existingShift)) {
                Date startTime = existingShift.getStartTime();
                Date endTime = existingShift.getEndTime();

                // Calculate the number of time slots based on 30-minute periods
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startTime);

                while (calendar.getTime().before(endTime)) {
                    // Calculate the end time of the current time slot (30 minutes later)
                    calendar.add(Calendar.MINUTE, 30);
                    Date slotEndTime = calendar.getTime();

                    // Ensure the calculated time slot end time is within the doctor's shift
                    if (slotEndTime.before(endTime) || slotEndTime.equals(endTime)) {
                        // Create a new time slot and add it to the list
                        Shift timeSlot = new Shift(startTime, slotEndTime, this.getId());
                        timeSlots.add(timeSlot);

                        // Set the start time of the next time slot to the end time of the current time slot
                        startTime = slotEndTime;
                    }
                }
            }
        }

        return timeSlots;
    }


    private boolean hasAvailableSlots(Shift shift) {
        // Check if there are available slots in the shift
        return shift.getAppointmentIDS().size() < calculateMaxAppointments(shift);
    }

    private int calculateMaxAppointments(Shift shift) {
        // Calculate the maximum number of appointments based on the available time slots
        long durationInMillis = shift.getEndTime().getTime() - shift.getStartTime().getTime();
        long slots = durationInMillis / (30 * 60 * 1000); // 30 minutes per slot

        return (int) slots;
    }


}
