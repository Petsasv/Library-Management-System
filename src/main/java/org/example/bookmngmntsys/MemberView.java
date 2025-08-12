package org.example.bookmngmntsys;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


public class MemberView {

    // Setup column names
    public static void setUpTableColumns(TableView<ObservableList<String>> tableView) {
        tableView.getColumns().clear();

        String[] columnNames = {"First Name", "Last Name", "Email", "Phone Number", "Registration Date"};

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

    // Fetch members data from DB
    public static void loadMembersData(TableView<ObservableList<String>> tableView, DatabaseManager dbManager) {
        try {
            List<ObservableList<String>> members = dbManager.getAllUsers();
            tableView.setItems(FXCollections.observableArrayList(members));
        } catch (SQLException e) {
            tableView.setPlaceholder(new Label("Error loading books from database."));
            e.printStackTrace(System.err);
        }
    }

    private static void styleTableButtons(Button... buttons) {
        for (Button btn : buttons) {
            btn.getStyleClass().add("table-button");
        }
    }

    public static Node getView(DatabaseManager dbManager) throws SQLException {

        BorderPane root = new BorderPane();

        // Toolbar
        Button previewBtn  = new Button("Preview Members");
        Button newEntryBtn = new Button("Add member");
        ToolBar toolbar = new ToolBar(previewBtn, newEntryBtn);
        root.setTop(toolbar);

        // Setting up TableView
        TableView<ObservableList<String>> tableView = new TableView<>();
        tableView.setMinHeight(400);

        // Set form layout and fields
        VBox formContainer = new VBox();
        formContainer.setPadding(new Insets(20));

        Label firstNameLabel = new Label("First name");
        TextField firstNameField = new TextField();

        Label lastNameLabel = new Label("Last name");
        TextField lastNameField = new TextField();

        Label emailLabel = new Label("Email");
        TextField emailField = new TextField();

        Label phoneLabel = new Label("Phone number");
        TextField phoneField = new TextField();

        // Submit button
        Button submitButton = new Button("Add member");

        // Form Layout
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        formGrid.add(firstNameLabel, 0, 0);
        formGrid.add(firstNameField, 1, 0);

        formGrid.add(lastNameLabel, 0, 1);
        formGrid.add(lastNameField, 1, 1);

        formGrid.add(emailLabel, 0, 2);
        formGrid.add(emailField, 1, 2);

        formGrid.add(phoneLabel, 0, 3);
        formGrid.add(phoneField, 1, 3);

        formGrid.add(submitButton,1, 4 );

        // Style container
        formContainer.getChildren().add(formGrid);

        // Set up TableView columns with hardcoded names
        setUpTableColumns(tableView);

        // Load the data into the table
        loadMembersData(tableView, dbManager);

        // Buttons below the table
        Button editBtn = new Button("Edit Selected");
        Button deleteBtn = new Button("Delete Selected");
        styleTableButtons(editBtn, deleteBtn);

        HBox buttonsBox = new HBox(10, editBtn, deleteBtn);  // 10 px spacing between buttons
        buttonsBox.setPadding(new Insets(10, 0, 0, 0)); // just top padding

        VBox previewView = new VBox(5, tableView, buttonsBox);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        previewView.setPadding(new Insets(10));

        // Button actions
        previewBtn.setOnAction(e -> root.setCenter(previewView));

        newEntryBtn.setOnAction(e -> root.setCenter(formContainer));

        editBtn.setOnAction(e -> {
            ObservableList<String> selectedMember = tableView.getSelectionModel().getSelectedItem();
            if (selectedMember == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Selection");
                alert.setHeaderText(null);
                alert.setContentText("Please select a member to edit.");
                alert.showAndWait();
                return;
            }

            String currentFirstName = selectedMember.get(0);
            String currentLastName = selectedMember.get(1);
            String currentEmail = selectedMember.get(2);
            String currentPhone = selectedMember.get(3);

            // Create EDIT window
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Member");
            dialog.setHeaderText("Modify the member details and click Update.");

            // Create labels and fields for editing
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField firstNameEdit = new TextField(currentFirstName);
            TextField lastNameEdit = new TextField(currentLastName);
            TextField emailEdit = new TextField(currentEmail);
            TextField phoneEdit = new TextField(currentPhone);

            grid.add(new Label("First Name:"), 0, 0);
            grid.add(firstNameEdit, 1, 0);
            grid.add(new Label("Last Name:"), 0, 1);
            grid.add(lastNameEdit, 1, 1);
            grid.add(new Label("Email:"), 0, 2);
            grid.add(emailEdit, 1, 2);
            grid.add(new Label("Phone Number:"), 0, 3);
            grid.add(phoneEdit, 1, 3);
            dialog.getDialogPane().setContent(grid);

            // Add Update and Cancel buttons
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Validate before closing on OK
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Validate inputs
                if (firstNameEdit.getText().trim().isEmpty() ||
                        lastNameEdit.getText().trim().isEmpty() ||
                        emailEdit.getText().trim().isEmpty() ||
                        phoneEdit.getText().trim().isEmpty()) {
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
                String newFirstName = firstNameEdit.getText().trim();
                String newLastName = lastNameEdit.getText().trim();
                String newEmail = emailEdit.getText().trim();
                String newPhone = phoneEdit.getText().trim();

                try {
                    int memberId = dbManager.getMemberIdByName(currentFirstName, currentLastName);

                    dbManager.editUser(memberId, newFirstName, newLastName, newEmail, newPhone);

                    // Refresh table data after edit
                    loadMembersData(tableView, dbManager);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Member updated successfully.");
                    alert.showAndWait();

                } catch (SQLException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Database Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error updating member: " + ex.getMessage());
                    alert.showAndWait();
                    ex.printStackTrace(System.err);
                    throw new RuntimeException(ex);
                }
            }
        });


        deleteBtn.setOnAction(e -> {
            ObservableList<String> selectedMember = tableView.getSelectionModel().getSelectedItem();
            if (selectedMember == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Selection Error");
                alert.setContentText("No selection. Please select a book to delete.");
                alert.showAndWait();
                return;
            }

            String firstName = selectedMember.get(1);
            String lastName = selectedMember.get(2);
            try {
                int memberId = dbManager.getMemberIdByName(firstName, lastName);

                // Confirm deletion
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Deletion");
                confirm.setHeaderText("Are you sure you want to delete this member?");

                if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                    return; // user canceled
                }

                // Try to delete
                dbManager.deleteUser(memberId);

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Member deleted successfully.");
                success.showAndWait();

                // reload table
                loadMembersData(tableView, dbManager);
            } catch (SQLException e1) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Database Error");
                error.setHeaderText("Failed to delete member");
                error.setContentText(e1.getMessage());
                error.showAndWait();
            }

        });

        // Submit adding a member function
        submitButton.setOnAction(e -> {
            try {
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();

                // Check for empty fields
                if (firstName.isEmpty() || lastName.isEmpty() ||
                    email.isEmpty() || phone.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Missing Required Fields");
                    alert.setContentText("Please fill in all required fields.");
                    alert.showAndWait();
                }

                dbManager.addUser(firstName, lastName, email, phone);

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Member Added");
                alert.setContentText("Member has been successfully added to the library.");
                alert.showAndWait();
                loadMembersData(tableView, dbManager);

                // Clear fields
                firstNameField.clear();
                lastNameField.clear();
                emailField.clear();
                phoneField.clear();

                // Switch back to preview view
                root.setCenter(tableView);

            } catch (RuntimeException ex) {
                // Handle database errors
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Failed to Add Member");
                alert.setContentText("An error occurred while adding the member to the database. Please try again.");
                alert.showAndWait();
                ex.printStackTrace(System.err);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        root.setCenter(previewView);
        return root;
    }
}
