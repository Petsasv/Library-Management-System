package org.example.bookmngmntsys;

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
import java.util.Arrays;

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
    public static void setUpTableColumns(TableView<Log> tableView) {
        tableView.getColumns().clear();

        // Table Name
        TableColumn<Log, String> tableNameCol = new TableColumn<>("Table Name");
        tableNameCol.setCellValueFactory(cellData -> cellData.getValue().tableNameProperty());

        // Action
        TableColumn<Log, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(cellData -> cellData.getValue().actionProperty());

        // Changed Data (double-click column)
        TableColumn<Log, String> changedDataCol = new TableColumn<>("Changed Data");
        changedDataCol.setCellValueFactory(cellData -> cellData.getValue().changedDataProperty());
        changedDataCol.setCellFactory(col -> {
            TableCell<Log, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : "Double Click");
                }
            };

            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (!cell.isEmpty() && event.getClickCount() == 2) {
                    Log log = cell.getTableRow().getItem();
                    openJsonDetailWindow(log.getChangedData());
                }
            });

            return cell;
        });

        // Action Timestamp
        TableColumn<Log, String> timestampCol = new TableColumn<>("Action Timestamp");
        timestampCol.setCellValueFactory(cellData -> cellData.getValue().actionTimestampProperty());

        tableView.getColumns().addAll(Arrays.asList(tableNameCol, actionCol, changedDataCol, timestampCol));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public static Node getView(DatabaseManager dbManager) {

        BorderPane root = new BorderPane();
        TableView<Log> tableView = new TableView<>();
        setUpTableColumns(tableView);

        try {
            ObservableList<Log> logs = dbManager.fetchLogs(100);
            tableView.setItems(logs);
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }

        root.setCenter(tableView);
        return root;
    }
}
