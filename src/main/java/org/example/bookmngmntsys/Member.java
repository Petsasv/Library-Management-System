package org.example.bookmngmntsys;

import javafx.beans.property.*;

public class Member {

    private final IntegerProperty memberId;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty email;
    private final StringProperty phone;
    private final StringProperty registrationDate;

    public Member(IntegerProperty memberId, StringProperty firstName, StringProperty lastName, StringProperty email, StringProperty phone, StringProperty registrationDate) {
        this.memberId = memberId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.registrationDate = registrationDate;
    }

    // Member ID
    public int getMemberId() {
        return memberId.get();
    }

    public void setMemberId(int memberId) {
        this.memberId.set(memberId);
    }

    public IntegerProperty memberIdProperty() {
        return memberId;
    }

    // First Name
    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    // Last Name
    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    // Email
    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    // Phone
    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    // Registration Date
    public String getRegistrationDate() {
        return registrationDate.get();
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate.set(registrationDate);
    }

    public StringProperty registrationDateProperty() {
        return registrationDate;
    }
}