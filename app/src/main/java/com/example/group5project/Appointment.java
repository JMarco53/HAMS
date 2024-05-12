package com.example.group5project;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Date;

public class Appointment extends User {
    private Date startTime;
    private Date endTime;
    private String status;
    private String appointmentID;
    private String assignedPatient;
    private String assignedDoctor;

    public Appointment() {

    }

    public Appointment(Date startTime, Date endTime, String status, String appointmentID, String assignedPatient) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.appointmentID = appointmentID;
        this.assignedPatient = assignedPatient;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public String getAppointmentID(){return appointmentID;}

    public String getAssignedPatient() {
        return assignedPatient;
    }
    public void setAssignedPatient(String assignedPatient) {
        this.assignedPatient = assignedPatient;
    }
    public String getAssignedDoctor() {
        return assignedDoctor;
    }
    public void setAssignedDoctor(String assignedDoctor) {
        this.assignedDoctor = assignedDoctor;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public boolean conflictsWith(Date otherStartTime, Date otherEndTime) {
        // Check for overlapping time range
        return (startTime.before(otherEndTime) && endTime.after(otherStartTime)) ||
                (otherStartTime.before(endTime) && otherEndTime.after(startTime));
    }

}
