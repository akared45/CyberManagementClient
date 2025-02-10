module com.example.cybermanagementclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    opens com.cyber.client to javafx.fxml;
    exports com.cyber.client;
    opens com.cyber.client.controller to javafx.fxml;
    opens com.cyber.client.client to javafx.fxml;
}
