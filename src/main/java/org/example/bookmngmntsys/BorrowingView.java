package org.example.bookmngmntsys;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class BorrowingView {

    // Set up column names on the TableView
    public static void setUpTableColumns(TableView<ObservableList<String>> tableView) {
        tableView.getColumns().clear();

        String[] columnNames = {"Borrow ID", "Book Name", "Member Name","Borrow Date", "Return Date"};

        for (int i=0; i<columnNames.length; i++) {
            final int columnIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnNames[i]);

            column.setCellValueFactory(param -> {
                ObservableList<String> row = param.getValue();
                if (row != null && columnIndex < row.size()) {
                    return new javafx.beans.property.SimpleStringProperty(row.get(columnIndex));
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });

            tableView.getColumns().add(column);
        }
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // Fetch borrowings from DB and display on TableView
    public static void loadBorrowData(TableView<ObservableList<String>> tableView, DatabaseManager dbManager) {
        try {
            List<ObservableList<String>> borrowings = dbManager.getAllBorrowings();
            tableView.setItems(FXCollections.observableArrayList(borrowings));
        } catch (Exception e) {
            tableView.setPlaceholder(new Label("Error loading borrowings from database."));
            e.printStackTrace(System.err);
        }
    }

    private static void returnBook(TableView<ObservableList<String>> tableView, DatabaseManager dbManager) {
        // Get selected row
        ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a borrowing record to return.");
            return;
        }

        if (selectedRow.size() < 3) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid row data - not enough columns.");
            return;
        }

        String borrowIdStr = selectedRow.get(0);
        String bookName = selectedRow.get(1);
        String memberName = selectedRow.get(2);

        // Confirm return
        Optional<ButtonType> result = showConfirmation(
                "Are you sure you want to return this book?\n\n" +
                        "Book: " + bookName + "\n" +
                        "Member: " + memberName + "\n\n" +
                        "This will remove the borrowing record."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int borrowId = Integer.parseInt(borrowIdStr);
                dbManager.returnBook(borrowId); // Calls return_book_delete function

                showAlert(Alert.AlertType.INFORMATION, "Success", "Book returned successfully!");
                // Refresh the table
                loadBorrowData(tableView, dbManager);

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid borrow ID format: " + borrowIdStr);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error returning book: " + e.getMessage());
            }
        }
    }

    // Utility method to show alerts
    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Utility method to show confirmation dialog
    private static Optional<ButtonType> showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Return");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    public static Node getView(DatabaseManager dbManager) {

        BorderPane root = new BorderPane();

        // Setting up TableView
        TableView<ObservableList<String>> tableView = new TableView<>();
        tableView.setMinHeight(400);

        //
        setUpTableColumns(tableView);
        loadBorrowData(tableView, dbManager);

        // Return borrowing button
        Button returnBtn = new Button("Return borrowing");
        returnBtn.getStyleClass().add("table-button");

        // Return button action
        returnBtn.setOnAction(e -> returnBook(tableView, dbManager));

        // Put TableView and button in VBox
        VBox tableAndButtonBox = new VBox(10, tableView, returnBtn);
        tableAndButtonBox.setPadding(new Insets(10));

        // Make sure TableView grows to fill space vertically
        VBox.setVgrow(tableView, Priority.ALWAYS);

        root.setCenter(tableAndButtonBox);

        return root;
    }
}
