package org.example.bookmngmntsys;

import javafx.beans.property.*;
public class Borrowing {

    private final IntegerProperty borrowId;
    private final StringProperty bookTitle;
    private final StringProperty memberName;
    private final StringProperty borrowDate;
    private final StringProperty returnDate;

    public Borrowing(int borrowId, String bookTitle, String memberName, String borrowDate, String returnDate) {
        this.borrowId = new SimpleIntegerProperty(borrowId);
        this.bookTitle = new SimpleStringProperty(bookTitle);
        this.memberName = new SimpleStringProperty(memberName);
        this.borrowDate = new SimpleStringProperty(borrowDate);
        this.returnDate = new SimpleStringProperty(returnDate);
    }

    public int getBorrowId() {
        return borrowId.get();
    }

    public void setBorrowId(int borrowId) {
        this.borrowId.set(borrowId);
    }

    public IntegerProperty borrowIdProperty() {
        return borrowId;
    }

    public String getBookTitle() {
        return bookTitle.get();
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle.set(bookTitle);
    }

    public StringProperty bookTitleProperty() {
        return bookTitle;
    }

    public String getMemberName() {
        return memberName.get();
    }

    public void setMemberName(String memberName) {
        this.memberName.set(memberName);
    }

    public StringProperty memberNameProperty() {
        return memberName;
    }

    public String getBorrowDate() {
        return borrowDate.get();
    }

    public void setBorrowDate(String borrowDate) {
        this.borrowDate.set(borrowDate);
    }

    public StringProperty borrowDateProperty() {
        return borrowDate;
    }

    public String getReturnDate() {
        return returnDate.get();
    }

    public void setReturnDate(String returnDate) {
        this.returnDate.set(returnDate);
    }

    public StringProperty returnDateProperty() {
        return returnDate;
    }
}
