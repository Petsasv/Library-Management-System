package org.example.bookmngmntsys;

import javafx.beans.property.*;

public class Log {
    private final IntegerProperty logId;
    private final StringProperty tableName;
    private final StringProperty action;
    private final StringProperty changedData;
    private final StringProperty actionTimestamp;

    // Constructor
    public Log(int logId, String tableName, String action, String changedData, String actionTimestamp) {
        this.logId = new SimpleIntegerProperty(logId);
        this.tableName = new SimpleStringProperty(tableName);
        this.action = new SimpleStringProperty(action);
        this.changedData = new SimpleStringProperty(changedData);
        this.actionTimestamp = new SimpleStringProperty(actionTimestamp);
    }

    // Getters and Property Methods
    public int getLogId() {
        return logId.get();
    }

    public IntegerProperty logIdProperty() {
        return logId;
    }

    public String getTableName() {
        return tableName.get();
    }

    public StringProperty tableNameProperty() {
        return tableName;
    }

    public String getAction() {
        return action.get();
    }

    public StringProperty actionProperty() {
        return action;
    }

    public String getChangedData() {
        return changedData.get();
    }

    public StringProperty changedDataProperty() {
        return changedData;
    }

    public String getActionTimestamp() {
        return actionTimestamp.get();
    }

    public StringProperty actionTimestampProperty() {
        return actionTimestamp;
    }
}