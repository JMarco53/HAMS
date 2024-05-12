package com.example.group5project;

import java.util.Date;

public class User {
    protected String firstName, lastName, phone, address, email, id;
    protected Date registrationDate;
    public User() {
    }

    public User(String firstName, String lastName, String phone, String address, String email, Date registrationDate, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.registrationDate = registrationDate;
        this.id = id;
    }
    //Getters for the common attributes of users
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public String getId() {
        return id;
    }
}
