package org.example.bookmngmntsys;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String URL = "jdbc:postgresql://dblabs.iee.ihu.gr:5432/it185328";
    // Get from environment variables
    private static final String USER = System.getProperty("DB_USER", System.getenv("DB_USER"));
    private static final String PASSWD = System.getProperty("DB_PASSWORD", System.getenv("DB_PASSWORD"));


    private final Connection connection;

    public DatabaseManager () throws SQLException {
        this.connection = DriverManager.getConnection(URL, USER, PASSWD);
        System.out.println("Successfully connected to DB!");
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed.");
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Fetch categories from DB
    public List<String> getAllCategories() throws SQLException{
        List<String> categories = new ArrayList<>();

        String query = "SELECT * FROM library.get_all_categories()";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
        return categories;
    }

    // Add book to DB
    public void addBook(String title, String author, String isbn, String category, int published_year) throws SQLException{
        String query = "SELECT library.add_book(?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, isbn);
            pstmt.setString(4, category);
            pstmt.setInt(5, published_year);

            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    // Display all books from DB
    public List<ObservableList<String>> getAllBooks() throws SQLException{
        List<ObservableList<String>> books = new ArrayList<>();
        String query = "SELECT * FROM library.get_all_books()";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("title"));
                row.add(rs.getString("author"));
                row.add(rs.getString("isbn"));
                row.add(rs.getString("category"));
                row.add(String.valueOf(rs.getInt("published_year")));
                row.add(rs.getString("availability")); // Should be "Yes" or "No"
                books.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching books via function: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        return books;
    }

    // Add a user to DB
    public void addUser(String firstName, String lastName, String email, String phone) throws SQLException{
        String query = "SELECT library.add_member(?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);

            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    // Display all users from DB
    public List<ObservableList<String>> getAllUsers() throws SQLException{
        List<ObservableList<String>> members = new ArrayList<>();
        String query = "SELECT * FROM library.get_all_members()";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("firstname"));
                row.add(rs.getString("lastname"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                row.add(rs.getDate("regdate").toString());
                members.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching members via function: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        return members;
    }

    // Get users by name
    public List<String> getUsersNames() throws SQLException{
        List<String> membersNames = new ArrayList<>();
        String query = "SELECT * FROM library.get_users_names() AS full_name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                membersNames.add(rs.getString("full_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
        return membersNames;
    }

    // Borrow a book by a user
    public void borrowBook(String bookTitle,  int memberId, LocalDate returnDate) throws SQLException{
        String query = "SELECT library.borrow_book(?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, bookTitle);
            pstmt.setInt(2, memberId);
            pstmt.setDate(3, java.sql.Date.valueOf(returnDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int borrowId = rs.getInt(1);
                System.out.println("Book borrowed with borrow ID: " + borrowId);
            }

        } catch (SQLException e) {
            System.err.println("Error borrowing book: " + e.getMessage());
            e.printStackTrace(System.err);
            throw new RuntimeException("Failed to borrow book: " + e.getMessage(), e);
        }

    }
    // need to fix this
    public int getMemberIdByName(String firstName, String lastName) throws SQLException {
        String sql = "SELECT library.get_member_id_by_name(?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Member not found");
            }
        }
    }

    // Display borrowings and which user has it
    public ObservableList<ObservableList<String>> getAllBorrowings() throws SQLException {
        ObservableList<ObservableList<String>> borrowings = FXCollections.observableArrayList();

        String query = "SELECT * FROM library.get_all_borrowings()";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("borrowId")));
                row.add(rs.getString("bookName"));
                row.add(rs.getString("memberName"));
                row.add(rs.getDate("borrowDate") != null ? rs.getDate("borrowDate").toString() : "");
                row.add(rs.getDate("returnDate") != null ? rs.getDate("returnDate").toString() : "");

                borrowings.add(row);
            }
        }
        return borrowings;
    }

    // Get category ID by category name
    public int getCategoryIdByName(String categoryName) throws SQLException {
        String query = "SELECT library.get_category_id_by_name(?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, categoryName);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Category not found: " + categoryName);
            }
        }
    }
    // Get book id by ISBN
    public int getBookIdByIsbn(String ISBN) throws SQLException{
        String query = "SELECT library.get_book_id_by_isbn(?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, ISBN);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("ISBN not found: " + ISBN);
            }
        }
    }
    // Edit a book
    public void editBook(int bookId, String title, String author, String isbn, String categoryName, int publishedYear,boolean availability) throws SQLException{
        int categoryId = getCategoryIdByName(categoryName);  // get the id from name

        String query = "SELECT library.edit_book(?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            pstmt.setString(2, title);
            pstmt.setString(3, author);
            pstmt.setInt(4, categoryId);
            pstmt.setString(5, isbn);
            pstmt.setInt(6, publishedYear);
            pstmt.setBoolean(7, availability);

            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    // Delete a book
    public void deleteBook(int bookId) throws SQLException{
        String query = "SELECT library.delete_book(?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            pstmt.executeQuery();
        }
    }

    // Return a book
    public void returnBook(int borrowId) throws SQLException {
        String query = "SELECT library.return_book_delete(?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, borrowId);
            pstmt.executeQuery();
        } catch (SQLException e) {
            throw new SQLException("Error returning book: " + e.getMessage(), e);
        }
    }

    // Edit a user
    public void editUser(int userId, String firstName, String lastName, String email, String phone) throws SQLException {
        String query = "SELECT library.edit_member(?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, email);
            pstmt.setString(5, phone);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    // Delete a user
    public void deleteUser(int userId) throws SQLException {
        String query = "SELECT library.delete_member(?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    // Fetch logs data from DB
    public ObservableList<Log> fetchLogs(int limit) throws SQLException {
        ObservableList<Log> logs = FXCollections.observableArrayList();

        String query = "SELECT * FROM library.get_recent_logs(?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, limit);

            ResultSet rs = pstmt.executeQuery();
            int idCounter = 1;
            while (rs.next()) {
                Log log = new Log(
                        idCounter++,
                        rs.getString("table_name"),
                        rs.getString("action"),
                        rs.getString("changed_data"),
                        rs.getString("action_timestamp")
                );
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
        return logs;
    }
}
