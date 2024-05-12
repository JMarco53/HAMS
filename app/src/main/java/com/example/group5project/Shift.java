package com.example.group5project;

import com.example.group5project.Doctor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Shift {
    private Date date;
    private Date startTime;
    private Date endTime;
    private String assignedDoctor;
    private List<String> appointmentIDS;
    public Shift(){
        appointmentIDS = new ArrayList<>();
    }

    public Shift(Date startTime, Date endTime, String assignedDoctor) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.assignedDoctor = assignedDoctor;
        this.appointmentIDS = new ArrayList<>();
    }

    public boolean conflictsWith(Date otherStartTime, Date otherEndTime) {
        // Check for overlapping time range
        return (startTime.before(otherEndTime) && endTime.after(otherStartTime)) ||
                (otherStartTime.before(endTime) && otherEndTime.after(startTime));
    }


    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
    public String getAssignedDoctor(){
        return assignedDoctor;
    }

    public List<String> getAppointmentIDS() {
        return appointmentIDS;
    }

    public void addAppointmentID(String appointmentID) {
        if (this.appointmentIDS == null || this.appointmentIDS.size() == 0) {
            this.appointmentIDS = new ArrayList<>();
        }
        appointmentIDS.add(appointmentID);
    }

    public void removeAppointmentID(String appointmentID) {
        appointmentIDS.remove(appointmentID);
    }



}