package org.example.bookmngmntsys;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import java.sql.SQLException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class LogsView {

    // Window that shows old and new data
    public static void openJsonDetailWindow(String jsonData) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Changed Data Details");

        ObjectMapper mapper = new ObjectMapper();
        String prettyJson;
        try {
            Object json = mapper.readValue(jsonData, Object.class);
            prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            prettyJson = jsonData;
        }

        TextArea textArea = new TextArea(prettyJson);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(600, 400);

        VBox root = new VBox(textArea);
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.showAndWait();
    }

    // Set up column names in the TableView
    public static void setUpTableColumns(TableView<ObservableList<String>> tableView) {
        tableView.getColumns().clear();

        String[] columnNames = {"Table Name", "Action", "Changed Data", "Action Timestamp"};

        for (int i = 0; i < columnNames.length; i++) {
            final int columnIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnNames[i]);

            if (columnNames[i].equals("Changed Data")) {
                // Cell factory to handle double-click and open detail window
                column.setCellFactory(col -> {
                    TableCell<ObservableList<String>, String> cell = new TableCell<>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty ? null : "Double Click");
                            setGraphic(null);
                        }
                    };

                    cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                        if (!cell.isEmpty() && event.getClickCount() == 2) {
                            ObservableList<String> row = cell.getTableView().getItems().get(cell.getIndex());
                            String jsonData = row.get(2); // "Changed Data" is at index 2

                            openJsonDetailWindow(jsonData); // You implement this method
                        }
                    });

                    return cell;
                });

            } else {
                // For all other columns, show actual data from the row
                column.setCellValueFactory(param -> {
                    ObservableList<String> row = param.getValue();
                    if (row != null && columnIndex < row.size()) {
                        return new SimpleStringProperty(row.get(columnIndex));
                    }
                    return new SimpleStringProperty("");
                });
            }
            tableView.getColumns().add(column);
        }
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public static Node getView(DatabaseManager dbManager) {

        BorderPane root = new BorderPane();
        TableView<ObservableList<String>> tableView = new TableView<>();
        setUpTableColumns(tableView);

        try {
            ObservableList<ObservableList<String>> logs = dbManager.fetchLogs(100);
            tableView.setItems(logs);
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }

        root.setCenter(tableView);
        return root;
    }
}
