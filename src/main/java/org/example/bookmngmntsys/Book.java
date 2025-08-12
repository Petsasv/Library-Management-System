package org.example.bookmngmntsys;

import javafx.beans.property.*;

public class Book {

    private final IntegerProperty bookId;
    private final StringProperty title;
    private final StringProperty author;
    private final StringProperty isbn;
    private final StringProperty category;
    private final BooleanProperty availability;

    public Book(int bookId, String title, String author, String isbn, String category,boolean availability) {
        this.bookId = new SimpleIntegerProperty(bookId);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.isbn = new SimpleStringProperty(isbn);
        this.category = new SimpleStringProperty(category);
        this.availability = new SimpleBooleanProperty(availability);
    }

    // Book ID
    public int getBookId() {
        return bookId.get();
    }

    public void setBookId(int bookId) {
        this.bookId.set(bookId);
    }

    public IntegerProperty bookIdProperty() {
        return bookId;
    }

    // Title
    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    // Author
    public String getAuthor() {
        return author.get();
    }

    public void setAuthor(String author) {
        this.author.set(author);
    }

    public StringProperty authorProperty() {
        return author;
    }

    // ISBN
    public String getIsbn() {
        return isbn.get();
    }

    public void setIsbn(String isbn) {
        this.isbn.set(isbn);
    }

    public StringProperty isbnProperty() {
        return isbn;
    }

    // Availability
    public boolean isAvailability() {
        return availability.get();
    }

    public void setAvailability(boolean availability) {
        this.availability.set(availability);
    }

    public BooleanProperty availabilityProperty() {
        return availability;
    }

    // Category
    public String isCategory() {
        return category.get();
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public StringProperty categoryProperty() {
        return category;
    }


}
