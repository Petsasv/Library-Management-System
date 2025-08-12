package org.example.bookmngmntsys;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;


public class MainApp extends Application {

    DatabaseManager dbManager;

    @Override
    public void start(Stage primaryStage) {
        // Try to connect to the DB
        try {
            dbManager  = new DatabaseManager();
        } catch (SQLException e) {
            System.out.println("Error connecting to DB " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }

        primaryStage.setTitle("Library Management System");

        // Setup left side menu
        TreeItem<String> rootItem = new TreeItem<>("Διαχείριση");
        rootItem.setExpanded(true);

        TreeItem<String> booksItem = new TreeItem<>("Βιβλία");
        TreeItem<String> membersItem = new TreeItem<>("Μέλη");
        TreeItem<String> borrowingsItem = new TreeItem<>("Δανεισμοί");
        TreeItem<String> LogsItem = new TreeItem<>("Logs");

        // asList because of generics types warnings
        rootItem.getChildren().addAll(Arrays.asList(booksItem, membersItem, borrowingsItem, LogsItem));

        TreeView<String> tree = new TreeView<>(rootItem);
        tree.setShowRoot(true);
        tree.setPrefWidth(180);

        Label placeholderLabel = new Label("Καλώς ήρθατε στο Σύστημα Διαχείρισης Βιβλιοθήκης");
        placeholderLabel.getStyleClass().add("welcome-label");

        StackPane contentPane = new StackPane(placeholderLabel);
        contentPane.getStyleClass().add("content-pane");

        // Setting layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(tree);
        mainLayout.setCenter(contentPane);

        // Manage the left side menu
        tree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            String selected = newVal.getValue();
            try {
                switch (selected) {
                    case "Βιβλία":
                        contentPane.getChildren().setAll(BookView.getView(dbManager));
                        break;
                    case "Μέλη":
                        contentPane.getChildren().setAll(MemberView.getView(dbManager));
                        break;
                    case "Δανεισμοί":
                        contentPane.getChildren().setAll(BorrowingView.getView(dbManager));
                        break;
                    case "Logs":
                        contentPane.getChildren().setAll(LogsView.getView(dbManager));
                        break;
                    default:
                        contentPane.getChildren().setAll(new Label("Επιλέξτε μία κατηγορία."));
                }
            } catch (SQLException e) {
                e.printStackTrace(System.err);
                contentPane.getChildren().setAll(new Label("Error loading view: " + e.getMessage()));
            }
        });

        Scene scene = new Scene(mainLayout, 900, 600);

        URL cssURL = getClass().getResource("/styles.css");
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        } else {
            System.err.println("Could not find css file");
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Close DB connection
        if (dbManager != null) {
            dbManager.close();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}