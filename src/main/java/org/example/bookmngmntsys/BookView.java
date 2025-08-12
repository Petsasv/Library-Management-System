package org.example.bookmngmntsys;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BookView {

    public static void showBorrowDialog(ObservableList<String> selectedBook, DatabaseManager dbManager, TableView<ObservableList<String>> tableView) {
        // Create dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Borrow dialog");
        dialog.setHeaderText("Borrow: " + selectedBook.getFirst());

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Member selection
        Label memberLabel = new Label("Select member:");
        ComboBox<String> memberCB = new ComboBox<>();
        memberCB.setPromptText("Choose a member");
        memberCB.setPrefWidth(200);

        // Populate member comboBox
        try {
            List<String> members = dbManager.getUsersNames();
            memberCB.setItems(FXCollections.observableList(members));
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }

        // Return date
        Label returnDateLabel = new Label("Return Date:");
        DatePicker returnDatePicker = new DatePicker();
        returnDatePicker.setValue(LocalDate.now().plusWeeks(2));
        returnDatePicker.setPrefWidth(200);

        grid.add(memberLabel, 0, 0);
        grid.add(memberCB, 1, 0);
        grid.add(returnDateLabel, 0, 1);
        grid.add(returnDatePicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Buttons
        ButtonType borrowButtonType = new ButtonType("Borrow", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(borrowButtonType, ButtonType.CANCEL);

        // Enable/Disable borrow button based on input
        Node borrowButton = dialog.getDialogPane().lookupButton(borrowButtonType);
        borrowButton.setDisable(true);

        memberCB.valueProperty().addListener((observable, oldValue, newValue) -> {
            borrowButton.setDisable(newValue == null || returnDatePicker.getValue() == null);
        });

        returnDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            borrowButton.setDisable(memberCB.getValue() == null || newValue == null || newValue.isBefore(LocalDate.now()));
        });

        // Handle result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == borrowButtonType) {
            try {
                String selectedMember = memberCB.getValue();
                String[] nameParts = selectedMember.split(" ");
                String firstName = nameParts[0];
                String lastName = nameParts.length > 1 ? nameParts[1] : "";

                int memberId = dbManager.getMemberIdByName(firstName, lastName);
                LocalDate returnDate = returnDatePicker.getValue();
                String bookTitle = selectedBook.get(0);

                // Check if book is available
                String availableStatus = selectedBook.get(5); // Assuming Available is at index 5
                if (!"Yes".equals(availableStatus)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Book Not Available");
                    alert.setHeaderText("Cannot Borrow Book");
                    alert.setContentText("This book is currently not available for borrowing.");
                    alert.showAndWait();
                    return;
                }

                // Borrow the book
                dbManager.borrowBook(bookTitle, memberId, returnDate);

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Book Borrowed");
                alert.setContentText("The book has been successfully borrowed by " + selectedMember + ".");
                alert.showAndWait();

                // Refresh table
                loadBooksData(tableView, dbManager);

            } catch (SQLException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Failed to Borrow Book");
                alert.setContentText("An error occurred while borrowing the book. Please try again.");
                alert.showAndWait();
                ex.printStackTrace(System.err);
            }
        }

    }

    // Set up column names
    private static void setupTableColumns(TableView<ObservableList<String>> tableView) {
        tableView.getColumns().clear();

        String[] columnNames = {"Title", "Author", "ISBN", "Category", "Year", "Available"};

        for (int i = 0; i < columnNames.length; i++) {
            final int columnIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnNames[i]);

            column.setCellValueFactory(param -> {
                ObservableList<String> row = param.getValue();
                if (row != null && columnIndex < row.size()) {
                    return new javafx.beans.property.SimpleStringProperty(row.get(columnIndex));
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });

            // Set column widths...
            tableView.getColumns().add(column);
        }
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // Παίρνω books data from DB
    private static void loadBooksData(TableView<ObservableList<String>> tableView, DatabaseManager dbManager) {
        try {
            List<ObservableList<String>> books = dbManager.getAllBooks();
            tableView.setItems(FXCollections.observableArrayList(books));

            if (books.isEmpty()) {
                tableView.setPlaceholder(new Label("No books found in the database."));
            }

        } catch (SQLException e) {
            tableView.setPlaceholder(new Label("Error loading books from database."));
            e.printStackTrace(System.err);
        }
    }

    public static BorderPane getView(DatabaseManager dbManager) throws SQLException {

        BorderPane root = new BorderPane();

        // Toolbar
        Button previewBtn  = new Button("Preview Books");
        Button newEntryBtn = new Button("Add Book");
        Button borrowBtn = new Button("Borrow Selected Book");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        ToolBar toolBar = new ToolBar(previewBtn, newEntryBtn, spacer, borrowBtn);
        root.setTop(toolBar);

        TableView<ObservableList<String>> tableView = new TableView<>();
        setupTableColumns(tableView);
        loadBooksData(tableView, dbManager);

        // default view
        root.setCenter(tableView);

        setupTableColumns(tableView);

        // Load data
        loadBooksData(tableView, dbManager);

        // Buttons below the table
        Button editBtn = new Button("Edit Selected");
        Button deleteBtn = new Button("Delete Selected");
        styleTableButtons(editBtn, deleteBtn);

        HBox buttonsBox = new HBox(10, editBtn, deleteBtn);  // 10 px spacing between buttons
        buttonsBox.setPadding(new Insets(10, 0, 0, 0)); // just top padding

        VBox previewView = new VBox(5, tableView, buttonsBox);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        previewView.setPadding(new Insets(10));

        // Φόρμα προσθήκης βιβλίου
        VBox formView = createBookForm(tableView, dbManager);

        root.setCenter(previewView);

        // Buttons's actions
        editBtn.setOnAction(e -> {
            ObservableList<String> selectedBook = tableView.getSelectionModel().getSelectedItem();
            if (selectedBook == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Selection");
                alert.setHeaderText(null);
                alert.setContentText("Please select a book to edit.");
                alert.showAndWait();
                return;
            }

            String currentTitle = selectedBook.get(0);
            String currentAuthor = selectedBook.get(1);
            String currentIsbn = selectedBook.get(2);
            String currentCategory = selectedBook.get(3);
            String yearStr = selectedBook.get(4);
            int currentYearEdit = Integer.parseInt(yearStr);
            String availableStr = selectedBook.get(5);
            boolean currentAvailable = availableStr.equalsIgnoreCase("true") || availableStr.equalsIgnoreCase("yes");

            // Create edit window
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Book");
            dialog.setHeaderText("Modify the book details and click Update.");

            // Create labels and fields for editing
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField titleFieldEdit = new TextField(currentTitle);
            TextField authorFieldEdit = new TextField(currentAuthor);
            TextField isbnFieldEdit = new TextField(currentIsbn);

            ComboBox<String> categoryComboEdit = new ComboBox<>();
            try {
                List<String> categories = dbManager.getAllCategories();
                categoryComboEdit.setItems(FXCollections.observableList(categories));

                for (String cat : categories) {
                    if (cat.equalsIgnoreCase(currentCategory.trim())) {
                        categoryComboEdit.setValue(cat);
                        break;
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace(System.err);
            }
            categoryComboEdit.setValue(currentCategory);

            Spinner<Integer> yearSpinner = new Spinner<>(1000, java.time.Year.now().getValue(), currentYearEdit);
            yearSpinner.setEditable(true);

            CheckBox availableCheckBox = new CheckBox("Available");
            availableCheckBox.setSelected(currentAvailable);

            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleFieldEdit, 1, 0);
            grid.add(new Label("Author:"), 0, 1);
            grid.add(authorFieldEdit, 1, 1);
            grid.add(new Label("ISBN:"), 0, 2);
            grid.add(isbnFieldEdit, 1, 2);
            grid.add(new Label("Category:"), 0, 3);
            grid.add(categoryComboEdit, 1, 3);
            grid.add(new Label("Published Year:"), 0, 4);
            grid.add(yearSpinner, 1, 4);
            grid.add(availableCheckBox, 1, 5);

            dialog.getDialogPane().setContent(grid);

            // Add Update and Cancel buttons
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Validate before closing on OK
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Validate inputs
                if (titleFieldEdit.getText().trim().isEmpty() ||
                        authorFieldEdit.getText().trim().isEmpty() ||
                        isbnFieldEdit.getText().trim().isEmpty() ||
                        categoryComboEdit.getValue() == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Please fill in all required fields.");
                    alert.showAndWait();
                    event.consume();  // prevent dialog from closing
                }
            });

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Get updated values
                String newTitle = titleFieldEdit.getText().trim();
                String newAuthor = authorFieldEdit.getText().trim();
                String newIsbn = isbnFieldEdit.getText().trim();
                String newCategory = categoryComboEdit.getValue();
                int newYear = yearSpinner.getValue();
                boolean newAvailable = availableCheckBox.isSelected();

                try {
                    // get book id from db through function
                    int bookId = dbManager.getBookIdByIsbn(currentIsbn);

                    dbManager.editBook(bookId, newTitle, newAuthor, newIsbn, newCategory, newYear, newAvailable);

                    // Refresh table data
                    loadBooksData(tableView, dbManager);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Book updated successfully.");
                    alert.showAndWait();

                } catch (SQLException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Database Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error updating book: " + ex.getMessage());
                    alert.showAndWait();
                    ex.printStackTrace(System.err);
                }
            }
        });

        // Delete book from DB button function
        deleteBtn.setOnAction(e -> {
            ObservableList<String> selectedBook = tableView.getSelectionModel().getSelectedItem();
            if (selectedBook == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Selection Error");
                alert.setContentText("No selection. Please select a book to delete.");
                alert.showAndWait();
                return;
            }

            String isbn = selectedBook.get(2); // ISBN is column 2
            try {
                int bookId = dbManager.getBookIdByIsbn(isbn);

                // Confirm deletion
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Deletion");
                confirm.setHeaderText("Are you sure you want to delete this book?");
                confirm.setContentText("Title: " + selectedBook.getFirst() + "\nISBN: " + isbn);

                if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                    return; // user canceled
                }

                // Try to delete
                dbManager.deleteBook(bookId);

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Book deleted successfully.");
                success.showAndWait();

                // refresh table
                loadBooksData(tableView, dbManager);

            } catch (SQLException ex) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Database Error");
                error.setHeaderText("Failed to delete book");
                error.setContentText(ex.getMessage()); // will show the stored procedure error
                error.showAndWait();
            }
        });


        previewBtn.setOnAction(e -> root.setCenter(previewView));

        newEntryBtn.setOnAction(e -> root.setCenter(formView));

        // Borrow Book action
        borrowBtn.setOnAction(e -> {
            ObservableList<String> selectedBook = tableView.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                showBorrowDialog(selectedBook, dbManager, tableView);
            }
            else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information message");
                alert.setTitle("No book selected");
                alert.setContentText("Please select a book first");
                alert.showAndWait();
            }
        });

        return root;
    }

    private static void styleTableButtons(Button... buttons) {
        for (Button btn : buttons) {
            btn.getStyleClass().add("table-button");
        }
    }

    private static VBox createBookForm(TableView<ObservableList<String>> tableView, DatabaseManager dbManager) throws SQLException {
        VBox formContainer = new VBox();
        formContainer.setPadding(new Insets(20));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField();

        Label authorLabel = new Label("Author:");
        TextField authorField = new TextField();

        Label categoryLabel = new Label("Category:");
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Select Category");
        List<String> categories = dbManager.getAllCategories();
        categoryComboBox.setItems(FXCollections.observableList(categories));

        Label isbnLabel = new Label("ISBN:");
        TextField isbnField = new TextField();

        Label yearLabel = new Label("Published Year:");
        int currentYear = java.time.Year.now().getValue();
        Spinner<Integer> yearField = new Spinner<>(1000, currentYear, currentYear);
        yearField.setEditable(true);

        Label availabilityLabel = new Label("Available:");
        CheckBox availabilityCheckBox = new CheckBox();
        availabilityCheckBox.setSelected(true);

        Button submitButton = new Button("Add Book");

        formGrid.add(titleLabel, 0, 0);
        formGrid.add(titleField, 1, 0);
        formGrid.add(authorLabel, 0, 1);
        formGrid.add(authorField, 1, 1);
        formGrid.add(categoryLabel, 0, 2);
        formGrid.add(categoryComboBox, 1, 2);
        formGrid.add(isbnLabel, 0, 3);
        formGrid.add(isbnField, 1, 3);
        formGrid.add(yearLabel, 0, 4);
        formGrid.add(yearField, 1, 4);
        formGrid.add(availabilityLabel, 0, 5);
        formGrid.add(availabilityCheckBox, 1, 5);
        formGrid.add(submitButton, 1, 6);

        formContainer.getChildren().add(formGrid);

        // Submit adding a book function
        submitButton.setOnAction(e -> {
            try {
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String isbn = isbnField.getText().trim();
                String category = categoryComboBox.getValue();
                Integer yearText = yearField.getValue();

                if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() ||
                        category == null || yearText == null) {

                    // Show error message
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Missing Required Fields");
                    alert.setContentText("Please fill in all required fields.");
                    alert.showAndWait();
                    return;
                }

                dbManager.addBook(title, author, isbn, category, yearText);

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Book Added");
                alert.setContentText("The book has been successfully added to the library.");
                alert.showAndWait();
                loadBooksData(tableView, dbManager);

                // Clear fields
                titleField.clear();
                authorField.clear();
                isbnField.clear();
                categoryComboBox.setValue(null);
                yearField.setValueFactory(null);
                availabilityCheckBox.setSelected(true);

            } catch (RuntimeException | SQLException ex) {
                // Handle database errors
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Failed to Add Book");
                alert.setContentText("An error occurred while adding the book to the database. Please try again.");
                alert.showAndWait();
                ex.printStackTrace(System.err);
            }
        });

        return formContainer;
    }
}
